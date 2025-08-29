package com.bugsee.kmp.internal

public actual class PlatformInfo {
    public actual companion object {
        public actual fun getPlatformType(): Platform {
            return Platform.IOS
        }
    }
}