package com.bugsee.kmp

import cocoapods.Bugsee.Bugsee
import cocoapods.Bugsee.BugseeAttachmentsDecisionBlock
import cocoapods.Bugsee.BugseeLogEvent
import cocoapods.Bugsee.BugseeLogFilterDecisionBlock
import cocoapods.Bugsee.BugseeNetworkEvent
import cocoapods.Bugsee.BugseeNetworkFilterDecisionBlock
import cocoapods.Bugsee.BugseeReport
import platform.darwin.NSObject

internal class BugseeDelegateWrapper : NSObject(), cocoapods.Bugsee.BugseeDelegateProtocol {
    private var feedbackListener: BugseeFeedbackEventListener? = null
    // private var networkFilter:

    override fun bugseeFilterLog(
        log: BugseeLogEvent,
        completionHandler: BugseeLogFilterDecisionBlock?
    ) {
        // TODO: Implement log filtering logic
    }

    override fun bugseeAttachmentsForReport(
        report: BugseeReport,
        completionHandler: BugseeAttachmentsDecisionBlock?
    ) {
        // TODO: Implement attachments logic
    }

    override fun bugsee(bugsee: Bugsee, didReceiveNewFeedback: List<*>) {
        // TODO: Implement feedback handling
    }

    override fun bugseeFilterNetworkEvent(
        event: BugseeNetworkEvent,
        completionHandler: BugseeNetworkFilterDecisionBlock?
    ) {
        // TODO: Implement network event filtering
    }
}