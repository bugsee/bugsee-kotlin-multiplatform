package com.bugsee.kmp

import com.bugsee.library.contracts.exchange.NetworkEvent
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertSame

/** [Bugsee.defaultNetworkSanitizer] runs the Android SDK's own sanitizer on the event (#55). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NetworkSanitizerAndroidTest {

    @Test
    fun `defaultNetworkSanitizer redacts the event in place`() {
        val native: NetworkEvent = TestNetworkEvent(
            url = "https://user:secret@example.com/api?token=abc&page=2",
            headers = mapOf("Authorization" to "Bearer abc", "Cookie" to "sid=abc", "Accept" to "application/json"),
            body = """{"password":"abc","name":"n"}""",
            errorDescription = "GET https://example.com/api?token=abc failed",
        )

        Bugsee.defaultNetworkSanitizer.sanitize(BugseeNetworkEvent(native))

        assertEquals("https://example.com/api?token=%3Credacted%3E&page=2", native.url)
        assertEquals(
            mapOf("Authorization" to "<redacted>", "Cookie" to "<redacted>", "Accept" to "application/json"),
            native.headers,
        )
        assertEquals("""{"password":"<redacted>","name":"n"}""", native.body)
        assertEquals("GET https://example.com/api?token=%3Credacted%3E failed", native.errorDescription)
    }

    @Test
    fun `defaultNetworkSanitizer is one instance`() {
        assertSame(Bugsee.defaultNetworkSanitizer, Bugsee.defaultNetworkSanitizer)
    }

    private class TestNetworkEvent(
        private var url: String?,
        private var headers: Map<String, String>?,
        private var body: String?,
        private var errorDescription: String?,
    ) : NetworkEvent {
        private var errorShortMessage: String? = null
        private var responseCode = 0
        private var statusText: String? = null

        override fun getId() = "1"
        override fun getMechanism() = "test"
        override fun getTimestamp() = 0L
        override fun getMethod() = "GET"
        override fun getSize() = 0L
        override fun getBodyAbsenceReason(): NetworkEvent.NetworkBodyAbsenceReason? = null
        override fun getNetworkEventType() = NetworkEvent.NetworkEventStage.RequestStarted
        override fun getWebSocketEventType(): NetworkEvent.WebSocketEventType? = null

        override fun getUrl() = url
        override fun setUrl(url: String?) { this.url = url }
        override fun getHeaders() = headers
        override fun setHeaders(headers: Map<String, String>?) { this.headers = headers }
        override fun getBody() = body
        override fun setBody(body: String?) { this.body = body }
        override fun getErrorShortMessage() = errorShortMessage
        override fun setErrorShortMessage(message: String?) { errorShortMessage = message }
        override fun getErrorDescription() = errorDescription
        override fun setErrorDescription(description: String?) { errorDescription = description }
        override fun getResponseCode() = responseCode
        override fun setResponseCode(responseCode: Int) { this.responseCode = responseCode }
        override fun getStatusText() = statusText
        override fun setStatusText(statusText: String?) { this.statusText = statusText }
    }
}
