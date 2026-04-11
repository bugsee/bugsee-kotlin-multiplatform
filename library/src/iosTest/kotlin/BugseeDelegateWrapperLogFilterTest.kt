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

    private fun makeNativeLog(text: String = "hello"): cocoapods.Bugsee.BugseeLogEvent {
        val event = cocoapods.Bugsee.BugseeLogEvent()
        event.text = text
        return event
    }

    @Test
    fun `no filter registered - completion receives original log unchanged`() {
        val (_, wrapper) = newWrapper()
        val native = makeNativeLog("abc")
        var captured: cocoapods.Bugsee.BugseeLogEvent? = null
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
        var captured: cocoapods.Bugsee.BugseeLogEvent? = null
        wrapper.bugseeFilterLog(native) { event -> captured = event }

        assertNotNull(captured)
        assertEquals("mutated", captured?.text, "In-place mutation should propagate")
    }

    @Test
    fun `filter returns null - completion receives null to drop log`() {
        val (internal, wrapper) = newWrapper()
        internal.logFilterHandler = { _ -> null }

        val native = makeNativeLog("drop me")
        var captured: cocoapods.Bugsee.BugseeLogEvent? = native // sentinel != null
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
        var captured: cocoapods.Bugsee.BugseeLogEvent? = null
        wrapper.bugseeFilterLog(native) { event -> captured = event }

        assertSame(replacementNative, captured, "Completion should receive the replacement's native impl")
        assertEquals("replacement", captured?.text)
    }

    @Test
    fun `filter throws - completion falls through to original log`() {
        val (internal, wrapper) = newWrapper()
        internal.logFilterHandler = { _ -> throw RuntimeException("boom") }

        val native = makeNativeLog("keep")
        var captured: cocoapods.Bugsee.BugseeLogEvent? = null
        wrapper.bugseeFilterLog(native) { event -> captured = event }

        assertSame(native, captured, "On exception, completion must receive the original log")
        assertEquals("keep", captured?.text)
    }
}
