package com.bugsee.kmp

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Rect
import android.net.Uri
import com.bugsee.kmp.internal.Logger
import com.bugsee.kmp.internal.MAX_ATTACHMENT_BYTES
import com.bugsee.kmp.internal.fitsAttachmentSizeLimit
import com.bugsee.library.contracts.exchange.NetworkEvent
import com.bugsee.library.contracts.lifecycle.LifecycleEvents
import com.bugsee.library.contracts.options.IssueSeverity
import com.bugsee.library.contracts.options.IssueType
import com.bugsee.library.contracts.options.LogLevel
import com.bugsee.library.contracts.reporting.Report
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.Serializable

internal class BugseeAndroidUtils {
    internal companion object {
        private const val TAG = "BugseeAndroidUtils"
        private const val ASSET_URI_PREFIX = "file:///android_asset/"

        fun convertIssueType(issueType: IssueType?): BugseeReportType {
            return when (issueType) {
                IssueType.Error -> BugseeReportType.Error
                IssueType.Crash -> BugseeReportType.Crash
                else -> BugseeReportType.Bug
            }
        }

        fun convertLogLevel(logLevel: BugseeLogLevel): LogLevel {
            return when (logLevel) {
                BugseeLogLevel.Debug -> LogLevel.Debug
                BugseeLogLevel.Info -> LogLevel.Info
                BugseeLogLevel.Warning -> LogLevel.Warning
                BugseeLogLevel.Error -> LogLevel.Error
                BugseeLogLevel.Verbose -> LogLevel.Verbose
            }
        }

        fun convertLogLevel(logLevel: LogLevel?): BugseeLogLevel {
            return when (logLevel) {
                LogLevel.Debug -> BugseeLogLevel.Debug
                LogLevel.Info -> BugseeLogLevel.Info
                LogLevel.Warning -> BugseeLogLevel.Warning
                LogLevel.Error -> BugseeLogLevel.Error
                LogLevel.Verbose, null -> BugseeLogLevel.Verbose
            }
        }

        fun convertSeverity(severity: BugseeSeverity): IssueSeverity {
            return when (severity) {
                BugseeSeverity.Critical -> IssueSeverity.Critical
                BugseeSeverity.High -> IssueSeverity.High
                BugseeSeverity.Medium -> IssueSeverity.Medium
                BugseeSeverity.VeryLow -> IssueSeverity.VeryLow
                BugseeSeverity.Blocker -> IssueSeverity.Blocker
            }
        }

        fun convertSeverity(severity: IssueSeverity?): BugseeSeverity {
            return when (severity) {
                IssueSeverity.Critical -> BugseeSeverity.Critical
                IssueSeverity.High -> BugseeSeverity.High
                IssueSeverity.Medium, null -> BugseeSeverity.Medium
                IssueSeverity.VeryLow -> BugseeSeverity.VeryLow
                IssueSeverity.Blocker -> BugseeSeverity.Blocker
            }
        }

        fun convertSecureRect(rect: BugseeSecureRectangle): Rect {
            return Rect(
                rect.x.toInt(),
                rect.y.toInt(),
                (rect.x + rect.width).toInt(),
                (rect.y + rect.height).toInt()
            )
        }

        fun convertSecureRect(rect: Rect): BugseeSecureRectangle {
            return BugseeSecureRectangle(
                rect.left.toDouble(),
                rect.top.toDouble(),
                rect.right.toDouble(),
                rect.bottom.toDouble()
            )
        }

        fun convertReport(report: Report): BugseeReport {
            return BugseeReport(
                convertIssueType(report.type),
                convertSeverity(report.severity),
                report.labels?.toList() ?: emptyList()
            )
        }

        fun convertReportFieldsFromNative(report: Report): BugseeReportFields {
            return BugseeReportFields(
                report.summary.orEmpty(),
                report.description.orEmpty(),
                convertSeverity(report.severity),
                report.labels?.toList() ?: emptyList()
            )
        }

        /**
         * Writes back only the fields that differ from [original], so an untouched `null`
         * summary or description is not turned into `""`.
         */
        fun applyReportFieldsToNative(fields: BugseeReportFields, original: BugseeReportFields, report: Report) {
            if (fields.summary != original.summary) report.summary = fields.summary
            if (fields.description != original.description) report.description = fields.description
            if (fields.severity != original.severity) report.severity = convertSeverity(fields.severity)
            if (fields.labels != original.labels) report.setLabels(ArrayList(fields.labels))
        }

        /**
         * Adds [attachment] to the native [report]. `file:///android_asset/`, `content://` and
         * `file://` URIs are read into memory first; plain paths are copied by the native SDK.
         *
         * An attachment over [MAX_ATTACHMENT_BYTES] is not added, with a warning. The size of a URI
         * or an asset is read before its content when it is known, and its content is never read
         * past the limit.
         */
        fun addAttachmentToReport(attachment: BugseeAttachment, report: Report) {
            val name = attachment.name
            val data = attachment.data
            if (data != null) {
                if (!fitsAttachmentSizeLimit(TAG, name, data.size.toLong())) return
                if (report.addAttachment(data, name, null) == null) {
                    Logger.e(TAG, "addAttachment: native SDK rejected in-memory attachment '$name'")
                }
                return
            }

            val filePath = attachment.filePath.orEmpty()
            val isAsset = filePath.startsWith(ASSET_URI_PREFIX)
            val isUri = !isAsset && (filePath.startsWith("content://") || filePath.startsWith("file://"))
            val added = if (isAsset || isUri) {
                // Read through the context; as a file path the SDK would reject it with no hint why.
                val context = applicationContext
                if (context == null) {
                    Logger.e(TAG, "addAttachment: cannot read '$filePath' for attachment '$name': $NO_KMP_CONTEXT")
                    return
                }
                val read = try {
                    if (isAsset) {
                        readAsset(context, filePath.removePrefix(ASSET_URI_PREFIX), name)
                    } else {
                        readUri(context, Uri.parse(filePath), name)
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "addAttachment: failed to read '$filePath'", e)
                    return
                }
                val bytes = read ?: return
                report.addAttachment(bytes, name, null)
            } else {
                val file = File(filePath)
                if (!fitsAttachmentSizeLimit(TAG, name, file.length())) return
                report.addAttachment(file, name, null, false)
            }
            if (added == null) {
                Logger.e(TAG, "addAttachment: native SDK rejected attachment '$name' ($filePath)")
            }
        }

        /** The content of the asset, or `null` when it is over the limit (logged). */
        private fun readAsset(context: Context, assetPath: String, name: String): ByteArray? {
            // openFd works only for an asset stored uncompressed; otherwise the size is not known.
            val size = try {
                context.assets.openFd(assetPath).use { it.length }
            } catch (e: IOException) {
                AssetFileDescriptor.UNKNOWN_LENGTH
            }
            if (size >= 0 && !fitsAttachmentSizeLimit(TAG, name, size)) return null
            return context.assets.open(assetPath).use { readWithinLimit(it, name) }
        }

        /** The content at [uri], or `null` when it is over the limit (logged). */
        private fun readUri(context: Context, uri: Uri, name: String): ByteArray? {
            val descriptor = context.contentResolver.openAssetFileDescriptor(uri, "r")
                ?: throw FileNotFoundException("no content at $uri")
            return descriptor.use { afd ->
                // The length the provider gave, else the size of the file behind the descriptor;
                // UNKNOWN_LENGTH for a pipe.
                val size = afd.length
                if (size >= 0 && !fitsAttachmentSizeLimit(TAG, name, size)) return null
                afd.createInputStream().use { readWithinLimit(it, name) }
            }
        }

        /**
         * Reads [stream] to its end, or returns `null` (logged) as soon as it gives more than
         * [MAX_ATTACHMENT_BYTES]: for content of unknown size, or that grew since its size was read.
         */
        internal fun readWithinLimit(stream: InputStream, name: String): ByteArray? {
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(64 * 1024)
            var total = 0L
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) return out.toByteArray()
                total += count
                if (total > MAX_ATTACHMENT_BYTES) {
                    Logger.w(
                        TAG,
                        "attachment '$name' is over the limit of $MAX_ATTACHMENT_BYTES bytes, so it is not added"
                    )
                    return null
                }
                out.write(buffer, 0, count)
            }
        }

        /**
         * Adds the attachments-provider result to [report], skipping names the report already
         * carries. `ReportHandler.onAfterReportCreated` is at-least-once: after a process death it
         * is replayed against a report that may hold the first call's attachments.
         */
        fun addProviderAttachments(attachments: List<BugseeAttachment>, report: Report) {
            val existingNames = report.attachments.mapTo(HashSet()) { it.name }
            for (attachment in attachments) {
                if (attachment.name in existingNames) {
                    Logger.d(TAG, "attachments provider: '${attachment.name}' is already attached — skipping")
                    continue
                }
                addAttachmentToReport(attachment, report)
            }
        }

        /**
         * Maps a 7.x lifecycle event name to the 0.1.x [BugseeLifecycleEvent]. Returns `null`
         * for events that have no 0.1.x counterpart (Launching, Stopping, ReportAssemblyFailed).
         */
        fun convertLifecycleEvent(event: String): BugseeLifecycleEvent? {
            return when (event) {
                LifecycleEvents.Launched -> BugseeLifecycleEvent.Launched
                LifecycleEvents.Stopped -> BugseeLifecycleEvent.Stopped
                LifecycleEvents.BlackoutEnded -> BugseeLifecycleEvent.Resumed
                LifecycleEvents.BlackoutStarted -> BugseeLifecycleEvent.Paused
                LifecycleEvents.RelaunchedAfterCrash -> BugseeLifecycleEvent.RelaunchedAfterCrash
                LifecycleEvents.BeforeReportShown -> BugseeLifecycleEvent.BeforeReportShown
                LifecycleEvents.AfterReportShown -> BugseeLifecycleEvent.AfterReportShown
                LifecycleEvents.BeforeReportUploaded -> BugseeLifecycleEvent.BeforeReportUploaded
                LifecycleEvents.AfterReportUploaded -> BugseeLifecycleEvent.AfterReportUploaded
                LifecycleEvents.BeforeReportAssembled -> BugseeLifecycleEvent.BeforeReportAssembled
                LifecycleEvents.AfterReportAssembled -> BugseeLifecycleEvent.AfterReportAssembled
                LifecycleEvents.ReportUploadFailedWithFutureRetry -> BugseeLifecycleEvent.ReportUploadFailedWithFutureRetry
                LifecycleEvents.ReportUploadFailed -> BugseeLifecycleEvent.ReportUploadFailed
                // Dispatched by the Feedback add-on (bugsee-kotlin-multiplatform-feedback). Its
                // FeedbackLifecycleEvents constants are not in the core SDK this module links.
                "com.bugsee.lifecycle.BeforeFeedbackShown" -> BugseeLifecycleEvent.BeforeFeedbackShown
                "com.bugsee.lifecycle.AfterFeedbackShown" -> BugseeLifecycleEvent.AfterFeedbackShown
                else -> null
            }
        }

        fun convertNetworkEventStage(stage: NetworkEvent.NetworkEventStage?): BugseeNetworkEventStage {
            return when (stage) {
                NetworkEvent.NetworkEventStage.RequestCompleted -> BugseeNetworkEventStage.Complete
                NetworkEvent.NetworkEventStage.Redirect -> BugseeNetworkEventStage.Redirect
                NetworkEvent.NetworkEventStage.RequestErrored -> BugseeNetworkEventStage.Errors
                NetworkEvent.NetworkEventStage.RequestAborted -> BugseeNetworkEventStage.Cancel
                NetworkEvent.NetworkEventStage.WebSocket -> BugseeNetworkEventStage.WebSocket
                else -> BugseeNetworkEventStage.Before
            }
        }

        /**
         * Copies [source] into the `Map<String, Serializable>` shape the 7.x SDK requires,
         * dropping (and logging) values that are not [Serializable].
         */
        fun toSerializableMap(source: Map<String, Any?>?, caller: String): HashMap<String, Serializable> {
            val result = HashMap<String, Serializable>()
            source?.forEach { (key, value) ->
                when (value) {
                    is Serializable -> result[key] = value
                    null -> Unit
                    else -> Logger.e(TAG, "$caller: dropping non-Serializable value for '$key' (${value::class.simpleName})")
                }
            }
            return result
        }
    }
}
