package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BugseeLaunchOptionsTest {

    @Test
    fun `test BugseeLaunchOptions default initialization`() {
        val options = BugseeLaunchOptions()
        
        // Test that options map is initialized
        assertNotNull(options.toMap())
        
        // Test wrapper info is included
        val map = options.toMap()
        assertTrue(map.containsKey("wrapper_info"))
        
        val wrapperInfo = map["wrapper_info"] as Map<String, Any>
        assertEquals("kmp", wrapperInfo["type"])
        assertEquals(BuildKonfig.libraryVersion, wrapperInfo["version"])
        assertNotNull(wrapperInfo["runtime"])
    }

    @Test
    fun `test BugseeLaunchOptions custom options`() {
        val options = BugseeLaunchOptions()
        
        options.setCustomOption("customKey", "customValue")
        options.setCustomOption("customNumber", 42)
        options.setCustomOption("customBoolean", true)
        
        val map = options.toMap()
        assertEquals("customValue", map["customKey"])
        assertEquals(42, map["customNumber"])
        assertEquals(true, map["customBoolean"])
    }

    @Test
    fun `test BugseeLaunchOptions maxNetworkBodySize`() {
        val options = BugseeLaunchOptions()
        
        options.maxNetworkBodySize = 1024
        assertEquals(1024, options.maxNetworkBodySize)
        
        val map = options.toMap()
        assertEquals(1024, map["bodySizeLimit"])
    }

    @Test
    fun `test BugseeLaunchOptions shakeToReport`() {
        val options = BugseeLaunchOptions()
        
        options.shakeToReport = true
        assertTrue(options.shakeToReport)
        
        options.shakeToReport = false
        assertFalse(options.shakeToReport)
    }

    @Test
    fun `test BugseeLaunchOptions crashReport`() {
        val options = BugseeLaunchOptions()
        
        options.crashReport = true
        assertTrue(options.crashReport)
        
        options.crashReport = false
        assertFalse(options.crashReport)
    }

    @Test
    fun `test BugseeLaunchOptions maxRecordingTime`() {
        val options = BugseeLaunchOptions()
        
        options.maxRecordingTime = 300
        assertEquals(300, options.maxRecordingTime)
        
        val map = options.toMap()
        assertEquals(300, map["MaxRecordingTime"])
    }

    @Test
    fun `test BugseeLaunchOptions videoEnabled`() {
        val options = BugseeLaunchOptions()
        
        options.videoEnabled = true
        assertTrue(options.videoEnabled)
        
        options.videoEnabled = false
        assertFalse(options.videoEnabled)
    }

    @Test
    fun `test BugseeLaunchOptions screenshotEnabled`() {
        val options = BugseeLaunchOptions()
        
        options.screenshotEnabled = true
        assertTrue(options.screenshotEnabled)
        
        options.screenshotEnabled = false
        assertFalse(options.screenshotEnabled)
    }

    @Test
    fun `test BugseeLaunchOptions captureLogs`() {
        val options = BugseeLaunchOptions()
        
        options.captureLogs = true
        assertTrue(options.captureLogs)
        
        options.captureLogs = false
        assertFalse(options.captureLogs)
    }

    @Test
    fun `test BugseeLaunchOptions monitorNetwork`() {
        val options = BugseeLaunchOptions()
        
        options.monitorNetwork = true
        assertTrue(options.monitorNetwork)
        
        val map = options.toMap()
        assertEquals(true, map["MonitorNetwork"])
        assertEquals(true, map["monitorNetwork"])
        
        options.monitorNetwork = false
        assertFalse(options.monitorNetwork)
    }

    @Test
    fun `test BugseeLaunchOptions wifiOnlyUpload`() {
        val options = BugseeLaunchOptions()
        
        options.wifiOnlyUpload = true
        assertTrue(options.wifiOnlyUpload)
        
        options.wifiOnlyUpload = false
        assertFalse(options.wifiOnlyUpload)
    }

    @Test
    fun `test BugseeLaunchOptions maxDataSize`() {
        val options = BugseeLaunchOptions()
        
        options.maxDataSize = 50000
        assertEquals(50000, options.maxDataSize)
        
        val map = options.toMap()
        assertEquals(50000, map["MaxDataSize"])
    }

    @Test
    fun `test BugseeLaunchOptions reportPrioritySelector`() {
        val options = BugseeLaunchOptions()
        
        options.reportPrioritySelector = true
        assertTrue(options.reportPrioritySelector)
        
        options.reportPrioritySelector = false
        assertFalse(options.reportPrioritySelector)
    }

    @Test
    fun `test BugseeLaunchOptions defaultCrashPriority`() {
        val options = BugseeLaunchOptions()
        
        options.defaultCrashPriority = BugseeSeverity.Critical
        assertEquals(BugseeSeverity.Critical, options.defaultCrashPriority)
        
        val map = options.toMap()
        assertEquals(BugseeSeverity.Critical.getLevel(), map["BugseeDefaultCrashPriority"])
    }

    @Test
    fun `test BugseeLaunchOptions defaultBugPriority`() {
        val options = BugseeLaunchOptions()
        
        options.defaultBugPriority = BugseeSeverity.High
        assertEquals(BugseeSeverity.High, options.defaultBugPriority)
        
        val map = options.toMap()
        assertEquals(BugseeSeverity.High.getLevel(), map["BugseeDefaultBugPriority"])
    }

    @Test
    fun `test BugseeLaunchOptions frameRate`() {
        val options = BugseeLaunchOptions()
        
        options.frameRate = BugseeFrameRate.Low
        assertEquals(BugseeFrameRate.Low, options.frameRate)
        
        val map = options.toMap()
        assertEquals(BugseeFrameRate.Low.getIntValue(), map["FrameRate"])
    }

    @Test
    fun `test BugseeLaunchOptions minFrameRate`() {
        val options = BugseeLaunchOptions()
        
        options.minFrameRate = 15
        assertEquals(15, options.minFrameRate)
        
        val map = options.toMap()
        assertEquals(15, map["MinFrameRate"])
    }

    @Test
    fun `test BugseeLaunchOptions maxFrameRate`() {
        val options = BugseeLaunchOptions()
        
        options.maxFrameRate = 60
        assertEquals(60, options.maxFrameRate)
        
        val map = options.toMap()
        assertEquals(60, map["MaxFrameRate"])
    }

    @Test
    fun `test BugseeLaunchOptions captureDeviceAndNetworkNames`() {
        val options = BugseeLaunchOptions()
        
        options.captureDeviceAndNetworkNames = true
        assertTrue(options.captureDeviceAndNetworkNames)
        
        options.captureDeviceAndNetworkNames = false
        assertFalse(options.captureDeviceAndNetworkNames)
    }

    @Test
    fun `test BugseeLaunchOptions reportSummaryRequired`() {
        val options = BugseeLaunchOptions()
        
        options.reportSummaryRequired = true
        assertTrue(options.reportSummaryRequired)
        
        options.reportSummaryRequired = false
        assertFalse(options.reportSummaryRequired)
    }

    @Test
    fun `test BugseeLaunchOptions reportDescriptionRequired`() {
        val options = BugseeLaunchOptions()
        
        options.reportDescriptionRequired = true
        assertTrue(options.reportDescriptionRequired)
        
        options.reportDescriptionRequired = false
        assertFalse(options.reportDescriptionRequired)
    }

    @Test
    fun `test BugseeLaunchOptions reportEmailRequired`() {
        val options = BugseeLaunchOptions()
        
        options.reportEmailRequired = true
        assertTrue(options.reportEmailRequired)
        
        options.reportEmailRequired = false
        assertFalse(options.reportEmailRequired)
    }

    @Test
    fun `test BugseeLaunchOptions reportLabelsRequired`() {
        val options = BugseeLaunchOptions()
        
        options.reportLabelsRequired = true
        assertTrue(options.reportLabelsRequired)
        
        options.reportLabelsRequired = false
        assertFalse(options.reportLabelsRequired)
    }

    @Test
    fun `test BugseeLaunchOptions reportLabelsEnabled`() {
        val options = BugseeLaunchOptions()
        
        options.reportLabelsEnabled = true
        assertTrue(options.reportLabelsEnabled)
        
        options.reportLabelsEnabled = false
        assertFalse(options.reportLabelsEnabled)
    }

    @Test
    fun `test BugseeLaunchOptions viewHierarchyEnabled`() {
        val options = BugseeLaunchOptions()
        
        options.viewHierarchyEnabled = true
        assertTrue(options.viewHierarchyEnabled)
        
        options.viewHierarchyEnabled = false
        assertFalse(options.viewHierarchyEnabled)
    }

    @Test
    fun `test BugseeLaunchOptions detectAppExit`() {
        val options = BugseeLaunchOptions()
        
        options.detectAppExit = true
        assertTrue(options.detectAppExit)
        
        options.detectAppExit = false
        assertFalse(options.detectAppExit)
    }

    @Test
    fun `test BugseeLaunchOptions multiple properties`() {
        val options = BugseeLaunchOptions()
        
        // Set multiple properties
        options.maxNetworkBodySize = 2048
        options.shakeToReport = true
        options.crashReport = false
        options.videoEnabled = true
        options.frameRate = BugseeFrameRate.Medium
        options.defaultCrashPriority = BugseeSeverity.Blocker
        options.setCustomOption("testKey", "testValue")
        
        // Verify all properties
        assertEquals(2048, options.maxNetworkBodySize)
        assertTrue(options.shakeToReport)
        assertFalse(options.crashReport)
        assertTrue(options.videoEnabled)
        assertEquals(BugseeFrameRate.Medium, options.frameRate)
        assertEquals(BugseeSeverity.Blocker, options.defaultCrashPriority)
        
        val map = options.toMap()
        assertEquals(2048, map["bodySizeLimit"])
        assertEquals(true, map["ShakeToTrigger"] ?: map["ShakeToReport"])
        assertEquals(false, map["CrashReport"])
        assertEquals(true, map["VideoEnabled"])
        assertEquals(BugseeFrameRate.Medium.getIntValue(), map["FrameRate"])
        assertEquals(BugseeSeverity.Blocker.getLevel(), map["BugseeDefaultCrashPriority"])
        assertEquals("testValue", map["testKey"])
    }

    @Test
    fun `test BugseeLaunchOptions custom options override built-in`() {
        val options = BugseeLaunchOptions()
        
        // Set built-in option
        options.maxNetworkBodySize = 1024
        
        // Set custom option with same key
        options.setCustomOption("bodySizeLimit", 2048)
        
        val map = options.toMap()
        // Custom option should NOT override built-in
        assertEquals(1024, map["bodySizeLimit"])
    }
}
