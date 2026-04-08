package com.bugsee.kmp

import android.graphics.Rect
import com.bugsee.library.data.IssueSeverity
import com.bugsee.library.data.IssueType
import com.bugsee.library.events.BugseeLogLevel as AndroidLogLevel
import com.bugsee.library.lifecycle.LifecycleEventTypes
import com.bugsee.library.network.data.NetworkEventType
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeAndroidUtilsTest {

    // --- convertIssueType ---

    @Test
    fun `convertIssueType - Android to KMP - all values`() {
        assertEquals(BugseeReportType.Bug, BugseeAndroidUtils.convertIssueType(IssueType.Bug))
        assertEquals(BugseeReportType.Error, BugseeAndroidUtils.convertIssueType(IssueType.Error))
        assertEquals(BugseeReportType.Crash, BugseeAndroidUtils.convertIssueType(IssueType.Crash))
    }

    @Test
    fun `convertIssueType - KMP to Android - all values`() {
        assertEquals(IssueType.Bug, BugseeAndroidUtils.convertIssueType(BugseeReportType.Bug))
        assertEquals(IssueType.Error, BugseeAndroidUtils.convertIssueType(BugseeReportType.Error))
        assertEquals(IssueType.Crash, BugseeAndroidUtils.convertIssueType(BugseeReportType.Crash))
    }

    @Test
    fun `convertIssueType - round trip preserves value`() {
        for (type in BugseeReportType.entries) {
            val native = BugseeAndroidUtils.convertIssueType(type)
            val roundTrip = BugseeAndroidUtils.convertIssueType(native)
            assertEquals(type, roundTrip, "Round trip failed for $type")
        }
    }

    // --- convertLogLevel ---

    @Test
    fun `convertLogLevel - KMP to Android - all values`() {
        assertEquals(AndroidLogLevel.Debug, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Debug))
        assertEquals(AndroidLogLevel.Info, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Info))
        assertEquals(AndroidLogLevel.Warning, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Warning))
        assertEquals(AndroidLogLevel.Error, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Error))
        assertEquals(AndroidLogLevel.Verbose, BugseeAndroidUtils.convertLogLevel(BugseeLogLevel.Verbose))
    }

    @Test
    fun `convertLogLevel - Android to KMP - all values`() {
        assertEquals(BugseeLogLevel.Debug, BugseeAndroidUtils.convertLogLevel(AndroidLogLevel.Debug))
        assertEquals(BugseeLogLevel.Info, BugseeAndroidUtils.convertLogLevel(AndroidLogLevel.Info))
        assertEquals(BugseeLogLevel.Warning, BugseeAndroidUtils.convertLogLevel(AndroidLogLevel.Warning))
        assertEquals(BugseeLogLevel.Error, BugseeAndroidUtils.convertLogLevel(AndroidLogLevel.Error))
        assertEquals(BugseeLogLevel.Verbose, BugseeAndroidUtils.convertLogLevel(AndroidLogLevel.Verbose))
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
        val nativeReport = com.bugsee.library.attachment.Report(
            IssueType.Bug,
            IssueSeverity.High,
            arrayListOf("label1", "label2")
        )
        val kmpReport = BugseeAndroidUtils.convertReport(nativeReport)
        assertEquals(BugseeReportType.Bug, kmpReport.type)
        assertEquals(BugseeSeverity.High, kmpReport.severity)
        assertEquals(listOf("label1", "label2"), kmpReport.labels)
    }

    @Test
    fun `convertReport - Android to KMP - null labels`() {
        val nativeReport = com.bugsee.library.attachment.Report(
            IssueType.Crash,
            IssueSeverity.Critical,
            null
        )
        val kmpReport = BugseeAndroidUtils.convertReport(nativeReport)
        assertEquals(BugseeReportType.Crash, kmpReport.type)
        assertEquals(BugseeSeverity.Critical, kmpReport.severity)
        assertEquals(emptyList(), kmpReport.labels)
    }

    @Test
    fun `convertReport - KMP to Android - with labels`() {
        val kmpReport = BugseeReport(
            BugseeReportType.Error,
            BugseeSeverity.Medium,
            listOf("qa", "test")
        )
        val nativeReport = BugseeAndroidUtils.convertReport(kmpReport)
        assertEquals(IssueType.Error, nativeReport.type)
        assertEquals(IssueSeverity.Medium, nativeReport.severity)
        assertEquals(listOf("qa", "test"), nativeReport.labels?.toList())
    }

    @Test
    fun `convertReport - KMP to Android - null labels`() {
        val kmpReport = BugseeReport(
            BugseeReportType.Bug,
            BugseeSeverity.VeryLow,
            null
        )
        val nativeReport = BugseeAndroidUtils.convertReport(kmpReport)
        assertEquals(IssueType.Bug, nativeReport.type)
        assertEquals(IssueSeverity.VeryLow, nativeReport.severity)
        assertNull(nativeReport.labels)
    }

    @Test
    fun `convertReport - KMP to Android - empty labels`() {
        val kmpReport = BugseeReport(
            BugseeReportType.Bug,
            BugseeSeverity.VeryLow,
            emptyList()
        )
        val nativeReport = BugseeAndroidUtils.convertReport(kmpReport)
        assertNotNull(nativeReport.labels)
        assertTrue(nativeReport.labels!!.isEmpty())
    }

    // --- convertAttachment ---

    @Test
    fun `convertAttachment - KMP with data to Android`() {
        val data = "test content".toByteArray()
        val kmpAttachment = BugseeAttachment.create("test.txt", data)
        val nativeAttachment = BugseeAndroidUtils.convertAttachment(kmpAttachment)
        assertEquals("test.txt", nativeAttachment.name)
        assertNotNull(nativeAttachment.dataBytes)
        assertTrue(data.contentEquals(nativeAttachment.dataBytes))
    }

    @Test
    fun `convertAttachment - Android with data to KMP`() {
        val data = "test content".toByteArray()
        val nativeAttachment = com.bugsee.library.attachment.CustomAttachment.fromDataBytes(data)
        nativeAttachment.name = "test.txt"
        val kmpAttachment = BugseeAndroidUtils.convertAttachment(nativeAttachment)
        assertEquals("test.txt", kmpAttachment.name)
        assertNotNull(kmpAttachment.data)
        assertTrue(data.contentEquals(kmpAttachment.data!!))
    }

    // --- convertReportFields ---

    @Test
    fun `convertReportFieldsFromNative - normal values`() {
        val nativeFields = com.bugsee.library.send.ReportFields(
            "Summary",
            "Description",
            arrayListOf("label1"),
            IssueSeverity.High
        )
        val kmpFields = BugseeAndroidUtils.convertReportFieldsFromNative(nativeFields)
        assertEquals("Summary", kmpFields.summary)
        assertEquals("Description", kmpFields.description)
        assertEquals(BugseeSeverity.High, kmpFields.severity)
        assertEquals(listOf("label1"), kmpFields.labels)
    }

    @Test
    fun `convertReportFieldsToNative - normal values`() {
        val kmpFields = BugseeReportFields(
            "Summary",
            "Description",
            BugseeSeverity.Critical,
            listOf("qa", "test")
        )
        val nativeFields = BugseeAndroidUtils.convertReportFieldsToNative(kmpFields)
        assertEquals("Summary", nativeFields.summary)
        assertEquals("Description", nativeFields.description)
        assertEquals(IssueSeverity.Critical, nativeFields.severity)
        assertEquals(listOf("qa", "test"), nativeFields.labels)
    }

    @Test
    fun `convertReportFields - empty strings and labels`() {
        val kmpFields = BugseeReportFields("", "", BugseeSeverity.Medium, emptyList())
        val nativeFields = BugseeAndroidUtils.convertReportFieldsToNative(kmpFields)
        assertEquals("", nativeFields.summary)
        assertEquals("", nativeFields.description)
        assertTrue(nativeFields.labels!!.isEmpty())
    }

    @Test
    fun `convertReportFieldsFromNative - empty labels`() {
        val nativeFields = com.bugsee.library.send.ReportFields(
            "Summary",
            "Description",
            arrayListOf(),
            IssueSeverity.Medium
        )
        val kmpFields = BugseeAndroidUtils.convertReportFieldsFromNative(nativeFields)
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
        assertEquals("com.test", nativeOptions.exceptionDomain)
        assertEquals(false, nativeOptions.includeVideo)
        assertEquals(arrayListOf("label1", "label2"), nativeOptions.labels)
        assertEquals(3, nativeOptions.Rules.skipFrames)
    }

    @Test
    fun `convertExceptionLoggingOptions - default values`() {
        val options = BugseeExceptionLoggingOptions()
        val nativeOptions = BugseeAndroidUtils.convertExceptionLoggingOptions(options)
        assertNotNull(nativeOptions)
        assertNull(nativeOptions.exceptionDomain)
        assertEquals(true, nativeOptions.includeVideo)
        assertNull(nativeOptions.labels)
        assertEquals(0, nativeOptions.Rules.skipFrames)
    }

    // --- convertLifecycleEvent ---

    @Test
    fun `convertLifecycleEvent - all event types`() {
        assertEquals(BugseeLifecycleEvent.Launched, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.Launched))
        assertEquals(BugseeLifecycleEvent.Started, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.Started))
        assertEquals(BugseeLifecycleEvent.Stopped, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.Stopped))
        assertEquals(BugseeLifecycleEvent.Resumed, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.Resumed))
        assertEquals(BugseeLifecycleEvent.Paused, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.Paused))
        assertEquals(BugseeLifecycleEvent.RelaunchedAfterCrash, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.RelaunchedAfterCrash))
        assertEquals(BugseeLifecycleEvent.BeforeReportShown, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.BeforeReportShown))
        assertEquals(BugseeLifecycleEvent.AfterReportShown, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.AfterReportShown))
        assertEquals(BugseeLifecycleEvent.BeforeReportUploaded, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.BeforeReportUploaded))
        assertEquals(BugseeLifecycleEvent.AfterReportUploaded, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.AfterReportUploaded))
        assertEquals(BugseeLifecycleEvent.BeforeFeedbackShown, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.BeforeFeedbackShown))
        assertEquals(BugseeLifecycleEvent.AfterFeedbackShown, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.AfterFeedbackShown))
        assertEquals(BugseeLifecycleEvent.BeforeReportAssembled, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.BeforeReportAssembled))
        assertEquals(BugseeLifecycleEvent.AfterReportAssembled, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.AfterReportAssembled))
        assertEquals(BugseeLifecycleEvent.ReportUploadFailedWithFutureRetry, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.ReportUploadFailedWithFutureRetry))
        assertEquals(BugseeLifecycleEvent.ReportUploadFailed, BugseeAndroidUtils.convertLifecycleEvent(LifecycleEventTypes.ReportUploadFailed))
    }

    // --- convertNetworkEventStage ---

    @Test
    fun `convertNetworkEventStage - all stages`() {
        assertEquals(BugseeNetworkEventStage.Before, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventType.Before))
        assertEquals(BugseeNetworkEventStage.Complete, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventType.Complete))
        assertEquals(BugseeNetworkEventStage.Redirect, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventType.Redirect))
        assertEquals(BugseeNetworkEventStage.Errors, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventType.Errors))
        assertEquals(BugseeNetworkEventStage.WebSocket, BugseeAndroidUtils.convertNetworkEventStage(NetworkEventType.WebSocket))
    }
}
