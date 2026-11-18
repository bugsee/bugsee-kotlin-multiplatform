package com.bugsee.kmp.internal

import kotlin.test.Test
import kotlin.test.assertEquals

class ThrowablesTest {

    @Test
    fun `a throwable is described by its toString`() {
        val error = IllegalStateException("boom")
        assertEquals(error.toString(), describeThrowable(error))
    }

    @Test
    fun `a throwable whose toString throws is described by its class name`() {
        assertEquals("ToStringThrows", describeThrowable(ToStringThrows()))
    }

    private class ToStringThrows : RuntimeException() {
        override fun toString(): String = throw IllegalStateException("toString")
    }
}
