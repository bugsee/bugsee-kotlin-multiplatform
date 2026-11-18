package com.bugsee.kmp

import com.bugsee.library.contracts.reporting.Report
import com.bugsee.library.contracts.reporting.ReportCreationListener
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeCreateReportAndroidTest {

    // Runs [call] against a mocked SDK and answers its createReport with [nativeReport].
    private fun answerCreateReport(nativeReport: Report?, call: () -> Unit) {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            call()

            val captor = ArgumentCaptor.forClass(ReportCreationListener::class.java)
            sdk.verify { com.bugsee.library.Bugsee.createReport(captor.capture()) }
            captor.value.onCreated(nativeReport)
        }
    }

    @Test
    fun `a created report goes to the provider`() {
        val nativeReport = mock(Report::class.java)
        var received: BugseeExtendedReport? = null
        var notCreated = 0

        answerCreateReport(nativeReport) {
            Bugsee.createReport(provider = { received = it }, onNotCreated = { notCreated++ })
        }

        assertSame(nativeReport, received?.underlyingReport)
        assertEquals(0, notCreated)
    }

    @Test
    fun `onNotCreated is called when the SDK creates no report`() {
        var received: BugseeExtendedReport? = null
        var notCreated = 0

        answerCreateReport(null) {
            Bugsee.createReport(provider = { received = it }, onNotCreated = { notCreated++ })
        }

        assertNull(received)
        assertEquals(1, notCreated)
    }

    @Test
    fun `without onNotCreated a missing report calls nothing`() {
        var calls = 0

        answerCreateReport(null) {
            Bugsee.createReport { calls++ }
        }

        assertEquals(0, calls)
    }

    @Test
    fun `createReportOrNull passes the report or null`() {
        val created = mutableListOf<BugseeExtendedReport?>()

        answerCreateReport(mock(Report::class.java)) { Bugsee.createReportOrNull { created += it } }
        answerCreateReport(null) { Bugsee.createReportOrNull { created += it } }

        assertEquals(2, created.size)
        assertNotNull(created[0])
        assertNull(created[1])
    }
}
