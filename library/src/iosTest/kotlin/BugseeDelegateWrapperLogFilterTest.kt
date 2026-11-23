package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Behavioral tests for [BugseeDelegateWrapper.bugseeFilterLog].
 *
 * These tests cover the "honor filter return value + support drop" fix:
 *   - filter returning null must drop the log (completion handler receives nil)
 *   - filter returning a (possibly mutated) event must keep it
 *   - filter returning a different wrapper instance must use that wrapper's native impl
 *   - a throwing filter must fall through to the original log without crashing
 *
 * [BugseeDelegateWrapper] is `internal` and [BugseeLogEvent.impl] is also
 * `internal`, both of which are visible from this test source set.
 */
class BugseeDelegateWrapperLogFilterTest {

    private fun newWrapper(): Pair<BugseeInternal, BugseeDelegateWrapper> {
        val internal = BugseeInternal()
        val wrapper = BugseeDelegateWrapper(internal)
        return internal to wrapper
    }

    private fun makeNativeLog(text: String = "hello"): swiftPMImport.com.bugsee.library.BugseeLogEvent {
        val event = swiftPMImport.com.bugsee.library.BugseeLogEvent()
        event.text = text
        return event
    }

    @Test
    fun `no filter registered - completion receives original log unchanged`() {
        val (_, wrapper) = newWrapper()
        val native = makeNativeLog("abc")
        var captured: swiftPMImport.com.bugsee.library.BugseeLogEvent? = null
        var called = false

        wrapper.bugseeFilterLog(native) { event ->
            captured = event
            called = true
        }

        assertTrue(called, "Completion handler must always be invoked")
        assertSame(native, captured, "Completion should receive the same native log")
        assertEquals("abc", captured?.text)
    }

    @Test
    fun `filter mutates message - completion receives log with new text`() {
        val (internal, wrapper) = newWrapper()
        internal.logFilterHandler = { event ->
            event?.apply { message = "mutated" }
        }

        val native = makeNativeLog("original")
        var captured: swiftPMImport.com.bugsee.library.BugseeLogEvent? = null
        wrapper.bugseeFilterLog(native) { event -> captured = event }

        assertNotNull(captured)
        assertEquals("mutated", captured?.text, "In-place mutation should propagate")
    }

    @Test
    fun `filter returns null - completion receives null to drop log`() {
        val (internal, wrapper) = newWrapper()
        internal.logFilterHandler = { _ -> null }

        val native = makeNativeLog("drop me")
        var captured: swiftPMImport.com.bugsee.library.BugseeLogEvent? = native // sentinel != null
        var called = false
        wrapper.bugseeFilterLog(native) { event ->
            captured = event
            called = true
        }

        assertTrue(called, "Completion handler must still be invoked when dropping")
        assertNull(captured, "null return from filter must drop the log")
    }

    @Test
    fun `filter returns different event instance - completion receives its impl`() {
        val (internal, wrapper) = newWrapper()
        val replacementNative = makeNativeLog("replacement")
        val replacement = BugseeLogEvent(replacementNative)
        internal.logFilterHandler = { _ -> replacement }

        val native = makeNativeLog("original")
        var captured: swiftPMImport.com.bugsee.library.BugseeLogEvent? = null
        wrapper.bugseeFilterLog(native) { event -> captured = event }

        assertSame(replacementNative, captured, "Completion should receive the replacement's native impl")
        assertEquals("replacement", captured?.text)
    }

    // The SDK drops a line whose filter fails, and so does the wrapper (#56).
    @Test
    fun `filter throws - completion receives null to drop log`() {
        val (internal, wrapper) = newWrapper()
        internal.logFilterHandler = { event ->
            event?.message = "half-redacted"
            throw RuntimeException("boom")
        }

        var called = false
        var captured: swiftPMImport.com.bugsee.library.BugseeLogEvent? = makeNativeLog("sentinel")
        wrapper.bugseeFilterLog(makeNativeLog("secret")) { event ->
            called = true
            captured = event
        }

        assertTrue(called, "Completion handler must still be invoked when the filter throws")
        assertNull(captured, "On exception, completion must receive null")
    }

    // A failure logged to the console from here would come back to the same filter (#51).
    @Test
    fun `filter throws on every line - each line is dropped and the failure is reported once`() {
        val (internal, wrapper) = newWrapper()
        val reports = mutableListOf<String>()
        internal.reportFilterFailureTo = { reports += it }
        internal.setLogFilter { _ -> throw RuntimeException("boom") }

        repeat(3) { i ->
            var captured: swiftPMImport.com.bugsee.library.BugseeLogEvent? = makeNativeLog("sentinel")
            wrapper.bugseeFilterLog(makeNativeLog("line $i")) { event -> captured = event }
            assertNull(captured, "Line $i must be dropped")
        }

        assertEquals(1, reports.size)
        assertTrue("boom" in reports.single(), reports.single())
    }

    @Test
    fun `a new filter reports its own first failure`() {
        val (internal, wrapper) = newWrapper()
        val reports = mutableListOf<String>()
        internal.reportFilterFailureTo = { reports += it }

        internal.setLogFilter { _ -> throw RuntimeException("first") }
        repeat(2) { wrapper.bugseeFilterLog(makeNativeLog()) { } }
        internal.setLogFilter { _ -> throw RuntimeException("second") }
        repeat(2) { wrapper.bugseeFilterLog(makeNativeLog()) { } }

        assertEquals(2, reports.size)
        assertTrue("first" in reports[0], reports[0])
        assertTrue("second" in reports[1], reports[1])
    }

    // The filter and its reporter are replaced together.
    @Test
    fun `a filter replaced while it runs reports through its own reporter`() {
        val (internal, wrapper) = newWrapper()
        val reports = mutableListOf<String>()
        internal.reportFilterFailureTo = { reports += it }
        internal.setLogFilter { _ ->
            internal.setLogFilter { _ -> throw RuntimeException("second") }
            throw RuntimeException("first")
        }

        repeat(2) { wrapper.bugseeFilterLog(makeNativeLog()) { } }

        assertEquals(2, reports.size)
        assertTrue("first" in reports[0], reports[0])
        assertTrue("second" in reports[1], reports[1])
    }

    @Test
    fun `a throwing completion handler is not reported as a filter failure`() {
        val (internal, wrapper) = newWrapper()
        val reports = mutableListOf<String>()
        internal.reportFilterFailureTo = { reports += it }
        internal.setLogFilter { event ->
            if (event?.message == "bad") throw RuntimeException("boom")
            event
        }

        wrapper.bugseeFilterLog(makeNativeLog("good")) { throw IllegalStateException("completion") }
        assertTrue(reports.isEmpty(), reports.toString())

        wrapper.bugseeFilterLog(makeNativeLog("bad")) { }
        assertEquals(1, reports.size)
        assertTrue("boom" in reports.single(), reports.single())
    }
}
