package com.bugsee.kmp

/**
 * Colors and texts of the Bugsee UI, available as [Bugsee.appearance].
 *
 * Colors are ARGB [Int]s (`0xAARRGGBB`). Each property names the platforms it applies to; on the
 * other platform, setting it is ignored (logged once per property) and reading it returns `null`.
 *
 * Reading a property returns the native SDK's current value: on Android `null` until it is set; on
 * iOS the SDK default until it is set, or `null` for the placeholders.
 *
 * The feedback properties take effect only in apps that use the `bugsee-kotlin-multiplatform-feedback`
 * artifact, which brings the feedback screens.
 *
 * Some properties are declared by a native SDK but not applied by its current UI; their KDoc says so.
 */
public expect class BugseeAppearance {

    // --- Report screen, Android and iOS ---

    /** Placeholder of the summary field on the report screen. Android and iOS. */
    public var reportSummaryPlaceholder: String?

    /** Placeholder of the description field on the report screen. Android and iOS. */
    public var reportDescriptionPlaceholder: String?

    /** Placeholder of the labels field on the report screen. Android and iOS. */
    public var reportLabelsPlaceholder: String?

    /** Placeholder of the e-mail field on the report screen. Android and iOS. */
    public var reportEmailPlaceholder: String?

    /** Text color of the app version label on the report screen. Android and iOS. */
    public var reportVersionColor: Int?

    /** Text color on the report screen. Android and iOS. */
    public var reportTextColor: Int?

    /** Background color of the report screen. Android and iOS. */
    public var reportBackgroundColor: Int?

    // --- Feedback screen, Android and iOS ---

    /** Background color of the feedback screen. Android and iOS. */
    public var feedbackBackgroundColor: Int?

    /**
     * Background color of incoming message bubbles. Android and iOS.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackIncomingBubbleColor: Int?

    /**
     * Background color of outgoing message bubbles. Android and iOS.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackOutgoingBubbleColor: Int?

    /**
     * Text color of incoming messages. Android and iOS.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackIncomingTextColor: Int?

    /**
     * Text color of outgoing messages. Android and iOS.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackOutgoingTextColor: Int?

    /**
     * Title text color of the feedback screen. Android and iOS.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackTitleTextColor: Int?

    /** Background color of the feedback e-mail screen. Android and iOS. */
    public var feedbackEmailBackgroundColor: Int?

    /**
     * Continue button background on the feedback e-mail screen while the e-mail is not valid. Android and iOS.
     */
    public var feedbackEmailContinueNotActiveColor: Int?

    /**
     * Continue button background on the feedback e-mail screen once the e-mail is valid. Android and iOS.
     */
    public var feedbackEmailContinueActiveColor: Int?

    /**
     * Skip button text color on the feedback e-mail screen. Android and iOS (`feedbackEmailSkipColor`).
     */
    public var feedbackEmailSkipTextColor: Int?

    /**
     * Text color of the message input. Android and iOS.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackInputTextColor: Int?

    // --- Android only ---

    /** Action bar color of the report screen. Android only. */
    public var reportActionBarColor: Int?

    /** Background color of the report input fields. Android only. */
    public var reportEditTextBackgroundColor: Int?

    /** Hint text color of the report input fields. Android only. */
    public var reportHintColor: Int?

    /** Action bar text color of the report screen. Android only. */
    public var reportActionBarTextColor: Int?

    /**
     * Background of a pressed action bar button on the report screen. Android only.
     * Android SDK 7.5 does not apply it.
     */
    public var reportActionBarButtonBackgroundClickedColor: Int?

    /** Color of the selected severity label on the report screen. Android only. */
    public var reportSeverityLabelActiveColor: Int?

    /** Action bar color of the feedback screen. Android only. */
    public var feedbackActionBarColor: Int?

    /**
     * Background of a pressed action bar button on the feedback screen. Android only.
     * Android SDK 7.5 does not apply it.
     */
    public var feedbackActionBarButtonBackgroundClickedColor: Int?

    /** Text color of message dates. Android only. */
    public var feedbackDateTextColor: Int?

    /**
     * Background of the pressed Skip button on the feedback e-mail screen. Android only.
     * Android SDK 7.5 does not apply it.
     */
    public var feedbackEmailSkipBackgroundClickedColor: Int?

    /**
     * Background of the pressed Continue button on the feedback e-mail screen. Android only.
     * Android SDK 7.5 does not apply it.
     */
    public var feedbackEmailContinueClickedColor: Int?

    /** Hint text color of the message input. Android only. */
    public var feedbackInputTextHintColor: Int?

    /** Color of the line above the message input. Android only. */
    public var feedbackBottomDelimiterColor: Int?

    /**
     * Background color of the feedback loading bar. Android only.
     * Android SDK 7.5 does not apply it.
     */
    public var feedbackLoadingBarBackgroundColor: Int?

    /** Text color of the feedback loading bar. Android only. Android SDK 7.5 does not apply it. */
    public var feedbackLoadingTextColor: Int?

    /** Text color of feedback errors. Android only. Android SDK 7.5 does not apply it. */
    public var feedbackErrorTextColor: Int?

    /**
     * Background color of the "app version changed" notice in the feedback thread. Android only.
     */
    public var feedbackVersionChangedBackgroundColor: Int?

    /** Text color of the "app version changed" notice in the feedback thread. Android only. */
    public var feedbackVersionChangedTextColor: Int?

    /**
     * String resource for the title of the reporting notification. Android only.
     *
     * Resolved to a string when assigned and stored as [notificationTitle].
     */
    public var notificationTitleResId: Int?

    /** Title of the reporting notification. Android only. */
    public var notificationTitle: String?

    // --- iOS only ---

    /** Background color of the report table cells. iOS only. */
    public var reportCellBackgroundColor: Int?

    /** Not applied by iOS SDK 7: the report screen uses the system Done button. iOS only. */
    @Deprecated("Not applied by iOS SDK 7: the report screen uses the system Done button.")
    public var reportSendButtonColor: Int?

    /** Not applied by iOS SDK 7: the report screen uses the system Cancel button. iOS only. */
    @Deprecated("Not applied by iOS SDK 7: the report screen uses the system Cancel button.")
    public var reportCloseButtonColor: Int?

    /** Placeholder text color of the report input fields. iOS only. */
    public var reportPlaceholderColor: Int?

    /** Navigation bar color of the report screen. iOS only. */
    public var reportNavigationBarColor: Int?

    /**
     * Navigation bar and bottom bar color of the feedback screen. iOS only.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackBarsColor: Int?

    /**
     * Background color of the message input. iOS only.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackInputBackgroundColor: Int?

    /**
     * Close button color of the feedback screen. iOS only.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackCloseButtonColor: Int?

    /**
     * Navigation bar color of the feedback screen. iOS only.
     * The iOS SDK 7.0 feedback screens do not apply it.
     */
    public var feedbackNavigationBarColor: Int?

    /**
     * Main Bugsee color: the logo and the high, critical and blocker severity marks. Read-only. iOS only.
     */
    public val mainBugseeColor: Int?

    /** Color of the low severity mark. Read-only. iOS only. */
    public val lowBugColor: Int?

    /** Color of the medium severity mark. Read-only. iOS only. */
    public val mediumBugColor: Int?

    /** Selection color of the screenshot drawing tools. Read-only. iOS only. */
    public val dotSelectorColor: Int?
}
