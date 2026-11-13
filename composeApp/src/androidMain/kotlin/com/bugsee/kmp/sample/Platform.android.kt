package com.bugsee.kmp.sample

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val token: String? = SampleConfig.androidToken.ifBlank { null }
    override val tokenProperty: String = "bugsee.sample.android.token"

    // Internal Android SDK 7 keys; the 6.x "endpoint" / "debug" keys are ignored.
    override val endpointLaunchOptions: Map<String, Any> = buildMap {
        SampleConfig.endpoint.ifBlank { null }?.let { put("com.bugsee.option.\$\$ENDPOINT", it) }
        put("com.bugsee.option.\$\$DEBUG", true)
    }

    override val tempDir: String = (System.getProperty("java.io.tmpdir") ?: "/tmp").let {
        if (it.endsWith("/")) it else "$it/"
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun writeTextFile(path: String, content: String) {
    java.io.File(path).writeText(content)
}