package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import platform.Foundation.NSSelectorFromString
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * iOS SDK 7 puts the fields returned by `bugseeAddFieldsBeforeReportCreated` in place of the
 * ones passed to `showReportDialog`, so a delegate that merely implements it empties the
 * dialog. The report hooks therefore live on the report handler, not on the delegate.
 */
class BugseeReportHooksIosTest {

    @Test
    fun `the delegate does not implement the report hooks`() {
        val delegate = BugseeDelegateWrapper(BugseeInternal())

        assertFalse(delegate.respondsToSelector(NSSelectorFromString("bugseeAddFieldsBeforeReportCreated")))
        assertFalse(delegate.respondsToSelector(NSSelectorFromString("bugseeAddFieldsBeforeReportCreatedWith:")))
        assertFalse(delegate.respondsToSelector(NSSelectorFromString("bugseeCheckFieldsAfterReportCreated:")))
        assertFalse(delegate.respondsToSelector(NSSelectorFromString("bugseeAttachmentsForReport:")))
        assertTrue(delegate.respondsToSelector(NSSelectorFromString("bugseeFilterLog:completionHandler:")))
        assertTrue(delegate.respondsToSelector(NSSelectorFromString("bugseeLifecycleEvent:")))
    }

    @Test
    fun `the report handler implements both callbacks`() {
        val handler = BugseeReportHandlerWrapper(BugseeInternal())

        assertTrue(handler.respondsToSelector(NSSelectorFromString("onBeforeReportCreated:isTerminating:completion:")))
        assertTrue(handler.respondsToSelector(NSSelectorFromString("onAfterReportCreated:isTerminating:completion:")))
    }
}
