package com.bugsee.kmp.sample

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val token: String = "323a506c-3756-49dd-9833-39fef912639c"
}

actual fun getPlatform(): Platform = AndroidPlatform()