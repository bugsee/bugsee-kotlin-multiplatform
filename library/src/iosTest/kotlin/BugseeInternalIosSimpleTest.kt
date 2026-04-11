package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeAttachment
import com.bugsee.kmp.BugseeAttachmentsProvider
import com.bugsee.kmp.BugseeExtendedReportProvider
import com.bugsee.kmp.BugseeLifecycleEventListener
import com.bugsee.kmp.BugseeLogFilter
import com.bugsee.kmp.BugseeNetworkFilter
import com.bugsee.kmp.BugseeReportFieldsFiller
import com.bugsee.kmp.BugseeReportFieldsFilter
import com.bugsee.kmp.EventHandler
import kotlin.test.Test
import kotlin.test.assertNotNull
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
        bugseeInternal.createReport(provider)
        assertTrue(true)
    }

    @Test
    fun `isLaunched returns false before launch`() {
        setup()
        // Sanity check the test fixture: construction does not implicitly launch.
        val launched = bugseeInternal.isLaunched()
        assertTrue(!launched, "isLaunched should be false before launch()")
    }
}
