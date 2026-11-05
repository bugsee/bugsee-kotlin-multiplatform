package com.bugsee.kmp

import com.bugsee.library.contracts.appearance.Appearance
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BugseeAppearanceAndroidTest {

    private val native: Appearance = mock(Appearance::class.java)
    private val appearance = BugseeAppearance(native)

    @Before
    fun clearLog() {
        ShadowLog.clear()
    }

    @Test
    fun `colors are set and read under the SDK 7 keys`() {
        appearance.reportTextColor = 0x11223344
        appearance.reportActionBarColor = 0x55667788
        verify(native).setColor("Report::TextColor", 0x11223344)
        verify(native).setColor("Report::ActionBarColor", 0x55667788)

        `when`(native.getColor("Report::TextColor")).thenReturn(0x11223344)
        assertEquals(0x11223344, appearance.reportTextColor)
    }

    @Test
    fun `feedback colors use the Feedback keys`() {
        appearance.feedbackEmailSkipTextColor = 1
        appearance.feedbackIncomingBubbleColor = 2

        verify(native).setColor("Feedback::EmailSkipTextColor", 1)
        verify(native).setColor("Feedback::IncomingBubbleColor", 2)
    }

    @Test
    fun `placeholders and the notification title are strings`() {
        appearance.reportSummaryPlaceholder = "What happened?"
        appearance.notificationTitle = "Report a bug"

        verify(native).setString("Report::SummaryPlaceholder", "What happened?")
        verify(native).setString("Notification::Title", "Report a bug")
    }

    @Test
    fun `null clears a value`() {
        appearance.reportTextColor = null
        verify(native).setColor("Report::TextColor", null)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `iOS-only properties are ignored with an error in the log`() {
        appearance.reportNavigationBarColor = 1
        appearance.reportSendButtonColor = 2
        appearance.feedbackBarsColor = null // clearing is not worth a log line

        verifyNoInteractions(native)
        assertNull(appearance.reportNavigationBarColor)
        assertNull(appearance.mainBugseeColor)
        val errors = ShadowLog.getLogsForTag("BugseeAppearance").map { it.msg }
        assertEquals(
            listOf(
                "reportNavigationBarColor is iOS only and is ignored on Android",
                "reportSendButtonColor is iOS only and is ignored on Android",
            ),
            errors,
        )
    }

    @Test
    fun `a failing native call is logged and reads as null`() {
        `when`(native.getColor(anyString())).thenThrow(IllegalStateException("boom"))

        assertNull(appearance.reportTextColor)
        assertTrue(ShadowLog.getLogsForTag("BugseeAppearance").any { "'Report::TextColor' failed" in it.msg })
    }
}
