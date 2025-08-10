package com.bugsee.kmp

public actual class BugseeExtendedReport {
    public actual val type: BugseeReportType
        get() = TODO("Not yet implemented")
    public actual var screenshot: Any?
        get() = TODO("Not yet implemented")
        set(value) {}
    public actual val screenshotChanged: Boolean
        get() = TODO("Not yet implemented")
    public actual var summary: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    public actual var description: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    public actual fun setAttribute(name: String, value: Any) {
    }

    public actual fun getAttribute(name: String): Any? {
        TODO("Not yet implemented")
    }

    public actual fun clearAttribute(name: String) {
    }

    public actual fun addAttachment(attachment: BugseeAttachment) {
    }

    public actual fun addLabel(label: String) {
    }

    public actual fun removeLabel(label: String) {
    }

    public actual fun clearLabels() {
    }

    public actual fun getLabels(): List<String> {
        TODO("Not yet implemented")
    }
}