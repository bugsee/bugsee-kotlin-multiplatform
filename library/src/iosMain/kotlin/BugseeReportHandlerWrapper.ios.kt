package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import com.bugsee.kmp.internal.Logger
import platform.darwin.NSObject
import swiftPMImport.com.bugsee.library.BugseeEmptyBlock
import swiftPMImport.com.bugsee.library.BugseeReport
import swiftPMImport.com.bugsee.library.BugseeReportHandlerProtocol
import kotlin.native.ref.WeakReference

private const val TAG = "BugseeReportHandler"

/**
 * Runs the report fields pre-/post-filters and the attachments provider on iOS SDK 7's report
 * handler, as the Android actual does. They are not delegate hooks any more: iOS SDK 7 replaces
 * the fields passed to `showReportDialog` with whatever `bugseeAddFieldsBeforeReportCreated`
 * returns, while the handler works on the live report, which already carries them.
 *
 * The pre-filter runs before the report is created (for a dialog, before it opens); the
 * post-filter and the attachments provider after. The completion is always called.
 */
internal class BugseeReportHandlerWrapper(bugseeInternal: BugseeInternal) : NSObject(), BugseeReportHandlerProtocol {
    private val weakBugseeInternal: WeakReference<BugseeInternal> = WeakReference(bugseeInternal)

    override fun onBeforeReportCreated(report: BugseeReport, isTerminating: Boolean, completion: BugseeEmptyBlock) {
        try {
            weakBugseeInternal.get()?.reportFieldsFiller?.let { filler ->
                val original = BugseeIOSUtils.convertReportFieldsFromNative(report)
                val fields = BugseeIOSUtils.convertReportFieldsFromNative(report)
                filler.invoke(fields)
                BugseeIOSUtils.applyReportFieldsToNative(fields, original, report)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "exception during report fields pre-filter execution", e)
        } finally {
            complete(completion)
        }
    }

    override fun onAfterReportCreated(report: BugseeReport, isTerminating: Boolean, completion: BugseeEmptyBlock) {
        val bugseeInternal = weakBugseeInternal.get()
        // The post-filter and the attachments provider were independent hooks in 0.1.x:
        // a failure in one must not cancel the other.
        try {
            bugseeInternal?.reportFieldsFilter?.let { filter ->
                val original = BugseeIOSUtils.convertReportFieldsFromNative(report)
                val fields = BugseeIOSUtils.convertReportFieldsFromNative(report)
                BugseeIOSUtils.applyReportFieldsToNative(filter.invoke(fields), original, report)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "exception during report fields post-filter execution", e)
        }

        try {
            bugseeInternal?.attachmentsProviderHandler?.invoke(BugseeIOSUtils.convertReport(report))?.let { attachments ->
                BugseeIOSUtils.addProviderAttachments(attachments, report)
            }
        } catch (e: Throwable) {
            Logger.e(TAG, "exception during report attachments provider execution", e)
        } finally {
            complete(completion)
        }
    }

    // An exception must not cross the Objective-C boundary.
    private fun complete(completion: BugseeEmptyBlock) {
        try {
            completion?.invoke()
        } catch (e: Throwable) {
            Logger.e(TAG, "report handler completion threw", e)
        }
    }
}
