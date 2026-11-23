package com.bugsee.kmp

import swiftPMImport.com.bugsee.library.BugseeLogFilterDecisionBlock
import com.bugsee.kmp.internal.BugseeInternal
import com.bugsee.kmp.internal.describeThrowable
import platform.darwin.NSObject
import kotlin.native.ref.WeakReference

// Only the log filter and the lifecycle events go through the delegate. The report fields
// filters and the attachments provider run on the report handler (BugseeReportHandlerWrapper):
// the wrapper must not implement bugseeAddFieldsBeforeReportCreated, whose fields iOS SDK 7
// puts in place of the ones passed to showReportDialog.
internal class BugseeDelegateWrapper(bugseeInternal: BugseeInternal) : NSObject(), swiftPMImport.com.bugsee.library.BugseeDelegateProtocol {
    private val weakBugseeInternal: WeakReference<BugseeInternal> = WeakReference(bugseeInternal)

    override fun bugseeFilterLog(
        log: swiftPMImport.com.bugsee.library.BugseeLogEvent,
        completionHandler: BugseeLogFilterDecisionBlock
    ) {
        // The original log when no filter is registered; otherwise what the filter returns
        // (null drops the log), and null when the filter throws, as the SDK drops it.
        var decision: swiftPMImport.com.bugsee.library.BugseeLogEvent? = log
        // Read once: the filter and its reporter, replaced together by setLogFilter.
        val state = weakBugseeInternal.get()?.logFilterState
        if (state != null) {
            try {
                val filtered = state.filter.invoke(BugseeLogEvent(log))
                // null => drop the log; non-null => keep (mutations in `impl` propagate)
                decision = filtered?.impl
            } catch (e: Throwable) {
                // Don't crash the app; drop the log. Not to the console: this runs on the SDK
                // thread that reads stderr (see FilterFailureReporter).
                state.failures.onFailure(e)
                decision = null
            }
        }
        try {
            // Always call the completion handler to indicate filtering is done
            completionHandler?.invoke(decision)
        } catch (ignored: Throwable) {
            // The SDK's callback, not the app's filter, so it is not reported as a filter failure.
            // Kept from crossing the Obj-C boundary, and not logged to the console from this thread.
        }
    }

    // Throwable: a Kotlin throwable that leaves a method called from Objective-C terminates the
    // app, and an Error (TODO(), AssertionError) is not an Exception.
    override fun bugseeLifecycleEvent(eventType: swiftPMImport.com.bugsee.library.BugseeLifecycleEventType) {
        try {
            val bugseeInternal = weakBugseeInternal.get()
            if (bugseeInternal != null) {
                // Capture the lifecycle event handler to avoid race conditions
                val lifecycleEventHandler = bugseeInternal.lifecycleEventHandler
                val kmpEventType = BugseeIOSUtils.convertLifecycleEvent(eventType)
                if (lifecycleEventHandler != null && kmpEventType != null) {
                    lifecycleEventHandler.invoke(kmpEventType)
                }
            }
        } catch (e: Throwable) {
            try {
                Bugsee.log(
                    "BugseeDelegateWrapper: the lifecycle events listener threw ${describeThrowable(e)}",
                    BugseeLogLevel.Warning
                )
            } catch (ignored: Throwable) {
            }
        }
    }
}
