package com.bugsee.kmp.sample

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val token: String = "629d2d4a-8804-4210-8951-3b3c3f83761a" // appdev
//        "323a506c-3756-49dd-9833-39fef912639c" // app


    override val appdevEndpoint: String = "https://apidev.bugsee.com"
}

actual fun getPlatform(): Platform = AndroidPlatform()