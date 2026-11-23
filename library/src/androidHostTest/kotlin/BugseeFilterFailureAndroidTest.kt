package com.bugsee.kmp.internal

import com.bugsee.library.contracts.common.Callback1
import com.bugsee.library.contracts.exchange.EventFilter
import com.bugsee.library.contracts.exchange.LogEvent
import com.bugsee.library.contracts.exchange.NetworkEvent
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
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A log or network filter that throws drops the event, as the SDK does (#56), and reports its first
 * failure once. The log filter must not log the failure to logcat on every line: the SDK reads
 * logcat back through the same filter, so that would never end (#51).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeFilterFailureAndroidTest {

    @Suppress("UNCHECKED_CAST")
    private fun installedLogFilter(sdk: MockedStatic<com.bugsee.library.Bugsee>, installs: Int = 1): EventFilter<LogEvent> {
        val captor = ArgumentCaptor.forClass(EventFilter::class.java)
        sdk.verify({ com.bugsee.library.Bugsee.setLogEventFilter(captor.capture() as EventFilter<LogEvent>?) }, times(installs))
        return captor.value as EventFilter<LogEvent>
    }

    @Suppress("UNCHECKED_CAST")
    private fun installedNetworkFilter(sdk: MockedStatic<com.bugsee.library.Bugsee>, installs: Int = 1): EventFilter<NetworkEvent> {
        val captor = ArgumentCaptor.forClass(EventFilter::class.java)
        sdk.verify({ com.bugsee.library.Bugsee.setNetworkEventFilter(captor.capture() as EventFilter<NetworkEvent>?) }, times(installs))
        return captor.value as EventFilter<NetworkEvent>
    }

    // The filter answers the SDK with null: the line is dropped.
    @Suppress("UNCHECKED_CAST")
    private fun runLogFilter(filter: EventFilter<LogEvent>) {
        val event = mock(LogEvent::class.java)
        val callback = mock(Callback1::class.java) as Callback1<LogEvent>
        filter.filter(event, callback)
        verify(callback).run(null)
    }

    // The filter answers the SDK with null: the event is dropped.
    @Suppress("UNCHECKED_CAST")
    private fun runNetworkFilter(filter: EventFilter<NetworkEvent>) {
        val event = mock(NetworkEvent::class.java)
        val callback = mock(Callback1::class.java) as Callback1<NetworkEvent>
        filter.filter(event, callback)
        verify(callback).run(null)
    }

    @Test
    fun `log filter throws on every line - each line is dropped and the failure is reported once`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            val bugseeInternal = BugseeInternal()
            bugseeInternal.setLogFilter { throw IllegalStateException("boom") }
            val filter = installedLogFilter(sdk)

            repeat(3) { runLogFilter(filter) }

            sdk.verify({ com.bugsee.library.Bugsee.log(contains("IllegalStateException"), eq(LogLevel.Error)) }, times(1))
            sdk.verify({ com.bugsee.library.Bugsee.log(contains("boom"), any(LogLevel::class.java)) }, never())
            assertEquals(1, ShadowLog.getLogsForTag("BugseeFilter").size)
            assertTrue(ShadowLog.getLogsForTag("BugseeInternal").none { "setLogFilter" in it.msg })
        }
    }

    @Test
    fun `a new log filter reports its own first failure`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            val bugseeInternal = BugseeInternal()
            bugseeInternal.setLogFilter { throw IllegalStateException("first") }
            repeat(2) { runLogFilter(installedLogFilter(sdk)) }
            bugseeInternal.setLogFilter { throw IllegalArgumentException("second") }
            repeat(2) { runLogFilter(installedLogFilter(sdk, installs = 2)) }

            sdk.verify({ com.bugsee.library.Bugsee.log(contains("IllegalStateException"), eq(LogLevel.Error)) }, times(1))
            sdk.verify({ com.bugsee.library.Bugsee.log(contains("IllegalArgumentException"), eq(LogLevel.Error)) }, times(1))
        }
    }

    @Test
    fun `the log filter failure reaches logcat when Bugsee log throws`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            sdk.`when`<Unit> { com.bugsee.library.Bugsee.log(anyString(), any(LogLevel::class.java)) }
                .thenThrow(IllegalStateException("log failed"))
            val bugseeInternal = BugseeInternal()
            bugseeInternal.setLogFilter { throw IllegalStateException("boom") }

            runLogFilter(installedLogFilter(sdk))

            val lines = ShadowLog.getLogsForTag("BugseeFilter")
            assertEquals(1, lines.size)
            assertTrue("IllegalStateException" in lines.single().msg, lines.single().msg)
        }
    }

    @Test
    fun `network filter throws on every event - each event is dropped and the failure is reported once`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            val bugseeInternal = BugseeInternal()
            // A parser's message can quote the event the filter was redacting.
            bugseeInternal.setNetworkEventFilter { throw IllegalStateException("token=deeptest-secret") }
            val filter = installedNetworkFilter(sdk)

            repeat(3) { runNetworkFilter(filter) }

            sdk.verify({ com.bugsee.library.Bugsee.log(contains("the network filter threw java.lang.IllegalStateException"), eq(LogLevel.Error)) }, times(1))
            sdk.verify({ com.bugsee.library.Bugsee.log(contains("deeptest-secret"), any(LogLevel::class.java)) }, never())
            val lines = ShadowLog.getLogsForTag("BugseeFilter")
            assertEquals(1, lines.size)
            assertTrue("the event was dropped" in lines.single().msg && "deeptest-secret" !in lines.single().msg, lines.single().msg)
        }
    }

    @Test
    fun `a new network filter reports its own first failure`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            val bugseeInternal = BugseeInternal()
            bugseeInternal.setNetworkEventFilter { throw IllegalStateException("first") }
            repeat(2) { runNetworkFilter(installedNetworkFilter(sdk)) }
            bugseeInternal.setNetworkEventFilter { throw IllegalArgumentException("second") }
            repeat(2) { runNetworkFilter(installedNetworkFilter(sdk, installs = 2)) }

            sdk.verify({ com.bugsee.library.Bugsee.log(contains("IllegalStateException"), eq(LogLevel.Error)) }, times(1))
            sdk.verify({ com.bugsee.library.Bugsee.log(contains("IllegalArgumentException"), eq(LogLevel.Error)) }, times(1))
        }
    }
}
