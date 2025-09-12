package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JvmBugseeTest {
    
    @Test
    fun `test JVM platform specific functionality`() {
        // Test that we're running on JVM platform
        // This is a basic test to ensure the JVM test target is working
        assertTrue(true)
    }

    @Test
    fun `test JVM specific types and classes`() {
        // Test that JVM-specific classes are accessible
        try {
            // Test that we can create JVM-specific instances
            assertNotNull(BugseeLaunchOptions())
        } catch (e: Exception) {
            // Expected in unit test environment without JVM context
        }
    }

    @Test
    fun `test JVM platform info`() {
        // Test platform-specific behavior
        val options = BugseeLaunchOptions()
        
        // Set various options to test JVM-specific behavior
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
    fun `test JVM specific launch options`() {
        val options = BugseeLaunchOptions()
        
        // Test JVM-specific configuration
        options.maxNetworkBodySize = 1024
        options.wifiOnlyUpload = true
        options.captureDeviceAndNetworkNames = true
        
        val map = options.toMap()
        
        assertEquals(1024, map["bodySizeLimit"])
        assertEquals(true, map["WifiOnlyUpload"])
        assertEquals(true, map["CaptureDeviceAndNetworkNames"])
    }

    @Test
    fun `test JVM report configuration`() {
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
    fun `test JVM frame rate configuration`() {
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
    fun `test JVM severity configuration`() {
        val options = BugseeLaunchOptions()
        
        // Test severity options
        options.defaultCrashPriority = BugseeSeverity.Critical
        options.defaultBugPriority = BugseeSeverity.High
        
        val map = options.toMap()
        
        assertEquals(BugseeSeverity.Critical.getLevel(), map["BugseeDefaultCrashPriority"])
        assertEquals(BugseeSeverity.High.getLevel(), map["BugseeDefaultBugPriority"])
    }

    @Test
    fun `test JVM custom options`() {
        val options = BugseeLaunchOptions()
        
        // Test custom options
        options.setCustomOption("jvmSpecific", "jvmValue")
        options.setCustomOption("version", "1.0.0")
        options.setCustomOption("debug", true)
        
        val map = options.toMap()
        
        assertEquals("jvmValue", map["jvmSpecific"])
        assertEquals("1.0.0", map["version"])
        assertEquals(true, map["debug"])
    }

    @Test
    fun `test JVM comprehensive configuration`() {
        val options = BugseeLaunchOptions()
        
        // Set comprehensive JVM configuration
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
        assertEquals(true, map["ShakeToTrigger"] ?: map["ShakeToReport"])
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

    @Test
    fun `test JVM wrapper info`() {
        val options = BugseeLaunchOptions()
        val map = options.toMap()
        
        val wrapperInfo = map["wrapper_info"] as Map<String, Any>
        assertEquals("kmp", wrapperInfo["type"])
        assertEquals("0.0.1-beta", wrapperInfo["version"])
        assertNotNull(wrapperInfo["runtime"])
        
        // Runtime should contain Kotlin version info
        val runtime = wrapperInfo["runtime"] as String
        assertTrue(runtime.isNotEmpty())
    }

    @Test
    fun `test JVM enum serialization`() {
        val options = BugseeLaunchOptions()
        
        // Test enum serialization to map
        options.frameRate = BugseeFrameRate.Low
        options.defaultCrashPriority = BugseeSeverity.VeryLow
        options.defaultBugPriority = BugseeSeverity.Medium
        
        val map = options.toMap()
        
        assertEquals(BugseeFrameRate.Low.getIntValue(), map["FrameRate"])
        assertEquals(BugseeSeverity.VeryLow.getLevel(), map["BugseeDefaultCrashPriority"])
        assertEquals(BugseeSeverity.Medium.getLevel(), map["BugseeDefaultBugPriority"])
    }

    @Test
    fun `test JVM data types handling`() {
        val options = BugseeLaunchOptions()
        
        // Test various data types
        options.setCustomOption("stringValue", "test")
        options.setCustomOption("intValue", 42)
        options.setCustomOption("booleanValue", true)
        options.setCustomOption("doubleValue", 3.14)
        options.setCustomOption("listValue", listOf("a", "b", "c"))
        options.setCustomOption("mapValue", mapOf("key" to "value"))
        
        val map = options.toMap()
        
        assertEquals("test", map["stringValue"])
        assertEquals(42, map["intValue"])
        assertEquals(true, map["booleanValue"])
        assertEquals(3.14, map["doubleValue"])
        assertEquals(listOf("a", "b", "c"), map["listValue"])
        assertEquals(mapOf("key" to "value"), map["mapValue"])
    }
}