package com.bugsee.kmp

import com.bugsee.kmp.internal.ExtendedReport
import com.bugsee.kmp.internal.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage

public actual class BugseeExtendedReport internal constructor(
    internal val underlyingReport: ExtendedReport
) {
    private companion object {
        private const val TAG = "BugseeExtendedReport"
    }

    // Native iOS BugseeExtendedReport does not expose report type — defaults to Bug
    public actual val type: BugseeReportType
        get() = BugseeReportType.Bug

    @OptIn(BetaInteropApi::class)
    public actual var screenshot: Any?
        get() = underlyingReport.screenshot
        set(value) {
            try {
                val image: UIImage? = when (value) {
                    is UIImage -> value
                    is ByteArray -> {
                        value.usePinned { pinned ->
                            val nsData = NSData.create(
                                bytes = pinned.addressOf(0),
                                length = value.size.toULong()
                            )
                            UIImage.imageWithData(nsData)
                        }
                    }
                    else -> null
                }
                if (image != null) {
                    underlyingReport.setScreenshot(image)
                } else if (value != null) {
                    Logger.d(TAG, "screenshot setter: unsupported type ${value::class.simpleName}, expected UIImage or ByteArray")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "screenshot setter: failed to process value", e)
            }
        }

    public actual val screenshotChanged: Boolean
        get() = underlyingReport.isScreenshotChanged

    public actual var summary: String?
        get() = underlyingReport.summary
        set(value) {
            underlyingReport.setSummary(value)
        }

    public actual var description: String?
        get() = underlyingReport.reportDescription
        set(value) {
            underlyingReport.setDescription(value)
        }

    public actual fun setAttribute(name: String, value: Any) {
        underlyingReport.setAttribute(name, withValue = value)
    }

    public actual fun getAttribute(name: String): Any? {
        val attrs = underlyingReport.attributes as? Map<*, *> ?: return null
        return attrs[name]
    }

    public actual fun clearAttribute(name: String) {
        underlyingReport.clearAttribute(name)
    }

    public actual fun addAttachment(attachment: BugseeAttachment) {
        val nativeAttachment = BugseeIOSUtils.convertAttachment(attachment)
        underlyingReport.setAttachment(nativeAttachment)
    }

    public actual fun addLabel(label: String) {
        val currentLabels = getLabelsFromNative()
        if (!currentLabels.contains(label)) {
            underlyingReport.labels = currentLabels + label
        }
    }

    public actual fun removeLabel(label: String) {
        val currentLabels = getLabelsFromNative()
        underlyingReport.labels = currentLabels.filter { it != label }
    }

    public actual fun clearLabels() {
        underlyingReport.labels = emptyList<String>()
    }

    public actual fun getLabels(): List<String> {
        return getLabelsFromNative()
    }

    private fun getLabelsFromNative(): List<String> {
        val raw = underlyingReport.labels ?: return emptyList()
        return (raw as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    }
}
