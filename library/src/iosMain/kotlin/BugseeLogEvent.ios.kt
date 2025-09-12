package com.bugsee.kmp

public actual class BugseeLogEvent (internal val impl: cocoapods.Bugsee.BugseeLogEvent) {
    public actual var message: String
        get() = impl.text ?: ""
        set(value) {
            impl.text = value
        }
    public actual val level: BugseeLogLevel = BugseeIOSUtils.convertLogLevel(impl.level)
}