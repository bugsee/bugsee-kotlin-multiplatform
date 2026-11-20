package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeExtendedReport
import com.bugsee.library.Bugsee
import com.bugsee.library.contracts.reporting.Report
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Timestamp

/** What the Android actuals hand to the SDK under the common value rule (#54). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SdkValuesAndroidTest {

    @Test
    fun `setAttribute passes a Byte as an Int and drops a list`() {
        mockStatic(Bugsee::class.java).use { sdk ->
            BugseeInternal().setAttribute("count", 3.toByte())
            BugseeInternal().setAttribute("tags", listOf("a"))

            sdk.verify { Bugsee.setAttribute("count", 3) }
            sdk.verify({ Bugsee.setAttribute(eq("tags"), any()) }, never())
        }
    }

    @Test
    fun `trace passes an object that is not Serializable as its text`() {
        mockStatic(Bugsee::class.java).use { sdk ->
            BugseeInternal().trace("cart", Cart(mutableListOf("apple")))

            sdk.verify { Bugsee.trace("cart", "Cart(items=[apple])") }
        }
    }

    @Test
    fun `trace passes an array as a list a BigDecimal as it is and a Date as an ISO string`() {
        mockStatic(Bugsee::class.java).use { sdk ->
            BugseeInternal().trace("bytes", byteArrayOf(1, 2, 3))
            BugseeInternal().trace("price", BigDecimal("1.50"))
            BugseeInternal().trace("count", BigInteger("123456789012345678901234567890"))
            BugseeInternal().trace("at", Timestamp(978_307_200_250))

            sdk.verify { Bugsee.trace("bytes", arrayListOf(1, 2, 3)) }
            sdk.verify { Bugsee.trace("price", BigDecimal("1.50")) }
            sdk.verify { Bugsee.trace("count", BigInteger("123456789012345678901234567890")) }
            sdk.verify { Bugsee.trace("at", "2001-01-01T00:00:00.250Z") }
        }
    }

    @Test
    fun `event passes a copy of the params`() {
        val tags = mutableListOf("a")
        mockStatic(Bugsee::class.java).use { sdk ->
            BugseeInternal().event("checkout", mapOf("tags" to tags))
            tags += "b"

            sdk.verify { Bugsee.event("checkout", hashMapOf<String, Any>("tags" to listOf("a"))) }
        }
    }

    @Test
    fun `a report attribute is passed as a list and a value of another type is dropped`() {
        val report = mock(Report::class.java)
        val extendedReport = BugseeExtendedReport(report)

        extendedReport.setAttribute("tags", setOf("a"))
        extendedReport.setAttribute("cart", Cart(mutableListOf("apple")))

        verify(report).setAttribute("tags", arrayListOf("a"))
        verify(report, never()).setAttribute(eq("cart"), any())
    }

    private data class Cart(val items: MutableList<String>)
}
