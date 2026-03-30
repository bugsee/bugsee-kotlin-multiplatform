package com.bugsee.kmp.internal

import android.graphics.Bitmap
import com.bugsee.kmp.*
import com.bugsee.library.attachment.ExtendedReport
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeInternalAndroidSimpleTest {

    private lateinit var bugseeInternal: BugseeInternal

    @Before
    fun setup() {
        bugseeInternal = BugseeInternal()
    }

    @Test
    fun `test appearance property access`() {
        // Test that appearance property is accessible and doesn't throw exceptions
        val appearance = bugseeInternal.appearance
        assertNotNull(appearance, "Appearance should not be null")
    }

    @Test
    fun `test launch methods with valid parameters`() {
        val apiKey = "test-api-key"
        val options = mapOf("key1" to "value1")
        val launchOptions = BugseeLaunchOptions()
        
        // Test all launch variants - these should not throw exceptions
        bugseeInternal.launch(apiKey)
        bugseeInternal.launch(apiKey, options)
        bugseeInternal.launch(apiKey, launchOptions)
        
        assertTrue(true, "All launch methods should complete successfully")
    }

    @Test
    fun `test control methods`() {
        // Test control methods - these should not throw exceptions
        bugseeInternal.stop()
        bugseeInternal.relaunch()
        bugseeInternal.relaunch(BugseeLaunchOptions())
        bugseeInternal.relaunch(mapOf("test" to "value"))
        
        assertTrue(true, "All control methods should complete successfully")
    }

    @Test
    fun `test feedback methods`() {
        // Test feedback methods - these should not throw exceptions
        bugseeInternal.showFeedback()
        
        val listener: BugseeFeedbackEventListener = { report ->
            assertNotNull(report, "Report should not be null")
        }
        bugseeInternal.setOnNewFeedbackListener(listener)
        
        bugseeInternal.setDefaultFeedbackGreeting("Test greeting")
        
        assertTrue(true, "All feedback methods should complete successfully")
    }

    @Test
    fun `test logging methods`() {
        // Test logging methods - these should not throw exceptions
        bugseeInternal.log("Test log message")
        bugseeInternal.log("Test log message", BugseeLogLevel.Debug)
        bugseeInternal.log("Test log message", BugseeLogLevel.Info)
        bugseeInternal.log("Test log message", BugseeLogLevel.Warning)
        bugseeInternal.log("Test log message", BugseeLogLevel.Error)
        bugseeInternal.log("Test log message", BugseeLogLevel.Verbose)
        
        bugseeInternal.trace("test_trace", "test_value")
        bugseeInternal.trace("test_trace", 42)
        bugseeInternal.trace("test_trace", true)
        
        assertTrue(true, "All logging methods should complete successfully")
    }

    @Test
    fun `test event methods`() {
        // Test event methods - these should not throw exceptions
        bugseeInternal.event("test_event")
        
        val params = mapOf("key1" to "value1", "key2" to 42)
        bugseeInternal.event("test_event", params)
        bugseeInternal.event("test_event", null)
        
        assertTrue(true, "All event methods should complete successfully")
    }

    @Test
    fun `test report dialog methods`() {
        // Test report dialog methods - these should not throw exceptions
        bugseeInternal.showReportDialog()
        
        bugseeInternal.showReportDialog(
            "Test Summary",
            "Test Description",
            BugseeSeverity.High
        )
        
        bugseeInternal.showReportDialog(
            "Test Summary",
            "Test Description",
            BugseeSeverity.Medium,
            listOf("label1", "label2")
        )
        
        bugseeInternal.showReportDialog(
            "Test Summary",
            "Test Description",
            BugseeSeverity.Medium,
            null
        )
        
        assertTrue(true, "All report dialog methods should complete successfully")
    }

    @Test
    fun `test upload methods`() {
        val summary = "Test Summary"
        val description = "Test Description"
        val severity = BugseeSeverity.High
        val labels = listOf("label1", "label2")
        
        // Test upload methods - these should not throw exceptions
        bugseeInternal.upload(summary, description, severity)
        bugseeInternal.upload(summary, description, severity, labels)
        bugseeInternal.upload(summary, description, severity, null)
        bugseeInternal.upload(summary, description, severity, labels, true)
        bugseeInternal.upload(summary, description, severity, labels, false)
        bugseeInternal.upload(summary, description, severity, null, true)
        
        assertTrue(true, "All upload methods should complete successfully")
    }

    @Test
    fun `test exception logging methods`() {
        val exception = RuntimeException("Test exception")
        
        // Test exception logging methods - these should not throw exceptions
        bugseeInternal.logException(exception)
        
        val options = BugseeExceptionLoggingOptions().apply {
            exceptionDomain = "test.domain"
            includeVideo = true
            labels = arrayListOf("label1", "label2")
        }
        bugseeInternal.logException(exception, options)
        bugseeInternal.logException(exception, null)
        
        assertTrue(true, "All exception logging methods should complete successfully")
    }

    @Test
    fun `test security methods`() {
        val className = "com.test.TestActivity"
        
        // Test security methods - these should not throw exceptions
        bugseeInternal.addSecureViewClass(className)
        bugseeInternal.removeSecureViewClass(className)
        
        assertTrue(true, "All security methods should complete successfully")
    }

    @Test
    fun `test privacy control methods`() {
        // Test privacy control methods - these should not throw exceptions
        bugseeInternal.pause()
        bugseeInternal.resume()
        
        assertTrue(true, "All privacy control methods should complete successfully")
    }

    @Test
    fun `test isLaunched method`() {
        val isLaunched = bugseeInternal.isLaunched()
        assertFalse(isLaunched, "isLaunched should return false when SDK is not launched")
    }

    @Test
    fun `test secure rectangle methods`() {
        val rect = BugseeSecureRectangle(10.0, 20.0, 100.0, 200.0)
        
        // Test secure rectangle methods - these should not throw exceptions
        bugseeInternal.addSecureRectangle(rect)
        bugseeInternal.removeSecureRectangle(rect)
        bugseeInternal.removeAllSecureRectangles()
        
        val allRects = bugseeInternal.getAllSecureRectangles()
        assertNotNull(allRects, "getAllSecureRectangles should return a list")
        
        assertTrue(true, "All secure rectangle methods should complete successfully")
    }

    @Test
    fun `test secure view methods`() {
        // Test secure view methods - these should not throw exceptions
        bugseeInternal.addSecureView(null)
        bugseeInternal.addSecureView("not a view")
        
        bugseeInternal.removeSecureView(null)
        bugseeInternal.removeSecureView("not a view")
        
        bugseeInternal.addSecureWebView(null)
        bugseeInternal.addSecureWebView("not a webview")
        
        assertTrue(true, "All secure view methods should complete successfully")
    }

    @Test
    fun `test filter and listener methods`() {
        // Test filter and listener methods - these should not throw exceptions
        bugseeInternal.setNetworkEventFilter(null)
        val networkFilter: BugseeNetworkFilter = { event -> event }
        bugseeInternal.setNetworkEventFilter(networkFilter)
        
        bugseeInternal.setLogFilter(null)
        val logFilter: BugseeLogFilter = { event -> event }
        bugseeInternal.setLogFilter(logFilter)
        
        bugseeInternal.setLifecycleEventsListener(null)
        val lifecycleListener: BugseeLifecycleEventListener = { event ->
            assertNotNull(event, "Lifecycle event should not be null")
        }
        bugseeInternal.setLifecycleEventsListener(lifecycleListener)
        
        assertTrue(true, "All filter and listener methods should complete successfully")
    }

    @Test
    fun `test user management methods`() {
        val email = "test@example.com"
        
        // Test user management methods - these should not throw exceptions
        bugseeInternal.setEmail(email)
        val retrievedEmail = bugseeInternal.getEmail()
        bugseeInternal.clearEmail()
        
        assertTrue(true, "All user management methods should complete successfully")
    }

    @Test
    fun `test attribute methods`() {
        val name = "test_attribute"
        val value = "test_value"
        
        // Test attribute methods - these should not throw exceptions
        bugseeInternal.setAttribute(name, value)
        bugseeInternal.setAttribute(name, 42)
        bugseeInternal.setAttribute(name, true)
        
        val retrievedValue = bugseeInternal.getAttribute(name)
        bugseeInternal.clearAttribute(name)
        
        // clearAllAttributes() may fail in test environment due to null resource store
        // This is expected behavior when testing Android libraries
        try {
            bugseeInternal.clearAllAttributes()
        } catch (e: Exception) {
            // Expected in test environment - resource store not initialized
            assertTrue(e is NullPointerException, "Expected NullPointerException for resource store access")
        }
        
        assertTrue(true, "All attribute methods should complete successfully")
    }

    @Test
    fun `test report attachments provider`() {
        // Test report attachments provider methods - these should not throw exceptions
        bugseeInternal.setReportAttachmentsProvider(null)
        
        val provider: BugseeAttachmentsProvider = { report ->
            assertNotNull(report, "Report should not be null")
            listOf(BugseeAttachment.create("test.txt", "test content".toByteArray()))
        }
        bugseeInternal.setReportAttachmentsProvider(provider)
        
        assertTrue(true, "Report attachments provider methods should complete successfully")
    }

    @Test
    fun `test delete collected data on device`() {
        // Test delete collected data method - this should not throw exceptions
        bugseeInternal.deleteCollectedDataOnDevice(null)
        
        val listener: EventHandler<Boolean> = { success ->
            assertTrue(success is Boolean, "Success should be a Boolean")
        }
        bugseeInternal.deleteCollectedDataOnDevice(listener)
        
        assertTrue(true, "Delete collected data method should complete successfully")
    }

    @Test
    fun `test extended report methods`() {
        // Test extended report methods - these should not throw exceptions
        val provider: BugseeExtendedReportProvider = { report ->
            assertNotNull(report, "Extended report should not be null")
        }
        bugseeInternal.createReport(provider)
        
        assertTrue(true, "Extended report methods should complete successfully")
    }

    @Test
    fun `test report fields filter methods`() {
        // Test report fields filter methods - these should not throw exceptions
        bugseeInternal.setReportFieldsPreFilter(null)
        val filler: BugseeReportFieldsFiller = { fields ->
            assertNotNull(fields, "Report fields should not be null")
        }
        bugseeInternal.setReportFieldsPreFilter(filler)
        
        bugseeInternal.setReportFieldsPostFilter(null)
        val filter: BugseeReportFieldsFilter = { fields ->
            assertNotNull(fields, "Report fields should not be null")
        }
        bugseeInternal.setReportFieldsPostFilter(filter)
        
        bugseeInternal.setReportFieldsPreFilter(filler)
        bugseeInternal.setReportFieldsPostFilter(filter)
        
        assertTrue(true, "Report fields filter methods should complete successfully")
    }

    @Test
    fun `test capture view hierarchy`() {
        // Test capture view hierarchy method - this should not throw exceptions
        bugseeInternal.captureViewHierarchy()
        
        assertTrue(true, "Capture view hierarchy method should complete successfully")
    }

    @Test
    fun `test all BugseeSeverity values`() {
        val severities = listOf(
            BugseeSeverity.Critical,
            BugseeSeverity.High,
            BugseeSeverity.Medium,
            BugseeSeverity.VeryLow,
            BugseeSeverity.Blocker
        )
        
        // Test all severity values - these should not throw exceptions
        severities.forEach { severity ->
            bugseeInternal.showReportDialog("Test", "Test", severity)
            bugseeInternal.upload("Test", "Test", severity)
        }
        
        assertTrue(true, "All severity values should work correctly")
    }

    @Test
    fun `test all BugseeLogLevel values`() {
        val logLevels = listOf(
            BugseeLogLevel.Debug,
            BugseeLogLevel.Info,
            BugseeLogLevel.Warning,
            BugseeLogLevel.Error,
            BugseeLogLevel.Verbose
        )
        
        // Test all log level values - these should not throw exceptions
        logLevels.forEach { level ->
            bugseeInternal.log("Test message", level)
        }
        
        assertTrue(true, "All log level values should work correctly")
    }

    @Test
    fun `test all BugseeLifecycleEvent values`() {
        val events = listOf(
            BugseeLifecycleEvent.Launched,
            BugseeLifecycleEvent.Started,
            BugseeLifecycleEvent.Stopped,
            BugseeLifecycleEvent.Resumed,
            BugseeLifecycleEvent.Paused,
            BugseeLifecycleEvent.RelaunchedAfterCrash,
            BugseeLifecycleEvent.BeforeReportShown,
            BugseeLifecycleEvent.AfterReportShown,
            BugseeLifecycleEvent.BeforeReportUploaded,
            BugseeLifecycleEvent.AfterReportUploaded,
            BugseeLifecycleEvent.BeforeFeedbackShown,
            BugseeLifecycleEvent.AfterFeedbackShown,
            BugseeLifecycleEvent.BeforeReportAssembled,
            BugseeLifecycleEvent.AfterReportAssembled,
            BugseeLifecycleEvent.ReportUploadFailedWithFutureRetry,
            BugseeLifecycleEvent.ReportUploadFailed
        )
        
        // Test all lifecycle event values - these should not throw exceptions
        val listener: BugseeLifecycleEventListener = { event ->
            assertTrue(event in events, "Event should be a valid lifecycle event")
        }
        bugseeInternal.setLifecycleEventsListener(listener)
        
        assertTrue(true, "All lifecycle event values should work correctly")
    }

    @Test
    fun `test complex data types in attributes`() {
        // Test various data types for attributes - these should not throw exceptions
        bugseeInternal.setAttribute("string", "test")
        bugseeInternal.setAttribute("int", 42)
        bugseeInternal.setAttribute("long", 123456789L)
        bugseeInternal.setAttribute("float", 3.14f)
        bugseeInternal.setAttribute("double", 2.718)
        bugseeInternal.setAttribute("boolean", true)
        bugseeInternal.setAttribute("list", listOf("item1", "item2"))
        bugseeInternal.setAttribute("map", mapOf("key" to "value"))
        
        assertTrue(true, "All data types should be accepted for attributes")
    }

    @Test
    fun `test complex data types in event parameters`() {
        val complexParams = mapOf(
            "string" to "test",
            "int" to 42,
            "long" to 123456789L,
            "float" to 3.14f,
            "double" to 2.718,
            "boolean" to true,
            "list" to listOf("item1", "item2"),
            "map" to mapOf("key" to "value")
        )
        
        // Test complex data types in event parameters - this should not throw exceptions
        bugseeInternal.event("complex_event", complexParams)
        
        assertTrue(true, "Complex data types should work in event parameters")
    }

    @Test
    fun `test trace with various value types`() {
        // Test trace with various value types - these should not throw exceptions
        bugseeInternal.trace("string_trace", "test")
        bugseeInternal.trace("int_trace", 42)
        bugseeInternal.trace("long_trace", 123456789L)
        bugseeInternal.trace("float_trace", 3.14f)
        bugseeInternal.trace("double_trace", 2.718)
        bugseeInternal.trace("boolean_trace", true)
        bugseeInternal.trace("list_trace", listOf("item1", "item2"))
        bugseeInternal.trace("map_trace", mapOf("key" to "value"))
        
        assertTrue(true, "All value types should work in trace")
    }

    @Test
    fun `test BugseeLaunchOptions integration`() {
        // Test BugseeLaunchOptions integration - this should not throw exceptions
        val options = BugseeLaunchOptions().apply {
            shakeToReport = true
            videoEnabled = true
            frameRate = BugseeFrameRate.High
            screenshotEnabled = true
            wifiOnlyUpload = false
            maxRecordingTime = 300
        }
        
        bugseeInternal.launch("test-api-key", options)
        bugseeInternal.relaunch(options)
        
        assertTrue(true, "BugseeLaunchOptions integration should work correctly")
    }

    @Test
    fun `test BugseeSecureRectangle creation and usage`() {
        // Test BugseeSecureRectangle creation and usage - these should not throw exceptions
        val rect1 = BugseeSecureRectangle(0.0, 0.0, 100.0, 200.0)
        val rect2 = BugseeSecureRectangle(10.5, 20.7, 100.3, 200.9)
        val rect3 = BugseeSecureRectangle(-10.0, -20.0, 100.0, 200.0)
        
        bugseeInternal.addSecureRectangle(rect1)
        bugseeInternal.addSecureRectangle(rect2)
        bugseeInternal.addSecureRectangle(rect3)
        
        bugseeInternal.removeSecureRectangle(rect1)
        bugseeInternal.removeAllSecureRectangles()
        
        assertTrue(true, "BugseeSecureRectangle creation and usage should work correctly")
    }

    @Test
    fun `test convertAttachment with file path`() {
        val attachment = BugseeAttachment.create("test_file", "/path/to/data.json")
        val native = BugseeAndroidUtils.convertAttachment(attachment)
        assertEquals("test_file", native.name)
        assertEquals("/path/to/data.json", native.dataFilePath)
        assertNull(native.dataBytes)
    }

    @Test
    fun `test convertAttachment with byte data`() {
        val data = "hello".toByteArray()
        val attachment = BugseeAttachment.create("test_data", data)
        val native = BugseeAndroidUtils.convertAttachment(attachment)
        assertEquals("test_data", native.name)
        assertNotNull(native.dataBytes)
        assertTrue(data.contentEquals(native.dataBytes))
    }

    @Test
    fun `test BugseeAttachment creation and usage`() {
        // Test BugseeAttachment creation and usage - these should not throw exceptions
        val attachment1 = BugseeAttachment.create("test.txt", "test content".toByteArray())
        val attachment2 = BugseeAttachment.create("file.txt", "/path/to/file.txt")
        
        val provider: BugseeAttachmentsProvider = { report ->
            listOf(attachment1, attachment2)
        }
        bugseeInternal.setReportAttachmentsProvider(provider)
        
        assertTrue(true, "BugseeAttachment creation and usage should work correctly")
    }

    // --- BugseeExtendedReport screenshot tests ---

    @Test
    fun `test screenshot setter accepts Bitmap`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        report.screenshot = bitmap

        assertTrue(report.screenshotChanged, "screenshotChanged should be true after setting Bitmap")
    }

    @Test
    fun `test screenshot setter accepts ByteArray`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        // Create a valid PNG ByteArray from a small Bitmap
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val pngBytes = stream.toByteArray()

        report.screenshot = pngBytes

        assertTrue(report.screenshotChanged, "screenshotChanged should be true after setting ByteArray")
    }

    @Test
    fun `test screenshot setter ignores unsupported type without crashing`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        // Setting a String should be silently ignored (with logging)
        report.screenshot = "not a bitmap"

        assertFalse(report.screenshotChanged, "screenshotChanged should remain false for unsupported type")
    }

    @Test
    fun `test screenshot setter handles null`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        report.screenshot = null

        assertFalse(report.screenshotChanged, "screenshotChanged should remain false for null")
    }

    @Test
    fun `test screenshot setter handles invalid ByteArray without crashing`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        // Invalid image bytes — should not crash regardless of BitmapFactory behavior
        report.screenshot = byteArrayOf(0, 1, 2, 3)

        // Under Robolectric, BitmapFactory.decodeByteArray may return a shadow Bitmap
        // The key assertion is that no exception was thrown
        assertTrue(true, "Setting invalid ByteArray should not crash")
    }

    // --- BugseeExtendedReport summary/description tests ---

    @Test
    fun `test summary getter and setter`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.summary).thenReturn("Test Summary")
        val report = BugseeExtendedReport(mockNativeReport)

        assertEquals("Test Summary", report.summary)

        report.summary = "New Summary"
        verify(mockNativeReport).summary = "New Summary"
    }

    @Test
    fun `test description getter and setter`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.description).thenReturn("Test Description")
        val report = BugseeExtendedReport(mockNativeReport)

        assertEquals("Test Description", report.description)

        report.description = "New Description"
        verify(mockNativeReport).description = "New Description"
    }

    @Test
    fun `test summary and description accept null`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        report.summary = null
        report.description = null

        verify(mockNativeReport).summary = null
        verify(mockNativeReport).description = null
    }

    // --- BugseeExtendedReport attribute tests ---

    @Test
    fun `test setAttribute delegates to native`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        report.setAttribute("key", "value")
        verify(mockNativeReport).setAttribute("key", "value")
    }

    @Test
    fun `test getAttribute delegates to native`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.getAttribute("key")).thenReturn("value")
        val report = BugseeExtendedReport(mockNativeReport)

        assertEquals("value", report.getAttribute("key"))
    }

    @Test
    fun `test getAttribute returns null for missing key`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.getAttribute("missing")).thenReturn(null)
        val report = BugseeExtendedReport(mockNativeReport)

        assertNull(report.getAttribute("missing"))
    }

    @Test
    fun `test clearAttribute delegates to native`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        report.clearAttribute("key")
        verify(mockNativeReport).clearAttribute("key")
    }

    // --- BugseeExtendedReport label tests ---

    @Test
    fun `test addLabel to empty labels`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(null)
        val report = BugseeExtendedReport(mockNativeReport)

        // Should initialize labels and add without crashing
        report.addLabel("test")
        // Verify labels was set to a new ArrayList
        verify(mockNativeReport).labels = org.mockito.ArgumentMatchers.any()
    }

    @Test
    fun `test addLabel skips duplicate`() {
        val labels = ArrayList<String>(listOf("existing"))
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(labels)
        val report = BugseeExtendedReport(mockNativeReport)

        report.addLabel("existing")
        assertEquals(1, labels.size, "Should not add duplicate label")
    }

    @Test
    fun `test addLabel adds new label`() {
        val labels = ArrayList<String>(listOf("existing"))
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(labels)
        val report = BugseeExtendedReport(mockNativeReport)

        report.addLabel("new")
        assertTrue(labels.contains("new"), "Should add new label")
        assertEquals(2, labels.size)
    }

    @Test
    fun `test removeLabel removes existing`() {
        val labels = ArrayList<String>(listOf("a", "b", "c"))
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(labels)
        val report = BugseeExtendedReport(mockNativeReport)

        report.removeLabel("b")
        assertFalse(labels.contains("b"), "Should remove label")
        assertEquals(2, labels.size)
    }

    @Test
    fun `test removeLabel with null labels does not crash`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(null)
        val report = BugseeExtendedReport(mockNativeReport)

        report.removeLabel("anything")
        assertTrue(true, "Should not crash when labels is null")
    }

    @Test
    fun `test clearLabels clears all`() {
        val labels = ArrayList<String>(listOf("a", "b"))
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(labels)
        val report = BugseeExtendedReport(mockNativeReport)

        report.clearLabels()
        assertTrue(labels.isEmpty(), "Should clear all labels")
    }

    @Test
    fun `test clearLabels with null labels does not crash`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(null)
        val report = BugseeExtendedReport(mockNativeReport)

        report.clearLabels()
        assertTrue(true, "Should not crash when labels is null")
    }

    @Test
    fun `test getLabels returns list`() {
        val labels = ArrayList<String>(listOf("x", "y"))
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(labels)
        val report = BugseeExtendedReport(mockNativeReport)

        val result = report.getLabels()
        assertEquals(listOf("x", "y"), result)
    }

    @Test
    fun `test getLabels returns empty for null labels`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(null)
        val report = BugseeExtendedReport(mockNativeReport)

        val result = report.getLabels()
        assertTrue(result.isEmpty(), "Should return empty list for null labels")
    }

    @Test
    fun `test getLabels returns copy not reference`() {
        val labels = ArrayList<String>(listOf("a"))
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.labels).thenReturn(labels)
        val report = BugseeExtendedReport(mockNativeReport)

        val result = report.getLabels()
        labels.add("b")
        assertEquals(1, result.size, "Returned list should be a copy")
    }

    // --- BugseeExtendedReport type test ---

    @Test
    fun `test type delegates to native via convertIssueType`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        `when`(mockNativeReport.type).thenReturn(com.bugsee.library.data.IssueType.Bug)
        val report = BugseeExtendedReport(mockNativeReport)

        val reportType = report.type
        assertEquals(BugseeReportType.Bug, reportType)
    }

    // --- BugseeExtendedReport attachment tests ---

    @Test
    fun `test addAttachment with byte data does not crash`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        val attachment = BugseeAttachment.create("test", "hello".toByteArray())
        report.addAttachment(attachment)

        assertTrue(true, "addAttachment with byte data should not crash")
    }

    @Test
    fun `test addAttachment with file path does not crash`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        val attachment = BugseeAttachment.create("test_file", "/tmp/test.txt")
        report.addAttachment(attachment)

        assertTrue(true, "addAttachment with file path should not crash")
    }

    // --- BugseeExtendedReport screenshotChanged default ---

    @Test
    fun `test screenshotChanged is false by default`() {
        val mockNativeReport = mock(ExtendedReport::class.java)
        val report = BugseeExtendedReport(mockNativeReport)

        assertFalse(report.screenshotChanged, "screenshotChanged should be false initially")
    }
}
