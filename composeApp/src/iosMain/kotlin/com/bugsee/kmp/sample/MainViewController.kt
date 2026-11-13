package com.bugsee.kmp.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController


fun MainViewController(): UIViewController {
    launchSampleBugsee()

    return ComposeUIViewController {
        App()
    }
}
