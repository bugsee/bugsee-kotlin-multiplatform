package com.bugsee.kmp

import com.bugsee.library.contracts.appearance.Appearance
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.util.Log
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
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

    private fun logs() = ShadowLog.getLogsForTag("BugseeAppearance")

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
    fun `iOS-only properties are ignored and logged once per property`() {
        appearance.reportNavigationBarColor = 1
        appearance.reportNavigationBarColor = 3 // shared theme code runs again
        appearance.reportSendButtonColor = 2
        appearance.feedbackBarsColor = null // clearing is not worth a log line

        verifyNoInteractions(native)
        assertNull(appearance.reportNavigationBarColor)
        assertNull(appearance.mainBugseeColor)
        assertEquals(
            listOf(
                "reportNavigationBarColor is iOS only and is ignored on Android",
                "reportSendButtonColor is iOS only and is ignored on Android",
            ),
            logs().map { it.msg },
        )
        assertTrue(logs().all { it.type == Log.INFO })
    }

    @Test
    fun `notificationTitleResId is resolved to the title`() {
        Robolectric.setupContentProvider(BugseeContextProvider::class.java)

        appearance.notificationTitleResId = android.R.string.ok
        verify(native).setString("Notification::Title", "OK")
        assertEquals(android.R.string.ok, appearance.notificationTitleResId)

        // Clearing the id clears the title it set
        `when`(native.getString("Notification::Title")).thenReturn("OK")
        appearance.notificationTitleResId = null
        verify(native).setString("Notification::Title", null)
    }

    @Test
    fun `clearing notificationTitleResId keeps a title set directly`() {
        Robolectric.setupContentProvider(BugseeContextProvider::class.java)
        appearance.notificationTitleResId = android.R.string.ok

        `when`(native.getString("Notification::Title")).thenReturn("Custom")
        appearance.notificationTitleResId = null

        verify(native, never()).setString("Notification::Title", null)
    }

    @Test
    fun `a failing native setter is logged and not thrown`() {
        doThrow(IllegalStateException("boom")).`when`(native).setColor(anyString(), any())

        appearance.reportTextColor = 1

        assertTrue(logs().any { it.type == Log.ERROR && "set 'Report::TextColor' failed" in it.msg })
    }

    @Test
    fun `a failing native call is logged and reads as null`() {
        `when`(native.getColor(anyString())).thenThrow(IllegalStateException("boom"))

        assertNull(appearance.reportTextColor)
        assertTrue(logs().any { it.type == Log.ERROR && "get 'Report::TextColor' failed" in it.msg })
    }
}
