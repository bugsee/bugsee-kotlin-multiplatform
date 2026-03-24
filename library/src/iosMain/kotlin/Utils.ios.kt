package com.bugsee.kmp

import com.bugsee.kmp.internal.Logger
import kotlinx.cinterop.*
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

        fun convertExceptionLoggingOptions(options: com.bugsee.kmp.BugseeExceptionLoggingOptions?): cocoapods.Bugsee.BugseeExceptionLoggingOptions? {
            if (options == null) {
                return null
            }

            val result = cocoapods.Bugsee.BugseeExceptionLoggingOptions()
            result.exceptionDomain = options.exceptionDomain
            result.labels = options.labels?.toList()
            result.includeVideo = options.includeVideo
            @Suppress("UNCHECKED_CAST")
            result.setMergingRules(options.rules.toMap() as Map<Any?, *>?)

            return result
        }

        @Suppress("UNCHECKED_CAST")
        fun convertReport(report: cocoapods.Bugsee.BugseeReport): BugseeReport {
            return BugseeReport(
                BugseeReportType.fromString(report.type),
                BugseeSeverity.fromLevel(report.severity.toInt()),
                (report.labels as? List<String>) ?: emptyList()
            )
        }

        fun convertReport(report: BugseeReport): cocoapods.Bugsee.BugseeReport {
            val nativeReport = cocoapods.Bugsee.BugseeReport()
            nativeReport.setType(report.type.toStringValue())
            nativeReport.setSeverity(report.severity.getLevelLong())
            nativeReport.labels = report.labels
            return nativeReport
        }

        @OptIn(BetaInteropApi::class)
        fun convertAttachment(attachment: BugseeAttachment): cocoapods.Bugsee.BugseeAttachment {
            val result = cocoapods.Bugsee.BugseeAttachment()
            if (attachment.data != null) {
                val size = attachment.data.size
                attachment.data.usePinned { pinned ->
                    result.data = NSData.create(
                        bytes = pinned.addressOf(0),
                        length = size.toULong()
                    )
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
                    }
                }
            }

            // Ensure attachment name is set
            result.name = attachment.name
            return result
        }
    }
}