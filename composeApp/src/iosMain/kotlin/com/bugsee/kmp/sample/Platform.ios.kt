package com.bugsee.kmp.sample

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val token: String = "377bb874-6ec8-4a70-8028-63e6972cedc6"
}

actual fun getPlatform(): Platform = IOSPlatform()