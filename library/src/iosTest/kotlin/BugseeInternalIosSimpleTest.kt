package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeAttachment
import com.bugsee.kmp.BugseeAttachmentsProvider
import com.bugsee.kmp.BugseeExtendedReportProvider
import com.bugsee.kmp.BugseeLaunchOptions
import com.bugsee.kmp.BugseeLifecycleEventListener
import com.bugsee.kmp.BugseeLogFilter
import com.bugsee.kmp.BugseeNetworkFilter
import com.bugsee.kmp.BugseeReportFieldsFiller
import com.bugsee.kmp.BugseeReportFieldsFilter
import com.bugsee.kmp.EventHandler
import platform.Foundation.NSDate
import platform.Foundation.NSRunLoop
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate
import platform.Foundation.timeIntervalSinceNow
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * iOS parallel of [BugseeInternalAndroidSimpleTest] — smoke-level coverage that
 * the handler-registration paths on iOS don't throw when called pre-launch.
 *
 * These tests specifically exercise the wiring of the iOS fixes:
 *   - [setNetworkEventFilter] (null + non-null)
 *   - [createReport] with a provider lambda
 *   - [deleteCollectedDataOnDevice] with an explicit listener block
 *
 * They do not drive the native SDK into a state where filters/providers are
 * actually invoked (which would require a real launch + traffic). The deep
 * behavioral test for the log filter fix lives in
 * `BugseeDelegateWrapperLogFilterTest`.
 */
class BugseeInternalIosSimpleTest {

    private lateinit var bugseeInternal: BugseeInternal

    private fun setup() {
        bugseeInternal = BugseeInternal()
    }

    @Test
    fun `network filter registration does not throw`() {
        setup()
        bugseeInternal.setNetworkEventFilter(null)
        val networkFilter: BugseeNetworkFilter = { event -> event }
        bugseeInternal.setNetworkEventFilter(networkFilter)
        // Re-registering null must also be safe (replaces block).
        bugseeInternal.setNetworkEventFilter(null)
        assertTrue(true)
    }

    @Test
    fun `log filter registration does not throw`() {
        setup()
        bugseeInternal.setLogFilter(null)
        val logFilter: BugseeLogFilter = { event -> event }
        bugseeInternal.setLogFilter(logFilter)
        bugseeInternal.setLogFilter(null)
        assertTrue(true)
    }

    @Test
    fun `lifecycle events listener registration does not throw`() {
        setup()
        bugseeInternal.setLifecycleEventsListener(null)
        val listener: BugseeLifecycleEventListener = { event ->
            assertNotNull(event)
        }
        bugseeInternal.setLifecycleEventsListener(listener)
        assertTrue(true)
    }

    @Test
    fun `report attachments provider registration does not throw`() {
        setup()
        bugseeInternal.setReportAttachmentsProvider(null)
        val provider: BugseeAttachmentsProvider = { report ->
            assertNotNull(report)
            listOf(BugseeAttachment.create("test.txt", "test content".encodeToByteArray()))
        }
        bugseeInternal.setReportAttachmentsProvider(provider)
        assertTrue(true)
    }

    @Test
    fun `report fields filter methods do not throw`() {
        setup()
        bugseeInternal.setReportFieldsPreFilter(null)
        val filler: BugseeReportFieldsFiller = { fields ->
            assertNotNull(fields)
        }
        bugseeInternal.setReportFieldsPreFilter(filler)

        bugseeInternal.setReportFieldsPostFilter(null)
        val filter: BugseeReportFieldsFilter = { fields ->
            assertNotNull(fields)
            fields
        }
        bugseeInternal.setReportFieldsPostFilter(filter)
        assertTrue(true)
    }

    @Test
    fun `deleteCollectedDataOnDevice registration does not throw`() {
        setup()
        // Null listener — previously this was passed to the bridge raw; now
        // we always install an explicit block.
        bugseeInternal.deleteCollectedDataOnDevice(null)

        val listener: EventHandler<Boolean> = { success ->
            // Just asserts the type at compile time; the SDK won't call this
            // without a launched instance.
            @Suppress("USELESS_IS_CHECK")
            assertTrue(success is Boolean)
        }
        bugseeInternal.deleteCollectedDataOnDevice(listener)
        assertTrue(true)
    }

    @Test
    fun `createReport registration does not throw`() {
        setup()
        val provider: BugseeExtendedReportProvider = { report ->
            assertNotNull(report)
        }
        bugseeInternal.createReport(provider, null)
        assertTrue(true)
    }

    @Test
    fun `isLaunched returns false before launch`() {
        setup()
        // Sanity check the test fixture: construction does not implicitly launch.
        val launched = bugseeInternal.isLaunched()
        assertTrue(!launched, "isLaunched should be false before launch()")
    }

    @Test
    fun `user identifier round trip`() {
        setup()
        // The native SDK persists the identifier: leave none behind, whatever fails.
        try {
            bugseeInternal.setUserIdentifier("user@example.com")
            assertEquals("user@example.com", bugseeInternal.getUserIdentifier())

            // iOS SDK 7 itself clears on ""; the wrapper clears before calling it.
            // Checked on the native value: getUserIdentifier() maps "" to null anyway.
            bugseeInternal.setUserIdentifier("")
            assertNull(swiftPMImport.com.bugsee.library.Bugsee.getUserIdentifier())
            assertNull(bugseeInternal.getUserIdentifier())

            bugseeInternal.setUserIdentifier("user-42")
            bugseeInternal.clearUserIdentifier()
            assertNull(swiftPMImport.com.bugsee.library.Bugsee.getUserIdentifier())

            bugseeInternal.setUserIdentifier("user-42")
            bugseeInternal.setUserIdentifier(null)
            assertNull(swiftPMImport.com.bugsee.library.Bugsee.getUserIdentifier())
        } finally {
            bugseeInternal.clearUserIdentifier()
        }
    }

    // relaunch goes through relaunchWithOptions:started: so that a started relaunch can install the
    // Kotlin crash hook; before a launch the SDK refuses it and the block gets NO.
    @Test
    fun `relaunch before launch does not throw`() {
        val bugseeInternal = BugseeInternal()

        bugseeInternal.relaunch()
        bugseeInternal.relaunch(BugseeLaunchOptions())
        bugseeInternal.relaunch(mapOf<String, Any>())
    }

    // Before a launch the SDK creates no report and calls back at once, on the calling thread. The
    // wrapper moves that call to the main queue (#62): onNotCreated is called instead of the provider,
    // on the main thread, after createReport returns. The tests never launch the SDK (a relaunch
    // before a launch is refused), so it is not launched here.
    @Test
    fun `createReport before launch calls onNotCreated on the main thread after it returns`() {
        assertTrue(NSThread.isMainThread, "the test runs on the main thread")
        var provided = 0
        var notCreated = 0
        var onMain = false

        BugseeInternal().createReport({ provided++ }, { notCreated++; onMain = NSThread.isMainThread })

        assertEquals(0, notCreated, "onNotCreated must not run before createReport returns")
        runMainLoopUntil { provided + notCreated > 0 }
        assertEquals(0, provided)
        assertEquals(1, notCreated)
        assertTrue(onMain)
    }

    @Test
    fun `createReport called off the main thread before launch calls onNotCreated on the main thread`() {
        val bugseeInternal = BugseeInternal()
        var notCreated = 0
        var onMain = false

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            bugseeInternal.createReport({}, { notCreated++; onMain = NSThread.isMainThread })
        }

        runMainLoopUntil { notCreated > 0 }
        assertEquals(1, notCreated)
        assertTrue(onMain)
    }

    private fun runMainLoopUntil(timeoutSeconds: Double = 5.0, done: () -> Boolean) {
        val deadline = NSDate.dateWithTimeIntervalSinceNow(timeoutSeconds)
        while (!done() && deadline.timeIntervalSinceNow > 0) {
            NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.05))
        }
    }
}
