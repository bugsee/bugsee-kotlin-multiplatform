package com.bugsee.kmp.feedback

import android.os.Looper
import com.bugsee.library.contracts.extensions.Feedback
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import com.bugsee.library.Bugsee as BugseeSDK

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeFeedbackAndroidTest {

    @Test
    fun `the native extension is registered on first use`() {
        val feedback = nativeFeedback()

        assertNotNull(feedback)
        assertSame(feedback, BugseeSDK.ext(Feedback::class.java))
    }

    @Test
    fun `greeting and listener calls do not throw before launch`() {
        BugseeFeedback.setGreeting("Hello from KMP")
        BugseeFeedback.setGreeting(null)
        BugseeFeedback.setListener(object : BugseeFeedbackListener {})
        BugseeFeedback.setListener(null)
        BugseeFeedback.show()
    }

    @Test
    fun `listener calls from a background thread arrive on the main thread`() {
        val received = mutableListOf<String>()
        val threads = mutableListOf<Thread>()
        val adapter = MainThreadFeedbackListener(object : BugseeFeedbackListener {
            override fun onNewMessagesReceived(messages: List<String>) {
                received += messages
                threads += Thread.currentThread()
            }

            override fun onNewMessageSent(message: String) {
                received += message
                threads += Thread.currentThread()
            }
        })

        val worker = Thread {
            adapter.onNewMessagesReceived(listOf("first", "second"))
            adapter.onNewMessageSent("reply")
        }
        worker.start()
        worker.join()
        assertTrue(received.isEmpty(), "delivered before the main looper ran")

        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(listOf("first", "second", "reply"), received)
        assertTrue(threads.all { it === Looper.getMainLooper().thread })
    }

    @Test
    fun `an exception from the listener does not escape`() {
        val adapter = MainThreadFeedbackListener(object : BugseeFeedbackListener {
            override fun onNewMessageSent(message: String) = throw IllegalStateException("listener bug")
        })

        adapter.onNewMessageSent("reply")
        shadowOf(Looper.getMainLooper()).idle()
    }
}
