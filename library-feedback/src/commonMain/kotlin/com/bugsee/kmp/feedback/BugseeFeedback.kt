package com.bugsee.kmp.feedback

/**
 * The in-app feedback chat between the app's user and your team on the Bugsee dashboard.
 *
 * It wraps the Feedback add-on of the Bugsee SDKs 7 (`com.bugsee:bugsee-android-feedback` on
 * Android, the `github.com/bugsee/feedback-spm` Swift package on iOS), which this artifact brings
 * in. Launch Bugsee first with `Bugsee.launch`: the Android SDK does not open the feedback screen
 * before that.
 */
public object BugseeFeedback {

    /** Opens the feedback screen over the app. */
    public fun show() {
        platformShowFeedback()
    }

    /**
     * Sets the greeting the feedback screen shows at the start of the conversation; `null`
     * removes a greeting set earlier. Android keeps it across app restarts, iOS only until the
     * app exits, so set it at every start.
     */
    public fun setGreeting(greeting: String?) {
        platformSetFeedbackGreeting(greeting)
    }

    /**
     * Sets the listener for the conversation's messages, replacing the previous one; `null`
     * removes it. Its methods are called on the main thread.
     */
    public fun setListener(listener: BugseeFeedbackListener?) {
        platformSetFeedbackListener(listener)
    }
}

/** Receives the messages of the feedback conversation. Override the callbacks you need. */
public interface BugseeFeedbackListener {

    /** New messages from your team arrived. */
    public fun onNewMessagesReceived(messages: List<String>) {}

    /** The user sent [message] from the feedback screen. */
    public fun onNewMessageSent(message: String) {}
}

internal expect fun platformShowFeedback()

internal expect fun platformSetFeedbackGreeting(greeting: String?)

internal expect fun platformSetFeedbackListener(listener: BugseeFeedbackListener?)
