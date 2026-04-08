package com.bugsee.kmp.sample

import androidx.compose.ui.window.ComposeUIViewController
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeAttachment
import com.bugsee.kmp.BugseeLaunchOptions
import com.bugsee.kmp.sample.composeapp.generated.resources.Res
import io.ktor.utils.io.core.toByteArray

fun MainViewController() = ComposeUIViewController {

    val options = BugseeLaunchOptions()
    options.monitorNetwork = true
    options.captureLogs = true
    options.videoEnabled = true
    options.viewHierarchyEnabled = true
    options.shakeToReport = true
    options.reportLabelsEnabled = true
    options.reportLabelsRequired = false
    options.setCustomOption("endpoint", getPlatform().appdevEndpoint)
    options.setCustomOption("debug", true)
    Bugsee.launch(getPlatform().token, options)

    Bugsee.setOnNewFeedbackListener { messages ->
        messages.forEach { message ->
            AppLogger.d("MainViewController", "New feedback: $message")
        }
    }

    App()
}