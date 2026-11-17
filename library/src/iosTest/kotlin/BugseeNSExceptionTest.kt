package com.bugsee.kmp.nsexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BugseeNSExceptionTest {

    @Test
    fun `the name carries the Kotlin class`() {
        val exception = BugseeNSException(IllegalStateException("boom"))

        assertEquals("KmpManagedException: kotlin.IllegalStateException", exception.name)
    }

    @Test
    fun `a throwable without a cause keeps its message as the reason`() {
        assertEquals("boom", BugseeNSException(RuntimeException("boom")).reason)
        assertNull(BugseeNSException(RuntimeException()).reason)
    }

    @Test
    fun `each cause follows the message on its own line`() {
        val throwable = RuntimeException(
            "Outer exception",
            IllegalStateException("Inner cause", IllegalArgumentException("Root")),
        )

        assertEquals(
            "Outer exception\n" +
                "Caused by: kotlin.IllegalStateException: Inner cause\n" +
                "Caused by: kotlin.IllegalArgumentException: Root",
            BugseeNSException(throwable).reason,
        )
    }

    @Test
    fun `a cause without a message is named by its class`() {
        val throwable = RuntimeException(null, IllegalStateException())

        assertEquals("Caused by: kotlin.IllegalStateException", BugseeNSException(throwable).reason)
    }

    @Test
    fun `a long cause chain is cut`() {
        var throwable: Throwable = RuntimeException("cause 0")
        for (i in 1..20) throwable = RuntimeException("cause $i", throwable)

        val lines = BugseeNSException(throwable).reason!!.lines()

        assertEquals("cause 20", lines.first())
        assertEquals(11, lines.size) // the message and 10 causes
        assertTrue(lines.drop(1).all { it.startsWith("Caused by: kotlin.RuntimeException: cause ") })
    }

    @Test
    fun `the frames stay those of the throwable`() {
        val throwable = RuntimeException("Outer", IllegalStateException("Inner"))
        val exception = BugseeNSException(throwable)

        assertEquals(throwable.getStackTrace().toList(), exception.callStackSymbols)
        assertEquals(exception.callStackSymbols.size, exception.callStackReturnAddresses.size)
    }
}
