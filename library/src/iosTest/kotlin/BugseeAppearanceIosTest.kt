package com.bugsee.kmp

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private typealias BugseeSDK = cocoapods.Bugsee.Bugsee

class BugseeAppearanceIosTest {

    private val appearance = BugseeAppearance()

    @AfterTest
    fun reset() {
        appearance.reportTextColor = null
        appearance.feedbackEmailSkipTextColor = null
        appearance.reportSummaryPlaceholder = null
    }

    @Test
    fun `colors round-trip as ARGB through BugseeTheme`() {
        appearance.reportTextColor = 0x80FF0000.toInt()

        assertEquals(0x80FF0000.toInt(), appearance.reportTextColor)
        assertNotNull(BugseeSDK.appearance().reportTextColor)
    }

    @Test
    fun `feedbackEmailSkipTextColor maps to feedbackEmailSkipColor`() {
        appearance.feedbackEmailSkipTextColor = 0xFF00FF00.toInt()

        assertEquals(0xFF00FF00.toInt(), appearance.feedbackEmailSkipTextColor)
        val native = BugseeSDK.appearance().feedbackEmailSkipColor
        assertNotNull(native)
        assertEquals(0xFF00FF00.toInt(), BugseeAppearance().feedbackEmailSkipTextColor)
    }

    @Test
    fun `an unset placeholder reads as null`() {
        appearance.reportSummaryPlaceholder = "What happened?"
        assertEquals("What happened?", appearance.reportSummaryPlaceholder)

        appearance.reportSummaryPlaceholder = null
        assertNull(appearance.reportSummaryPlaceholder)
    }

    @Test
    fun `Android-only properties read as null on iOS`() {
        appearance.reportActionBarColor = 0xFF000000.toInt()
        appearance.notificationTitle = "Report a bug"

        assertNull(appearance.reportActionBarColor)
        assertNull(appearance.notificationTitle)
    }

    @Test
    fun `read-only theme colors have SDK defaults`() {
        assertNotNull(appearance.mainBugseeColor)
    }
}
