package com.bugsee.kmp.feedback

import android.os.Looper
import com.bugsee.library.contracts.extensions.Feedback
import com.bugsee.library.contracts.lifecycle.FeedbackLifecycleEvents
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import com.bugsee.library.Bugsee as BugseeSDK

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeFeedbackAndroidTest {

    // The SDK's extension registry is static and the tests share it, so each test starts with
    // the extension unregistered, as an app whose init provider was stripped does.
    @Before
    fun unregisterExtensions() {
        val registry = Class.forName("com.bugsee.library.BugseeExtensions")
        for (name in listOf("sLoadedExtensions", "sLaunchedExtensions")) {
            val field = registry.getDeclaredField(name).apply { isAccessible = true }
            when (val value = field.get(null)) {
                is MutableMap<*, *> -> value.clear()
                is MutableCollection<*> -> value.clear()
                else -> error("BugseeExtensions.$name is ${value?.javaClass}")
            }
        }
    }

    @After
    fun removeListener() {
        BugseeFeedback.setListener(null)
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `the native extension is registered when missing and reused after`() {
        assertNull(BugseeSDK.ext(Feedback::class.java))

        val feedback = nativeFeedback()

        assertNotNull(feedback)
        assertSame(feedback, BugseeSDK.ext(Feedback::class.java))
        assertSame(feedback, nativeFeedback())
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
        BugseeFeedback.setListener(object : BugseeFeedbackListener {
            override fun onNewMessagesReceived(messages: List<String>) {
                received += messages
                threads += Thread.currentThread()
            }

            override fun onNewMessageSent(message: String) {
                received += message
                threads += Thread.currentThread()
            }
        })
        val adapter = assertNotNull(currentListenerAdapter)

        onWorkerThread {
            adapter.onNewMessagesReceived(listOf("first", "second"))
            adapter.onNewMessageSent("reply")
        }
        assertTrue(received.isEmpty(), "delivered before the main looper ran")

        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(listOf("first", "second", "reply"), received)
        assertTrue(threads.all { it === Looper.getMainLooper().thread })
    }

    @Test
    fun `callbacks posted for a removed listener are dropped`() {
        val received = mutableListOf<String>()
        BugseeFeedback.setListener(recordingListener(received))
        val removed = assertNotNull(currentListenerAdapter)

        onWorkerThread { removed.onNewMessageSent("late") }
        BugseeFeedback.setListener(null)
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(received.isEmpty())
    }

    // As on iOS, which reads the current listener when it delivers. The add-on reports each
    // message once, so a message dropped here would be lost.
    @Test
    fun `callbacks posted for a replaced listener reach the new listener`() {
        val old = mutableListOf<String>()
        val new = mutableListOf<String>()
        BugseeFeedback.setListener(recordingListener(old))
        val replaced = assertNotNull(currentListenerAdapter)

        onWorkerThread {
            replaced.onNewMessagesReceived(listOf("hi"))
            replaced.onNewMessageSent("late")
        }
        BugseeFeedback.setListener(recordingListener(new))
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(old.isEmpty())
        assertEquals(listOf("hi", "late"), new)
    }

    @Test
    fun `an error from the listener does not escape`() {
        BugseeFeedback.setListener(object : BugseeFeedbackListener {
            override fun onNewMessageSent(message: String) = TODO("listener bug")
        })
        val adapter = assertNotNull(currentListenerAdapter)

        adapter.onNewMessageSent("reply")
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `the core maps the add-on's lifecycle event names`() {
        // :library maps these names without depending on the add-on; keep them in sync.
        assertEquals("com.bugsee.lifecycle.BeforeFeedbackShown", FeedbackLifecycleEvents.BeforeFeedbackShown)
        assertEquals("com.bugsee.lifecycle.AfterFeedbackShown", FeedbackLifecycleEvents.AfterFeedbackShown)
    }

    private fun recordingListener(received: MutableList<String>) = object : BugseeFeedbackListener {
        override fun onNewMessagesReceived(messages: List<String>) {
            received += messages
        }

        override fun onNewMessageSent(message: String) {
            received += message
        }
    }

    private fun onWorkerThread(block: () -> Unit) {
        val worker = Thread(block)
        worker.start()
        worker.join()
    }
}
