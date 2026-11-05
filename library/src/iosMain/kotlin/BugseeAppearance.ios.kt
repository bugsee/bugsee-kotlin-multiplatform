package com.bugsee.kmp

import cocoapods.Bugsee.BugseeTheme
import com.bugsee.kmp.internal.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreGraphics.CGFloatVar
import platform.UIKit.UIColor
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

private typealias BugseeSDK = cocoapods.Bugsee.Bugsee

private const val TAG = "BugseeAppearance"

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

private fun theme(): BugseeTheme = BugseeSDK.appearance()

private fun color(ref: KMutableProperty1<BugseeTheme, UIColor?>) = object : ReadWriteProperty<Any?, Int?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int? = colorToArgbInt(ref.get(theme()))

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
        ref.set(theme(), value?.let(::argbIntToUIColor))
    }
}

private fun string(ref: KMutableProperty1<BugseeTheme, String?>) = object : ReadWriteProperty<Any?, String?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String? = ref.get(theme())

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        ref.set(theme(), value)
    }
}

private fun <T> androidOnly() = object : ReadWriteProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = null

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value != null) Logger.e(TAG, "${property.name} is Android only and is ignored on iOS")
    }
}

public actual class BugseeAppearance {

    // --- Report screen, Android and iOS ---

    public actual var reportSummaryPlaceholder: String? by string(BugseeTheme::reportSummaryPlaceholder)
    public actual var reportDescriptionPlaceholder: String? by string(BugseeTheme::reportDescriptionPlaceholder)
    public actual var reportLabelsPlaceholder: String? by string(BugseeTheme::reportLabelsPlaceholder)
    public actual var reportEmailPlaceholder: String? by string(BugseeTheme::reportEmailPlaceholder)
    public actual var reportVersionColor: Int? by color(BugseeTheme::reportVersionColor)
    public actual var reportTextColor: Int? by color(BugseeTheme::reportTextColor)
    public actual var reportBackgroundColor: Int? by color(BugseeTheme::reportBackgroundColor)

    // --- Feedback screen, Android and iOS ---

    public actual var feedbackBackgroundColor: Int? by color(BugseeTheme::feedbackBackgroundColor)
    public actual var feedbackIncomingBubbleColor: Int? by color(BugseeTheme::feedbackIncomingBubbleColor)
    public actual var feedbackOutgoingBubbleColor: Int? by color(BugseeTheme::feedbackOutgoingBubbleColor)
    public actual var feedbackIncomingTextColor: Int? by color(BugseeTheme::feedbackIncomingTextColor)
    public actual var feedbackOutgoingTextColor: Int? by color(BugseeTheme::feedbackOutgoingTextColor)
    public actual var feedbackTitleTextColor: Int? by color(BugseeTheme::feedbackTitleTextColor)
    public actual var feedbackEmailBackgroundColor: Int? by color(BugseeTheme::feedbackEmailBackgroundColor)
    public actual var feedbackEmailContinueNotActiveColor: Int? by color(BugseeTheme::feedbackEmailContinueNotActiveColor)
    public actual var feedbackEmailContinueActiveColor: Int? by color(BugseeTheme::feedbackEmailContinueActiveColor)
    public actual var feedbackEmailSkipTextColor: Int? by color(BugseeTheme::feedbackEmailSkipColor)
    public actual var feedbackInputTextColor: Int? by color(BugseeTheme::feedbackInputTextColor)

    // --- Android only ---

    public actual var reportActionBarColor: Int? by androidOnly()
    public actual var reportEditTextBackgroundColor: Int? by androidOnly()
    public actual var reportHintColor: Int? by androidOnly()
    public actual var reportActionBarTextColor: Int? by androidOnly()
    public actual var reportActionBarButtonBackgroundClickedColor: Int? by androidOnly()
    public actual var reportSeverityLabelActiveColor: Int? by androidOnly()
    public actual var feedbackActionBarColor: Int? by androidOnly()
    public actual var feedbackActionBarButtonBackgroundClickedColor: Int? by androidOnly()
    public actual var feedbackDateTextColor: Int? by androidOnly()
    public actual var feedbackEmailSkipBackgroundClickedColor: Int? by androidOnly()
    public actual var feedbackEmailContinueClickedColor: Int? by androidOnly()
    public actual var feedbackInputTextHintColor: Int? by androidOnly()
    public actual var feedbackBottomDelimiterColor: Int? by androidOnly()
    public actual var feedbackLoadingBarBackgroundColor: Int? by androidOnly()
    public actual var feedbackLoadingTextColor: Int? by androidOnly()
    public actual var feedbackErrorTextColor: Int? by androidOnly()
    public actual var feedbackVersionChangedBackgroundColor: Int? by androidOnly()
    public actual var feedbackVersionChangedTextColor: Int? by androidOnly()
    public actual var notificationTitleResId: Int? by androidOnly()
    public actual var notificationTitle: String? by androidOnly()

    // --- iOS only ---

    public actual var reportCellBackgroundColor: Int? by color(BugseeTheme::reportCellBackgroundColor)

    @Deprecated("Not applied by iOS SDK 7: the report screen uses the system Done button.")
    public actual var reportSendButtonColor: Int? by color(BugseeTheme::reportSendButtonColor)

    @Deprecated("Not applied by iOS SDK 7: the report screen uses the system Cancel button.")
    public actual var reportCloseButtonColor: Int? by color(BugseeTheme::reportCloseButtonColor)

    public actual var reportPlaceholderColor: Int? by color(BugseeTheme::reportPlaceholderColor)
    public actual var reportNavigationBarColor: Int? by color(BugseeTheme::reportNavigationBarColor)
    public actual var feedbackBarsColor: Int? by color(BugseeTheme::feedbackBarsColor)
    public actual var feedbackInputBackgroundColor: Int? by color(BugseeTheme::feedbackInputBackgroundColor)
    public actual var feedbackCloseButtonColor: Int? by color(BugseeTheme::feedbackCloseButtonColor)
    public actual var feedbackNavigationBarColor: Int? by color(BugseeTheme::feedbackNavigationBarColor)
    public actual val mainBugseeColor: Int? get() = colorToArgbInt(theme().mainBugseeColor)
    public actual val lowBugColor: Int? get() = colorToArgbInt(theme().lowBugColor)
    public actual val mediumBugColor: Int? get() = colorToArgbInt(theme().mediumBugColor)
    public actual val dotSelectorColor: Int? get() = colorToArgbInt(theme().dotSelectorColor)
}
