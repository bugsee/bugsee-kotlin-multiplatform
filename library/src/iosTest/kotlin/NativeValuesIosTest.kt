package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import com.bugsee.kmp.internal.ExtendedReport
import com.bugsee.kmp.internal.snapshotForCapture
import com.bugsee.kmp.internal.snapshotForNative
import platform.Foundation.NSDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import swiftPMImport.com.bugsee.library.BugseeNetworkEvent as NativeNetworkEvent

/**
 * The iOS SDK keeps values by reference and reads them later on its own threads, while
 * Kotlin/Native passes a Kotlin collection to it as a live view. An app that changes a traced
 * list afterwards terminated the app (#53).
 */
class NativeValuesIosTest {

    @Test
    fun `nested collections are copied`() {
        val numbers = mutableListOf(1, 2)
        val names = mutableSetOf("a")
        val map = mutableMapOf<String, Any>("numbers" to numbers, "names" to names)
        val list = mutableListOf<Any?>(map, "text", null)

        val copy = snapshotForNative(list)
        numbers += 3
        names += "b"
        map["added"] = true
        list += "added"

        assertEquals(listOf(mapOf("numbers" to listOf(1, 2), "names" to setOf("a")), "text", null), copy)
    }

    @Test
    fun `strings numbers booleans Foundation values and by default other objects are kept`() {
        val date = NSDate()
        val other = Any()

        assertEquals("text", snapshotForNative("text"))
        assertEquals(42L, snapshotForNative(42L))
        assertEquals(true, snapshotForNative(true))
        assertSame(date, snapshotForNative(date))
        assertSame(other, snapshotForNative(other))
    }

    @Test
    fun `a trace value or event param that is not a collection is taken as its text`() {
        val cart = Cart(mutableListOf("apple"))
        val date = NSDate()

        val copy = snapshotForCapture(mapOf("cart" to cart, "count" to 1, "at" to date))
        cart.items += "pear"

        assertEquals(mapOf("cart" to "Cart(items=[apple])", "count" to 1, "at" to date), copy)
        assertSame(date, (copy as Map<*, *>)["at"])
    }

    @Test
    fun `a toString that throws is replaced by the class name`() {
        assertEquals(listOf("ToStringThrows"), snapshotForCapture(listOf(ToStringThrows())))
    }

    @Test
    fun `a report attribute keeps the value that was set`() {
        val report = BugseeExtendedReport(ExtendedReport())
        val labels = mutableListOf("a")

        report.setAttribute("labels", labels)
        labels += "b"

        assertEquals(listOf("a"), report.getAttribute("labels"))
    }

    @Test
    fun `network headers keep the value that was set`() {
        val event = BugseeNetworkEvent(NativeNetworkEvent())
        val cookies = mutableListOf("a=1")
        val headers = mutableMapOf<String, Any>("Set-Cookie" to cookies)

        event.headers = headers
        cookies += "b=2"
        headers["X-Added"] = "1"

        assertEquals(mapOf("Set-Cookie" to listOf("a=1")), event.headers)
    }

    // The SDK keeps a global attribute in NSUserDefaults, which stores a copy: the wrapper does not copy it.
    @Test
    fun `a global attribute keeps the value that was set`() {
        val internal = BugseeInternal()
        val labels = mutableListOf("a")
        try {
            internal.setAttribute("native_values_test", labels)
            labels += "b"

            assertEquals(listOf("a"), internal.getAttribute("native_values_test"))
        } finally {
            internal.clearAttribute("native_values_test")
        }
    }

    private data class Cart(val items: MutableList<String>)

    private class ToStringThrows {
        override fun toString(): String = throw IllegalStateException("toString")
    }
}
