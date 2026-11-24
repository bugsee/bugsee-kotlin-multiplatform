package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeOptions
import com.bugsee.kmp.BugseeOptionsSpec

/** A [BugseeOptions] constant: its [name] and its [key]. */
internal class OptionConstant(val name: String, val key: String)

/**
 * What became of a launch option key from before 0.2.0: [replacement] is the option that takes it
 * over or, for an option SDK 7 [removed], the option MIGRATION.md points to instead; `null` when
 * SDK 7 has nothing like it.
 */
internal class LegacyLaunchOptionKey(val replacement: OptionConstant?, val removed: Boolean)

private fun renamed(name: String, key: String) = LegacyLaunchOptionKey(OptionConstant(name, key), removed = false)

private fun removed(name: String, key: String) = LegacyLaunchOptionKey(OptionConstant(name, key), removed = true)

private val noReplacement = LegacyLaunchOptionKey(null, removed = true)

/**
 * Launch option keys from before 0.2.0. The launch options ignore them on both platforms, with an
 * error that names the option to use instead; they are not translated.
 *
 * - The keys of KMP 0.1.x: the raw keys its `BugseeLaunchOptions(Map)` translated, and the keys
 *   its `BugseeLaunchOptions`, `BugseeLaunchOptionsAndroid` and `BugseeLaunchOptionsIos` sent.
 * - The Android SDK 6 keys (`Bugsee.Option` in 6.0.2), which 0.1.x passed to SDK 6 from the `Map`
 *   overloads of `launch` and `relaunch` on Android. Their replacements are those of the Android
 *   SDK's `docs/migration-6.x-to-7.x.md`; `RememberUserDecisionForScreenCapture` and
 *   `CaptureVideoFullscreenRememberUserDecision` have the same description.
 * - The iOS SDK 6 keys that iOS SDK 7 still reads (`legacyKeyAliases`, `BugseeOptions.m:376-436`
 *   in iOS SDK 7.1.0) onto the option of the new key. The launch options also send the new key of
 *   every option with a default, after the custom options, and the SDK applies the keys in the
 *   order of the map, so the default replaced the old key's value. For an option without a default
 *   (`BuildTarget`, `BuildType`) the old key took effect, on iOS only. Its `endpoint` is not here:
 *   its option, `com.bugsee.option.$$ENDPOINT`, is not a [BugseeOptions] one.
 *
 * Android SDK 7 drops all of them without a word (`options/OptionsContainerImpl.java:59-63`).
 * After bumping the iOS SDK, compare this with its `legacyKeyAliases`.
 */
internal val legacyLaunchOptionKeys: Map<String, LegacyLaunchOptionKey> = mapOf(
    // KMP 0.1.x, both platforms
    "MaxRecordingTime" to renamed("Duration", BugseeOptions.Duration),
    "VideoEnabled" to renamed("CaptureVideo", BugseeOptions.CaptureVideo),
    "ScreenshotEnabled" to renamed("CaptureScreenshot", BugseeOptions.CaptureScreenshot),
    "FrameRate" to renamed("CaptureVideoFrameRate", BugseeOptions.CaptureVideoFrameRate),
    "MinFrameRate" to renamed("CaptureVideoMinFrameRate", BugseeOptions.CaptureVideoMinFrameRate),
    "MaxFrameRate" to renamed("CaptureVideoMaxFrameRate", BugseeOptions.CaptureVideoMaxFrameRate),
    "ViewHierarchyEnabled" to renamed("CaptureViewHierarchy", BugseeOptions.CaptureViewHierarchy),
    "CaptureLogs" to renamed("CaptureLogs", BugseeOptions.CaptureLogs),
    "MonitorNetwork" to renamed("CaptureNetwork", BugseeOptions.CaptureNetwork),
    "monitorNetwork" to renamed("CaptureNetwork", BugseeOptions.CaptureNetwork),
    "bodySizeLimit" to renamed("CaptureNetworkBodySizeLimit", BugseeOptions.CaptureNetworkBodySizeLimit),
    "CaptureDeviceAndNetworkNames" to renamed("CaptureDeviceAndNetworkNames", BugseeOptions.CaptureDeviceAndNetworkNames),
    "CrashReport" to renamed("DetectAndReportCrash", BugseeOptions.DetectAndReportCrash),
    "DetectAppExit" to renamed("DetectAndReportExit", BugseeOptions.DetectAndReportExit),
    "ShakeToTrigger" to renamed("ReportingTriggerByShake", BugseeOptions.ReportingTriggerByShake),
    "ShakeToReport" to renamed("ReportingTriggerByShake", BugseeOptions.ReportingTriggerByShake),
    "ScreenshotToTrigger" to renamed("ReportingTriggerByScreenshot", BugseeOptions.ReportingTriggerByScreenshot),
    "ScreenshotToReport" to renamed("ReportingTriggerByScreenshot", BugseeOptions.ReportingTriggerByScreenshot),
    "BugseeDefaultCrashPriority" to renamed("ReportingDefaultCrashPriority", BugseeOptions.ReportingDefaultCrashPriority),
    "BugseeDefaultBugPriority" to renamed("ReportingDefaultBugPriority", BugseeOptions.ReportingDefaultBugPriority),
    "BugseeReportPrioritySelector" to renamed("ReportingUIPrioritySelectorEnabled", BugseeOptions.ReportingUIPrioritySelectorEnabled),
    "ReportSummaryRequired" to renamed("ReportingUISummaryRequired", BugseeOptions.ReportingUISummaryRequired),
    "ReportDescriptionRequired" to renamed("ReportingUIDescriptionRequired", BugseeOptions.ReportingUIDescriptionRequired),
    "ReportEmailRequired" to renamed("ReportingUIEmailRequired", BugseeOptions.ReportingUIEmailRequired),
    "ReportLabelsEnabled" to renamed("ReportingUILabelsEnabled", BugseeOptions.ReportingUILabelsEnabled),
    "ReportLabelsRequired" to renamed("ReportingUILabelsRequired", BugseeOptions.ReportingUILabelsRequired),
    "WifiOnlyUpload" to renamed("WifiOnlyUpload", BugseeOptions.WifiOnlyUpload),
    "MaxDataSize" to renamed("MaxDataSize", BugseeOptions.MaxDataSize),

    // KMP 0.1.x, BugseeLaunchOptionsAndroid
    "NotificationBarTrigger" to renamed("ReportingTriggerByNotification", BugseeOptions.ReportingTriggerByNotification),
    "NdkCrashReport" to renamed("NdkDetectAndReport", BugseeOptions.NdkDetectAndReport),
    "VideoMode" to renamed("CaptureVideoMode", BugseeOptions.CaptureVideoMode),
    "VideoQuality" to renamed("CaptureVideoQuality", BugseeOptions.CaptureVideoQuality),
    "ServiceMode" to removed("CaptureVideoMode", BugseeOptions.CaptureVideoMode),
    "FallbackVideoMode" to removed("CaptureVideoMode", BugseeOptions.CaptureVideoMode),
    "HandleAnr" to removed("DetectAndReportExitNotResponding", BugseeOptions.DetectAndReportExitNotResponding),

    // The other Android SDK 6 keys
    "LogLevel" to renamed("CaptureLogsLevel", BugseeOptions.CaptureLogsLevel),
    "RecordHttpBodyWithoutType" to renamed("CaptureNetworkBodyWithoutType", BugseeOptions.CaptureNetworkBodyWithoutType),
    "DetectAppLaunchCrash" to renamed("DetectAndReportEarlyCrash", BugseeOptions.DetectAndReportEarlyCrash),
    "RememberUserDecisionForScreenCapture" to renamed(
        "CaptureVideoFullscreenRememberUserDecision",
        BugseeOptions.CaptureVideoFullscreenRememberUserDecision,
    ),
    "SkipFramesOnScroll" to removed("CaptureVideoSecureScrolling", BugseeOptions.CaptureVideoSecureScrolling),
    "UseSdCard" to noReplacement, // deprecated in 6.x, "with no replacement"
    "ExtendedVideoMode" to noReplacement, // deprecated in 6.x
    "AnrIgnoreDebugger" to noReplacement,

    // KMP 0.1.x, BugseeLaunchOptionsIos
    "VideoScale" to renamed("CaptureVideoScale", BugseeOptions.CaptureVideoScale),
    "CaptureAVPlayer" to renamed("CaptureAVPlayer", BugseeOptions.CaptureAVPlayer),
    "CaptureOSLogs" to renamed("CaptureOSLogs", BugseeOptions.CaptureOSLogs),
    "MonitorWebSocket" to renamed("CaptureWebSocket", BugseeOptions.CaptureWebSocket),
    "MonitorDiskSpace" to renamed("CaptureDiskSpace", BugseeOptions.CaptureDiskSpace),
    "MonitorBluetoothStatus" to renamed("CaptureBluetoothStatus", BugseeOptions.CaptureBluetoothStatus),
    "StatusBarInfo" to renamed("CaptureStatusBarInfo", BugseeOptions.CaptureStatusBarInfo),
    "BugseeDefaultErrorPriority" to renamed("ReportingDefaultErrorPriority", BugseeOptions.ReportingDefaultErrorPriority),
    "BugseeStyle" to renamed("ReportingUIStyle", BugseeOptions.ReportingUIStyle),
    "BugseeEnableMachExceptions" to removed("CaptureCrashHandlerType", BugseeOptions.CaptureCrashHandlerType),
    "BugseeKillDetectionKey" to removed("DetectAndReportExit", BugseeOptions.DetectAndReportExit),

    // The other iOS SDK 6 keys of legacyKeyAliases
    "NotifyFlushDelay" to renamed("NotifyFlushDelay", BugseeOptions.NotifyFlushDelay),
    "BuildTarget" to renamed("BuildTarget", BugseeOptions.BuildTarget),
    "BuildType" to renamed("BuildType", BugseeOptions.BuildType),
    "DataEncryption" to renamed("DataEncryption", BugseeOptions.DataEncryption),
    "BugseeAppLaunchCrashDetectionKey" to renamed("DetectAndReportEarlyCrash", BugseeOptions.DetectAndReportEarlyCrash),
    "SanitizeNetworkData" to renamed("CaptureNetworkUseDefaultSanitizer", BugseeOptions.CaptureNetworkUseDefaultSanitizer),
    "CaptureVideoAdaptive" to renamed("CaptureVideoAdaptive", BugseeOptions.CaptureVideoAdaptive),
    "BugseeEnableOnDeviceSymbolication" to renamed("CaptureOnDeviceSymbolication", BugseeOptions.CaptureOnDeviceSymbolication),
    "PerformanceMonitoring" to renamed("PerformanceMonitoring", BugseeOptions.PerformanceMonitoring),
    "PerformanceSampleRate" to renamed("PerformanceSampleRate", BugseeOptions.PerformanceSampleRate),
    "PerformanceAdaptiveSampling" to renamed("PerformanceAdaptiveSampling", BugseeOptions.PerformanceAdaptiveSampling),
)

/** The error for [key], a key of [legacyLaunchOptionKeys] that maps to [legacy], on the platform of [isAndroid]. */
internal fun legacyLaunchOptionMessage(key: String, legacy: LegacyLaunchOptionKey, isAndroid: Boolean): String {
    val ignoring = "Ignoring launch option '$key', a key from before 0.2.0: "
    val replacement = legacy.replacement ?: return ignoring + "SDK 7 has no such option"
    val constant = "BugseeOptions.${replacement.name} (\"${replacement.key}\")"
    val otherSdk = when {
        replacement.key in BugseeOptionsSpec.knownKeys(isAndroid) -> null
        isAndroid -> "iOS"
        else -> "Android"
    }
    return ignoring + when {
        legacy.removed && otherSdk == null -> "SDK 7 removed its option; see $constant instead"
        legacy.removed -> "SDK 7 removed its option; see $constant, which only the $otherSdk SDK has"
        otherSdk == null -> "use $constant instead"
        else -> "its option is now $constant, which only the $otherSdk SDK has"
    }
}
