package com.bugsee.kmp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import com.bugsee.kmp.internal.Logger
import com.bugsee.kmp.internal.sdkValue
import com.bugsee.library.contracts.reporting.Report
import java.io.Serializable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

public actual class BugseeExtendedReport internal constructor(
    internal val underlyingReport: Report
) {
    private companion object {
        private const val TAG = "BugseeExtendedReport"

        // One thread for every report: decodes the bytes of setScreenshotAsync and hands each
        // value to the SDK in the order of the calls.
        val screenshotThread: ExecutorService by lazy {
            Executors.newSingleThreadExecutor { task -> Thread(task, "BugseeScreenshot").apply { isDaemon = true } }
        }
        val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
    }

    // 7.x only exposes the captured screenshot asynchronously, so the synchronous getter
    // returns the bitmap last assigned through this wrapper.
    @Volatile
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
                    // Assigned once the SDK took it: the getter never returns an image the report lacks.
                    underlyingReport.setScreenshot(bitmap)
                    assignedScreenshot = bitmap
                } else if (value != null) {
                    Logger.d(TAG, "screenshot setter: unsupported type ${value::class.simpleName}, expected Bitmap or ByteArray")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "screenshot setter: failed to process value", e)
            }
        }

    public actual fun setScreenshotAsync(screenshot: Any?, onSet: () -> Unit) {
        screenshotThread.execute {
            val bitmap: Bitmap? = try {
                when (screenshot) {
                    is Bitmap -> screenshot
                    is ByteArray -> BitmapFactory.decodeByteArray(screenshot, 0, screenshot.size).also {
                        if (it == null) Logger.e(TAG, "setScreenshotAsync: the bytes are not an image")
                    }
                    null -> null
                    else -> null.also {
                        Logger.e(TAG, "setScreenshotAsync: unsupported type ${screenshot::class.simpleName}, expected Bitmap or ByteArray")
                    }
                }
            } catch (e: Throwable) {
                Logger.e(TAG, "setScreenshotAsync: failed to decode the screenshot", e)
                null
            }
            if (bitmap == null) {
                mainHandler.post { callOnSet(onSet) }
                return@execute
            }
            // Assigned before the SDK call, so that onSet already sees it, and put back if the SDK
            // throws: the getter never returns an image the report lacks.
            val previous = assignedScreenshot
            try {
                assignedScreenshot = bitmap
                // Called off a Looper thread, the SDK posts the completion to the main thread once
                // the image is written (IssueReportScreenshot.java:150-155).
                underlyingReport.setScreenshotAsync(bitmap) { callOnSet(onSet) }
            } catch (e: Throwable) {
                assignedScreenshot = previous
                Logger.e(TAG, "setScreenshotAsync: the SDK failed to set the screenshot", e)
                mainHandler.post { callOnSet(onSet) }
            }
        }
    }

    private fun callOnSet(onSet: () -> Unit) {
        try {
            onSet()
        } catch (e: Throwable) {
            Logger.e(TAG, "setScreenshotAsync: onSet threw", e)
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

    // Every value sdkValue returns on Android is Serializable: a String, a boxed number or Boolean, a
    // BigDecimal or a BigInteger, an ArrayList or a LinkedHashMap.
    public actual fun setAttribute(name: String, value: Any) {
        underlyingReport.setAttribute(name, sdkValue(value) as Serializable)
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
