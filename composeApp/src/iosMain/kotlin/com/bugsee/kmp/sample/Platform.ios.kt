package com.bugsee.kmp.sample

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val token: String = "a710ea5f-a2bc-4486-b84a-bf1bfcc79b12"
}

actual fun getPlatform(): Platform = IOSPlatform()