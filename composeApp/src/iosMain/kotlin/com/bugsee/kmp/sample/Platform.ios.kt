package com.bugsee.kmp.sample

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val token: String? = SampleConfig.iosToken.ifBlank { null }
    override val tokenProperty: String = "bugsee.sample.ios.token"

    // The internal iOS SDK 7 key, as on Android; the SDK appends /v2 to the API base. iOS SDK 7.1.0
    // takes no launch option for its debug logging: it ignores the older "debug" key.
    override val endpointLaunchOptions: Map<String, Any> = buildMap {
        SampleConfig.endpoint.ifBlank { null }?.let { put("com.bugsee.option.\$\$ENDPOINT", it) }
    }

    override val tempDir: String = NSTemporaryDirectory()
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun writeTextFile(path: String, content: String) {
    NSString.create(string = content).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
}
