package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeLaunchOptions
import kotlin.test.Test
import kotlin.test.assertFalse
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
}
