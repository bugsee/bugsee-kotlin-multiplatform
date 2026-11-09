package com.bugsee.kmp.feedback

import platform.Foundation.NSLog
import platform.darwin.NSObject
import swiftPMImport.com.bugsee.library.feedback.BugseeFeedbackListenerProtocol
import kotlin.concurrent.Volatile
import swiftPMImport.com.bugsee.library.feedback.BugseeFeedback as NativeBugseeFeedback

/**
 * The Feedback extension of iOS SDK 7. The package's BugseeFeedbackAutoRegister registers it
 * when it is loaded; registering again is idempotent, and an extension registered after launch
 * is launched right away.
 */
internal fun nativeFeedback(): NativeBugseeFeedback {
    NativeBugseeFeedback.register()
    return NativeBugseeFeedback.shared
}

// The add-on holds its listener weakly, so the adapter is kept alive here.
@Volatile
internal var listenerAdapter: FeedbackListenerAdapter? = null

internal actual fun platformShowFeedback() {
    nativeFeedback().showFeedbackUI()
}

internal actual fun platformSetFeedbackGreeting(greeting: String?) {
    nativeFeedback().setGreeting(greeting)
}

internal actual fun platformSetFeedbackListener(listener: BugseeFeedbackListener?) {
    val adapter = listener?.let(::FeedbackListenerAdapter)
    listenerAdapter = adapter
    nativeFeedback().setListener(adapter)
}

/** iOS SDK 7 already calls the listener on the main thread. */
internal class FeedbackListenerAdapter(
    private val listener: BugseeFeedbackListener,
) : NSObject(), BugseeFeedbackListenerProtocol {

    override fun onNewMessagesReceived(messages: List<*>) {
        deliver { listener.onNewMessagesReceived(messages.filterIsInstance<String>()) }
    }

    override fun onNewMessageSent(message: String) {
        deliver { listener.onNewMessageSent(message) }
    }

    // An exception must not cross the Objective-C boundary.
    private inline fun deliver(call: () -> Unit) {
        try {
            call()
        } catch (e: Throwable) {
            // A Kotlin String is not an Objective-C object in a variadic call: no %@ arguments.
            NSLog("ERROR: [BugseeFeedback] BugseeFeedbackListener threw: $e".replace("%", "%%"))
        }
    }
}
