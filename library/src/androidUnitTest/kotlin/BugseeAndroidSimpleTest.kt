package com.bugsee.kmp

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Use API 28 for Robolectric
class BugseeAndroidSimpleTest {

    @Test
    fun `test Bugsee object exists in Android environment`() {
        // Test that the Bugsee object is accessible in Android environment
        assertNotNull(Bugsee, "Bugsee object should exist")
    }

    @Test
    fun `test BugseeLaunchOptions with Android-specific properties`() {
        // Test Android-specific launch options
        val options = BugseeLaunchOptions()
        
        // Test Android-specific properties
        options.shakeToReport = true
        assertTrue(options.shakeToReport, "shakeToReport should be settable")
        
        // Test that Android-specific keys are properly handled
        val map = options.toMap()
        assertNotNull(map, "toMap() should return a valid map")
        
        // Verify Android-specific keys are present
        assertTrue(map.containsKey("ShakeToTrigger"), "Should contain Android-specific ShakeToTrigger key")
    }

    @Test
    fun `test BugseeLaunchOptions Android platform detection`() {
        val options = BugseeLaunchOptions()
        
        // Test platform-specific behavior
        options.monitorNetwork = true
        
        val map = options.toMap()
        
        // On Android, monitorNetwork should map to the correct keys
        assertTrue(map.containsKey("MonitorNetwork"), "Should contain MonitorNetwork key")
        assertTrue(map.containsKey("monitorNetwork"), "Should contain monitorNetwork key")
    }

    @Test
    fun `test BugseeLaunchOptions Android configuration validation`() {
        val options = BugseeLaunchOptions()
        
        // Test Android-specific configuration
        options.maxNetworkBodySize = 1024 * 1024 // 1MB
        options.frameRate = BugseeFrameRate.High
        options.videoEnabled = true
        options.screenshotEnabled = true
        
        val map = options.toMap()
        
        // Verify Android-specific values
        assertTrue(map.containsKey("bodySizeLimit"), "Should contain bodySizeLimit")
        assertTrue(map.containsKey("FrameRate"), "Should contain FrameRate")
        assertTrue(map.containsKey("VideoEnabled"), "Should contain VideoEnabled")
        assertTrue(map.containsKey("ScreenshotEnabled"), "Should contain ScreenshotEnabled")
    }

    @Test
    fun `test BugseeLaunchOptions Android custom options`() {
        val options = BugseeLaunchOptions()
        
        // Test Android-specific custom options
        options.setCustomOption("AndroidSpecificKey", "AndroidValue")
        options.setCustomOption("AndroidDebugMode", true)
        
        val map = options.toMap()
        
        // Verify custom options are preserved
        assertTrue(map.containsKey("AndroidSpecificKey"), "Should contain custom Android key")
        assertTrue(map.containsKey("AndroidDebugMode"), "Should contain custom Android debug mode")
    }

    @Test
    fun `test Android-specific enum values`() {
        // Test that Android-specific enum values work correctly
        
        // Test BugseeFrameRate
        val frameRate = BugseeFrameRate.High
        assertNotNull(frameRate, "BugseeFrameRate should be accessible")
        
        // Test BugseeSeverity
        val severity = BugseeSeverity.High
        assertNotNull(severity, "BugseeSeverity should be accessible")
        
        // Test BugseeLogLevel
        val logLevel = BugseeLogLevel.Debug
        assertNotNull(logLevel, "BugseeLogLevel should be accessible")
    }

    @Test
    fun `test Android data classes`() {
        // Test Android-specific data class behavior
        
        // Test BugseeSecureRectangle
        val rectangle = BugseeSecureRectangle(10.0, 20.0, 100.0, 200.0)
        assertNotNull(rectangle, "BugseeSecureRectangle should be creatable")
        assertTrue(rectangle.x == 10.0, "X coordinate should be correct")
        assertTrue(rectangle.y == 20.0, "Y coordinate should be correct")
        assertTrue(rectangle.width == 100.0, "Width should be correct")
        assertTrue(rectangle.height == 200.0, "Height should be correct")
        
        // Test BugseeAttachment
        val attachment = BugseeAttachment.create("test.txt", "test content".toByteArray())
        assertNotNull(attachment, "BugseeAttachment should be creatable")
        assertTrue(attachment.name == "test.txt", "Attachment name should be correct")
        assertNotNull(attachment.data, "Attachment data should not be null")
    }

    @Test
    fun `test Android lifecycle events`() {
        // Test Android lifecycle event handling
        
        val lifecycleEvent = BugseeLifecycleEvent.Started
        assertNotNull(lifecycleEvent, "BugseeLifecycleEvent should be accessible")
        
        // Test all lifecycle events
        val events = listOf(
            BugseeLifecycleEvent.Started,
            BugseeLifecycleEvent.Stopped,
            BugseeLifecycleEvent.Paused,
            BugseeLifecycleEvent.Resumed
        )
        
        events.forEach { event ->
            assertNotNull(event, "Lifecycle event should not be null")
        }
    }

    @Test
    fun `test Android network monitoring`() {
        // Test Android network monitoring functionality
        
        val options = BugseeLaunchOptions()
        options.monitorNetwork = true
        options.maxNetworkBodySize = 1024 * 1024 // 1MB
        options.wifiOnlyUpload = true
        
        val map = options.toMap()
        
        // Verify network monitoring options
        assertTrue(map.containsKey("MonitorNetwork"), "Should contain MonitorNetwork")
        assertTrue(map.containsKey("monitorNetwork"), "Should contain monitorNetwork")
        assertTrue(map.containsKey("bodySizeLimit"), "Should contain bodySizeLimit")
        assertTrue(map.containsKey("WifiOnlyUpload"), "Should contain WifiOnlyUpload")
    }

    @Test
    fun `test Android video and screenshot options`() {
        // Test Android video and screenshot functionality
        
        val options = BugseeLaunchOptions()
        options.videoEnabled = true
        options.screenshotEnabled = true
        options.frameRate = BugseeFrameRate.High
        options.maxRecordingTime = 30 // 30 seconds
        
        val map = options.toMap()
        
        // Verify video and screenshot options
        assertTrue(map.containsKey("VideoEnabled"), "Should contain VideoEnabled")
        assertTrue(map.containsKey("ScreenshotEnabled"), "Should contain ScreenshotEnabled")
        assertTrue(map.containsKey("FrameRate"), "Should contain FrameRate")
        assertTrue(map.containsKey("MaxRecordingTime"), "Should contain MaxRecordingTime")
    }
}
