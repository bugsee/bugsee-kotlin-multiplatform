package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeLaunchOptions
import com.bugsee.kmp.nsexception.setBugseeUnhandledExceptionHook
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BugseeUnhandledExceptionHookTest {

    @Test
    fun `the hook is installed by default`() {
        assertTrue(shouldInstallUnhandledExceptionHook(null))
        assertTrue(shouldInstallUnhandledExceptionHook(BugseeLaunchOptions()))
    }

    @Test
    fun `detectAndReportCrash false skips the hook`() {
        val options = BugseeLaunchOptions().apply { detectAndReportCrash = false }

        assertFalse(shouldInstallUnhandledExceptionHook(options))
    }

    // Every successful launch asks for the hook; a second one would wrap the first and log one
    // Kotlin crash twice.
    @OptIn(ExperimentalNativeApi::class)
    @Test
    fun `a second install keeps the hook of the first`() {
        setBugseeUnhandledExceptionHook()
        val installed = setUnhandledExceptionHook(null)
        setUnhandledExceptionHook(installed)

        setBugseeUnhandledExceptionHook()

        assertSame(installed, setUnhandledExceptionHook(installed))
    }
}
