package com.bugsee.kmp.sample

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToFile
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val token: String = "54d447f4-7058-4ad5-99da-96345330f40f" // appdev

    //"a710ea5f-a2bc-4486-b84a-bf1bfcc79b12" // app

    override val appdevEndpoint: String = "https://apidev.bugsee.com/v2"

    override val tempDir: String = NSTemporaryDirectory()
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalForeignApi::class)
actual fun writeTextFile(path: String, content: String) {
    (content as NSString).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
}