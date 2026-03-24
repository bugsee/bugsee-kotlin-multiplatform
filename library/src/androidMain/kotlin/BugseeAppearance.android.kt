package com.bugsee.kmp

import java.lang.reflect.Field

public actual class BugseeAppearance internal constructor(
    private val bugseeAppearance: com.bugsee.library.data.BugseeAppearance
) {
    private val fieldsMap = HashMap<String, Field>()

    private fun getAndCacheField(name: String): Field? {
        return try {
            val field = bugseeAppearance.javaClass.getDeclaredField(name)
            field.isAccessible = true
            fieldsMap.put(name, field)
            field
        } catch (_: Throwable) {
            null
        }
    }

    private fun getPropertyValue(name: String): Any? {
        val field = fieldsMap.get(name) ?: getAndCacheField(name)

        return try {
            field?.get(bugseeAppearance)
        } catch (_: Throwable) {
            null
        }
    }

    private fun setPropertyValue(name: String, value: Any?) {
        val field = fieldsMap.get(name) ?: getAndCacheField(name)

        try {
            field?.set(bugseeAppearance, value)
        } catch (_: Throwable) {
        }
    }


    public actual var reportActionBarColor: Int?
        get() = getPropertyValue("ReportActionBarColor") as? Int?
        set(value) {
            setPropertyValue("ReportActionBarColor", value)
        }

    public actual var reportEditTextBackgroundColor: Int?
        get() = getPropertyValue("ReportEditTextBackgroundColor") as? Int?
        set(value) {
            setPropertyValue("ReportEditTextBackgroundColor", value)
        }

    public actual var reportVersionColor: Int?
        get() = getPropertyValue("ReportVersionColor") as? Int?
        set(value) {
            setPropertyValue("ReportVersionColor", value)
        }

    public actual var reportTextColor: Int?
        get() = getPropertyValue("ReportTextColor") as? Int?
        set(value) {
            setPropertyValue("ReportTextColor", value)
        }

    public actual var reportHintColor: Int?
        get() = getPropertyValue("ReportHintColor") as? Int?
        set(value) {
            setPropertyValue("ReportHintColor", value)
        }

    public actual var reportActionBarTextColor: Int?
        get() = getPropertyValue("ReportActionBarTextColor") as? Int?
        set(value) {
            setPropertyValue("ReportActionBarTextColor", value)
        }

    public actual var reportActionBarButtonBackgroundClickedColor: Int?
        get() = getPropertyValue("ReportActionBarButtonBackgroundClickedColor") as? Int?
        set(value) {
            setPropertyValue("ReportActionBarButtonBackgroundClickedColor", value)
        }

    public actual var reportBackgroundColor: Int?
        get() = getPropertyValue("ReportBackgroundColor") as? Int?
        set(value) {
            setPropertyValue("ReportBackgroundColor", value)
        }

    public actual var reportSeverityLabelActiveColor: Int?
        get() = getPropertyValue("ReportSeverityLabelActiveColor") as? Int?
        set(value) {
            setPropertyValue("ReportSeverityLabelActiveColor", value)
        }

    public actual var reportSummaryPlaceholder: String?
        get() = getPropertyValue("ReportSummaryPlaceholder") as? String?
        set(value) {
            setPropertyValue("ReportSummaryPlaceholder", value)
        }

    public actual var reportDescriptionPlaceholder: String?
        get() = getPropertyValue("ReportDescriptionPlaceholder") as? String?
        set(value) {
            setPropertyValue("ReportDescriptionPlaceholder", value)
        }

    public actual var reportLabelsPlaceholder: String?
        get() = getPropertyValue("ReportLabelsPlaceholder") as? String?
        set(value) {
            setPropertyValue("ReportLabelsPlaceholder", value)
        }

    public actual var reportEmailPlaceholder: String?
        get() = getPropertyValue("ReportEmailPlaceholder") as? String?
        set(value) {
            setPropertyValue("ReportEmailPlaceholder", value)
        }

    public actual var feedbackActionBarColor: Int?
        get() = getPropertyValue("FeedbackActionBarColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackActionBarColor", value)
        }

    public actual var feedbackBackgroundColor: Int?
        get() = getPropertyValue("FeedbackBackgroundColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackBackgroundColor", value)
        }

    public actual var feedbackActionBarButtonBackgroundClickedColor: Int?
        get() = getPropertyValue("FeedbackActionBarButtonBackgroundClickedColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackActionBarButtonBackgroundClickedColor", value)
        }

    public actual var feedbackIncomingBubbleColor: Int?
        get() = getPropertyValue("FeedbackIncomingBubbleColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackIncomingBubbleColor", value)
        }

    public actual var feedbackOutgoingBubbleColor: Int?
        get() = getPropertyValue("FeedbackOutgoingBubbleColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackOutgoingBubbleColor", value)
        }

    public actual var feedbackIncomingTextColor: Int?
        get() = getPropertyValue("FeedbackIncomingTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackIncomingTextColor", value)
        }

    public actual var feedbackOutgoingTextColor: Int?
        get() = getPropertyValue("FeedbackOutgoingTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackOutgoingTextColor", value)
        }

    public actual var feedbackDateTextColor: Int?
        get() = getPropertyValue("FeedbackDateTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackDateTextColor", value)
        }

    public actual var feedbackTitleTextColor: Int?
        get() = getPropertyValue("FeedbackTitleTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackTitleTextColor", value)
        }

    public actual var feedbackEmailSkipTextColor: Int?
        get() = getPropertyValue("FeedbackEmailSkipTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackEmailSkipTextColor", value)
        }

    public actual var feedbackEmailSkipBackgroundClickedColor: Int?
        get() = getPropertyValue("FeedbackEmailSkipBackgroundClickedColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackEmailSkipBackgroundClickedColor", value)
        }

    public actual var feedbackEmailBackgroundColor: Int?
        get() = getPropertyValue("FeedbackEmailBackgroundColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackEmailBackgroundColor", value)
        }

    public actual var feedbackEmailContinueNotActiveColor: Int?
        get() = getPropertyValue("FeedbackEmailContinueNotActiveColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackEmailContinueNotActiveColor", value)
        }

    public actual var feedbackEmailContinueActiveColor: Int?
        get() = getPropertyValue("FeedbackEmailContinueActiveColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackEmailContinueActiveColor", value)
        }

    public actual var feedbackEmailContinueClickedColor: Int?
        get() = getPropertyValue("FeedbackEmailContinueClickedColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackEmailContinueClickedColor", value)
        }

    public actual var feedbackInputTextColor: Int?
        get() = getPropertyValue("FeedbackInputTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackInputTextColor", value)
        }

    public actual var feedbackInputTextHintColor: Int?
        get() = getPropertyValue("FeedbackInputTextHintColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackInputTextHintColor", value)
        }

    public actual var feedbackBottomDelimiterColor: Int?
        get() = getPropertyValue("FeedbackBottomDelimiterColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackBottomDelimiterColor", value)
        }

    public actual var feedbackLoadingBarBackgroundColor: Int?
        get() = getPropertyValue("FeedbackLoadingBarBackgroundColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackLoadingBarBackgroundColor", value)
        }

    public actual var feedbackLoadingTextColor: Int?
        get() = getPropertyValue("FeedbackLoadingTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackLoadingTextColor", value)
        }

    public actual var feedbackErrorTextColor: Int?
        get() = getPropertyValue("FeedbackErrorTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackErrorTextColor", value)
        }

    public actual var feedbackVersionChangedBackgroundColor: Int?
        get() = getPropertyValue("FeedbackVersionChangedBackgroundColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackVersionChangedBackgroundColor", value)
        }

    public actual var feedbackVersionChangedTextColor: Int?
        get() = getPropertyValue("FeedbackVersionChangedTextColor") as? Int?
        set(value) {
            setPropertyValue("FeedbackVersionChangedTextColor", value)
        }

    public actual var notificationTitleResId: Int?
        get() = getPropertyValue("NotificationTitleResId") as? Int?
        set(value) {
            setPropertyValue("NotificationTitleResId", value)
        }

    public actual var notificationTitle: String?
        get() = getPropertyValue("NotificationTitle") as? String?
        set(value) {
            setPropertyValue("NotificationTitle", value)
        }

    // iOS specific fields — no-op on Android
    public actual var reportCellBackgroundColor: Int?
        get() = null
        set(value) {}
    public actual var reportSendButtonColor: Int?
        get() = null
        set(value) {}
    public actual var reportCloseButtonColor: Int?
        get() = null
        set(value) {}
    public actual var reportPlaceholderColor: Int?
        get() = null
        set(value) {}
    public actual var reportNavigationBarColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackBarsColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackInputBackgroundColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackCloseButtonColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackNavigationBarColor: Int?
        get() = null
        set(value) {}
    public actual val mainBugseeColor: Int?
        get() = null
    public actual val lowBugColor: Int?
        get() = null
    public actual val mediumBugColor: Int?
        get() = null
    public actual val dotSelectorColor: Int?
        get() = null
}