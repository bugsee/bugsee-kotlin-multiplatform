package com.bugsee.kmp

/**
 * Wraps a platform-native extended report created via [Bugsee.createReport].
 *
 * Modify properties (summary, description, screenshot, attributes, labels, attachments)
 * then pass back to [Bugsee.upload] to submit.
 */
public expect class BugseeExtendedReport {
    /**
     * The report type (Bug, Error, or Crash).
     *
     * Note: on iOS, always returns [BugseeReportType.Bug].
     */
    public val type: BugseeReportType

    /**
     * The report screenshot.
     *
     * **Setter** accepts:
     * - `ByteArray` — PNG or JPEG bytes (works from common code on both platforms)
     * - `android.graphics.Bitmap` — Android only
     * - `platform.UIKit.UIImage` — iOS only
     *
     * Unsupported types are logged and ignored.
     *
     * On Android the setter decodes the bytes, then encodes the image and writes it to disk on
     * the calling thread, which takes over 100 ms for a screen-sized image. The callbacks of
     * [Bugsee.createReport] run on the main thread there, so use [setScreenshotAsync] in them.
     *
     * **Getter** returns the platform-native image object
     * (`Bitmap` on Android, `UIImage` on iOS), or `null`. On Android it returns only an image
     * assigned through this property or [setScreenshotAsync]: Android SDK 7 exposes the captured
     * screenshot asynchronously, so it is not available here.
     */
    public var screenshot: Any?

    /**
     * Sets the screenshot like [screenshot] without blocking the calling thread, and calls
     * [onSet] on the main thread once it is stored.
     *
     * Upload the report from [onSet], not right after this call: on Android a report passed to
     * [Bugsee.upload] before the screenshot is stored can be sent without it.
     *
     * ```
     * Bugsee.createReport { report ->
     *     report.setScreenshotAsync(pngBytes) { Bugsee.upload(report) }
     * }
     * ```
     *
     * Values are the ones [screenshot] accepts. As with [screenshot], `null` changes nothing, and an
     * unsupported value or bytes that are not an image change nothing and are logged; [onSet] is
     * called in every case. Calls of this function are applied in the order they are made.
     *
     * On Android the bytes are decoded on a background thread, and the SDK encodes and writes the
     * image on its own. On iOS the image is set at once, as [screenshot] does, which is cheap there.
     *
     * On Android the value is read after this call returns, so until [onSet] runs:
     * - do not change it: do not recycle or draw on a `Bitmap`, or write into the `ByteArray`. The
     *   report can otherwise get the changed image, or no screenshot at all for a recycled `Bitmap`;
     * - do not set [screenshot] on the same report: a call of this function that is still pending
     *   can replace the value set there.
     */
    public fun setScreenshotAsync(screenshot: Any?, onSet: () -> Unit)

    /**
     * Whether the screenshot has been modified since the report was created.
     *
     * Delegates to the native SDK's change tracking on both platforms.
     */
    public val screenshotChanged: Boolean

    public var summary: String?
    public var description: String?

    /**
     * Sets the report attribute [name] to [value], stored as [Bugsee.trace] records a value: arrays
     * and collections as lists, dates as ISO 8601 strings, and any other value that is not a String,
     * a Boolean, a number or a map as its `toString()`. Arrays and collections are copied.
     *
     * On iOS the SDK writes a Boolean into the report as `1` or `0`.
     */
    public fun setAttribute(name: String, value: Any)
    public fun getAttribute(name: String): Any?
    public fun clearAttribute(name: String)

    /**
     * Adds [attachment] to the report.
     *
     * On Android the call is synchronous: it stores the attachment in the report on the calling
     * thread, and the time it takes grows with the attachment's size.
     * - [BugseeAttachment.data] is written to a file.
     * - A `content://`, `file://` or `file:///android_asset/` path is read into memory whole,
     *   then written to a file.
     * - Any other path is copied as a file.
     *
     * The callbacks of [Bugsee.createReport] run on the main thread there, so do not add a large
     * attachment on that thread.
     *
     * On iOS a report holds at most 5 attachments, counting those of
     * [Bugsee.setReportAttachmentsProvider], each of at most 3 MB (3 145 728 bytes). An empty
     * attachment or one over 3 MB is not added, and a warning is logged; a file over 3 MB is not
     * read. At upload the SDK drops the attachments past the fifth and logs each to the console.
     */
    public fun addAttachment(attachment: BugseeAttachment)

    public fun addLabel(label: String)
    public fun removeLabel(label: String)
    public fun clearLabels()
    public fun getLabels(): List<String>
}
