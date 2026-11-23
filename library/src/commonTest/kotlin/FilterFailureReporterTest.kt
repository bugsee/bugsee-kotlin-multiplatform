package com.bugsee.kmp.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilterFailureReporterTest {

    @Test
    fun `reports only the first failure`() {
        val messages = mutableListOf<String>()
        val reporter = FilterFailureReporter(SdkFilter.Log) { messages += it }

        repeat(3) { reporter.onFailure(IllegalStateException("boom $it")) }

        assertEquals(1, messages.size)
        assertTrue("IllegalStateException" in messages.single(), messages.single())
    }

    // The message is the app's text and may quote the event, as a JSON parser's error quotes the body.
    @Test
    fun `the report gives the exception's class and not its message`() {
        val messages = mutableListOf<String>()

        FilterFailureReporter(SdkFilter.Network) { messages += it }
            .onFailure(IllegalArgumentException("""Value {"password":"hunter2" cannot be converted"""))

        assertTrue("IllegalArgumentException" in messages.single(), messages.single())
        assertTrue("hunter2" !in messages.single() && "password" !in messages.single(), messages.single())
    }

    @Test
    fun `the report names the filter and what was dropped`() {
        val messages = mutableListOf<String>()

        FilterFailureReporter(SdkFilter.Log) { messages += it }.onFailure(IllegalStateException("boom"))
        FilterFailureReporter(SdkFilter.Network) { messages += it }.onFailure(IllegalStateException("boom"))

        assertTrue("the log filter threw" in messages[0] && "the line was dropped" in messages[0], messages[0])
        assertTrue("the network filter threw" in messages[1] && "the event was dropped" in messages[1], messages[1])
    }

    @Test
    fun `an exception whose toString throws is reported by its class name`() {
        val messages = mutableListOf<String>()

        FilterFailureReporter(SdkFilter.Log) { messages += it }.onFailure(ToStringThrows())

        assertTrue("ToStringThrows" in messages.single(), messages.single())
    }

    @Test
    fun `a failing report does not escape`() {
        val reporter = FilterFailureReporter(SdkFilter.Network) { throw IllegalStateException("report failed") }

        reporter.onFailure(IllegalStateException("boom"))
        reporter.onFailure(IllegalStateException("boom again"))
    }

    private class ToStringThrows : Exception() {
        override fun toString(): String = throw IllegalStateException("toString")
    }
}
