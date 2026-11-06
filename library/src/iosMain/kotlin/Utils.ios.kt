package com.bugsee.kmp

import com.bugsee.kmp.internal.Logger
import kotlinx.cinterop.*
import swiftPMImport.com.bugsee.library.BugseeLifecycleEventType
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL

internal class BugseeIOSUtils {
    internal companion object {

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

        @OptIn(BetaInteropApi::class)
        fun convertAttachment(attachment: BugseeAttachment): swiftPMImport.com.bugsee.library.BugseeAttachment {
            val result = swiftPMImport.com.bugsee.library.BugseeAttachment()
            try {
                if (attachment.data != null) {
                    val size = attachment.data.size
                    // addressOf(0) throws on an empty ByteArray, so produce empty NSData directly.
                    result.data = if (size == 0) {
                        NSData()
                    } else {
                        attachment.data.usePinned { pinned ->
                            NSData.create(
                                bytes = pinned.addressOf(0),
                                length = size.toULong()
                            )
                        }
                    }
                } else if (!attachment.filePath.isNullOrEmpty()) {
                    Logger.d("Utils", "convertAttachment filePath = ${attachment.filePath}")
                    val url = when {
                        // file:// URL (e.g. from Compose Resources Res.getUri())
                        attachment.filePath.startsWith("file://") ->
                            NSURL.URLWithString(attachment.filePath)
                        // Absolute path (e.g. /tmp/generated_file.txt)
                        attachment.filePath.startsWith("/") ->
                            NSURL.fileURLWithPath(attachment.filePath)
                        // Relative path — resolved against the app bundle's resource directory
                        else ->
                            NSURL.fileURLWithPath(
                                NSBundle.mainBundle.resourcePath + "/" + attachment.filePath
                            )
                    }
                    if (url != null) {
                        val fileData = NSData.dataWithContentsOfURL(url)
                        if (fileData != null) {
                            result.data = fileData
                            result.filename = attachment.filePath.substringAfterLast('/').ifEmpty { attachment.name }
                        } else {
                            Logger.d("Utils", "convertAttachment: could not read file at $url for '${attachment.name}'")
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("Utils", "convertAttachment failed for '${attachment.name}'", e)
            }

            // Ensure attachment name is set
            result.name = attachment.name
            return result
        }

        @Suppress("UNCHECKED_CAST")
        fun convertReportFieldsFromNative(nativeFields: swiftPMImport.com.bugsee.library.BugseeReportFields): BugseeReportFields {
            return BugseeReportFields(
                nativeFields.summary ?: "",
                nativeFields.reportDescription ?: "",
                BugseeSeverity.fromLevel(nativeFields.severity.toInt()),
                (nativeFields.labels as? List<String>) ?: emptyList()
            )
        }

        fun convertReportFieldsToNative(kmpFields: BugseeReportFields): swiftPMImport.com.bugsee.library.BugseeReportFields {
            return swiftPMImport.com.bugsee.library.BugseeReportFields.reportFieldsWith(
                kmpFields.summary,
                kmpFields.description,
                kmpFields.severity.getLevelLong(),
                kmpFields.labels
            )
        }
    }
}