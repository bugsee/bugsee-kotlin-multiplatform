package com.bugsee.kmp

public enum class BugseeNetworkEventStage(private val stage: String) {

    Before("before"),
    Complete("complete"),
    Cancel("cancel"),
    Redirect("redirect"),
    Errors("error"),
    WebSocket("ws");

    public fun getStringValue(): String {
        return stage
    }

    public companion object {
        public fun fromString(stage: String?): BugseeNetworkEventStage {
            return when (stage) {
                "before" -> Before
                "complete" -> Complete
                "cancel" -> Cancel
                "redirect" -> Redirect
                "error" -> Errors
                "ws" -> WebSocket
                else -> Before
            }
        }
    }
}