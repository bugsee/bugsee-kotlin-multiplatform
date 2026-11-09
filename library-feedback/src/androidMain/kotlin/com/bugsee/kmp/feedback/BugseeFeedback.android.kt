package com.bugsee.kmp.feedback

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bugsee.library.contracts.extensions.Feedback
import com.bugsee.library.contracts.feedback.FeedbackListener
import com.bugsee.library.Bugsee as BugseeSDK
import com.bugsee.library.BugseeFeedback as NativeBugseeFeedback

private const val TAG = "BugseeFeedback"

/**
 * The Feedback extension of Android SDK 7. Its init provider registers it at app start, but the
 * Bugsee Gradle plugin can strip that provider, so it is registered here as well. Registration
 * is idempotent, and an extension registered while the SDK runs is launched right away.
 */
internal fun nativeFeedback(): Feedback? {
    NativeBugseeFeedback.registerFeedbackExtension()
    val feedback = BugseeSDK.ext(Feedback::class.java)
    if (feedback == null) {
        Log.e(TAG, "The Bugsee Feedback extension is not available")
    }
    return feedback
}

internal actual fun platformShowFeedback() {
    nativeFeedback()?.showFeedbackActivity()
}

internal actual fun platformSetFeedbackGreeting(greeting: String?) {
    nativeFeedback()?.setDefaultFeedbackGreeting(greeting)
}

internal actual fun platformSetFeedbackListener(listener: BugseeFeedbackListener?) {
    nativeFeedback()?.setOnNewFeedbackListener(listener?.let(::MainThreadFeedbackListener))
}

/**
 * Android SDK 7 calls the listener on the thread that synced the conversation; iOS calls it on
 * the main thread. This moves the calls to the main thread, as on iOS.
 */
internal class MainThreadFeedbackListener(
    private val listener: BugseeFeedbackListener,
) : FeedbackListener {
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onNewMessagesReceived(newMessages: List<String>) {
        val messages = newMessages.toList()
        mainHandler.post { deliver { listener.onNewMessagesReceived(messages) } }
    }

    override fun onNewMessageSent(message: String) {
        mainHandler.post { deliver { listener.onNewMessageSent(message) } }
    }

    private inline fun deliver(call: () -> Unit) {
        try {
            call()
        } catch (e: Exception) {
            Log.e(TAG, "BugseeFeedbackListener threw", e)
        }
    }
}
