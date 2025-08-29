package com.bugsee.kmp.internal

public enum class Platform(type: Int) {
    ANDROID(1),
    IOS(2)
}

public expect class PlatformInfo() {
    public companion object {
        public fun getPlatformType(): Platform
    }
}