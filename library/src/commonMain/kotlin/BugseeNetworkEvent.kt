package com.bugsee.kmp


// TODO: Refine this object and add/remove fields as needed

public expect class BugseeNetworkEvent {
//    public val id: String?
//    public val type: BugseeNetworkType
//    public val redirectedFromURL: String?
//    public val error: Map<String, Any>?
//
//    public val bugseeNetworkEventType: String
//    public val override: Boolean
//    public val timestamp: Double
//    public val urlChanged: Boolean
//    public val rURLChanged: Boolean
//    public val bodyChanged: Boolean
//    public val errorChanged: Boolean
//    public val headersChanged: Boolean


    public val stage: BugseeNetworkEventStage

    public val method: String
    public val responseCode: Int
    public val noBodyReason: String?

    public val errorDescription: String?
    public val errorShort: String?

    public var url: String?
    public var body: String?
    public var headers: Map<String, Any>?
}