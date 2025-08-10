package com.bugsee.kmp

public actual class BugseeNetworkEvent {
    public actual val stage: BugseeNetworkEventStage
        get() = TODO("Not yet implemented")
    public actual val method: String
        get() = TODO("Not yet implemented")
    public actual val responseCode: Int
        get() = TODO("Not yet implemented")
    public actual val noBodyReason: String?
        get() = TODO("Not yet implemented")
    public actual val errorDescription: String?
        get() = TODO("Not yet implemented")
    public actual val errorShort: String?
        get() = TODO("Not yet implemented")
    public actual var url: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    public actual var body: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    public actual var headers: Map<String, Any>?
        get() = TODO("Not yet implemented")
        set(value) {}
}