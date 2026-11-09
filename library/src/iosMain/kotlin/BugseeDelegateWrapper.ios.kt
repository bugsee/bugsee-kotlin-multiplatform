package com.bugsee.kmp

import swiftPMImport.com.bugsee.library.BugseeLogFilterDecisionBlock
import com.bugsee.kmp.internal.BugseeInternal
import com.bugsee.kmp.internal.Logger
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
        // Default to keeping the original log; flipped to filtered.impl (or null
        // to drop) if a filter is registered and runs successfully.
        var decision: swiftPMImport.com.bugsee.library.BugseeLogEvent? = log
        try {
            // Capture the filter handler to avoid race conditions
            val filterHandler = weakBugseeInternal.get()?.logFilterHandler
            if (filterHandler != null) {
                val filtered = filterHandler.invoke(BugseeLogEvent(log))
                // null => drop the log; non-null => keep (mutations in `impl` propagate)
                decision = filtered?.impl
            }
        } catch (e: Throwable) {
            // Log the error but don't crash the app — fall through with the original log
            Logger.e("BugseeDelegateWrapper", "bugseeFilterLog: caught exception: ${e.message}")
            decision = log
        }
        try {
            // Always call the completion handler to indicate filtering is done
            completionHandler?.invoke(decision)
        } catch (e: Throwable) {
            // Don't let an exception cross the Obj-C boundary
            Logger.e("BugseeDelegateWrapper", "bugseeFilterLog: completionHandler threw: ${e.message}")
        }
    }

    override fun bugseeLifecycleEvent(eventType: swiftPMImport.com.bugsee.library.BugseeLifecycleEventType) {
        try {
            val bugseeInternal = weakBugseeInternal.get()
            if (bugseeInternal != null) {
                // Capture the lifecycle event handler to avoid race conditions
                val lifecycleEventHandler = bugseeInternal.lifecycleEventHandler
                val kmpEventType = BugseeIOSUtils.convertLifecycleEvent(eventType)
                if (lifecycleEventHandler != null && kmpEventType != null) {
                    try {
                        lifecycleEventHandler.invoke(kmpEventType)
                    } catch (e: Exception) {
                        // Log the error but don't crash the app
                        Bugsee.log("BugseeDelegateWrapper: exception during lifecycleEventHandler invoke: ${e.message}", BugseeLogLevel.Warning)
                    }
                }
            }
        } catch (e: Exception) {
            // Ensure we don't crash the native SDK
            Bugsee.log("BugseeDelegateWrapper: bugseeLifecycleEvent: caught exception: ${e.message}", BugseeLogLevel.Warning)
        }
    }
}
