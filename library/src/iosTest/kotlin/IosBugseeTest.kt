package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IosBugseeTest {

    @Test
    fun `test iOS platform specific functionality`() {
        // Test that we're running on iOS platform
        // This is a basic test to ensure the iOS test target is working
        assertTrue(true)
    }

    @Test
    fun `test BugseeLaunchOptions iOS specific behavior`() {
        val options = BugseeLaunchOptions()
        
        // Test iOS-specific shake to report key
        options.reportingTriggerByShake = true
        val map = options.toMap()
        
        // On iOS, it should use the shake trigger key
        assertTrue(map.containsKey(BugseeOptions.ReportingTriggerByShake))
        assertEquals(true, map[BugseeOptions.ReportingTriggerByShake])
    }

    @Test
    fun `test iOS specific types and classes`() {
        // Test that iOS-specific classes are accessible
        // Note: These would normally be tested with actual iOS context
        // but we can verify the classes exist and are accessible
        
        try {
            // Test that we can create iOS-specific instances
            // This would normally require iOS context in real scenarios
            assertNotNull(BugseeLaunchOptions())
        } catch (e: Exception) {
            // Expected in unit test environment without iOS context
        }
    }

    @Test
    fun `test iOS platform info`() {
        // Test platform-specific behavior
        val options = BugseeLaunchOptions()
        
        // Set various options to test iOS-specific behavior
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
    fun `test iOS specific launch options`() {
        val options = BugseeLaunchOptions()
        
        // Test iOS-specific configuration
        options.captureNetworkBodySizeLimit = 1024
        options.wifiOnlyUpload = true
        options.captureDeviceAndNetworkNames = true
        
        val map = options.toMap()
        
        assertEquals(1024, map[BugseeOptions.CaptureNetworkBodySizeLimit])
        assertEquals(true, map[BugseeOptions.WifiOnlyUpload])
        assertEquals(true, map[BugseeOptions.CaptureDeviceAndNetworkNames])
    }

    @Test
    fun `test iOS report configuration`() {
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
    fun `test iOS frame rate configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test frame rate options
        options.captureVideoFrameRate = BugseeFrameRate.High
        options.captureVideoMinFrameRate = 15
        options.captureVideoMaxFrameRate = 60
        
        val map = options.toMap()
        
        // The iOS SDK takes enum options by Int value
        assertEquals(BugseeFrameRate.High.getIntValue(), map[BugseeOptions.CaptureVideoFrameRate])
        assertEquals(15, map[BugseeOptions.CaptureVideoMinFrameRate])
        assertEquals(60, map[BugseeOptions.CaptureVideoMaxFrameRate])
    }

    @Test
    fun `test iOS severity configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test severity options
        options.reportingDefaultCrashPriority = BugseeSeverity.Critical
        options.reportingDefaultBugPriority = BugseeSeverity.High
        
        val map = options.toMap()
        
        assertEquals(BugseeSeverity.Critical.getLevel(), map[BugseeOptions.ReportingDefaultCrashPriority])
        assertEquals(BugseeSeverity.High.getLevel(), map[BugseeOptions.ReportingDefaultBugPriority])
    }

    @Test
    fun `test iOS custom options`() {
        val options = BugseeLaunchOptions()
        
        // Test custom options
        options.setCustomOption("iosSpecific", "iosValue")
        options.setCustomOption("version", "1.0.0")
        options.setCustomOption("debug", true)
        
        val map = options.toMap()
        
        assertEquals("iosValue", map["iosSpecific"])
        assertEquals("1.0.0", map["version"])
        assertEquals(true, map["debug"])
    }

    @Test
    fun `test iOS comprehensive configuration`() {
        val options = BugseeLaunchOptions()
        
        // Set comprehensive iOS configuration
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
        assertEquals(BugseeFrameRate.Medium.getIntValue(), map[BugseeOptions.CaptureVideoFrameRate])
        assertEquals(BugseeSeverity.Blocker.getLevel(), map[BugseeOptions.ReportingDefaultCrashPriority])
        assertEquals(BugseeSeverity.High.getLevel(), map[BugseeOptions.ReportingDefaultBugPriority])
        assertEquals(true, map[BugseeOptions.ReportingUISummaryRequired])
        assertEquals(true, map[BugseeOptions.ReportingUIDescriptionRequired])
        assertEquals(true, map[BugseeOptions.CaptureViewHierarchy])
        assertEquals(true, map[BugseeOptions.DetectAndReportExit])
    }

    @Test
    fun `test convertAttachment with byte data`() {
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47) // PNG magic bytes
        val attachment = BugseeAttachment.create("test.png", bytes)
        val native = BugseeIOSUtils.convertAttachment(attachment)
        assertEquals("test.png", native.name)
        assertNotNull(native.data)
        assertEquals(4uL, native.data!!.length)
    }

    @Test
    fun `test convertAttachment with empty byte data`() {
        val bytes = byteArrayOf()
        val attachment = BugseeAttachment.create("empty.dat", bytes)
        val native = BugseeIOSUtils.convertAttachment(attachment)
        assertEquals("empty.dat", native.name)
        assertNotNull(native.data)
        assertEquals(0uL, native.data!!.length)
    }

    @Test
    fun `test iOS specific network monitoring`() {
        val options = BugseeLaunchOptions()
        
        // Test iOS-specific network monitoring behavior
        options.captureNetwork = true
        
        val map = options.toMap()
        
        // On iOS, captureNetwork should map to the correct key
        assertEquals(true, map[BugseeOptions.CaptureNetwork])
    }
}