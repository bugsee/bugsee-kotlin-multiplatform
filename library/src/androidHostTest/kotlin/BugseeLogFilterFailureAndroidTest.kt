package com.bugsee.kmp.internal

import com.bugsee.library.contracts.common.Callback1
import com.bugsee.library.contracts.exchange.EventFilter
import com.bugsee.library.contracts.exchange.LogEvent
import com.bugsee.library.contracts.options.LogLevel
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.contains
import org.mockito.ArgumentMatchers.eq
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A log filter that throws must not log the failure to logcat on every line: the SDK reads logcat
 * back through the same filter, so that would never end (#51).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeLogFilterFailureAndroidTest {

    @Suppress("UNCHECKED_CAST")
    private fun installedFilter(sdk: MockedStatic<com.bugsee.library.Bugsee>, installs: Int = 1): EventFilter<LogEvent> {
        val captor = ArgumentCaptor.forClass(EventFilter::class.java)
        sdk.verify({ com.bugsee.library.Bugsee.setLogEventFilter(captor.capture() as EventFilter<LogEvent>?) }, times(installs))
        return captor.value as EventFilter<LogEvent>
    }

    @Suppress("UNCHECKED_CAST")
    private fun runFilter(filter: EventFilter<LogEvent>) {
        val event = mock(LogEvent::class.java)
        val callback = mock(Callback1::class.java) as Callback1<LogEvent>
        filter.filter(event, callback)
        verify(callback).run(event)
    }

    @Test
    fun `filter throws on every line - each line is kept and the failure is reported once`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            val bugseeInternal = BugseeInternal()
            bugseeInternal.setLogFilter { throw IllegalStateException("boom") }
            val filter = installedFilter(sdk)

            repeat(3) { runFilter(filter) }

            sdk.verify({ com.bugsee.library.Bugsee.log(contains("boom"), eq(LogLevel.Error)) }, times(1))
            assertEquals(1, ShadowLog.getLogsForTag("BugseeLogFilter").size)
            assertTrue(ShadowLog.getLogsForTag("BugseeInternal").none { "setLogFilter" in it.msg })
        }
    }

    @Test
    fun `a new filter reports its own first failure`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            val bugseeInternal = BugseeInternal()
            bugseeInternal.setLogFilter { throw IllegalStateException("first") }
            repeat(2) { runFilter(installedFilter(sdk)) }
            bugseeInternal.setLogFilter { throw IllegalStateException("second") }
            repeat(2) { runFilter(installedFilter(sdk, installs = 2)) }

            sdk.verify({ com.bugsee.library.Bugsee.log(contains("first"), eq(LogLevel.Error)) }, times(1))
            sdk.verify({ com.bugsee.library.Bugsee.log(contains("second"), eq(LogLevel.Error)) }, times(1))
        }
    }

    @Test
    fun `the failure reaches logcat when Bugsee log throws`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            sdk.`when`<Unit> { com.bugsee.library.Bugsee.log(anyString(), any(LogLevel::class.java)) }
                .thenThrow(IllegalStateException("log failed"))
            val bugseeInternal = BugseeInternal()
            bugseeInternal.setLogFilter { throw IllegalStateException("boom") }

            runFilter(installedFilter(sdk))

            val lines = ShadowLog.getLogsForTag("BugseeLogFilter")
            assertEquals(1, lines.size)
            assertTrue("boom" in lines.single().msg, lines.single().msg)
        }
    }
}
