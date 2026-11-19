package com.bugsee.kmp.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/** One rule on both platforms for the values the wrapper hands to the SDKs (#54). */
class SdkValuesTest {

    @Test
    fun `a global attribute or a custom option takes strings booleans and numbers`() {
        assertEquals("text", attributeValue("a", "text"))
        assertEquals(true, attributeValue("a", true))
        assertEquals(42, attributeValue("a", 42))
        assertEquals(42L, attributeValue("a", 42L))
        assertEquals(1.5f, attributeValue("a", 1.5f))
        assertEquals(2.5, attributeValue("a", 2.5))
        assertEquals("text", customOptionValue("a", "text"))
        assertEquals(42, customOptionValue("a", 42))
    }

    @Test
    fun `small and unsigned numbers are passed as Int or Long`() {
        assertEquals(3, attributeValue("a", 3.toByte()))
        assertEquals(3, attributeValue("a", 3.toShort()))
        assertEquals(3, attributeValue("a", 3.toUByte()))
        assertEquals(3, attributeValue("a", 3.toUShort()))
        assertEquals(3L, attributeValue("a", 3u))
        assertEquals(3L, attributeValue("a", 3uL))
        assertNull(attributeValue("a", ULong.MAX_VALUE))
    }

    @Test
    fun `a global attribute or a custom option of another type is dropped`() {
        val values = listOf<Any>(listOf("a"), setOf("a"), mapOf("a" to 1), Plan.Pro, Cart(mutableListOf()), byteArrayOf(1), 'A', Any())
        for (value in values) {
            assertNull(attributeValue("a", value), "attribute $value")
            assertNull(customOptionValue("a", value), "custom option $value")
        }
    }

    @Test
    fun `a trace value keeps collections as copies and a set as a list`() {
        val numbers = mutableListOf(1, 2)
        val names = linkedSetOf("a", "b")
        val map = mutableMapOf<String, Any>("numbers" to numbers, "names" to names)
        val list = mutableListOf<Any?>(map, "text", null)

        val copy = traceValue(list)
        numbers += 3
        names += "c"
        map["added"] = "x"
        list += "added"

        assertEquals(listOf(mapOf("numbers" to listOf(1, 2), "names" to listOf("a", "b")), "text", null), copy)
    }

    @Test
    fun `in a trace value another type and a map key that is not a string become their text`() {
        val cart = Cart(mutableListOf("apple"))

        val copy = traceValue(mapOf("cart" to cart, "plan" to Plan.Pro, "grade" to 'A', "codes" to mapOf(1 to "one", null to "none")))
        cart.items += "pear"

        assertEquals(mapOf("cart" to "Cart(items=[apple])", "plan" to "Pro", "grade" to "A", "codes" to mapOf("1" to "one", "null" to "none")), copy)
    }

    @Test
    fun `a toString that throws is replaced by the class name`() {
        assertEquals(listOf("ToStringThrows"), traceValue(listOf(ToStringThrows())))
    }

    @Test
    fun `event params keep their order`() {
        val params = linkedMapOf<String, Any>("z" to 1, "a" to 2, "m" to 3)

        assertEquals(listOf("z", "a", "m"), eventParams(params).keys.toList())
    }

    @Test
    fun `a report attribute keeps collections and drops a value that is or holds another type`() {
        assertEquals(
            mapOf("tags" to listOf("a", null), "count" to 2),
            reportAttributeValue("a", mapOf("tags" to linkedSetOf("a", null), "count" to 2.toByte())),
        )
        assertNull(reportAttributeValue("a", Plan.Pro))
        assertNull(reportAttributeValue("a", listOf("a", Any())))
        assertNull(reportAttributeValue("a", mapOf("nested" to mapOf(1 to "one"))))
    }

    @Test
    fun `copied headers keep other values as they are`() {
        val other = Any()
        val cookies = mutableListOf("a=1")

        val copy = copiedValue(mapOf("Set-Cookie" to cookies, "X-Other" to other)) as Map<*, *>
        cookies += "b=2"

        assertEquals(listOf("a=1"), copy["Set-Cookie"])
        assertSame(other, copy["X-Other"])
    }

    private enum class Plan { Pro }

    private data class Cart(val items: MutableList<String>)

    private class ToStringThrows {
        override fun toString(): String = throw IllegalStateException("toString")
    }
}
