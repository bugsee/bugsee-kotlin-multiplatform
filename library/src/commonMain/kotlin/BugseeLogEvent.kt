package com.bugsee.kmp

public expect class BugseeLogEvent {
    public var message: String
    // level is immutable for changes
    public val level: BugseeLogLevel
}
