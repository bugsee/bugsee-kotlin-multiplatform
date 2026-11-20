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
     * **Getter** returns the platform-native image object
     * (`Bitmap` on Android, `UIImage` on iOS), or `null`. On Android it returns only an image
     * assigned through this property: Android SDK 7 exposes the captured screenshot
     * asynchronously, so it is not available here.
     */
    public var screenshot: Any?

    /**
     * Whether the screenshot has been modified since the report was created.
     *
     * Delegates to the native SDK's change tracking on both platforms.
     */
    public val screenshotChanged: Boolean

    public var summary: String?
    public var description: String?

    /**
     * Sets the report attribute [name] to [value]: a String, a Boolean, a number, or an array, a
     * collection or a map with String keys of such values, at any depth; arrays and collections are
     * stored as lists. Dates and the other platform values are stored as [Bugsee.trace] records them.
     * A value that is, or holds, anything else is not set, and an error is logged. Arrays and
     * collections are copied.
     *
     * On iOS the SDK writes a Boolean into the report as `1` or `0`.
     */
    public fun setAttribute(name: String, value: Any)
    public fun getAttribute(name: String): Any?
    public fun clearAttribute(name: String)

    public fun addAttachment(attachment: BugseeAttachment)

    public fun addLabel(label: String)
    public fun removeLabel(label: String)
    public fun clearLabels()
    public fun getLabels(): List<String>
}
