package com.bugsee.kmp

import com.bugsee.kmp.internal.Logger
import com.bugsee.library.contracts.appearance.Appearance
import com.bugsee.library.contracts.appearance.NotificationAppearance
import com.bugsee.library.contracts.appearance.ReportAppearance
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public actual class BugseeAppearance internal constructor(
    private val bugseeAppearance: Appearance
) {
    private companion object {
        private const val TAG = "BugseeAppearance"

        // FeedbackAppearance lives in bugsee-android-feedback, which the library does not depend on;
        // the keys take effect only when the app adds that module.
        private fun feedback(name: String): String = "Feedback::$name"
    }

    private fun <T> call(op: String, key: String, block: () -> T?): T? = try {
        block()
    } catch (e: Throwable) {
        Logger.e(TAG, "$op '$key' failed", e)
        null
    }

    private fun color(key: String) = object : ReadWriteProperty<Any?, Int?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? =
            call("get", key) { bugseeAppearance.getColor(key) }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            call("set", key) { bugseeAppearance.setColor(key, value) }
        }
    }

    private fun string(key: String) = object : ReadWriteProperty<Any?, String?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): String? =
            call("get", key) { bugseeAppearance.getString(key) }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            call("set", key) { bugseeAppearance.setString(key, value) }
        }
    }

    // Shared theme code in commonMain sets every property, so the log line is written once per property.
    private fun iosOnly() = object : ReadWriteProperty<Any?, Int?> {
        private var logged = false

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? = null

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            if (value != null && !logged) {
                logged = true
                Logger.i(TAG, "${property.name} is iOS only and is ignored on Android")
            }
        }
    }

    // --- Report screen, Android and iOS ---

    public actual var reportSummaryPlaceholder: String? by string(ReportAppearance.SummaryPlaceholder)
    public actual var reportDescriptionPlaceholder: String? by string(ReportAppearance.DescriptionPlaceholder)
    public actual var reportLabelsPlaceholder: String? by string(ReportAppearance.LabelsPlaceholder)
    public actual var reportEmailPlaceholder: String? by string(ReportAppearance.EmailPlaceholder)
    public actual var reportVersionColor: Int? by color(ReportAppearance.VersionColor)
    public actual var reportTextColor: Int? by color(ReportAppearance.TextColor)
    public actual var reportBackgroundColor: Int? by color(ReportAppearance.BackgroundColor)

    // --- Feedback screen, Android and iOS ---

    public actual var feedbackBackgroundColor: Int? by color(feedback("BackgroundColor"))
    public actual var feedbackIncomingBubbleColor: Int? by color(feedback("IncomingBubbleColor"))
    public actual var feedbackOutgoingBubbleColor: Int? by color(feedback("OutgoingBubbleColor"))
    public actual var feedbackIncomingTextColor: Int? by color(feedback("IncomingTextColor"))
    public actual var feedbackOutgoingTextColor: Int? by color(feedback("OutgoingTextColor"))
    public actual var feedbackTitleTextColor: Int? by color(feedback("TitleTextColor"))
    public actual var feedbackEmailBackgroundColor: Int? by color(feedback("EmailBackgroundColor"))
    public actual var feedbackEmailContinueNotActiveColor: Int? by color(feedback("EmailContinueNotActiveColor"))
    public actual var feedbackEmailContinueActiveColor: Int? by color(feedback("EmailContinueActiveColor"))
    public actual var feedbackEmailSkipTextColor: Int? by color(feedback("EmailSkipTextColor"))
    public actual var feedbackInputTextColor: Int? by color(feedback("InputTextColor"))

    // --- Android only ---

    public actual var reportActionBarColor: Int? by color(ReportAppearance.ActionBarColor)
    public actual var reportEditTextBackgroundColor: Int? by color(ReportAppearance.EditTextBackgroundColor)
    public actual var reportHintColor: Int? by color(ReportAppearance.HintColor)
    public actual var reportActionBarTextColor: Int? by color(ReportAppearance.ActionBarTextColor)
    public actual var reportActionBarButtonBackgroundClickedColor: Int? by color(ReportAppearance.ActionBarButtonBackgroundClickedColor)
    public actual var reportSeverityLabelActiveColor: Int? by color(ReportAppearance.SeverityLabelActiveColor)
    public actual var feedbackActionBarColor: Int? by color(feedback("ActionBarColor"))
    public actual var feedbackActionBarButtonBackgroundClickedColor: Int? by color(feedback("ActionBarButtonBackgroundClickedColor"))
    public actual var feedbackDateTextColor: Int? by color(feedback("DateTextColor"))
    public actual var feedbackEmailSkipBackgroundClickedColor: Int? by color(feedback("EmailSkipBackgroundClickedColor"))
    public actual var feedbackEmailContinueClickedColor: Int? by color(feedback("EmailContinueClickedColor"))
    public actual var feedbackInputTextHintColor: Int? by color(feedback("InputTextHintColor"))
    public actual var feedbackBottomDelimiterColor: Int? by color(feedback("BottomDelimiterColor"))
    public actual var feedbackLoadingBarBackgroundColor: Int? by color(feedback("LoadingBarBackgroundColor"))
    public actual var feedbackLoadingTextColor: Int? by color(feedback("LoadingTextColor"))
    public actual var feedbackErrorTextColor: Int? by color(feedback("ErrorTextColor"))
    public actual var feedbackVersionChangedBackgroundColor: Int? by color(feedback("VersionChangedBackgroundColor"))
    public actual var feedbackVersionChangedTextColor: Int? by color(feedback("VersionChangedTextColor"))

    public actual var notificationTitle: String? by string(NotificationAppearance.Title)

    // 7.x has no resource-id variant of the notification title; the id is resolved to
    // a string and stored under Notification::Title.
    private var notificationTitleResIdValue: Int? = null
    private var titleResolvedFromResId: String? = null

    public actual var notificationTitleResId: Int?
        get() = notificationTitleResIdValue
        set(value) {
            notificationTitleResIdValue = value
            if (value == null) {
                // Clear only a title that came from the resource id, not one set directly.
                if (titleResolvedFromResId != null && notificationTitle == titleResolvedFromResId) {
                    notificationTitle = null
                }
                titleResolvedFromResId = null
                return
            }

            val title = try {
                applicationContext?.getString(value)
            } catch (e: Exception) {
                Logger.e(TAG, "notificationTitleResId: cannot resolve string resource $value", e)
                null
            }
            if (title != null) {
                titleResolvedFromResId = title
                notificationTitle = title
            }
        }

    // --- iOS only ---

    public actual var reportCellBackgroundColor: Int? by iosOnly()

    @Deprecated("Not applied by iOS SDK 7: the report screen uses the system Done button.")
    public actual var reportSendButtonColor: Int? by iosOnly()

    @Deprecated("Not applied by iOS SDK 7: the report screen uses the system Cancel button.")
    public actual var reportCloseButtonColor: Int? by iosOnly()

    public actual var reportPlaceholderColor: Int? by iosOnly()
    public actual var reportNavigationBarColor: Int? by iosOnly()
    public actual var feedbackBarsColor: Int? by iosOnly()
    public actual var feedbackInputBackgroundColor: Int? by iosOnly()
    public actual var feedbackCloseButtonColor: Int? by iosOnly()
    public actual var feedbackNavigationBarColor: Int? by iosOnly()
    public actual val mainBugseeColor: Int? get() = null
    public actual val lowBugColor: Int? get() = null
    public actual val mediumBugColor: Int? get() = null
    public actual val dotSelectorColor: Int? get() = null
}
