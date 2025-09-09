package com.bugsee.kmp

public class BugseeLaunchOptionsIos: BugseeLaunchOptions() {

    override fun setDefaults() {
        super.setDefaults()

        // This is set to false by default on iOS,
        // as the new privacy rules forbid sending
        // disk space information off the device
        this.monitorDiskSpace = false;
        this.defaultErrorPriority = BugseeSeverity.High;
        this.killDetection = false;
        this.videoScale = 1.0;
        this.monitorBluetoothStatus = false;
        this.captureAVPlayer = false;
        this.captureOSLogs = false;
        this.monitorWebSocket = true;
        this.enableMachExceptions = false;
        this.statusBarInfo = false;
        this.bugseeStyle = "System";
    }

    public var monitorDiskSpace: Boolean
        get() = getBooleanOption("MonitorDiskSpace")
        set(value) {
            options["MonitorDiskSpace"] = value
        }

    public var killDetection: Boolean
        get() = getBooleanOption("BugseeKillDetectionKey")
        set(value) {
            options["BugseeKillDetectionKey"] = value
        }

    public var videoScale: Double
        get() = options["VideoScale"] as Double
        set(value) {
            options["VideoScale"] = value
        }

    public var monitorBluetoothStatus: Boolean
        get() = getBooleanOption("MonitorBluetoothStatus")
        set(value) {
            options["MonitorBluetoothStatus"] = value
        }

    /**
     * When enabled, videos playing via AVPlayerLayer
     * will be captured on video. May incur additional
     * performance overhead
     */
    public var captureAVPlayer: Boolean
        get() = getBooleanOption("CaptureAVPlayer")
        set(value) {
            options["CaptureAVPlayer"] = value
        }

    public var captureOSLogs: Boolean
        get() = getBooleanOption("CaptureOSLogs")
        set(value) {
            options["CaptureOSLogs"] = value
        }

    public var monitorWebSocket: Boolean
        get() = getBooleanOption("MonitorWebSocket")
        set(value) {
            options["MonitorWebSocket"] = value
        }


    public var defaultErrorPriority: BugseeSeverity
        get() = BugseeSeverity.fromLevel(options["BugseeDefaultErrorPriority"] as? Int, BugseeSeverity.High)
        set(value) {
            options["BugseeDefaultErrorPriority"] = value.getLevel()
        }

    public var enableMachExceptions: Boolean
        get() = getBooleanOption("BugseeEnableMachExceptions")
        set(value) {
            options["BugseeEnableMachExceptions"] = value
        }

    public var statusBarInfo: Boolean
        get() = getBooleanOption("StatusBarInfo")
        set(value) {
            options["StatusBarInfo"] = value
        }

//    "Default", "Dark", "BasedOnStatusBar", "System"
    public var bugseeStyle: String
        get() = options["BugseeStyle"] as String
        set(value) {
            options["BugseeStyle"] = value
        }

}