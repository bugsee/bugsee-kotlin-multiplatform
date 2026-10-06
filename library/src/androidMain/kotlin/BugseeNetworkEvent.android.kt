package com.bugsee.kmp

import com.bugsee.library.contracts.exchange.NetworkEvent

public actual class BugseeNetworkEvent(
    internal val underlyingEvent: NetworkEvent
) {
    public actual val stage: BugseeNetworkEventStage
        get() = BugseeAndroidUtils.convertNetworkEventStage(underlyingEvent.networkEventType)

    public actual val noBodyReason: String?
        get() = underlyingEvent.bodyAbsenceReason?.value

    public actual val method: String
        get() = underlyingEvent.method.orEmpty()

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

    // 7.x stores header values as strings; non-string values are stringified on write.
    public actual var headers: Map<String, Any>?
        get() = underlyingEvent.headers
        set(value) {
            underlyingEvent.headers = value?.mapValues { it.value.toString() }
        }
}
