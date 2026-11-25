package com.bugsee.kmp

import com.bugsee.kmp.internal.ExtendedReport
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate
import platform.Foundation.timeIntervalSinceNow
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGSizeMake
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** [BugseeExtendedReport.setScreenshotAsync] on iOS (#60). */
@OptIn(ExperimentalForeignApi::class)
class BugseeScreenshotAsyncIosTest {

    private fun image(): UIImage =
        UIGraphicsImageRenderer(size = CGSizeMake(2.0, 2.0)).imageWithActions { }

    private fun runMainLoopUntil(timeoutSeconds: Double = 5.0, done: () -> Boolean) {
        val deadline = NSDate.dateWithTimeIntervalSinceNow(timeoutSeconds)
        while (!done() && deadline.timeIntervalSinceNow > 0) {
            NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.05))
        }
    }

    @Test
    fun `the image is set at once and onSet runs later on the main thread`() {
        val native = ExtendedReport()
        val report = BugseeExtendedReport(native)
        val image = image()
        var calls = 0
        var onMain = false

        report.setScreenshotAsync(image) { calls++; onMain = NSThread.isMainThread }

        assertEquals(image, native.screenshot) // the same UIImage; Kotlin/Native may give it another wrapper
        assertEquals(0, calls) // not inline
        runMainLoopUntil { calls > 0 }
        assertEquals(1, calls)
        assertTrue(onMain)
    }

    @Test
    fun `an unsupported value changes nothing and onSet is still called`() {
        val native = ExtendedReport()
        val report = BugseeExtendedReport(native)
        var calls = 0

        report.setScreenshotAsync("not an image") { calls++ }

        runMainLoopUntil { calls > 0 }
        assertEquals(1, calls)
        assertEquals(null, native.screenshot)
    }
}
