package com.bugsee.kmp

public open class BugseeLaunchOptions {
    protected val options: HashMap<String, Any> = HashMap()
    private val customOptions: HashMap<String, Any> = HashMap()

    init {
        setDefaults()
    }

    internal fun toMap(): Map<String, Any> {
        // Join maps
        val result = HashMap<String, Any>()

        // Set custom options first, to avoid
        // overwriting built-in values
        result.putAll(customOptions)

        // Now, put built-in values
        result.putAll(options)

        // Finally, put the wrapper info
        result.put("wrapper_info", getWrapperInfo())

        return result
    }

    private fun getWrapperInfo(): Map<String, Any> {
        val runtime = mapOf("version" to KotlinVersion.CURRENT.toString())

        return mapOf("type" to "kmp", "version" to "", "runtime" to runtime)
    }

    protected open fun setDefaults() {
        options.clear()
    }

    protected fun getBooleanOption(key: String): Boolean {
        val optionValue = options[key]
        if (optionValue is Boolean) {
            return optionValue;
        }

        return false;
    }

    public fun setCustomOption(key: String, value: Any) {
        customOptions[key] = value
    }

    public var maxNetworkBodySize: Int
        get() = options["bodySizeLimit"] as Int
        set(value) {
            options["bodySizeLimit"] = value
        }

    public var shakeToReport: Boolean
        get() = getBooleanOption("ShakeToReport")
        set(value) {
            options["ShakeToReport"] = value
        }

    public var crashReport: Boolean
        get() = getBooleanOption("CrashReport")
        set(value) {
            options["CrashReport"] = value
        }

    public var maxRecordingTime: Int
        get() = options["MaxRecordingTime"] as Int
        set(value) {
            options["MaxRecordingTime"] = value
        }

    public var videoEnabled: Boolean
        get() = getBooleanOption("VideoEnabled")
        set(value) {
            options["VideoEnabled"] = value
        }

    public var screenshotEnabled: Boolean
        get() = getBooleanOption("ScreenshotEnabled")
        set(value) {
            options["ScreenshotEnabled"] = value
        }

    public var captureLogs: Boolean
        get() = getBooleanOption("CaptureLogs")
        set(value) {
            options["CaptureLogs"] = value
        }

    public var monitorNetwork: Boolean
        get() = getBooleanOption("MonitorNetwork")
        set(value) {
            options["MonitorNetwork"] = value
            // this is set to true as in iOS we store values
            // in both variants (in BugseeOptions and as
            // raw dictionary)
            options["monitorNetwork"] = value
        }

    public var wifiOnlyUpload: Boolean
        get() = getBooleanOption("WifiOnlyUpload")
        set(value) {
            options["WifiOnlyUpload"] = value
        }

    public var maxDataSize: Int
        get() = options["MaxDataSize"] as Int
        set(value) {
            options["MaxDataSize"] = value
        }

    public var reportPrioritySelector: Boolean
        get() = getBooleanOption("BugseeReportPrioritySelector")
        set(value) {
            options["BugseeReportPrioritySelector"] = value
        }

    public var defaultCrashPriority: BugseeSeverity
        get() = BugseeSeverity.fromLevel(options["BugseeDefaultCrashPriority"] as? Int, BugseeSeverity.Blocker)
        set(value) {
            options["BugseeDefaultCrashPriority"] = value.getLevel()
        }

    public var defaultBugPriority: BugseeSeverity
        get() = BugseeSeverity.fromLevel(options["BugseeDefaultBugPriority"] as? Int, BugseeSeverity.VeryLow)
        set(value) {
            options["BugseeDefaultBugPriority"] = value.getLevel()
        }

    public var frameRate: BugseeFrameRate
        get() = BugseeFrameRate.fromIntValue(options["FrameRate"] as? Int)
        set(value) {
            options["FrameRate"] = value.getIntValue()
        }

    public var minFrameRate: Int
        get() = options["MinFrameRate"] as Int
        set(value) {
            options["MinFrameRate"] = value
        }

    public var maxFrameRate: Int
        get() = options["MaxFrameRate"] as Int
        set(value) {
            options["MaxFrameRate"] = value
        }

    public var captureDeviceAndNetworkNames: Boolean
        get() = getBooleanOption("CaptureDeviceAndNetworkNames")
        set(value) {
            options["CaptureDeviceAndNetworkNames"] = value
        }

    public var reportSummaryRequired: Boolean
        get() = getBooleanOption("ReportSummaryRequired")
        set(value) {
            options["ReportSummaryRequired"] = value
        }

    public var reportDescriptionRequired: Boolean
        get() = getBooleanOption("ReportDescriptionRequired")
        set(value) {
            options["ReportDescriptionRequired"] = value
        }

    public var reportEmailRequired: Boolean
        get() = getBooleanOption("ReportEmailRequired")
        set(value) {
            options["ReportEmailRequired"] = value
        }
}
