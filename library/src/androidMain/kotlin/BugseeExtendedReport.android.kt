package com.bugsee.kmp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bugsee.kmp.internal.Logger
import com.bugsee.library.attachment.CustomAttachment
import com.bugsee.library.attachment.ExtendedReport

public actual class BugseeExtendedReport internal constructor(
    internal val underlyingReport: ExtendedReport
) {
    private companion object {
        private const val TAG = "BugseeExtendedReport"
    }

    private var screenshotWasChanged = false

    public actual val type: BugseeReportType
        get() = BugseeAndroidUtils.convertIssueType(underlyingReport.type)

    public actual var screenshot: Any?
        get() = underlyingReport.screenshot
        set(value) {
            try {
                val bitmap: Bitmap? = when (value) {
                    is Bitmap -> value
                    is ByteArray -> BitmapFactory.decodeByteArray(value, 0, value.size)
                    else -> null
                }
                if (bitmap != null) {
                    screenshotWasChanged = true
                    underlyingReport.screenshot = bitmap
                } else if (value != null) {
                    Logger.d(TAG, "screenshot setter: unsupported type ${value::class.simpleName}, expected Bitmap or ByteArray")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "screenshot setter: failed to process value", e)
            }
        }

    public actual val screenshotChanged: Boolean
        get() = screenshotWasChanged

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
        underlyingReport.setAttribute(name, value)
    }

    public actual fun getAttribute(name: String): Any? {
        return underlyingReport.getAttribute(name)
    }

    public actual fun clearAttribute(name: String) {
        underlyingReport.clearAttribute(name)
    }

    public actual fun addAttachment(attachment: BugseeAttachment) {
        try {
            var internalAttachment: CustomAttachment
            if (attachment.data != null) {
                internalAttachment = CustomAttachment.fromDataBytes(attachment.data)
            } else {
                internalAttachment = CustomAttachment.fromDataFilePath(attachment.filePath)
                internalAttachment.setFileName(attachment.filePath)
            }

            // Ensure the attachment name is not empty
            internalAttachment.name = attachment.name

            underlyingReport.attachments.add(internalAttachment)
        } catch (e: Exception) {
            Logger.e(TAG, "addAttachment failed for '${attachment.name}'", e)
        }
    }

    public actual fun addLabel(label: String) {
        try {
            if (underlyingReport.labels.isNullOrEmpty()) {
                underlyingReport.labels = ArrayList<String>()
            }

            if (!underlyingReport.labels.contains(label)) {
                underlyingReport.labels.add(label)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "addLabel failed", e)
        }
    }

    public actual fun removeLabel(label: String) {
        try {
            if (!underlyingReport.labels.isNullOrEmpty()) {
                underlyingReport.labels.remove(label)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "removeLabel failed", e)
        }
    }

    public actual fun clearLabels() {
        try {
            if (!underlyingReport.labels.isNullOrEmpty()) {
                underlyingReport.labels.clear()
            }
        } catch (e: Exception) {
            Logger.e(TAG, "clearLabels failed", e)
        }
    }

    public actual fun getLabels(): List<String> {
        try {
            if (!underlyingReport.labels.isNullOrEmpty()) {
                return underlyingReport.labels.toList()
            }
        } catch (e: Exception) {
            Logger.e(TAG, "getLabels failed", e)
        }

        return emptyList()
    }
}