package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BugseeIOSUtilsTest {

    // --- convertLogLevel (KMP to ULong) ---

    @Test
    fun `convertLogLevel to ULong - Error maps to 1`() {
        assertEquals(1uL, BugseeIOSUtils.convertLogLevel(BugseeLogLevel.Error))
    }

    @Test
    fun `convertLogLevel to ULong - Warning maps to 2`() {
        assertEquals(2uL, BugseeIOSUtils.convertLogLevel(BugseeLogLevel.Warning))
    }

    @Test
    fun `convertLogLevel to ULong - Info maps to 3`() {
        assertEquals(3uL, BugseeIOSUtils.convertLogLevel(BugseeLogLevel.Info))
    }

    @Test
    fun `convertLogLevel to ULong - Debug maps to 4`() {
        assertEquals(4uL, BugseeIOSUtils.convertLogLevel(BugseeLogLevel.Debug))
    }

    @Test
    fun `convertLogLevel to ULong - Verbose maps to 5`() {
        assertEquals(5uL, BugseeIOSUtils.convertLogLevel(BugseeLogLevel.Verbose))
    }

    // --- convertLogLevel (ULong to KMP) ---

    @Test
    fun `convertLogLevel from ULong - known values`() {
        assertEquals(BugseeLogLevel.Error, BugseeIOSUtils.convertLogLevel(1uL))
        assertEquals(BugseeLogLevel.Warning, BugseeIOSUtils.convertLogLevel(2uL))
        assertEquals(BugseeLogLevel.Info, BugseeIOSUtils.convertLogLevel(3uL))
        assertEquals(BugseeLogLevel.Debug, BugseeIOSUtils.convertLogLevel(4uL))
        assertEquals(BugseeLogLevel.Verbose, BugseeIOSUtils.convertLogLevel(5uL))
    }

    @Test
    fun `convertLogLevel from ULong - unknown value defaults to Info`() {
        assertEquals(BugseeLogLevel.Info, BugseeIOSUtils.convertLogLevel(0uL))
        assertEquals(BugseeLogLevel.Info, BugseeIOSUtils.convertLogLevel(6uL))
        assertEquals(BugseeLogLevel.Info, BugseeIOSUtils.convertLogLevel(99uL))
        assertEquals(BugseeLogLevel.Info, BugseeIOSUtils.convertLogLevel(ULong.MAX_VALUE))
    }

    @Test
    fun `convertLogLevel - round trip preserves all values`() {
        for (level in BugseeLogLevel.entries) {
            val ulong = BugseeIOSUtils.convertLogLevel(level)
            val roundTrip = BugseeIOSUtils.convertLogLevel(ulong)
            assertEquals(level, roundTrip, "Round trip failed for $level")
        }
    }

    // --- convertNetworkEventStage ---

    @Test
    fun `convertNetworkEventStage - known strings`() {
        assertEquals(BugseeNetworkEventStage.Before, BugseeIOSUtils.convertNetworkEventStage("before"))
        assertEquals(BugseeNetworkEventStage.Complete, BugseeIOSUtils.convertNetworkEventStage("complete"))
        assertEquals(BugseeNetworkEventStage.Cancel, BugseeIOSUtils.convertNetworkEventStage("cancel"))
        assertEquals(BugseeNetworkEventStage.Errors, BugseeIOSUtils.convertNetworkEventStage("error"))
    }

    @Test
    fun `convertNetworkEventStage - unknown string defaults to Before`() {
        assertEquals(BugseeNetworkEventStage.Before, BugseeIOSUtils.convertNetworkEventStage("unknown"))
        assertEquals(BugseeNetworkEventStage.Before, BugseeIOSUtils.convertNetworkEventStage(""))
        assertEquals(BugseeNetworkEventStage.Before, BugseeIOSUtils.convertNetworkEventStage("BEFORE"))
        assertEquals(BugseeNetworkEventStage.Before, BugseeIOSUtils.convertNetworkEventStage("redirect"))
    }

    // --- convertExceptionLoggingOptions ---

    @Test
    fun `convertExceptionLoggingOptions - null returns null`() {
        assertNull(BugseeIOSUtils.convertExceptionLoggingOptions(null))
    }

    @Test
    fun `convertExceptionLoggingOptions - populated options`() {
        val options = BugseeExceptionLoggingOptions()
        options.exceptionDomain = "com.test.ios"
        options.includeVideo = false
        options.labels = arrayListOf("ios-label")
        options.rules.skipFrames = 2

        val nativeOptions = BugseeIOSUtils.convertExceptionLoggingOptions(options)
        // Verify non-null (native type details depend on cocoapods.Bugsee availability)
        assertEquals("com.test.ios", nativeOptions?.exceptionDomain)
        assertEquals(false, nativeOptions?.includeVideo)
    }
}
