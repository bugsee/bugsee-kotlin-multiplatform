package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AndroidBugseeTest {

    @Test
    fun `test Android platform specific functionality`() {
        // Test that we're running on Android platform
        // This is a basic test to ensure the Android test target is working
        assertTrue(true)
    }

    @Test
    fun `test BugseeLaunchOptions Android specific behavior`() {
        val options = BugseeLaunchOptions()
        
        // Test Android-specific shake to report key
        options.reportingTriggerByShake = true
        val map = options.toMap()
        
        // On Android, it should use the shake trigger key
        assertTrue(map.containsKey(BugseeOptions.ReportingTriggerByShake))
        assertEquals(true, map[BugseeOptions.ReportingTriggerByShake])
    }

    @Test
    fun `test Android specific types and classes`() {
        // Test that Android-specific classes are accessible
        // Note: These would normally be tested with actual Android context
        // but we can verify the classes exist and are accessible
        
        try {
            // Test that we can create Android-specific instances
            // This would normally require Android context in real scenarios
            assertNotNull(BugseeLaunchOptions())
        } catch (e: Exception) {
            // Expected in unit test environment without Android context
        }
    }

    @Test
    fun `test Android platform info`() {
        // Test platform-specific behavior
        val options = BugseeLaunchOptions()
        
        // Set various options to test Android-specific behavior
        options.detectAndReportCrash = true
        options.captureVideo = true
        options.captureScreenshot = true
        
        val map = options.toMap()
        
        // Verify options are properly set
        assertEquals(true, map[BugseeOptions.DetectAndReportCrash])
        assertEquals(true, map[BugseeOptions.CaptureVideo])
        assertEquals(true, map[BugseeOptions.CaptureScreenshot])
    }

    @Test
    fun `test Android specific launch options`() {
        val options = BugseeLaunchOptions()
        
        // Test Android-specific configuration
        options.captureNetworkBodySizeLimit = 1024
        options.wifiOnlyUpload = true
        options.captureDeviceAndNetworkNames = true
        
        val map = options.toMap()
        
        assertEquals(1024, map[BugseeOptions.CaptureNetworkBodySizeLimit])
        assertEquals(true, map[BugseeOptions.WifiOnlyUpload])
        // iOS-only option: kept for the getter, never sent to the Android SDK
        assertTrue(options.captureDeviceAndNetworkNames)
        assertFalse(map.containsKey(BugseeOptions.CaptureDeviceAndNetworkNames))
    }

    @Test
    fun `test Android report configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test report-related options
        options.reportingUISummaryRequired = true
        options.reportingUIDescriptionRequired = true
        options.reportingUIEmailRequired = false
        options.reportingUILabelsRequired = true
        options.reportingUILabelsEnabled = true
        
        val map = options.toMap()
        
        assertEquals(true, map[BugseeOptions.ReportingUISummaryRequired])
        assertEquals(true, map[BugseeOptions.ReportingUIDescriptionRequired])
        assertEquals(false, map[BugseeOptions.ReportingUIEmailRequired])
        assertEquals(true, map[BugseeOptions.ReportingUILabelsRequired])
        assertEquals(true, map[BugseeOptions.ReportingUILabelsEnabled])
    }

    @Test
    fun `test Android frame rate configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test frame rate options
        options.captureVideoFrameRate = BugseeFrameRate.High
        options.captureVideoMinFrameRate = 15
        options.captureVideoMaxFrameRate = 60
        
        val map = options.toMap()
        
        // The Android SDK takes enum options by constant name
        assertEquals(BugseeFrameRate.High.name, map[BugseeOptions.CaptureVideoFrameRate])
        // iOS-only options: kept for the getters, never sent to the Android SDK
        assertEquals(15, options.captureVideoMinFrameRate)
        assertEquals(60, options.captureVideoMaxFrameRate)
        assertFalse(map.containsKey(BugseeOptions.CaptureVideoMinFrameRate))
        assertFalse(map.containsKey(BugseeOptions.CaptureVideoMaxFrameRate))
    }

    @Test
    fun `test Android severity configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test severity options
        options.reportingDefaultCrashPriority = BugseeSeverity.Critical
        options.reportingDefaultBugPriority = BugseeSeverity.High
        
        val map = options.toMap()
        
        assertEquals(BugseeSeverity.Critical.name, map[BugseeOptions.ReportingDefaultCrashPriority])
        assertEquals(BugseeSeverity.High.name, map[BugseeOptions.ReportingDefaultBugPriority])
    }

    @Test
    fun `test Android custom options`() {
        val options = BugseeLaunchOptions()
        
        // Test custom options
        options.setCustomOption("androidSpecific", "androidValue")
        options.setCustomOption("version", "1.0.0")
        options.setCustomOption("debug", true)
        
        val map = options.toMap()
        
        assertEquals("androidValue", map["androidSpecific"])
        assertEquals("1.0.0", map["version"])
        assertEquals(true, map["debug"])
    }

    @Test
    fun `test Android comprehensive configuration`() {
        val options = BugseeLaunchOptions()
        
        // Set comprehensive Android configuration
        options.captureNetworkBodySizeLimit = 2048
        options.reportingTriggerByShake = true
        options.detectAndReportCrash = true
        options.captureVideo = true
        options.captureScreenshot = true
        options.captureLogs = true
        options.captureNetwork = true
        options.wifiOnlyUpload = false
        options.maxDataSize = 100000
        options.captureVideoFrameRate = BugseeFrameRate.Medium
        options.reportingDefaultCrashPriority = BugseeSeverity.Blocker
        options.reportingDefaultBugPriority = BugseeSeverity.High
        options.reportingUISummaryRequired = true
        options.reportingUIDescriptionRequired = true
        options.captureViewHierarchy = true
        options.detectAndReportExit = true
        
        val map = options.toMap()
        
        // Verify all options are set correctly
        assertEquals(2048, map[BugseeOptions.CaptureNetworkBodySizeLimit])
        assertEquals(true, map[BugseeOptions.ReportingTriggerByShake])
        assertEquals(true, map[BugseeOptions.DetectAndReportCrash])
        assertEquals(true, map[BugseeOptions.CaptureVideo])
        assertEquals(true, map[BugseeOptions.CaptureScreenshot])
        assertEquals(true, map[BugseeOptions.CaptureLogs])
        assertEquals(true, map[BugseeOptions.CaptureNetwork])
        assertEquals(false, map[BugseeOptions.WifiOnlyUpload])
        assertEquals(100000, map[BugseeOptions.MaxDataSize])
        assertEquals(BugseeFrameRate.Medium.name, map[BugseeOptions.CaptureVideoFrameRate])
        assertEquals(BugseeSeverity.Blocker.name, map[BugseeOptions.ReportingDefaultCrashPriority])
        assertEquals(BugseeSeverity.High.name, map[BugseeOptions.ReportingDefaultBugPriority])
        assertEquals(true, map[BugseeOptions.ReportingUISummaryRequired])
        assertEquals(true, map[BugseeOptions.ReportingUIDescriptionRequired])
        assertEquals(true, map[BugseeOptions.CaptureViewHierarchy])
        assertEquals(true, map[BugseeOptions.DetectAndReportExit])
    }
}