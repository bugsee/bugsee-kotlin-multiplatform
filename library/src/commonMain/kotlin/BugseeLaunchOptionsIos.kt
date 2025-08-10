package com.bugsee.kmp

public class BugseeLaunchOptionsIos: BugseeLaunchOptions() {

    override fun setDefaults() {
        super.setDefaults()

        // This is set to false by default on iOS,
        // as the new privacy rules forbid sending
        // disk space information off the device
        this.monitorDiskSpace = false;
        this.captureDeviceAndNetworkNames = false;
        this.captureLogs = true;
        this.crashReport = true;
        this.defaultBugPriority = BugseeSeverity.VeryLow;
        this.defaultCrashPriority = BugseeSeverity.Blocker;
        this.killDetection = false;
        this.maxRecordingTime = 60;
        this.monitorNetwork = true;
        this.reportPrioritySelector = false;
        this.screenshotToReport = true;
        this.shakeToReport = false;
        // this.style = ???
        this.videoEnabled = true;
        this.frameRate = BugseeFrameRate.High;
        this.minFrameRate = 1;
        this.maxFrameRate = 30;
        this.screenshotEnabled = true;
        this.wifiOnlyUpload = false;
        this.maxDataSize = 50;
        this.videoScale = 1.0;
        this.monitorBluetoothStatus = false;
        this.maxNetworkBodySize = 20 * 1024;
        this.captureAVPlayer = false;
    }

    public var monitorDiskSpace: Boolean
        get() = getBooleanOption("MonitorDiskSpace")
        set(value) {
            options["MonitorDiskSpace"] = value
        }

    public var screenshotToReport: Boolean
        get() = getBooleanOption("ScreenshotToReport")
        set(value) {
            options["ScreenshotToReport"] = value
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
}