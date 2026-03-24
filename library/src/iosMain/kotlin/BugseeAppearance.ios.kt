package com.bugsee.kmp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreGraphics.CGFloatVar
import platform.UIKit.UIColor

private typealias BugseeSDK = cocoapods.Bugsee.Bugsee

@OptIn(ExperimentalForeignApi::class)
private fun colorToArgbInt(color: UIColor?): Int? {
    if (color == null) return null
    return memScoped {
        val r = alloc<CGFloatVar>()
        val g = alloc<CGFloatVar>()
        val b = alloc<CGFloatVar>()
        val a = alloc<CGFloatVar>()
        if (!color.getRed(r.ptr, green = g.ptr, blue = b.ptr, alpha = a.ptr)) return null
        val ai = (a.value * 255.0).toInt().coerceIn(0, 255)
        val ri = (r.value * 255.0).toInt().coerceIn(0, 255)
        val gi = (g.value * 255.0).toInt().coerceIn(0, 255)
        val bi = (b.value * 255.0).toInt().coerceIn(0, 255)
        (ai shl 24) or (ri shl 16) or (gi shl 8) or bi
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun argbIntToUIColor(value: Int): UIColor {
    val a = ((value ushr 24) and 0xFF) / 255.0
    val r = ((value ushr 16) and 0xFF) / 255.0
    val g = ((value ushr 8) and 0xFF) / 255.0
    val b = (value and 0xFF) / 255.0
    return UIColor(red = r, green = g, blue = b, alpha = a)
}

public actual class BugseeAppearance {
    public actual var reportSummaryPlaceholder: String?
        get() = BugseeSDK.appearance().reportSummaryPlaceholder ?: ""
        set(value) {
            BugseeSDK.appearance().reportSummaryPlaceholder = value
        }
    public actual var reportDescriptionPlaceholder: String?
        get() = BugseeSDK.appearance().reportDescriptionPlaceholder ?: ""
        set(value) {
            BugseeSDK.appearance().reportDescriptionPlaceholder = value
        }
    public actual var reportLabelsPlaceholder: String?
        get() = BugseeSDK.appearance().reportLabelsPlaceholder ?: ""
        set(value) {
            BugseeSDK.appearance().reportLabelsPlaceholder = value
        }
    public actual var reportEmailPlaceholder: String?
        get() = BugseeSDK.appearance().reportEmailPlaceholder ?: ""
        set(value) {
            BugseeSDK.appearance().reportEmailPlaceholder = value
        }

    public actual var reportVersionColor: Int?
        get() = BugseeSDK.appearance().reportVersionColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().reportVersionColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var reportTextColor: Int?
        get() = BugseeSDK.appearance().reportTextColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().reportTextColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var reportBackgroundColor: Int?
        get() = BugseeSDK.appearance().reportBackgroundColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().reportBackgroundColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackBackgroundColor: Int?
        get() = BugseeSDK.appearance().feedbackBackgroundColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackBackgroundColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackIncomingBubbleColor: Int?
        get() = BugseeSDK.appearance().feedbackIncomingBubbleColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackIncomingBubbleColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackOutgoingBubbleColor: Int?
        get() = BugseeSDK.appearance().feedbackOutgoingBubbleColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackOutgoingBubbleColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackIncomingTextColor: Int?
        get() = BugseeSDK.appearance().feedbackIncomingTextColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackIncomingTextColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackOutgoingTextColor: Int?
        get() = BugseeSDK.appearance().feedbackOutgoingTextColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackOutgoingTextColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackTitleTextColor: Int?
        get() = BugseeSDK.appearance().feedbackTitleTextColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackTitleTextColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackEmailBackgroundColor: Int?
        get() = BugseeSDK.appearance().feedbackEmailBackgroundColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackEmailBackgroundColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackEmailContinueNotActiveColor: Int?
        get() = BugseeSDK.appearance().feedbackEmailContinueNotActiveColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackEmailContinueNotActiveColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackEmailContinueActiveColor: Int?
        get() = BugseeSDK.appearance().feedbackEmailContinueActiveColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackEmailContinueActiveColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackInputTextColor: Int?
        get() = BugseeSDK.appearance().feedbackInputTextColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackInputTextColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var reportCellBackgroundColor: Int?
        get() = BugseeSDK.appearance().reportCellBackgroundColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().reportCellBackgroundColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var reportSendButtonColor: Int?
        get() = BugseeSDK.appearance().reportSendButtonColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().reportSendButtonColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var reportCloseButtonColor: Int?
        get() = BugseeSDK.appearance().reportCloseButtonColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().reportCloseButtonColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var reportPlaceholderColor: Int?
        get() = BugseeSDK.appearance().reportPlaceholderColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().reportPlaceholderColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var reportNavigationBarColor: Int?
        get() = BugseeSDK.appearance().reportNavigationBarColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().reportNavigationBarColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackBarsColor: Int?
        get() = BugseeSDK.appearance().feedbackBarsColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackBarsColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackInputBackgroundColor: Int?
        get() = BugseeSDK.appearance().feedbackInputBackgroundColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackInputBackgroundColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackCloseButtonColor: Int?
        get() = BugseeSDK.appearance().feedbackCloseButtonColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackCloseButtonColor = value?.let { argbIntToUIColor(it) }
        }
    public actual var feedbackNavigationBarColor: Int?
        get() = BugseeSDK.appearance().feedbackNavigationBarColor?.let { colorToArgbInt(it) }
        set(value) {
            BugseeSDK.appearance().feedbackNavigationBarColor = value?.let { argbIntToUIColor(it) }
        }
    public actual val mainBugseeColor: Int?
        get() = BugseeSDK.appearance().mainBugseeColor?.let { colorToArgbInt(it) }
    public actual val lowBugColor: Int?
        get() = BugseeSDK.appearance().lowBugColor?.let { colorToArgbInt(it) }
    public actual val mediumBugColor: Int?
        get() = BugseeSDK.appearance().mediumBugColor?.let { colorToArgbInt(it) }
    public actual val dotSelectorColor: Int?
        get() = BugseeSDK.appearance().dotSelectorColor?.let { colorToArgbInt(it) }


    // Android specific fields — no-op on iOS
    public actual var reportSeverityLabelActiveColor: Int?
        get() = null
        set(value) {}
    public actual var reportActionBarColor: Int?
        get() = null
        set(value) {}
    public actual var reportEditTextBackgroundColor: Int?
        get() = null
        set(value) {}
    public actual var reportHintColor: Int?
        get() = null
        set(value) {}
    public actual var reportActionBarTextColor: Int?
        get() = null
        set(value) {}
    public actual var reportActionBarButtonBackgroundClickedColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackActionBarColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackActionBarButtonBackgroundClickedColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackDateTextColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackEmailSkipTextColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackEmailSkipBackgroundClickedColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackEmailContinueClickedColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackInputTextHintColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackBottomDelimiterColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackLoadingBarBackgroundColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackLoadingTextColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackErrorTextColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackVersionChangedBackgroundColor: Int?
        get() = null
        set(value) {}
    public actual var feedbackVersionChangedTextColor: Int?
        get() = null
        set(value) {}
    public actual var notificationTitleResId: Int?
        get() = null
        set(value) {}
    public actual var notificationTitle: String?
        get() = null
        set(value) {}
}