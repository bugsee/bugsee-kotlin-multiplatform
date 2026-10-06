package com.bugsee.kmp

import android.graphics.Rect
import android.net.Uri
import com.bugsee.kmp.internal.Logger
import com.bugsee.library.contracts.exchange.NetworkEvent
import com.bugsee.library.contracts.lifecycle.LifecycleEvents
import com.bugsee.library.contracts.options.IssueSeverity
import com.bugsee.library.contracts.options.IssueType
import com.bugsee.library.contracts.options.LogLevel
import com.bugsee.library.contracts.reporting.ExceptionOptions
import com.bugsee.library.contracts.reporting.Report
import java.io.File
import java.io.Serializable

internal class BugseeAndroidUtils {
    internal companion object {
        private const val TAG = "BugseeAndroidUtils"

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
         */
        fun addAttachmentToReport(attachment: BugseeAttachment, report: Report) {
            val data = attachment.data
            if (data != null) {
                if (report.addAttachment(data, attachment.name, null) == null) {
                    Logger.e(TAG, "addAttachment: native SDK rejected in-memory attachment '${attachment.name}'")
                }
                return
            }

            val filePath = attachment.filePath.orEmpty()
            val bytes: ByteArray? = try {
                when {
                    filePath.startsWith("file:///android_asset/") -> {
                        val assetPath = filePath.removePrefix("file:///android_asset/")
                        applicationContext?.assets?.open(assetPath)?.use { it.readBytes() }
                    }
                    filePath.startsWith("content://") || filePath.startsWith("file://") -> {
                        applicationContext?.contentResolver?.openInputStream(Uri.parse(filePath))?.use { it.readBytes() }
                    }
                    else -> null
                }
            } catch (e: Exception) {
                Logger.e(TAG, "addAttachment: failed to read '$filePath'", e)
                null
            }

            val added = if (bytes != null) {
                report.addAttachment(bytes, attachment.name, null)
            } else {
                report.addAttachment(File(filePath), attachment.name, null, false)
            }
            if (added == null) {
                Logger.e(TAG, "addAttachment: native SDK rejected attachment '${attachment.name}' ($filePath)")
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

        fun convertExceptionLoggingOptions(options: BugseeExceptionLoggingOptions?): Map<String, Any>? {
            if (options == null) {
                return null
            }

            val result = HashMap<String, Any>()
            // Merging-rule custom options first, so the documented keys below win
            for ((key, value) in options.rules.toMap()) {
                if (value != null) {
                    result[key] = value
                }
            }
            result[ExceptionOptions.SkipFrames] = options.rules.skipFrames
            options.exceptionDomain?.let { result[ExceptionOptions.Domain] = it }
            // "labels" and "includeVideo" are documented on Bugsee.logException, but SDK 7.3.0
            // does not read them yet; they are passed for forward compatibility.
            options.labels?.let { result["labels"] = ArrayList(it) }
            result["includeVideo"] = options.includeVideo

            return result
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
