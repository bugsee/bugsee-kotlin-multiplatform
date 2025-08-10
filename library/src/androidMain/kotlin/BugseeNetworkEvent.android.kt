package com.bugsee.kmp

public actual class BugseeNetworkEvent(
    internal val underlyingEvent: com.bugsee.library.network.data.BugseeNetworkEvent
) {
    public actual val stage: BugseeNetworkEventStage
        get() = BugseeAndroidUtils.convertNetworkEventStage(underlyingEvent.eventType)

    public actual val noBodyReason: String?
        get() = underlyingEvent.noBodyReason?.toString()

    public actual val method: String
        get() = underlyingEvent.method

    public actual val responseCode: Int
        get() = underlyingEvent.responseCode

    public actual val errorDescription: String?
        get() = underlyingEvent.errorDescription

    public actual val errorShort: String?
        get() = underlyingEvent.errorShortMessage

    public actual var url: String?
        get() = underlyingEvent.url
        set(value) {
            underlyingEvent.url = value
        }

    public actual var body: String?
        get() = underlyingEvent.body
        set(value) {
            underlyingEvent.body = value
        }

    public actual var headers: Map<String, Any>?
        get() = underlyingEvent.headers
        set(value) {
            underlyingEvent.headers = value
        }
}