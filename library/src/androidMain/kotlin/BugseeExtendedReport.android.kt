package com.bugsee.kmp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bugsee.kmp.internal.Logger
import com.bugsee.library.contracts.reporting.Report
import java.io.Serializable

public actual class BugseeExtendedReport internal constructor(
    internal val underlyingReport: Report
) {
    private companion object {
        private const val TAG = "BugseeExtendedReport"
    }

    // 7.x only exposes the captured screenshot asynchronously, so the synchronous getter
    // returns the bitmap last assigned through this wrapper.
    private var assignedScreenshot: Bitmap? = null

    public actual val type: BugseeReportType
        get() = BugseeAndroidUtils.convertIssueType(underlyingReport.type)

    public actual var screenshot: Any?
        get() = assignedScreenshot
        set(value) {
            try {
                val bitmap: Bitmap? = when (value) {
                    is Bitmap -> value
                    is ByteArray -> BitmapFactory.decodeByteArray(value, 0, value.size)
                    else -> null
                }
                if (bitmap != null) {
                    assignedScreenshot = bitmap
                    underlyingReport.setScreenshot(bitmap)
                } else if (value != null) {
                    Logger.d(TAG, "screenshot setter: unsupported type ${value::class.simpleName}, expected Bitmap or ByteArray")
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
            underlyingReport.summary = value
        }

    public actual var description: String?
        get() = underlyingReport.description
        set(value) {
            underlyingReport.description = value
        }

    public actual fun setAttribute(name: String, value: Any) {
        if (value is Serializable) {
            underlyingReport.setAttribute(name, value)
        } else {
            Logger.e(TAG, "setAttribute: value for '$name' is not Serializable (${value::class.simpleName}) — ignoring")
        }
    }

    public actual fun getAttribute(name: String): Any? {
        return underlyingReport.getAttribute(name)
    }

    public actual fun clearAttribute(name: String) {
        underlyingReport.removeAttribute(name)
    }

    public actual fun addAttachment(attachment: BugseeAttachment) {
        try {
            BugseeAndroidUtils.addAttachmentToReport(attachment, underlyingReport)
        } catch (e: Exception) {
            Logger.e(TAG, "addAttachment failed for '${attachment.name}'", e)
        }
    }

    public actual fun addLabel(label: String) {
        try {
            if (underlyingReport.labels?.contains(label) != true) {
                underlyingReport.addLabel(label)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "addLabel failed", e)
        }
    }

    public actual fun removeLabel(label: String) {
        try {
            val labels = underlyingReport.labels
            if (!labels.isNullOrEmpty() && labels.contains(label)) {
                underlyingReport.setLabels(labels.filter { it != label })
            }
        } catch (e: Exception) {
            Logger.e(TAG, "removeLabel failed", e)
        }
    }

    public actual fun clearLabels() {
        try {
            underlyingReport.clearLabels()
        } catch (e: Exception) {
            Logger.e(TAG, "clearLabels failed", e)
        }
    }

    public actual fun getLabels(): List<String> {
        try {
            return underlyingReport.labels?.toList() ?: emptyList()
        } catch (e: Exception) {
            Logger.e(TAG, "getLabels failed", e)
        }

        return emptyList()
    }
}
