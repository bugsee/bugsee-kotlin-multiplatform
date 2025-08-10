package com.bugsee.kmp

public expect class BugseeExtendedReport {
    public val type: BugseeReportType

    public var screenshot: Any?
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