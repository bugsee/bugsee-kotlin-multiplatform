package com.bugsee.kmp

public expect class BugseeLogEvent {
    public var message: String
    public var level: BugseeLogLevel
}
