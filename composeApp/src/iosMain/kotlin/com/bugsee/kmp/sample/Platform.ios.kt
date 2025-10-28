package com.bugsee.kmp.sample

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val token: String = "54d447f4-7058-4ad5-99da-96345330f40f" // appdev

    //"a710ea5f-a2bc-4486-b84a-bf1bfcc79b12" // app

    override val appdevEndpoint: String = "https://apidev.bugsee.com/v2"
}

actual fun getPlatform(): Platform = IOSPlatform()