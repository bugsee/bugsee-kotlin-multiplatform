package com.bugsee.kmp

public actual enum class BugseeNetworkType {
    BugseeNetwork, BugseeWebSocket, BugseeUDPSocket
}

public actual class BugseeNetworkEvent(
    public actual val id: String?,
    public actual val type: BugseeNetworkType,
    public actual val url: String?,
    public actual val redirectedFromURL: String?,
    public actual val body: ByteArray?,
    public actual val error: Map<String, Any>?,
    public actual val headers: Map<String, Any>?,
    public actual val method: String,
    public actual val noBodyReason: String?,
    public actual val dataSize: Long,
    public actual val bugseeNetworkEventType: String,
    public actual val responseCode: Long,
    public actual val override: Boolean,
    public actual val timestamp: Double,
    public actual val urlChanged: Boolean,
    public actual val rURLChanged: Boolean,
    public actual val bodyChanged: Boolean,
    public actual val errorChanged: Boolean,
    public actual val headersChanged: Boolean
) {
    public actual companion object {
        public actual fun create(
            id: String,
            method: String?,
            networkType: BugseeNetworkType,
            eventType: String?,
            url: String?,
            redirectedUrl: String?,
            body: ByteArray?,
            error: Map<String, Any>?,
            headers: Map<String, Any>?,
            noBodyReason: String?,
            dataSize: Long,
            responseCode: Long
        ): BugseeNetworkEvent {
            return BugseeNetworkEvent(
                id = id,
                type = networkType,
                url = url,
                redirectedFromURL = redirectedUrl,
                body = body,
                error = error,
                headers = headers,
                method = method ?: "",
                noBodyReason = noBodyReason,
                dataSize = dataSize,
                bugseeNetworkEventType = eventType ?: "",
                responseCode = responseCode,
                override = false,
                timestamp = System.currentTimeMillis() / 1000.0,
                urlChanged = false,
                rURLChanged = false,
                bodyChanged = false,
                errorChanged = false,
                headersChanged = false
            )
        }
    }
}
