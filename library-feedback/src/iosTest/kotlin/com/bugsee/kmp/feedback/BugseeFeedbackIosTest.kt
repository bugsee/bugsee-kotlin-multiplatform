package com.bugsee.kmp.feedback

import platform.darwin.NSObject
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import swiftPMImport.com.bugsee.library.Bugsee as BugseeSDK
import swiftPMImport.com.bugsee.library.feedback.BugseeFeedback as NativeBugseeFeedback

class BugseeFeedbackIosTest {

    @AfterTest
    fun removeListener() {
        BugseeFeedback.setListener(null)
    }

    @Test
    fun `the native extension is registered`() {
        val feedback = nativeFeedback()

        val registered = assertNotNull(BugseeSDK.ext(NativeBugseeFeedback))
        // Two Kotlin references to one Objective-C object are not ===; compare the objects.
        assertTrue((registered as NSObject).isEqual(feedback))
    }

    @Test
    fun `greeting calls do not throw before launch`() {
        BugseeFeedback.setGreeting("Hello from KMP")
        BugseeFeedback.setGreeting(null)
    }

    @Test
    fun `the listener adapter is kept while the listener is set`() {
        val listener = object : BugseeFeedbackListener {}

        BugseeFeedback.setListener(listener)
        assertNotNull(listenerAdapter)

        BugseeFeedback.setListener(null)
        assertNull(listenerAdapter)
    }

    @Test
    fun `the listener adapter forwards the messages`() {
        val received = mutableListOf<String>()
        val adapter = FeedbackListenerAdapter(object : BugseeFeedbackListener {
            override fun onNewMessagesReceived(messages: List<String>) {
                received += messages
            }

            override fun onNewMessageSent(message: String) {
                received += message
            }
        })

        adapter.onNewMessagesReceived(listOf("first", 42, "second"))
        adapter.onNewMessageSent("reply")

        assertEquals(listOf("first", "second", "reply"), received)
    }

    @Test
    fun `an exception from the listener does not escape`() {
        val adapter = FeedbackListenerAdapter(object : BugseeFeedbackListener {
            override fun onNewMessageSent(message: String) = throw IllegalStateException("listener bug")
        })

        adapter.onNewMessageSent("reply")
    }
}
