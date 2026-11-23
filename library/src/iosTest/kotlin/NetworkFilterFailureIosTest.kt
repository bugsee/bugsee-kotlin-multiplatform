package com.bugsee.kmp.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import swiftPMImport.com.bugsee.library.BugseeNetworkEvent as NativeNetworkEvent

/** What the iOS network filter answers the SDK: a filter that throws drops the event (#56). */
class NetworkFilterFailureIosTest {

    private val reports = mutableListOf<String>()
    private val failures = FilterFailureReporter(SdkFilter.Network) { reports += it }

    private fun nativeEvent(url: String = "https://example.com/?token=secret") =
        NativeNetworkEvent().apply { this.url = url }

    @Test
    fun `the event the filter returns is passed on`() {
        val native = nativeEvent()

        val decision = networkFilterDecision({ event -> event?.apply { url = "https://example.com/" } }, failures, native)

        assertSame(native, decision)
        assertEquals("https://example.com/", decision?.url)
    }

    @Test
    fun `a filter that returns null drops the event`() {
        assertNull(networkFilterDecision({ null }, failures, nativeEvent()))
        assertTrue(reports.isEmpty(), reports.toString())
    }

    @Test
    fun `a filter that throws drops the event and reports its first failure once`() {
        val filter: (com.bugsee.kmp.BugseeNetworkEvent?) -> com.bugsee.kmp.BugseeNetworkEvent? = { event ->
            event?.headers = mapOf("Authorization" to "<redacted>")
            throw IllegalStateException("boom")
        }

        repeat(3) { assertNull(networkFilterDecision(filter, failures, nativeEvent())) }

        assertEquals(1, reports.size)
        assertTrue("the network filter threw" in reports.single() && "boom" in reports.single(), reports.single())
    }

    @Test
    fun `no event is not passed to the filter`() {
        var calls = 0

        assertNull(networkFilterDecision({ calls++; it }, failures, null))
        assertEquals(0, calls)
    }
}
