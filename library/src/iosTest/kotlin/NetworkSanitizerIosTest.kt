package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import swiftPMImport.com.bugsee.library.BugseeNetworkEvent as NativeNetworkEvent

/** [Bugsee.defaultNetworkSanitizer] runs the iOS SDK's own sanitizer on the event (#55). */
class NetworkSanitizerIosTest {

    @Test
    fun `defaultNetworkSanitizer redacts the event in place`() {
        val event = BugseeNetworkEvent(NativeNetworkEvent())
        event.url = "https://example.com/api?token=abc&page=2"
        event.headers = mapOf("Authorization" to "Bearer abc", "Cookie" to "sid=abc", "Content-Type" to "application/json")
        // One key: the SDK writes the JSON back with its keys in another order.
        event.body = """{"password":"abc"}"""

        Bugsee.defaultNetworkSanitizer.sanitize(event)

        assertEquals("https://example.com/api?token=%3Credacted%3E&page=2", event.url)
        assertEquals(
            mapOf("Authorization" to "<redacted>", "Cookie" to "<redacted>", "Content-Type" to "application/json"),
            event.headers,
        )
        assertEquals("""{"password":"<redacted>"}""", event.body)
    }

    @Test
    fun `defaultNetworkSanitizer is one instance`() {
        assertSame(Bugsee.defaultNetworkSanitizer, Bugsee.defaultNetworkSanitizer)
    }
}
