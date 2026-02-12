package com.bugsee.kmp

public expect class BugseeAppearance {
    //Common
    public var reportSummaryPlaceholder: String?
    public var reportDescriptionPlaceholder: String?
    public var reportLabelsPlaceholder: String?
    public var reportEmailPlaceholder: String?

    public var reportVersionColor: Int?
    public var reportTextColor: Int?
    public var reportBackgroundColor: Int?
    public var feedbackBackgroundColor: Int?
    /**
     *  Incoming message bubble background color
     */
    public var feedbackIncomingBubbleColor: Int?
    /**
     *  Outgoing message bubble background color
     */
    public var feedbackOutgoingBubbleColor: Int?
    public var feedbackIncomingTextColor: Int?
    public var feedbackOutgoingTextColor: Int?
    public var feedbackTitleTextColor: Int?
    /**
     *  Ask for email popup background color
     */
    public var feedbackEmailBackgroundColor: Int?
    /**
     *  Ask for email continue not active button background color
     */
    public var feedbackEmailContinueNotActiveColor: Int?
    /**
     *  Ask for email continue button background color
     */
    public var feedbackEmailContinueActiveColor: Int?
    public var feedbackInputTextColor: Int?


    // Android
    public var reportActionBarColor: Int?
    public var reportEditTextBackgroundColor: Int?
    public var reportHintColor: Int?
    public var reportActionBarTextColor: Int?
    public var reportActionBarButtonBackgroundClickedColor: Int?
    public var reportSeverityLabelActiveColor: Int?
    public var feedbackActionBarColor: Int?
    public var feedbackActionBarButtonBackgroundClickedColor: Int?
    public var feedbackDateTextColor: Int?
    public var feedbackEmailSkipTextColor: Int?
    public var feedbackEmailSkipBackgroundClickedColor: Int?
    public var feedbackEmailContinueClickedColor: Int?
    public var feedbackInputTextHintColor: Int?
    public var feedbackBottomDelimiterColor: Int?
    public var feedbackLoadingBarBackgroundColor: Int?
    public var feedbackLoadingTextColor: Int?
    public var feedbackErrorTextColor: Int?
    public var feedbackVersionChangedBackgroundColor: Int?
    public var feedbackVersionChangedTextColor: Int?
    public var notificationTitleResId: Int?
    public var notificationTitle: String?


    // iOS
    /**
     *  UITableView cells background color
     */
    public var reportCellBackgroundColor: Int?
    public var reportSendButtonColor: Int?
    public var reportCloseButtonColor: Int?
    public var reportPlaceholderColor: Int?
    public var reportNavigationBarColor: Int?
    /**
     *  Navigation bar and bottom bar color
     */
    public var feedbackBarsColor: Int?
    public var feedbackInputBackgroundColor: Int?
    public var feedbackCloseButtonColor: Int?
    public var feedbackNavigationBarColor: Int?
    public val mainBugseeColor: Int?
    public val lowBugColor: Int?
    public val mediumBugColor: Int?
    public val dotSelectorColor: Int?
}