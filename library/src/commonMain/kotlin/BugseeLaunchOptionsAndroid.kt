package com.bugsee.kmp

public class BugseeLaunchOptionsAndroid: BugseeLaunchOptions() {
    override fun setDefaults() {
        super.setDefaults()

        this.notificationBarTrigger = true;
        this.serviceMode = false;
        this.videoMode = BugseeVideoMode.V3;
        this.videoQuality = BugseeVideoQuality.Default;
        this.fallbackVideoMode = BugseeVideoMode.V1;
        this.handleAnr = false;
        this.ndkCrashReport = false;
    }

    public var ndkCrashReport: Boolean
        get() = getBooleanOption("NdkCrashReport")
        set(value) {
            options["NdkCrashReport"] = value
        }

    public var notificationBarTrigger: Boolean
        get() = getBooleanOption("NotificationBarTrigger")
        set(value) {
            options["NotificationBarTrigger"] = value
        }

    public var serviceMode: Boolean
        get() = getBooleanOption("ServiceMode")
        set(value) {
            options["ServiceMode"] = value
        }

    public var videoMode: BugseeVideoMode
        get() = BugseeVideoMode.fromIntValue(options["VideoMode"] as Int, BugseeVideoMode.V3)
        set(value) {
            options["VideoMode"] = value.toIntValue()
        }

    public var videoQuality: BugseeVideoQuality
        get() = BugseeVideoQuality.fromIntValue(options["VideoQuality"] as? Int, BugseeVideoQuality.Default)
        set(value) {
            options["VideoQuality"] = value.toIntValue()
        }

    public var fallbackVideoMode: BugseeVideoMode
        get() = BugseeVideoMode.fromIntValue(options["FallbackVideoMode"] as? Int, BugseeVideoMode.V3)
        set(value) {
            options["FallbackVideoMode"] = value.toIntValue()
        }

    public var handleAnr: Boolean
        get() = getBooleanOption("HandleAnr")
        set(value) {
            options["HandleAnr"] = value
        }
}