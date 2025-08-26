package com.bugsee.kmp.sample

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val token: String = "431f6313-07b5-47e5-b283-adb8982f867a"
}

actual fun getPlatform(): Platform = AndroidPlatform()