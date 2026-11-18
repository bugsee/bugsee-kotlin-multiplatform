package com.bugsee.kmp.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogFilterFailureReporterTest {

    @Test
    fun `reports only the first failure`() {
        val messages = mutableListOf<String>()
        val reporter = LogFilterFailureReporter { messages += it }

        repeat(3) { reporter.onFailure(IllegalStateException("boom $it")) }

        assertEquals(1, messages.size)
        assertTrue("IllegalStateException: boom 0" in messages.single(), messages.single())
    }

    @Test
    fun `an exception whose toString throws is described by its class name`() {
        val messages = mutableListOf<String>()

        LogFilterFailureReporter { messages += it }.onFailure(ToStringThrows())

        assertTrue("ToStringThrows" in messages.single(), messages.single())
    }

    @Test
    fun `a failing report does not escape`() {
        val reporter = LogFilterFailureReporter { throw IllegalStateException("report failed") }

        reporter.onFailure(IllegalStateException("boom"))
        reporter.onFailure(IllegalStateException("boom again"))
    }

    private class ToStringThrows : Exception() {
        override fun toString(): String = throw IllegalStateException("toString")
    }
}
