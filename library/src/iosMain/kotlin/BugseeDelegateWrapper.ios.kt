package com.bugsee.kmp

import cocoapods.Bugsee.BugseeLogFilterDecisionBlock
import cocoapods.Bugsee.BugseeNetworkFilterDecisionBlock
import cocoapods.Bugsee.BugseeReport
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

    override fun bugseeAttachmentsForReport(report: BugseeReport): List<*> {
        var attachments: List<BugseeAttachment>? = null
        try {
            val bugseeInternal = weakBugseeInternal.get()
            if (bugseeInternal != null) {
                // Capture the provider to avoid race conditions
                val attachmentsProviderHandler = bugseeInternal.attachmentsProviderHandler
                if (attachmentsProviderHandler != null) {
                    try {
                        attachments = attachmentsProviderHandler.invoke(BugseeIOSUtils.convertReport(report))
                    } catch (e: Exception) {
                        // Log the error but don't crash the app
                        Bugsee.log("BugseeDelegateWrapper: exception during attachmentsProviderHandler invoke: ${e.message}", BugseeLogLevel.Warning)
                    }
                }
            }
        } catch (e: Exception) {
            // Ensure we don't crash the native SDK
            Bugsee.log("BugseeDelegateWrapper: bugseeAttachmentsForReport: caught exception: ${e.message}", BugseeLogLevel.Warning)
        }

        if (attachments == null) {
            return listOf<cocoapods.Bugsee.BugseeAttachment>()
        }

        return ArrayList(attachments.map(BugseeIOSUtils.Companion::convertAttachment))
    }

    override fun bugsee(bugsee: cocoapods.Bugsee.Bugsee, didReceiveNewFeedback: List<*>) {
        // TODO: Implement feedback handling
    }

    override fun bugseeFilterNetworkEvent(
        event: cocoapods.Bugsee.BugseeNetworkEvent,
        completionHandler: BugseeNetworkFilterDecisionBlock?
    ) {
        try {
            val bugseeInternal = weakBugseeInternal.get()
            if (bugseeInternal != null) {
                // Capture the filter handler to avoid race conditions
                val filterHandler = bugseeInternal.networkFilterHandler
                if (filterHandler != null) {
                    try {
                        val kmpNetworkEvent = BugseeNetworkEvent(impl = event)
                        val filteredNetworkEvent =
                            filterHandler.invoke(kmpNetworkEvent)

                    } catch (e: Exception) {
                        // Log the error but don't crash the app
                        // The original network event will be used if filtering fails
                        Bugsee.log("BugseeDelegateWrapper: exception during networkFilterHandler invoke: ${e.message}", BugseeLogLevel.Warning)
                    }
                }
            }
        } catch (e: Exception) {
            // Ensure we don't crash the native SDK
            Bugsee.log("BugseeDelegateWrapper: bugseeFilterNetworkEvent: caught exception: ${e.message}", BugseeLogLevel.Warning)
        } finally {
            // Always call the completion handler to indicate filtering is done
            completionHandler?.invoke(event)
        }
    }

    override fun bugseeLifecycleEvent(eventType: cocoapods.Bugsee.BugseeLifecycleEventType) {
        try {
            val bugseeInternal = weakBugseeInternal.get()
            if (bugseeInternal != null) {
                // Capture the lifecycle event handler to avoid race conditions
                val lifecycleEventHandler = bugseeInternal.lifecycleEventHandler
                if (lifecycleEventHandler != null) {
                    try {
                        val kmpEventType = BugseeLifecycleEvent.fromEventType(eventType.value.toInt())
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