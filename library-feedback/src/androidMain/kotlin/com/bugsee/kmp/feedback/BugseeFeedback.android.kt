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
 * Bugsee Gradle plugin can strip that provider, so it is registered here when the SDK does not
 * have it. An extension registered while the SDK runs is launched right away. (The add-on's own
 * registerFeedbackExtension() guard looks it up under the wrong class and never matches, so it is
 * not called once registered.)
 */
internal fun nativeFeedback(): Feedback? {
    BugseeSDK.ext(Feedback::class.java)?.let { return it }
    NativeBugseeFeedback.registerFeedbackExtension()
    val feedback = BugseeSDK.ext(Feedback::class.java)
    if (feedback == null) {
        Log.e(TAG, "The Bugsee Feedback extension is not available")
    }
    return feedback
}

// The adapter of the listener set last. Callbacks are delivered to its listener when they run.
@Volatile
internal var currentListenerAdapter: MainThreadFeedbackListener? = null

// Keeps currentListenerAdapter and the add-on's listener the same when setListener is called
// from several threads: the add-on applies listeners in the order they are passed to it.
private val listenerLock = Any()

internal actual fun platformShowFeedback() {
    nativeFeedback()?.showFeedbackActivity()
}

internal actual fun platformSetFeedbackGreeting(greeting: String?) {
    nativeFeedback()?.setDefaultFeedbackGreeting(greeting)
}

internal actual fun platformSetFeedbackListener(listener: BugseeFeedbackListener?) {
    val adapter = listener?.let(::MainThreadFeedbackListener)
    synchronized(listenerLock) {
        currentListenerAdapter = adapter
        nativeFeedback()?.setOnNewFeedbackListener(adapter)
    }
}

/**
 * Android SDK 7 calls the listener on the thread that synced the conversation; iOS calls it on
 * the main thread. This moves the calls to the main thread, as on iOS, and, as iOS does, delivers
 * them to the listener that is current when they run: a call posted before the listener was
 * replaced reaches the new one, and one posted before it was removed is dropped. The add-on
 * reports each message once, so dropping it for a replaced listener would lose it.
 */
internal class MainThreadFeedbackListener(
    internal val listener: BugseeFeedbackListener,
) : FeedbackListener {
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onNewMessagesReceived(newMessages: List<String>) {
        val messages = newMessages.toList()
        post { it.onNewMessagesReceived(messages) }
    }

    override fun onNewMessageSent(message: String) {
        post { it.onNewMessageSent(message) }
    }

    private fun post(call: (BugseeFeedbackListener) -> Unit) {
        mainHandler.post {
            val listener = currentListenerAdapter?.listener ?: return@post
            // Throwable, as on iOS: an Error such as TODO()'s must not crash the main thread.
            try {
                call(listener)
            } catch (e: Throwable) {
                Log.e(TAG, "BugseeFeedbackListener threw", e)
            }
        }
    }
}
