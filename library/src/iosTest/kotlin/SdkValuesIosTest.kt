package com.bugsee.kmp

import com.bugsee.kmp.internal.BugseeInternal
import com.bugsee.kmp.internal.ExtendedReport
import com.bugsee.kmp.internal.sdkValue
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import swiftPMImport.com.bugsee.library.BugseeNetworkEvent as NativeNetworkEvent

/**
 * The common value rule (#54) on iOS. The SDK keeps values by reference and reads them later on its
 * own threads, while Kotlin/Native would hand it the app's collection itself (#53).
 */
class SdkValuesIosTest {

    @Test
    fun `a Foundation value in a trace value is kept`() {
        val url = NSURL(string = "https://example.com")

        assertSame(url, (sdkValue(listOf(url)) as List<*>).single())
    }

    @Test
    fun `an NSDate is an ISO 8601 string in UTC to the millisecond`() {
        assertEquals("2001-01-01T00:00:00.250Z", sdkValue(NSDate(timeIntervalSinceReferenceDate = 0.2505)))
        assertEquals(listOf("2001-01-01T00:00:00Z"), BugseeExtendedReport(ExtendedReport()).run {
            setAttribute("at", listOf(NSDate(timeIntervalSinceReferenceDate = 0.0)))
            getAttribute("at")
        })
    }

    @Test
    fun `a global attribute does not take a list`() {
        val internal = BugseeInternal()
        try {
            internal.clearAttribute(KEY)
            internal.setAttribute(KEY, listOf("a"))

            assertNull(internal.getAttribute(KEY))
        } finally {
            internal.clearAttribute(KEY)
        }
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
    fun `a report attribute holding a Kotlin object holds its text`() {
        val report = BugseeExtendedReport(ExtendedReport())

        report.setAttribute("plans", listOf(BugseeFrameRate.High))

        assertEquals(listOf("High"), report.getAttribute("plans"))
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

    private companion object {
        const val KEY = "sdk_values_test"
    }
}
