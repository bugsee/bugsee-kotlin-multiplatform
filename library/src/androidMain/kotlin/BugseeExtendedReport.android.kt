package com.bugsee.kmp

import android.graphics.Bitmap
import com.bugsee.library.attachment.CustomAttachment
import com.bugsee.library.attachment.ExtendedReport

public actual class BugseeExtendedReport internal constructor(
    internal val underlyingReport: ExtendedReport
) {
    private var screenshotWasChanged = false;

    public actual val type: BugseeReportType
        get() = BugseeAndroidUtils.convertIssueType(underlyingReport.type)

    public actual var screenshot: Any?
        get() = underlyingReport.screenshot
        set(value) {
            if (value is Bitmap) {
                screenshotWasChanged = true
                underlyingReport.screenshot = value
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
    }

    public actual fun addLabel(label: String) {
        if (underlyingReport.labels.contains(label)) {
            underlyingReport.labels.add(label)
        }
    }

    public actual fun removeLabel(label: String) {
        try {
            underlyingReport.labels.remove(label)
        } catch (_: Throwable) {}
    }

    public actual fun clearLabels() {
        underlyingReport.labels.clear()
    }

    public actual fun getLabels(): List<String> {
        return underlyingReport.labels.toList()
    }
}