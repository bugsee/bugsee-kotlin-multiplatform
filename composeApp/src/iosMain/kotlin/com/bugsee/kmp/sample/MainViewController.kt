package com.bugsee.kmp.sample

import androidx.compose.ui.window.ComposeUIViewController
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeLaunchOptions
import platform.UIKit.UIViewController


fun MainViewController(): UIViewController {

    val options = BugseeLaunchOptions()
    options.captureNetwork = true
    options.captureLogs = true
    options.captureVideo = true
    options.captureViewHierarchy = true
    options.reportingTriggerByShake = true
    options.reportingUILabelsEnabled = true
    options.reportingUILabelsRequired = false
    options.setCustomOption("endpoint", getPlatform().appdevEndpoint)
    options.setCustomOption("debug", true)
    Bugsee.launch(getPlatform().token, options)

    Bugsee.setOnNewFeedbackListener { messages ->
        messages.forEach { message ->
            AppLogger.d("MainViewController", "New feedback: $message")
        }
    }

    return ComposeUIViewController {
        App()
    }
}