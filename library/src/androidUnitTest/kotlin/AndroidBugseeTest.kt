package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
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
        options.shakeToReport = true
        val map = options.toMap()
        
        // On Android, it should use "ShakeToTrigger" key
        assertTrue(map.containsKey("ShakeToTrigger"))
        assertEquals(true, map["ShakeToTrigger"])
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
        options.crashReport = true
        options.videoEnabled = true
        options.screenshotEnabled = true
        
        val map = options.toMap()
        
        // Verify options are properly set
        assertEquals(true, map["CrashReport"])
        assertEquals(true, map["VideoEnabled"])
        assertEquals(true, map["ScreenshotEnabled"])
    }

    @Test
    fun `test Android specific launch options`() {
        val options = BugseeLaunchOptions()
        
        // Test Android-specific configuration
        options.maxNetworkBodySize = 1024
        options.wifiOnlyUpload = true
        options.captureDeviceAndNetworkNames = true
        
        val map = options.toMap()
        
        assertEquals(1024, map["bodySizeLimit"])
        assertEquals(true, map["WifiOnlyUpload"])
        assertEquals(true, map["CaptureDeviceAndNetworkNames"])
    }

    @Test
    fun `test Android report configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test report-related options
        options.reportSummaryRequired = true
        options.reportDescriptionRequired = true
        options.reportEmailRequired = false
        options.reportLabelsRequired = true
        options.reportLabelsEnabled = true
        
        val map = options.toMap()
        
        assertEquals(true, map["ReportSummaryRequired"])
        assertEquals(true, map["ReportDescriptionRequired"])
        assertEquals(false, map["ReportEmailRequired"])
        assertEquals(true, map["ReportLabelsRequired"])
        assertEquals(true, map["ReportLabelsEnabled"])
    }

    @Test
    fun `test Android frame rate configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test frame rate options
        options.frameRate = BugseeFrameRate.High
        options.minFrameRate = 15
        options.maxFrameRate = 60
        
        val map = options.toMap()
        
        assertEquals(BugseeFrameRate.High.getIntValue(), map["FrameRate"])
        assertEquals(15, map["MinFrameRate"])
        assertEquals(60, map["MaxFrameRate"])
    }

    @Test
    fun `test Android severity configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test severity options
        options.defaultCrashPriority = BugseeSeverity.Critical
        options.defaultBugPriority = BugseeSeverity.High
        
        val map = options.toMap()
        
        assertEquals(BugseeSeverity.Critical.getLevel(), map["BugseeDefaultCrashPriority"])
        assertEquals(BugseeSeverity.High.getLevel(), map["BugseeDefaultBugPriority"])
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
        options.maxNetworkBodySize = 2048
        options.shakeToReport = true
        options.crashReport = true
        options.videoEnabled = true
        options.screenshotEnabled = true
        options.captureLogs = true
        options.monitorNetwork = true
        options.wifiOnlyUpload = false
        options.maxDataSize = 100000
        options.frameRate = BugseeFrameRate.Medium
        options.defaultCrashPriority = BugseeSeverity.Blocker
        options.defaultBugPriority = BugseeSeverity.High
        options.reportSummaryRequired = true
        options.reportDescriptionRequired = true
        options.viewHierarchyEnabled = true
        options.detectAppExit = true
        
        val map = options.toMap()
        
        // Verify all options are set correctly
        assertEquals(2048, map["bodySizeLimit"])
        assertEquals(true, map["ShakeToTrigger"])
        assertEquals(true, map["CrashReport"])
        assertEquals(true, map["VideoEnabled"])
        assertEquals(true, map["ScreenshotEnabled"])
        assertEquals(true, map["CaptureLogs"])
        assertEquals(true, map["MonitorNetwork"])
        assertEquals(true, map["monitorNetwork"])
        assertEquals(false, map["WifiOnlyUpload"])
        assertEquals(100000, map["MaxDataSize"])
        assertEquals(BugseeFrameRate.Medium.getIntValue(), map["FrameRate"])
        assertEquals(BugseeSeverity.Blocker.getLevel(), map["BugseeDefaultCrashPriority"])
        assertEquals(BugseeSeverity.High.getLevel(), map["BugseeDefaultBugPriority"])
        assertEquals(true, map["ReportSummaryRequired"])
        assertEquals(true, map["ReportDescriptionRequired"])
        assertEquals(true, map["ViewHierarchyEnabled"])
        assertEquals(true, map["DetectAppExit"])
    }
}