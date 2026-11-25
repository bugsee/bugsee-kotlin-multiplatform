package com.bugsee.kmp

import android.content.Context
import com.bugsee.kmp.internal.BugseeInternal
import com.bugsee.library.contracts.common.Callback1
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
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

    /**
     * Launches as in a process where BugseeContextProvider did not run, and returns the options
     * and the completion callback passed to the SDK.
     */
    @Suppress("UNCHECKED_CAST")
    private fun launchWithoutKmpContext(launch: (BugseeInternal) -> Unit): Pair<Map<String, Serializable>, Callback1<Boolean>> {
        val saved = applicationContext
        applicationContext = null
        try {
            mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
                launch(BugseeInternal())

                val options = ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Serializable>>
                val callback = ArgumentCaptor.forClass(Callback1::class.java) as ArgumentCaptor<Callback1<Boolean>>
                sdk.verify { com.bugsee.library.Bugsee.launch(isNull(), eq("token"), options.capture(), callback.capture()) }
                sdk.verify({ com.bugsee.library.Bugsee.launch(any(Context::class.java), anyString(), anyMap()) }, never())
                return options.value to callback.value
            }
        } finally {
            applicationContext = saved
        }
    }

    private fun launchWarnings() =
        ShadowLog.getLogsForTag("BugseeInternal").filter { it.type == android.util.Log.WARN && "was not launched" in it.msg }

    // #58: it used to return with a debug log, so Bugsee did not start in a secondary process.
    @Test
    fun `launch without the KMP context lets the SDK find the context`() {
        val (options, _) = launchWithoutKmpContext { it.launch("token", BugseeLaunchOptions().apply { duration = 30 }) }
        assertEquals(30, options[BugseeOptions.Duration])

        val (rawOptions, _) = launchWithoutKmpContext { it.launch("token", mapOf(BugseeOptions.Duration to 45)) }
        assertEquals(45, rawOptions[BugseeOptions.Duration])
    }

    @Test
    fun `a launch without the KMP context that fails is a warning`() {
        val (_, callback) = launchWithoutKmpContext { it.launch("token", BugseeLaunchOptions()) }

        callback.run(true)
        assertEquals(0, launchWarnings().size)

        callback.run(false)
        assertEquals(1, launchWarnings().size)
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
        // The wrapper metadata comes from the registered BugseeWrapperAdapter, not an option.
        assertTrue("com.bugsee.option.\$\$WRAPPER" !in map)
        assertTrue("wrapper_info" !in map)
    }

    @Test
    fun `raw map launch ignores keys from before 0_2_0`() {
        val map = launchAndCapture { it.launch("token", mapOf("VideoEnabled" to false, "MaxRecordingTime" to 120)) }

        assertTrue("VideoEnabled" !in map)
        assertTrue("MaxRecordingTime" !in map)
        assertEquals(true, map[BugseeOptions.CaptureVideo])
        assertEquals(60, map[BugseeOptions.Duration])
        val errors = ShadowLog.getLogsForTag("BugseeLaunchOptions").map { it.msg }
        assertTrue(errors.any { "'VideoEnabled'" in it && "BugseeOptions.CaptureVideo" in it }, errors.toString())
        assertTrue(errors.any { "'MaxRecordingTime'" in it && "BugseeOptions.Duration" in it }, errors.toString())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `raw map relaunch goes through the launch options`() {
        mockStatic(com.bugsee.library.Bugsee::class.java).use { sdk ->
            BugseeInternal().relaunch(mapOf(BugseeOptions.CaptureVideoFrameRate to BugseeFrameRate.Low))

            val captor = ArgumentCaptor.forClass(Map::class.java) as ArgumentCaptor<Map<String, Serializable>>
            sdk.verify { com.bugsee.library.Bugsee.relaunch(captor.capture()) }
            val map = captor.value
            assertEquals("Low", map[BugseeOptions.CaptureVideoFrameRate]) // constant name, not the KMP enum
            assertEquals(60, map[BugseeOptions.Duration])
            assertTrue("com.bugsee.option.\$\$WRAPPER" !in map)
        }
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
