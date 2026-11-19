package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import com.bugsee.kmp.internal.Logger
import swiftPMImport.com.bugsee.library.BugseeLifecycleEventType
import swiftPMImport.com.bugsee.library.BugseeReport
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * An Error (TODO(), AssertionError) thrown by an app's callback must not leave a method the SDK
 * calls from Objective-C: that terminates the app (#52).
 */
class BugseeCallbackErrorsIosTest {

    @Test
    fun `an Error in the lifecycle listener does not escape`() {
        val internal = BugseeInternal()
        var calls = 0
        internal.lifecycleEventHandler = { calls++; TODO("listener") }

        BugseeDelegateWrapper(internal).bugseeLifecycleEvent(BugseeLifecycleEventType.BugseeLifecycleEventLaunched)

        assertEquals(1, calls)
    }

    @Test
    fun `an Error in the post-filter still runs the attachments provider`() {
        val internal = BugseeInternal()
        var providerCalls = 0
        internal.reportFieldsFilter = { TODO("post-filter") }
        internal.attachmentsProviderHandler = { providerCalls++; emptyList() }
        var completions = 0

        BugseeReportHandlerWrapper(internal).onAfterReportCreated(bareReport(), false) { completions++ }

        assertEquals(1, providerCalls)
        assertEquals(1, completions)
    }

    @Test
    fun `an exception whose toString throws is logged without escaping`() {
        val handlerError = ToStringThrows()
        val internal = BugseeInternal()
        internal.attachmentsProviderHandler = { throw handlerError }
        var completions = 0

        BugseeReportHandlerWrapper(internal).onAfterReportCreated(bareReport(), false) { completions++ }

        assertEquals(1, completions)
        Logger.e("BugseeCallbackErrorsIosTest", "cause", IllegalStateException("outer", ToStringThrows()))
    }

    // The SDK has no public initializer for a report; a bare one (every field nil) is enough to
    // drive the hooks.
    @Suppress("DEPRECATION")
    private fun bareReport(): BugseeReport = BugseeReport()

    private class ToStringThrows : RuntimeException() {
        override fun toString(): String = throw IllegalStateException("toString")
    }
}
