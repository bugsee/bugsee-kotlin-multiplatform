package com.bugsee.kmp

import cocoapods.Bugsee.BugseeAttachmentsDecisionBlock
import cocoapods.Bugsee.BugseeLogFilterDecisionBlock
import com.bugsee.kmp.internal.BugseeInternal
import platform.darwin.NSObject
import kotlin.native.ref.WeakReference

internal class BugseeDelegateWrapper(bugseeInternal: BugseeInternal) : NSObject(), cocoapods.Bugsee.BugseeDelegateProtocol {
    private val weakBugseeInternal: WeakReference<BugseeInternal> = WeakReference(bugseeInternal)

    override fun bugseeFilterLog(
        log: cocoapods.Bugsee.BugseeLogEvent,
        completionHandler: BugseeLogFilterDecisionBlock?
    ) {
        try {
            val bugseeInternal = weakBugseeInternal.get()
            if (bugseeInternal != null) {
                // Capture the filter handler to avoid race conditions
                val filterHandler = bugseeInternal.logFilterHandler
                if (filterHandler != null) {
                    try {
                        val kmpLogEvent = BugseeLogEvent(log)
                        val filteredLogEvent = filterHandler.invoke(kmpLogEvent)
                        
                        // Update the log text with the filtered message
                        if (filteredLogEvent != null) {
                            log.text = filteredLogEvent.message
                        }
                    } catch (e: Exception) {
                        // Log the error but don't crash the app
                        // The original log will be used if filtering fails
                        println("BugseeDelegateWrapper: exception during logFilterHandler invoke: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            // Ensure we don't crash the native SDK
            println("BugseeDelegateWrapper: bugseeFilterLog: caught exception: ${e.message}")
        } finally {
            // Always call the completion handler to indicate filtering is done
            completionHandler?.invoke(log)
        }
    }

    override fun bugseeAttachmentsForReport(
        report: cocoapods.Bugsee.BugseeReport,
        completionHandler: BugseeAttachmentsDecisionBlock?
    ) {
        // TODO: Implement attachments logic
        completionHandler?.invoke(null)
    }

    override fun bugsee(bugsee: cocoapods.Bugsee.Bugsee, didReceiveNewFeedback: List<*>) {
        // TODO: Implement feedback handling
    }

//    override fun bugseeFilterNetworkEvent(
//        event: cocoapods.Bugsee.BugseeNetworkEvent,
//        completionHandler: BugseeNetworkFilterDecisionBlock?
//    ) {
//        // TODO: Implement network event filtering
//        completionHandler?.invoke(null)
//    }
}