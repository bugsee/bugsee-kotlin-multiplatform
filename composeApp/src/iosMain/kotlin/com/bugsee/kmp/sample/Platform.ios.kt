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

    // The iOS SDK maps these keys to its internal endpoint and debug options. Its endpoint is the
    // API base plus /v2.
    override val endpointLaunchOptions: Map<String, Any> = buildMap {
        SampleConfig.endpoint.ifBlank { null }?.let { put("endpoint", "$it/v2") }
        put("debug", true)
    }

    override val tempDir: String = NSTemporaryDirectory()
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun writeTextFile(path: String, content: String) {
    NSString.create(string = content).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
}
