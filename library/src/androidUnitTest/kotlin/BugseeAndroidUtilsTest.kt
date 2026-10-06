package com.bugsee.kmp

import android.graphics.Rect
import com.bugsee.library.contracts.exchange.NetworkEvent.NetworkEventStage
import com.bugsee.library.contracts.lifecycle.LifecycleEvents
import com.bugsee.library.contracts.options.IssueSeverity
import com.bugsee.library.contracts.options.IssueType
import com.bugsee.library.contracts.options.LogLevel
import com.bugsee.library.contracts.reporting.Attachment
import com.bugsee.library.contracts.reporting.ExceptionOptions
import com.bugsee.library.contracts.reporting.Report
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeAndroidUtilsTest {

    private fun mockNativeReport(
        type: IssueType? = IssueType.Bug,
        severity: IssueSeverity? = IssueSeverity.Medium,
        labels: List<String>? = null,
        summary: String? = null,
        description: String? = null
    ): Report {
        val report = mock(Report::class.java)
        `when`(report.type).thenReturn(type)
        `when`(report.severity).thenReturn(severity)
        `when`(report.labels).thenReturn(labels)
        `when`(report.summary).thenReturn(summary)
        `when`(report.description).thenReturn(description)
        return report
    }

    // --- convertIssueType ---

    @Test
    fun `convertIssueType - Android to KMP - all values`() {
        assertEquals(BugseeReportType.Bug, BugseeAndroidUtils.convertIssueType(IssueType.Bug))
        assertEquals(BugseeReportType.Error, BugseeAndroidUtils.convertIssueType(IssueType.Error))
        assertEquals(BugseeReportType.Crash, BugseeAndroidUtils.convertIssueType(IssueType.Crash))
    }

    @Test
    fun `convertIssueType - Android to KMP - null falls back to Bug`() {
        assertEquals(BugseeReportType.Bug, BugseeAndroidUtils.convertIssueType(null))
    }

    // --- convertLogLevel ---

    @Test
    fun `convertLogLevel - KMP to Android - all values`() {
        assertEquals(LogLevel.Debug, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Debug))
        assertEquals(LogLevel.Info, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Info))
        assertEquals(LogLevel.Warning, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Warning))
        assertEquals(LogLevel.Error, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Error))
        assertEquals(LogLevel.Verbose, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Verbose))
    }

    @Test
    fun `convertLogLevel - Android to KMP - all values`() {
        assertEquals(BugseeLogLevel.Debug, BugseeAndroidUtils.convertLogLevel(LogLevel.Debug))
        assertEquals(BugseeLogLevel.Info, BugseeAndroidUtils.convertLogLevel(LogLevel.Info))
        assertEquals(BugseeLogLevel.Warning, BugseeAndroidUtils.convertLogLevel(LogLevel.Warning))
        assertEquals(BugseeLogLevel.Error, BugseeAndroidUtils.convertLogLevel(LogLevel.Error))
        assertEquals(BugseeLogLevel.Verbose, BugseeAndroidUtils.convertLogLevel(LogLevel.Verbose))
    }

    @Test
    fun `convertLogLevel - Android to KMP - null falls back to Verbose`() {
        assertEquals(BugseeLogLevel.Verbose, BugseeAndroidUtils.convertLogLevel(null as LogLevel?))
    }

    @Test
    fun `convertLogLevel - round trip preserves value`() {
        for (level in BugseeLogLevel.entries) {
            val native = BugseeAndroidUtils.convertLogLevel(level)
            val roundTrip = BugseeAndroidUtils.convertLogLevel(native)
            assertEquals(level, roundTrip, "Round trip failed for $level")
        }
    }

    // --- convertSeverity ---

    @Test
    fun `convertSeverity - KMP to Android - all values`() {
        assertEquals(IssueSeverity.Critical, BugseeAndroidUtils.convertSeverity(BugseeSeverity.Critical))
        assertEquals(IssueSeverity.High, BugseeAndroidUtils.convertSeverity(BugseeSeverity.High))
        assertEquals(IssueSeverity.Medium, BugseeAndroidUtils.convertSeverity(BugseeSeverity.Medium))
        assertEquals(IssueSeverity.VeryLow, BugseeAndroidUtils.convertSeverity(BugseeSeverity.VeryLow))
        assertEquals(IssueSeverity.Blocker, BugseeAndroidUtils.convertSeverity(BugseeSeverity.Blocker))
    }

    @Test
    fun `convertSeverity - Android to KMP - all values`() {
        assertEquals(BugseeSeverity.Critical, BugseeAndroidUtils.convertSeverity(IssueSeverity.Critical))
        assertEquals(BugseeSeverity.High, BugseeAndroidUtils.convertSeverity(IssueSeverity.High))
        assertEquals(BugseeSeverity.Medium, BugseeAndroidUtils.convertSeverity(IssueSeverity.Medium))
        assertEquals(BugseeSeverity.VeryLow, BugseeAndroidUtils.convertSeverity(IssueSeverity.VeryLow))
        assertEquals(BugseeSeverity.Blocker, BugseeAndroidUtils.convertSeverity(IssueSeverity.Blocker))
    }

    @Test
    fun `convertSeverity - Android to KMP - null falls back to Medium`() {
        assertEquals(BugseeSeverity.Medium, BugseeAndroidUtils.convertSeverity(null as IssueSeverity?))
    }

    @Test
    fun `convertSeverity - round trip preserves value`() {
        for (severity in BugseeSeverity.entries) {
            val native = BugseeAndroidUtils.convertSeverity(severity)
            val roundTrip = BugseeAndroidUtils.convertSeverity(native)
            assertEquals(severity, roundTrip, "Round trip failed for $severity")
        }
    }

    // --- convertSecureRect ---

    @Test
    fun `convertSecureRect - KMP to Android - normal values`() {
        val kmpRect = BugseeSecureRectangle(10.0, 20.0, 100.0, 200.0)
        val androidRect = BugseeAndroidUtils.convertSecureRect(kmpRect)
        assertEquals(10, androidRect.left)
        assertEquals(20, androidRect.top)
        assertEquals(110, androidRect.right)  // x + width
        assertEquals(220, androidRect.bottom) // y + height
    }

    @Test
    fun `convertSecureRect - KMP to Android - zero rect`() {
        val kmpRect = BugseeSecureRectangle(0.0, 0.0, 0.0, 0.0)
        val androidRect = BugseeAndroidUtils.convertSecureRect(kmpRect)
        assertEquals(0, androidRect.left)
        assertEquals(0, androidRect.top)
        assertEquals(0, androidRect.right)
        assertEquals(0, androidRect.bottom)
    }

    @Test
    fun `convertSecureRect - KMP to Android - fractional values truncated`() {
        val kmpRect = BugseeSecureRectangle(10.7, 20.3, 100.9, 200.1)
        val androidRect = BugseeAndroidUtils.convertSecureRect(kmpRect)
        assertEquals(10, androidRect.left)
        assertEquals(20, androidRect.top)
        // (10.7 + 100.9).toInt() = 111.6.toInt() = 111
        assertEquals(111, androidRect.right)
        // (20.3 + 200.1).toInt() = 220.4.toInt() = 220
        assertEquals(220, androidRect.bottom)
    }

    @Test
    fun `convertSecureRect - Android to KMP - normal values`() {
        val androidRect = Rect(10, 20, 110, 220)
        val kmpRect = BugseeAndroidUtils.convertSecureRect(androidRect)
        assertEquals(10.0, kmpRect.x)
        assertEquals(20.0, kmpRect.y)
        // NOTE: Current implementation passes right/bottom as width/height.
        // This documents the actual behavior (which may be a bug).
        assertEquals(110.0, kmpRect.width)
        assertEquals(220.0, kmpRect.height)
    }

    @Test
    fun `convertSecureRect - Android to KMP - zero rect`() {
        val androidRect = Rect(0, 0, 0, 0)
        val kmpRect = BugseeAndroidUtils.convertSecureRect(androidRect)
        assertEquals(0.0, kmpRect.x)
        assertEquals(0.0, kmpRect.y)
        assertEquals(0.0, kmpRect.width)
        assertEquals(0.0, kmpRect.height)
    }

    // --- convertReport ---

    @Test
    fun `convertReport - Android to KMP - with labels`() {
        val nativeReport = mockNativeReport(
            type = IssueType.Bug,
            severity = IssueSeverity.High,
            labels = arrayListOf("label1", "label2")
        )
        val kmpReport = BugseeAndroidUtils.convertReport(nativeReport)
        assertEquals(BugseeReportType.Bug, kmpReport.type)
        assertEquals(BugseeSeverity.High, kmpReport.severity)
        assertEquals(listOf("label1", "label2"), kmpReport.labels)
    }

    @Test
    fun `convertReport - Android to KMP - null labels`() {
        val nativeReport = mockNativeReport(
            type = IssueType.Crash,
            severity = IssueSeverity.Critical,
            labels = null
        )
        val kmpReport = BugseeAndroidUtils.convertReport(nativeReport)
        assertEquals(BugseeReportType.Crash, kmpReport.type)
        assertEquals(BugseeSeverity.Critical, kmpReport.severity)
        assertEquals(emptyList(), kmpReport.labels)
    }

    // --- addAttachmentToReport ---

    @Test
    fun `addAttachmentToReport - data attachment is added in memory`() {
        val data = "test content".toByteArray()
        val kmpAttachment = BugseeAttachment.create("test.txt", data)
        val nativeReport = mockNativeReport()

        BugseeAndroidUtils.addAttachmentToReport(kmpAttachment, nativeReport)

        verify(nativeReport).addAttachment(data, "test.txt", null)
        verify(nativeReport, never()).addAttachment(org.mockito.ArgumentMatchers.any(File::class.java), anyString(), isNull(), anyBoolean())
    }

    // --- addProviderAttachments ---

    @Test
    fun `addProviderAttachments - skips names already on the report`() {
        val existing = mock(Attachment::class.java)
        `when`(existing.name).thenReturn("log.txt")
        val nativeReport = mockNativeReport()
        `when`(nativeReport.attachments).thenReturn(listOf(existing))
        val logData = "replayed".toByteArray()
        val configData = "config".toByteArray()

        BugseeAndroidUtils.addProviderAttachments(
            listOf(
                BugseeAttachment.create("log.txt", logData),
                BugseeAttachment.create("config.json", configData)
            ),
            nativeReport
        )

        verify(nativeReport, never()).addAttachment(logData, "log.txt", null)
        verify(nativeReport).addAttachment(configData, "config.json", null)
    }

    @Test
    fun `addProviderAttachments - keeps provider duplicates absent from the report`() {
        val nativeReport = mockNativeReport()
        `when`(nativeReport.attachments).thenReturn(emptyList())
        val first = "first".toByteArray()
        val second = "second".toByteArray()

        BugseeAndroidUtils.addProviderAttachments(
            listOf(BugseeAttachment.create("same.txt", first), BugseeAttachment.create("same.txt", second)),
            nativeReport
        )

        verify(nativeReport).addAttachment(first, "same.txt", null)
        verify(nativeReport).addAttachment(second, "same.txt", null)
    }

    // --- convertReportFields ---

    @Test
    fun `convertReportFieldsFromNative - normal values`() {
        val nativeReport = mockNativeReport(
            severity = IssueSeverity.High,
            labels = arrayListOf("label1"),
            summary = "Summary",
            description = "Description"
        )
        val kmpFields = BugseeAndroidUtils.convertReportFieldsFromNative(nativeReport)
        assertEquals("Summary", kmpFields.summary)
        assertEquals("Description", kmpFields.description)
        assertEquals(BugseeSeverity.High, kmpFields.severity)
        assertEquals(listOf("label1"), kmpFields.labels)
    }

    @Test
    fun `convertReportFieldsFromNative - null summary and description fall back to empty`() {
        val nativeReport = mockNativeReport(summary = null, description = null)
        val kmpFields = BugseeAndroidUtils.convertReportFieldsFromNative(nativeReport)
        assertEquals("", kmpFields.summary)
        assertEquals("", kmpFields.description)
    }

    @Test
    fun `applyReportFieldsToNative - normal values`() {
        val kmpFields = BugseeReportFields(
            "Summary",
            "Description",
            BugseeSeverity.Critical,
            listOf("qa", "test")
        )
        val nativeReport = mockNativeReport()
        val original = BugseeAndroidUtils.convertReportFieldsFromNative(nativeReport)

        BugseeAndroidUtils.applyReportFieldsToNative(kmpFields, original, nativeReport)

        verify(nativeReport).summary = "Summary"
        verify(nativeReport).description = "Description"
        verify(nativeReport).severity = IssueSeverity.Critical
        verify(nativeReport).setLabels(listOf("qa", "test"))
    }

    @Test
    fun `applyReportFieldsToNative - empty strings and labels`() {
        val kmpFields = BugseeReportFields("", "", BugseeSeverity.Medium, emptyList())
        val nativeReport = mockNativeReport(summary = "Summary", description = "Description", labels = listOf("qa"))
        val original = BugseeAndroidUtils.convertReportFieldsFromNative(nativeReport)

        BugseeAndroidUtils.applyReportFieldsToNative(kmpFields, original, nativeReport)

        verify(nativeReport).summary = ""
        verify(nativeReport).description = ""
        verify(nativeReport).setLabels(emptyList())
    }

    @Test
    fun `applyReportFieldsToNative - untouched fields are not written back`() {
        val nativeReport = mockNativeReport()
        val original = BugseeAndroidUtils.convertReportFieldsFromNative(nativeReport)
        val fields = BugseeAndroidUtils.convertReportFieldsFromNative(nativeReport)

        BugseeAndroidUtils.applyReportFieldsToNative(fields, original, nativeReport)

        // A null summary/description must stay null rather than become ""
        verify(nativeReport, never()).summary = anyString()
        verify(nativeReport, never()).description = anyString()
        verify(nativeReport, never()).severity = IssueSeverity.Medium
        verify(nativeReport, never()).setLabels(emptyList())
    }

    @Test
    fun `convertReportFieldsFromNative - empty labels`() {
        val nativeReport = mockNativeReport(
            labels = arrayListOf(),
            summary = "Summary",
            description = "Description"
        )
        val kmpFields = BugseeAndroidUtils.convertReportFieldsFromNative(nativeReport)
        assertTrue(kmpFields.labels.isEmpty())
    }

    // --- convertExceptionLoggingOptions ---

    @Test
    fun `convertExceptionLoggingOptions - null returns null`() {
        assertNull(BugseeAndroidUtils.convertExceptionLoggingOptions(null))
    }

    @Test
    fun `convertExceptionLoggingOptions - populated options`() {
        val options = BugseeExceptionLoggingOptions()
        options.exceptionDomain = "com.test"
        options.includeVideo = false
        options.labels = arrayListOf("label1", "label2")
        options.rules.skipFrames = 3
        options.rules.setCustomOption("customKey", "customValue")

        val nativeOptions = BugseeAndroidUtils.convertExceptionLoggingOptions(options)
        assertNotNull(nativeOptions)
        assertEquals("com.test", nativeOptions[ExceptionOptions.Domain])
        assertEquals(false, nativeOptions["includeVideo"])
        assertEquals(arrayListOf("label1", "label2"), nativeOptions["labels"])
        assertEquals(3, nativeOptions[ExceptionOptions.SkipFrames])
        assertEquals("customValue", nativeOptions["customKey"])
    }

    @Test
    fun `convertExceptionLoggingOptions - default values`() {
        val options = BugseeExceptionLoggingOptions()
        val nativeOptions = BugseeAndroidUtils.convertExceptionLoggingOptions(options)
        assertNotNull(nativeOptions)
        assertFalse(nativeOptions.containsKey(ExceptionOptions.Domain))
        assertEquals(true, nativeOptions["includeVideo"])
        assertFalse(nativeOptions.containsKey("labels"))
        assertEquals(0, nativeOptions[ExceptionOptions.SkipFrames])
    }

    // --- convertLifecycleEvent ---

    @Test
    fun `convertLifecycleEvent - all event types`() {
        assertEquals(BugseeLifecycleEvent.Launched, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.Launched))
        assertEquals(BugseeLifecycleEvent.Stopped, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.Stopped))
        assertEquals(BugseeLifecycleEvent.Resumed, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.BlackoutEnded))
        assertEquals(BugseeLifecycleEvent.Paused, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.BlackoutStarted))
        assertEquals(BugseeLifecycleEvent.RelaunchedAfterCrash, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.RelaunchedAfterCrash))
        assertEquals(BugseeLifecycleEvent.BeforeReportShown, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.BeforeReportShown))
        assertEquals(BugseeLifecycleEvent.AfterReportShown, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.AfterReportShown))
        assertEquals(BugseeLifecycleEvent.BeforeReportUploaded, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.BeforeReportUploaded))
        assertEquals(BugseeLifecycleEvent.AfterReportUploaded, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.AfterReportUploaded))
        assertEquals(BugseeLifecycleEvent.BeforeReportAssembled, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.BeforeReportAssembled))
        assertEquals(BugseeLifecycleEvent.AfterReportAssembled, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.AfterReportAssembled))
        assertEquals(BugseeLifecycleEvent.ReportUploadFailedWithFutureRetry, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.ReportUploadFailedWithFutureRetry))
        assertEquals(BugseeLifecycleEvent.ReportUploadFailed, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.ReportUploadFailed))
    }

    @Test
    fun `convertLifecycleEvent - events without KMP counterpart return null`() {
        assertNull(BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.Launching))
        assertNull(BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.Stopping))
        assertNull(BugseeAndroidUtils.convertLifecycleEvent(LifecycleEvents.ReportAssemblyFailed))
        assertNull(BugseeAndroidUtils.convertLifecycleEvent("com.bugsee.lifecycle.Unknown"))
    }

    // --- convertNetworkEventStage ---

    @Test
    fun `convertNetworkEventStage - all stages`() {
        assertEquals(BugseeNetworkEventStage.Before, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventStage.RequestStarted))
        assertEquals(BugseeNetworkEventStage.Complete, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventStage.RequestCompleted))
        assertEquals(BugseeNetworkEventStage.Redirect, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventStage.Redirect))
        assertEquals(BugseeNetworkEventStage.Errors, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventStage.RequestErrored))
        assertEquals(BugseeNetworkEventStage.Cancel, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventStage.RequestAborted))
        assertEquals(BugseeNetworkEventStage.WebSocket, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventStage.WebSocket))
        // No KMP counterpart — falls back to Before
        assertEquals(BugseeNetworkEventStage.Before, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventStage.RequestTimingsReceived))
        assertEquals(BugseeNetworkEventStage.Before, BugseeAndroidUtils.convertNetworkEventStage(null))
    }

    // --- toSerializableMap ---

    @Test
    fun `toSerializableMap - keeps Serializable values and drops the rest`() {
        val source = mapOf<String, Any?>(
            "string" to "value",
            "int" to 42,
            "null" to null,
            "notSerializable" to Any()
        )
        val result = BugseeAndroidUtils.toSerializableMap(source, "test")
        assertEquals(mapOf<String, Any>("string" to "value", "int" to 42), result)
    }

    @Test
    fun `toSerializableMap - null source returns empty map`() {
        assertTrue(BugseeAndroidUtils.toSerializableMap(null, "test").isEmpty())
    }
}
