package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import swiftPMImport.com.bugsee.library.BugseeOptions as NativeOptions

class LegacyLaunchOptionKeysIosTest {

    @Suppress("UNCHECKED_CAST")
    private fun nativeOptions(options: BugseeLaunchOptions): NativeOptions =
        assertNotNull(NativeOptions.optionsFrom(options.toMap() as Map<Any?, *>))

    // iOS SDK 7 reads "BuildType" and "VideoEnabled" as older names of build-type and capture.video
    // (legacyKeyAliases). Before #57 they reached it, and "BuildType", an option without a default,
    // took effect on iOS only.
    @Test
    fun `the iOS SDK gets no key from before 0_2_0`() {
        val native = nativeOptions(BugseeLaunchOptions(mapOf("BuildType" to "beta", "VideoEnabled" to false)))

        assertNull(native.buildType)
        assertTrue(native.videoEnabled)
    }

    @Test
    fun `the iOS SDK gets the new key`() {
        val native = nativeOptions(BugseeLaunchOptions(mapOf(BugseeOptions.BuildType to "beta")))

        assertEquals("beta", native.buildType)
    }
}
