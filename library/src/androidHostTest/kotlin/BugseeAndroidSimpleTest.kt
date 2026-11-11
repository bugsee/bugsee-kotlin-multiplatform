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
        options.reportingTriggerByShake = true
        assertTrue(options.reportingTriggerByShake, "reportingTriggerByShake should be settable")
        
        // Test that Android-specific keys are properly handled
        val map = options.toMap()
        assertNotNull(map, "toMap() should return a valid map")
        
        // Verify Android-specific keys are present
        assertTrue(map.containsKey(BugseeOptions.ReportingTriggerByShake), "Should contain the shake trigger key")
    }

    @Test
    fun `test BugseeLaunchOptions Android platform detection`() {
        val options = BugseeLaunchOptions()
        
        // Test platform-specific behavior
        options.captureNetwork = true
        
        val map = options.toMap()
        
        // On Android, captureNetwork should map to the correct key
        assertTrue(map.containsKey(BugseeOptions.CaptureNetwork), "Should contain CaptureNetwork key")
    }

    @Test
    fun `test BugseeLaunchOptions Android configuration validation`() {
        val options = BugseeLaunchOptions()
        
        // Test Android-specific configuration
        options.captureNetworkBodySizeLimit = 1024 * 1024 // 1MB
        options.captureVideoFrameRate = BugseeFrameRate.High
        options.captureVideo = true
        options.captureScreenshot = true
        
        val map = options.toMap()
        
        // Verify Android-specific values
        assertTrue(map.containsKey(BugseeOptions.CaptureNetworkBodySizeLimit), "Should contain CaptureNetworkBodySizeLimit")
        assertTrue(map.containsKey(BugseeOptions.CaptureVideoFrameRate), "Should contain CaptureVideoFrameRate")
        assertTrue(map.containsKey(BugseeOptions.CaptureVideo), "Should contain CaptureVideo")
        assertTrue(map.containsKey(BugseeOptions.CaptureScreenshot), "Should contain CaptureScreenshot")
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
        options.captureNetwork = true
        options.captureNetworkBodySizeLimit = 1024 * 1024 // 1MB
        options.wifiOnlyUpload = true
        
        val map = options.toMap()
        
        // Verify network monitoring options
        assertTrue(map.containsKey(BugseeOptions.CaptureNetwork), "Should contain CaptureNetwork")
        assertTrue(map.containsKey(BugseeOptions.CaptureNetworkBodySizeLimit), "Should contain CaptureNetworkBodySizeLimit")
        assertTrue(map.containsKey(BugseeOptions.WifiOnlyUpload), "Should contain WifiOnlyUpload")
    }

    @Test
    fun `test Android video and screenshot options`() {
        // Test Android video and screenshot functionality
        
        val options = BugseeLaunchOptions()
        options.captureVideo = true
        options.captureScreenshot = true
        options.captureVideoFrameRate = BugseeFrameRate.High
        options.duration = 30 // 30 seconds
        
        val map = options.toMap()
        
        // Verify video and screenshot options
        assertTrue(map.containsKey(BugseeOptions.CaptureVideo), "Should contain CaptureVideo")
        assertTrue(map.containsKey(BugseeOptions.CaptureScreenshot), "Should contain CaptureScreenshot")
        assertTrue(map.containsKey(BugseeOptions.CaptureVideoFrameRate), "Should contain CaptureVideoFrameRate")
        assertTrue(map.containsKey(BugseeOptions.Duration), "Should contain Duration")
    }
}
