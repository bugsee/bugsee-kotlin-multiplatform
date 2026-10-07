package com.bugsee.kmp

import android.content.Context
import com.bugsee.kmp.internal.BugseeInternal
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mockStatic
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.Serializable
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeLaunchAndroidTest {

    @Suppress("UNCHECKED_CAST")
    private fun launchAndCapture(launch: (BugseeInternal) -> Unit): Map<String, Serializable> {
        // Supplies the application context that launch() needs
        Robolectric.setupContentProvider(BugseeContextProvider::class.java)

        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            launch(BugseeInternal())

            val captor = ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Serializable>>
            sdk.verify { com.bugsee.library.Bugsee.launch(any(Context::class.java), eq("token"), captor.capture()) }
            return captor.value
        }
    }

    @Test
    fun `raw map launch goes through the launch options`() {
        val map = launchAndCapture {
            it.launch(
                "token",
                mapOf(
                    BugseeOptions.Duration to 30,
                    BugseeOptions.CaptureLogs to "yes",
                    "custom" to 1,
                )
            )
        }

        assertEquals(30, map[BugseeOptions.Duration])
        assertEquals(true, map[BugseeOptions.CaptureLogs]) // invalid value ignored, default sent
        assertEquals(150, map[BugseeOptions.MaxDataSize]) // Android default
        assertEquals(1, map["custom"])
        assertTrue("wrapper_info" in map)
    }

    @Test
    fun `typed launch options are sent with Android enum names`() {
        val options = BugseeLaunchOptions().apply {
            captureVideoFrameRate = BugseeFrameRate.Low
            captureAVPlayer = true // iOS only
        }

        val map = launchAndCapture { it.launch("token", options) }

        assertEquals("Low", map[BugseeOptions.CaptureVideoFrameRate])
        assertTrue(BugseeOptions.CaptureAVPlayer !in map)
    }
}
