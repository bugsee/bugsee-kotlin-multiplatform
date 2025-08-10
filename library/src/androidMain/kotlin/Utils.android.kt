package com.bugsee.kmp

import android.graphics.Rect
import com.bugsee.library.data.IssueSeverity
import com.bugsee.library.data.IssueType
import com.bugsee.library.events.BugseeLogLevel
import com.bugsee.library.network.data.NetworkEventType

internal class BugseeAndroidUtils {
    internal companion object {
        fun convertIssueType(issueType: IssueType): BugseeReportType {
            return when (issueType) {
                IssueType.Bug -> BugseeReportType.Bug
                IssueType.Error -> BugseeReportType.Error
                IssueType.Crash -> BugseeReportType.Crash
            }
        }

        fun convertIssueType(issueType: BugseeReportType): IssueType {
            return when (issueType) {
                BugseeReportType.Bug -> IssueType.Bug
                BugseeReportType.Error -> IssueType.Error
                BugseeReportType.Crash -> IssueType.Crash
            }
        }

        fun convertLogLevel(logLevel: com.bugsee.kmp.BugseeLogLevel): BugseeLogLevel {
            return when (logLevel) {
                com.bugsee.kmp.BugseeLogLevel.Debug -> BugseeLogLevel.Debug
                com.bugsee.kmp.BugseeLogLevel.Info -> BugseeLogLevel.Info
                com.bugsee.kmp.BugseeLogLevel.Warning -> BugseeLogLevel.Warning
                com.bugsee.kmp.BugseeLogLevel.Error -> BugseeLogLevel.Error
                com.bugsee.kmp.BugseeLogLevel.Verbose -> BugseeLogLevel.Verbose
            }
        }

        fun convertLogLevel(logLevel: BugseeLogLevel): com.bugsee.kmp.BugseeLogLevel {
            return when (logLevel) {
                BugseeLogLevel.Debug -> com.bugsee.kmp.BugseeLogLevel.Debug
                BugseeLogLevel.Info -> com.bugsee.kmp.BugseeLogLevel.Info
                BugseeLogLevel.Warning -> com.bugsee.kmp.BugseeLogLevel.Warning
                BugseeLogLevel.Error -> com.bugsee.kmp.BugseeLogLevel.Error
                BugseeLogLevel.Verbose -> com.bugsee.kmp.BugseeLogLevel.Verbose
            }
        }

        fun convertSeverity(severity: com.bugsee.kmp.BugseeSeverity): IssueSeverity {
            return when (severity) {
                com.bugsee.kmp.BugseeSeverity.Critical -> IssueSeverity.Critical
                com.bugsee.kmp.BugseeSeverity.High -> IssueSeverity.High
                com.bugsee.kmp.BugseeSeverity.Medium -> IssueSeverity.Medium
                com.bugsee.kmp.BugseeSeverity.VeryLow -> IssueSeverity.VeryLow
                com.bugsee.kmp.BugseeSeverity.Blocker -> IssueSeverity.Blocker
            }
        }

        fun convertSeverity(severity: IssueSeverity): com.bugsee.kmp.BugseeSeverity {
            return when (severity) {
                IssueSeverity.Critical -> com.bugsee.kmp.BugseeSeverity.Critical
                IssueSeverity.High -> com.bugsee.kmp.BugseeSeverity.High
                IssueSeverity.Medium -> com.bugsee.kmp.BugseeSeverity.Medium
                IssueSeverity.VeryLow -> com.bugsee.kmp.BugseeSeverity.VeryLow
                IssueSeverity.Blocker -> com.bugsee.kmp.BugseeSeverity.Blocker
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

        fun convertReport(report: com.bugsee.library.attachment.Report): BugseeReport {
            return BugseeReport(
                convertIssueType(report.type),
                convertSeverity(report.severity),
                report.labels?.toList() ?: emptyList()
            )
        }

        fun convertReport(report: BugseeReport): com.bugsee.library.attachment.Report {
            return com.bugsee.library.attachment.Report(
                convertIssueType(report.type),
                convertSeverity(report.severity),
                if (report.labels != null) ArrayList(report.labels) else null
            )
        }

        fun convertAttachment(attachment: com.bugsee.library.attachment.CustomAttachment): BugseeAttachment {
            if (attachment.dataBytes != null) {
                return BugseeAttachment.create(
                    attachment.name,
                    attachment.dataBytes
                )
            }

            return BugseeAttachment.create(
                attachment.name,
                attachment.dataFilePath
            )
        }

        fun convertAttachment(attachment: BugseeAttachment): com.bugsee.library.attachment.CustomAttachment {
            var result: com.bugsee.library.attachment.CustomAttachment

            if (attachment.data != null) {
                result =
                    com.bugsee.library.attachment.CustomAttachment.fromDataBytes(attachment.data)
            } else {
                result =
                    com.bugsee.library.attachment.CustomAttachment.fromDataFilePath(attachment.filePath)
            }

            // Ensure attachment name is set
            result.name = attachment.name

            return result
        }

        fun convertReportFields(reportFields: com.bugsee.library.send.ReportFields): BugseeReportFields {
            return BugseeReportFields(
                reportFields.summary,
                reportFields.description,
                convertSeverity(reportFields.severity),
                reportFields.labels?.toList() ?: emptyList()
            )
        }

        fun convertReportFields(reportFields: BugseeReportFields): com.bugsee.library.send.ReportFields {
            return com.bugsee.library.send.ReportFields(
                reportFields.summary,
                reportFields.description,
                ArrayList(reportFields.labels),
                convertSeverity(reportFields.severity)
            )
        }

        fun convertExtendedReport(extendedReport: com.bugsee.library.attachment.ExtendedReport): BugseeExtendedReport {
            return BugseeExtendedReport(extendedReport)
        }

        fun convertExtendedReport(extendedReport: BugseeExtendedReport): com.bugsee.library.attachment.ExtendedReport {
            return extendedReport.underlyingReport
        }

        fun convertExceptionLoggingOptions(options: com.bugsee.kmp.BugseeExceptionLoggingOptions?): com.bugsee.library.ExceptionLoggingOptions? {
            if (options == null) {
                return null
            }

            val result = com.bugsee.library.ExceptionLoggingOptions()
            result.labels = options.labels
            result.includeVideo = options.includeVideo
            result.exceptionDomain = options.exceptionDomain

            val customOptions = options.rules.toMap()
            for (option in customOptions) {
                result.Rules.setCustomOption(option.key, option.value)
            }
            result.Rules.skipFrames = options.rules.skipFrames

            return result
        }

        fun convertLifecycleEvent(event: com.bugsee.library.lifecycle.LifecycleEventTypes): BugseeLifecycleEvent {
            return when (event) {
                com.bugsee.library.lifecycle.LifecycleEventTypes.Launched -> BugseeLifecycleEvent.Launched
                com.bugsee.library.lifecycle.LifecycleEventTypes.Started -> BugseeLifecycleEvent.Started
                com.bugsee.library.lifecycle.LifecycleEventTypes.Stopped -> BugseeLifecycleEvent.Stopped
                com.bugsee.library.lifecycle.LifecycleEventTypes.Resumed -> BugseeLifecycleEvent.Resumed
                com.bugsee.library.lifecycle.LifecycleEventTypes.Paused -> BugseeLifecycleEvent.Paused
                com.bugsee.library.lifecycle.LifecycleEventTypes.RelaunchedAfterCrash -> BugseeLifecycleEvent.RelaunchedAfterCrash
                com.bugsee.library.lifecycle.LifecycleEventTypes.BeforeReportShown -> BugseeLifecycleEvent.BeforeReportShown
                com.bugsee.library.lifecycle.LifecycleEventTypes.AfterReportShown -> BugseeLifecycleEvent.AfterReportShown
                com.bugsee.library.lifecycle.LifecycleEventTypes.BeforeReportUploaded -> BugseeLifecycleEvent.BeforeReportUploaded
                com.bugsee.library.lifecycle.LifecycleEventTypes.AfterReportUploaded -> BugseeLifecycleEvent.AfterReportUploaded
                com.bugsee.library.lifecycle.LifecycleEventTypes.BeforeFeedbackShown -> BugseeLifecycleEvent.BeforeFeedbackShown
                com.bugsee.library.lifecycle.LifecycleEventTypes.AfterFeedbackShown -> BugseeLifecycleEvent.AfterFeedbackShown
                com.bugsee.library.lifecycle.LifecycleEventTypes.BeforeReportAssembled -> BugseeLifecycleEvent.BeforeReportAssembled
                com.bugsee.library.lifecycle.LifecycleEventTypes.AfterReportAssembled -> BugseeLifecycleEvent.AfterReportAssembled
                com.bugsee.library.lifecycle.LifecycleEventTypes.ReportUploadFailedWithFutureRetry -> BugseeLifecycleEvent.ReportUploadFailedWithFutureRetry
                com.bugsee.library.lifecycle.LifecycleEventTypes.ReportUploadFailed -> BugseeLifecycleEvent.ReportUploadFailed
            }
        }

        fun convertNetworkEventStage(stage: NetworkEventType): BugseeNetworkEventStage {
            return when (stage) {
                NetworkEventType.Before -> BugseeNetworkEventStage.Before
                NetworkEventType.Complete -> BugseeNetworkEventStage.Complete
                NetworkEventType.Redirect -> BugseeNetworkEventStage.Redirect
                NetworkEventType.Errors -> BugseeNetworkEventStage.Errors
                NetworkEventType.WebSocket -> BugseeNetworkEventStage.WebSocket
            }
        }
    }
}