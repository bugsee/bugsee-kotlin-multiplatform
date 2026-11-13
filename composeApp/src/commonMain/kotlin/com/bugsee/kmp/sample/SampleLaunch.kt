package com.bugsee.kmp.sample

import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeLaunchOptions
import com.bugsee.kmp.feedback.BugseeFeedback
import com.bugsee.kmp.feedback.BugseeFeedbackListener

private const val TAG = "SampleLaunch"

/** Shown and logged when this platform has no app token, so the sample does not launch Bugsee. */
const val BUGSEE_NOT_CONFIGURED: String =
    "Bugsee is not launched: set bugsee.sample.android.token and bugsee.sample.ios.token in " +
        "local.properties (see CLAUDE.md) and rebuild."

/**
 * The options the sample launches Bugsee with. Launch and Relaunch in the menu pass them again:
 * `relaunch()` without options restarts both SDKs with their defaults, which would drop the
 * endpoint of [Platform.endpointLaunchOptions].
 */
fun sampleLaunchOptions(): BugseeLaunchOptions = BugseeLaunchOptions().apply {
    captureNetwork = true
    captureLogs = true
    captureVideo = true
    captureViewHierarchy = true
    reportingTriggerByShake = true
    reportingUILabelsEnabled = true
    reportingUILabelsRequired = false
    // Chosen on the Crash Signals screen; on Android it takes effect with bugsee-android-ndk only.
    captureCrashSignals = SampleSettings.crashSignals
    getPlatform().endpointLaunchOptions.forEach { (key, value) -> setCustomOption(key, value) }
}

/**
 * Launches Bugsee when the app starts (the Android Application, the iOS view controller) and
 * sets the feedback listener. The filters, listeners and providers that are on are set first, so
 * the crash report of the previous run, built during this launch, gets them too.
 */
fun launchSampleBugsee() {
    val token = getPlatform().token
    if (token == null) {
        AppLogger.e(TAG, BUGSEE_NOT_CONFIGURED)
        return
    }

    installSavedHandlers()

    val options = sampleLaunchOptions()
    launchedCrashSignals = options.captureCrashSignals
    Bugsee.launch(token, options)

    BugseeFeedback.setListener(object : BugseeFeedbackListener {
        override fun onNewMessagesReceived(messages: List<String>) {
            messages.forEach { message ->
                AppLogger.d(TAG, "New feedback: $message")
            }
        }

        override fun onNewMessageSent(message: String) {
            AppLogger.d(TAG, "Feedback sent: $message")
        }
    })
}
