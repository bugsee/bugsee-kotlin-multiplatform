package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeAttachment
import com.bugsee.kmp.BugseeLifecycleEvent
import com.bugsee.library.contracts.common.Callback1
import com.bugsee.library.contracts.exchange.EventFilter
import com.bugsee.library.contracts.exchange.LogEvent
import com.bugsee.library.contracts.exchange.NetworkEvent
import com.bugsee.library.contracts.lifecycle.LifecycleEventListener
import com.bugsee.library.contracts.lifecycle.LifecycleEvents
import com.bugsee.library.contracts.reporting.Report
import com.bugsee.library.contracts.reporting.ReportCreationListener
import com.bugsee.library.contracts.reporting.ReportHandler
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

/**
 * An Error (TODO(), AssertionError) thrown by an app's callback must not escape into the SDK,
 * which crashes the app or drops the event (#52). Each test drives the handler that the wrapper
 * installs in the mocked SDK.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeCallbackErrorsAndroidTest {

    // The handler the wrapper passed to the SDK; [verification] calls capture() inside verify.
    private inline fun <reified T> captured(verification: (ArgumentCaptor<T>) -> Unit): T {
        val captor = ArgumentCaptor.forClass<T, T>(T::class.java)
        verification(captor)
        return captor.value
    }

    private fun withSdk(block: (MockedStatic<com.bugsee.library.Bugsee>) -> Unit) {
        mockStatic(com.bugsee.library.Bugsee::class.java).use(block)
    }

    @Test
    fun `an Error in the lifecycle listener does not escape`() = withSdk { sdk ->
        var calls = 0
        BugseeInternal().setLifecycleEventsListener { calls++; TODO("listener") }
        val listener = captured<LifecycleEventListener> { captor ->
            sdk.verify { com.bugsee.library.Bugsee.setLifecycleEventsListener(captor.capture()) }
        }

        listener.onEvent(LifecycleEvents.Launched, null)

        assertEquals(1, calls)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `an Error in the network filter drops the event`() = withSdk { sdk ->
        BugseeInternal().setNetworkEventFilter { TODO("filter") }
        val filter = captured<EventFilter<*>> { captor ->
            sdk.verify { com.bugsee.library.Bugsee.setNetworkEventFilter(captor.capture() as EventFilter<NetworkEvent>?) }
        } as EventFilter<NetworkEvent>
        val event = mock(NetworkEvent::class.java)
        val callback = mock(Callback1::class.java) as Callback1<NetworkEvent>

        filter.filter(event, callback)

        verify(callback).run(null)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `an Error in the log filter drops the line`() = withSdk { sdk ->
        BugseeInternal().setLogFilter { TODO("filter") }
        val filter = captured<EventFilter<*>> { captor ->
            sdk.verify { com.bugsee.library.Bugsee.setLogEventFilter(captor.capture() as EventFilter<LogEvent>?) }
        } as EventFilter<LogEvent>
        val event = mock(LogEvent::class.java)
        val callback = mock(Callback1::class.java) as Callback1<LogEvent>

        filter.filter(event, callback)

        verify(callback).run(null)
    }

    @Test
    fun `an Error in the pre-filter still completes the report`() = withSdk { sdk ->
        BugseeInternal().setReportFieldsPreFilter { TODO("pre-filter") }
        val handler = captured<ReportHandler> { captor -> sdk.verify { com.bugsee.library.Bugsee.setReportHandler(captor.capture()) } }
        var completions = 0

        handler.onBeforeReportCreated(mock(Report::class.java), false) { completions++ }

        assertEquals(1, completions)
    }

    @Test
    fun `an Error in the post-filter still runs the attachments provider`() = withSdk { sdk ->
        val internal = BugseeInternal()
        var providerCalls = 0
        internal.setReportFieldsPostFilter { TODO("post-filter") }
        internal.setReportAttachmentsProvider { providerCalls++; emptyList<BugseeAttachment>() }
        val handler = captured<ReportHandler> { captor ->
            sdk.verify({ com.bugsee.library.Bugsee.setReportHandler(captor.capture()) }, times(2))
        }
        var completions = 0

        handler.onAfterReportCreated(mock(Report::class.java), false) { completions++ }

        assertEquals(1, providerCalls)
        assertEquals(1, completions)
    }

    @Test
    fun `an Error in the attachments provider still completes the report`() = withSdk { sdk ->
        BugseeInternal().setReportAttachmentsProvider { TODO("provider") }
        val handler = captured<ReportHandler> { captor -> sdk.verify { com.bugsee.library.Bugsee.setReportHandler(captor.capture()) } }
        var completions = 0

        handler.onAfterReportCreated(mock(Report::class.java), false) { completions++ }

        assertEquals(1, completions)
    }

    @Test
    fun `an Error in the createReport provider does not escape`() = withSdk { sdk ->
        var calls = 0
        BugseeInternal().createReport(provider = { calls++; TODO("provider") }, onNotCreated = null)
        val listener = captured<ReportCreationListener> { captor -> sdk.verify { com.bugsee.library.Bugsee.createReport(captor.capture()) } }

        listener.onCreated(mock(Report::class.java))

        assertEquals(1, calls)
    }

    @Test
    fun `an Error in onNotCreated does not escape`() = withSdk { sdk ->
        var calls = 0
        BugseeInternal().createReport(provider = { }, onNotCreated = { calls++; TODO("onNotCreated") })
        val listener = captured<ReportCreationListener> { captor -> sdk.verify { com.bugsee.library.Bugsee.createReport(captor.capture()) } }

        listener.onCreated(null)

        assertEquals(1, calls)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `an Error in the deletion listener does not escape`() = withSdk { sdk ->
        var calls = 0
        BugseeInternal().deleteCollectedDataOnDevice { calls++; TODO("listener") }
        val callback = captured<Callback1<*>> { captor ->
            sdk.verify {
                com.bugsee.library.Bugsee.deleteCollectedDataOnDevice(eq(true), eq(true), captor.capture() as Callback1<Boolean>?)
            }
        } as Callback1<Boolean>

        callback.run(true)

        assertEquals(1, calls)
    }

    // Log.e prints the stack trace, which calls the throwable's toString. ShadowLog prints it only
    // when it has a stream.
    @Test
    fun `Logger e survives a throwable whose toString throws`() {
        val saved = ShadowLog.stream
        ShadowLog.stream = PrintStream(ByteArrayOutputStream())
        try {
            Logger.e("BugseeCallbackErrorsAndroidTest", "message", ToStringThrows())
        } finally {
            ShadowLog.stream = saved
        }

        val lines = ShadowLog.getLogsForTag("BugseeCallbackErrorsAndroidTest")
        assertEquals("message: ToStringThrows", lines.last().msg)
    }

    private class ToStringThrows : RuntimeException() {
        override fun toString(): String = throw IllegalStateException("toString")
    }
}
