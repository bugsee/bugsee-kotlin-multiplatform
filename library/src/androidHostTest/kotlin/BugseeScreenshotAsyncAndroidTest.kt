package com.bugsee.kmp

import android.graphics.Bitmap
import android.os.Looper
import com.bugsee.library.contracts.reporting.Report
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** [BugseeExtendedReport.setScreenshotAsync] on Android (#60). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeScreenshotAsyncAndroidTest {

    private val native: Report = mock(Report::class.java)
    private val report = BugseeExtendedReport(native)
    private var onSetCalls = 0

    @Before
    fun clearLog() {
        ShadowLog.clear()
    }

    private fun png(): ByteArray {
        val stream = ByteArrayOutputStream()
        Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /** The completion the wrapper gave the SDK for [bitmap] (any bitmap when null). */
    private fun nativeCompletion(bitmap: Bitmap? = null): Runnable {
        val completion = ArgumentCaptor.forClass(Runnable::class.java)
        if (bitmap != null) {
            verify(native, timeout(5000)).setScreenshotAsync(org.mockito.ArgumentMatchers.same(bitmap), completion.capture())
        } else {
            verify(native, timeout(5000)).setScreenshotAsync(any(Bitmap::class.java), completion.capture())
        }
        return completion.value
    }

    /** Runs the main looper until [done], as the wrapper posts some callbacks there from its thread. */
    private fun idleMainUntil(done: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5000
        while (!done() && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
    }

    @Test
    fun `a Bitmap goes to the SDK asynchronously and onSet runs from its completion`() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        report.setScreenshotAsync(bitmap) { onSetCalls++ }

        val completion = nativeCompletion(bitmap)
        verify(native, never()).setScreenshot(any(Bitmap::class.java))
        assertEquals(0, onSetCalls)
        completion.run()
        assertEquals(1, onSetCalls)
        assertSame(bitmap, report.screenshot)
    }

    @Test
    fun `bytes are decoded off the calling thread and go to the SDK asynchronously`() {
        val caller = Thread.currentThread()
        var decodedOn: Thread? = null
        org.mockito.Mockito.doAnswer { decodedOn = Thread.currentThread(); null }
            .`when`(native).setScreenshotAsync(any(Bitmap::class.java), any(Runnable::class.java))

        report.setScreenshotAsync(png()) { onSetCalls++ }

        val completion = nativeCompletion()
        assertTrue(decodedOn != null && decodedOn != caller, "the SDK was called from the wrapper's thread")
        completion.run()
        assertEquals(1, onSetCalls)
        assertTrue(report.screenshot is Bitmap)
    }

    @Test
    fun `calls are applied in the order they are made`() {
        val second = Bitmap.createBitmap(3, 3, Bitmap.Config.ARGB_8888)

        report.setScreenshotAsync(png()) {}
        report.setScreenshotAsync(second) {}

        val bitmaps = ArgumentCaptor.forClass(Bitmap::class.java)
        verify(native, timeout(5000).times(2)).setScreenshotAsync(bitmaps.capture(), any(Runnable::class.java))
        assertTrue(bitmaps.allValues[0] !== second)
        assertSame(second, bitmaps.allValues[1])
        assertSame(second, report.screenshot)
    }

    @Test
    fun `null and an unsupported value change nothing and still call onSet on the main thread`() {
        var onMain = true
        report.setScreenshotAsync(null) { onSetCalls++; onMain = onMain && Looper.myLooper() == Looper.getMainLooper() }
        report.setScreenshotAsync("not an image") { onSetCalls++; onMain = onMain && Looper.myLooper() == Looper.getMainLooper() }

        idleMainUntil { onSetCalls == 2 }

        assertEquals(2, onSetCalls)
        assertTrue(onMain)
        verify(native, never()).setScreenshotAsync(any(Bitmap::class.java), any(Runnable::class.java))
        assertNull(report.screenshot)
        assertTrue(ShadowLog.getLogsForTag("BugseeExtendedReport").any { "unsupported type String" in it.msg })
    }

    @Test
    fun `onSet is called when the SDK throws`() {
        doThrow(IllegalStateException("sdk")).`when`(native).setScreenshotAsync(any(Bitmap::class.java), any(Runnable::class.java))

        report.setScreenshotAsync(Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)) { onSetCalls++ }

        idleMainUntil { onSetCalls == 1 }
        assertEquals(1, onSetCalls)
    }

    @Test
    fun `an onSet that throws is logged`() {
        report.setScreenshotAsync(Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888)) { error("app") }

        nativeCompletion().run()

        assertTrue(ShadowLog.getLogsForTag("BugseeExtendedReport").any { "onSet threw" in it.msg })
    }
}
