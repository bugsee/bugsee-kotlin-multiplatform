package com.bugsee.kmp

import com.bugsee.kmp.internal.Logger
import kotlinx.cinterop.*
import swiftPMImport.com.bugsee.library.BugseeLifecycleEventType
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL

private const val TAG = "Utils"

internal class BugseeIOSUtils {
    internal companion object {

        /**
         * The largest attachment the iOS SDK keeps, in bytes: `MAX_SIZE_ATTACHMENT` in its
         * `BGSAttachment.h`. It drops a larger attachment, and an empty one, with only an internal log.
         */
        const val MAX_ATTACHMENT_BYTES: Long = 3L * 1024 * 1024

        fun convertLogLevel(logLevel: com.bugsee.kmp.BugseeLogLevel): ULong {
            return when (logLevel) {
                com.bugsee.kmp.BugseeLogLevel.Error -> 1
                com.bugsee.kmp.BugseeLogLevel.Warning -> 2
                com.bugsee.kmp.BugseeLogLevel.Info -> 3
                com.bugsee.kmp.BugseeLogLevel.Debug -> 4
                com.bugsee.kmp.BugseeLogLevel.Verbose -> 5
            }.toULong()
        }

        fun convertLogLevel(logLevel: ULong): com.bugsee.kmp.BugseeLogLevel {
            return when (logLevel) {
                1uL -> com.bugsee.kmp.BugseeLogLevel.Error
                2uL -> com.bugsee.kmp.BugseeLogLevel.Warning
                3uL -> com.bugsee.kmp.BugseeLogLevel.Info
                4uL -> com.bugsee.kmp.BugseeLogLevel.Debug
                5uL -> com.bugsee.kmp.BugseeLogLevel.Verbose

                else -> com.bugsee.kmp.BugseeLogLevel.Info
            }
        }

        fun convertNetworkEventStage(stage: String): BugseeNetworkEventStage {
            return when (stage) {
                "before" -> BugseeNetworkEventStage.Before
                "complete" -> BugseeNetworkEventStage.Complete
                "cancel" -> BugseeNetworkEventStage.Cancel
                "error" -> BugseeNetworkEventStage.Errors

                else ->  BugseeNetworkEventStage.Before
            }
        }

        // iOS SDK 7 renumbered the event type: Started, Resumed and Paused are gone and the
        // blackout events are new. Mapped by name, as on Android: a blackout is the 0.1.x pause.
        fun convertLifecycleEvent(eventType: BugseeLifecycleEventType): BugseeLifecycleEvent? {
            return when (eventType) {
                BugseeLifecycleEventType.BugseeLifecycleEventLaunched -> BugseeLifecycleEvent.Launched
                BugseeLifecycleEventType.BugseeLifecycleEventStopped -> BugseeLifecycleEvent.Stopped
                BugseeLifecycleEventType.BugseeLifecycleEventBlackoutEnded -> BugseeLifecycleEvent.Resumed
                BugseeLifecycleEventType.BugseeLifecycleEventBlackoutStarted -> BugseeLifecycleEvent.Paused
                BugseeLifecycleEventType.BugseeLifecycleEventRelaunchedAfterCrash -> BugseeLifecycleEvent.RelaunchedAfterCrash
                BugseeLifecycleEventType.BugseeLifecycleEventBeforeReportShown -> BugseeLifecycleEvent.BeforeReportShown
                BugseeLifecycleEventType.BugseeLifecycleEventAfterReportShown -> BugseeLifecycleEvent.AfterReportShown
                BugseeLifecycleEventType.BugseeLifecycleEventBeforeReportUploaded -> BugseeLifecycleEvent.BeforeReportUploaded
                BugseeLifecycleEventType.BugseeLifecycleEventAfterReportUploaded -> BugseeLifecycleEvent.AfterReportUploaded
                BugseeLifecycleEventType.BugseeLifecycleEventBeforeFeedbackShown -> BugseeLifecycleEvent.BeforeFeedbackShown
                BugseeLifecycleEventType.BugseeLifecycleEventAfterFeedbackShown -> BugseeLifecycleEvent.AfterFeedbackShown
                BugseeLifecycleEventType.BugseeLifecycleEventBeforeReportAssembled -> BugseeLifecycleEvent.BeforeReportAssembled
                BugseeLifecycleEventType.BugseeLifecycleEventAfterReportAssembled -> BugseeLifecycleEvent.AfterReportAssembled
                BugseeLifecycleEventType.BugseeLifecycleEventReportUploadFailedWithFutureRetry -> BugseeLifecycleEvent.ReportUploadFailedWithFutureRetry
                BugseeLifecycleEventType.BugseeLifecycleEventReportUploadFailed -> BugseeLifecycleEvent.ReportUploadFailed
                else -> null
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun convertReport(report: swiftPMImport.com.bugsee.library.BugseeReport): BugseeReport {
            return BugseeReport(
                BugseeReportType.fromString(report.type),
                BugseeSeverity.fromLevel(report.severity.toInt()),
                (report.labels as? List<String>) ?: emptyList()
            )
        }

        /**
         * Converts [attachment] for the SDK. Returns `null`, and logs a warning, when the SDK would
         * drop it: it has no data, or more than [MAX_ATTACHMENT_BYTES]. The size of a file is checked
         * before the file is read, so a larger file is not read into memory.
         */
        @OptIn(BetaInteropApi::class)
        fun convertAttachment(attachment: BugseeAttachment): swiftPMImport.com.bugsee.library.BugseeAttachment? {
            val name = attachment.name
            return try {
                val bytes = attachment.data
                val filePath = attachment.filePath
                when {
                    bytes != null -> {
                        if (!fitsSdkLimits(name, bytes.size.toLong())) return null
                        val data = bytes.usePinned { pinned ->
                            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                        }
                        nativeAttachment(name, null, data)
                    }
                    !filePath.isNullOrEmpty() -> convertFileAttachment(name, filePath)
                    else -> {
                        Logger.w(TAG, "attachment '$name' has neither data nor a file path, so it is not added")
                        null
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "convertAttachment failed for '$name'", e)
                null
            }
        }

        private fun convertFileAttachment(
            name: String,
            filePath: String
        ): swiftPMImport.com.bugsee.library.BugseeAttachment? {
            Logger.d(TAG, "convertAttachment filePath = $filePath")
            val url = when {
                // file:// URL (e.g. from Compose Resources Res.getUri())
                filePath.startsWith("file://") -> NSURL.URLWithString(filePath)
                // Absolute path (e.g. /tmp/generated_file.txt)
                filePath.startsWith("/") -> NSURL.fileURLWithPath(filePath)
                // Relative path — resolved against the app bundle's resource directory
                else -> NSURL.fileURLWithPath(NSBundle.mainBundle.resourcePath + "/" + filePath)
            }
            if (url == null) {
                Logger.w(TAG, "attachment '$name': '$filePath' is not a valid file URL, so it is not added")
                return null
            }
            val size = url.path?.let(::fileSize)
            if (size != null && !fitsSdkLimits(name, size)) return null
            val data = NSData.dataWithContentsOfURL(url)
            if (data == null) {
                Logger.w(TAG, "attachment '$name': cannot read $url, so it is not added")
                return null
            }
            // The file may have changed since its size was read.
            if (!fitsSdkLimits(name, data.length.toLong())) return null
            return nativeAttachment(name, filePath.substringAfterLast('/').ifEmpty { name }, data)
        }

        internal fun fileSize(path: String): Long? {
            val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, null) ?: return null
            return when (val size = attributes[NSFileSize]) {
                is Number -> size.toLong()
                is NSNumber -> size.longLongValue
                else -> null
            }
        }

        private fun fitsSdkLimits(name: String, size: Long): Boolean = when {
            size == 0L -> {
                Logger.w(TAG, "attachment '$name' is empty, and the iOS SDK drops empty attachments, so it is not added")
                false
            }
            size > MAX_ATTACHMENT_BYTES -> {
                Logger.w(
                    TAG,
                    "attachment '$name' is $size bytes, over the iOS SDK's limit of $MAX_ATTACHMENT_BYTES bytes, " +
                        "so it is not added"
                )
                false
            }
            else -> true
        }

        private fun nativeAttachment(
            name: String,
            filename: String?,
            data: NSData
        ): swiftPMImport.com.bugsee.library.BugseeAttachment {
            val result = swiftPMImport.com.bugsee.library.BugseeAttachment()
            result.name = name
            if (filename != null) result.filename = filename
            result.data = data
            return result
        }

        @Suppress("UNCHECKED_CAST")
        fun convertReportFieldsFromNative(report: swiftPMImport.com.bugsee.library.BugseeReport): BugseeReportFields {
            return BugseeReportFields(
                report.summary ?: "",
                report.reportDescription ?: "",
                BugseeSeverity.fromLevel(report.severity.toInt()),
                (report.labels as? List<String>) ?: emptyList()
            )
        }

        /**
         * Writes back only the fields that differ from [original], so an untouched `nil`
         * summary or description is not turned into `""`.
         */
        fun applyReportFieldsToNative(
            fields: BugseeReportFields,
            original: BugseeReportFields,
            report: swiftPMImport.com.bugsee.library.BugseeReport
        ) {
            if (fields.summary != original.summary) report.summary = fields.summary
            if (fields.description != original.description) report.reportDescription = fields.description
            if (fields.severity != original.severity) report.severity = fields.severity.getLevelLong()
            if (fields.labels != original.labels) report.labels = fields.labels
        }

        /**
         * Adds the provider's attachments to [report], skipping names already attached: the
         * report handler is not guaranteed to run once per report. `addAttachment` does not say
         * whether the SDK kept the attachment, so a drop is found by the count of attachments.
         */
        fun addProviderAttachments(
            attachments: List<BugseeAttachment>,
            report: swiftPMImport.com.bugsee.library.BugseeReport
        ) {
            val attached = report.attachments
            val existingNames = attached
                .mapNotNullTo(HashSet()) { (it as? swiftPMImport.com.bugsee.library.BugseeAttachment)?.name }
            var count = attached.size
            for (attachment in attachments) {
                if (attachment.name in existingNames) {
                    Logger.d(TAG, "attachments provider: '${attachment.name}' is already attached — skipping")
                    continue
                }
                val nativeAttachment = convertAttachment(attachment) ?: continue
                report.addAttachment(nativeAttachment)
                val newCount = report.attachments.size
                if (newCount > count) {
                    count = newCount
                } else {
                    Logger.w(
                        TAG,
                        "attachments provider: the iOS SDK did not add '${attachment.name}': the provider has " +
                            "already given as many attachments as the SDK keeps, or the SDK stopped waiting for it"
                    )
                }
            }
        }
    }
}