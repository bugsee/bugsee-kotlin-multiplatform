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
     * (`Bitmap` on Android, `UIImage` on iOS), or `null`.
     */
    public var screenshot: Any?

    /**
     * Whether the screenshot has been modified since the report was created.
     *
     * Implementation note: on Android this is tracked by the KMP wrapper;
     * on iOS it delegates to the native SDK's change tracking.
     */
    public val screenshotChanged: Boolean

    public var summary: String?
    public var description: String?

    public fun setAttribute(name: String, value: Any)
    public fun getAttribute(name: String): Any?
    public fun clearAttribute(name: String)

    public fun addAttachment(attachment: BugseeAttachment)

    public fun addLabel(label: String)
    public fun removeLabel(label: String)
    public fun clearLabels()
    public fun getLabels(): List<String>
}
