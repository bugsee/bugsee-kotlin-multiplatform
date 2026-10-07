# Migrating from 0.1.x to 0.2.0

> Work in progress. This file is filled in as the `nextgen` branch moves the library to the
> Bugsee native SDKs 7.x. It is the source for the published migration guide.

## Requirements

- **iOS 15.0 or later.** The minimum iOS deployment target is raised from 12.0 to 15.0.
- **Kotlin 2.4 or later in your project.** The library is built with Kotlin 2.4.20 and depends on
  `kotlin-stdlib` 2.4.20. Its iOS klibs carry metadata version 2.4.0, which older compilers cannot
  read; Android consumers on Kotlin 2.3 may work but are not supported.
- **Android: Bugsee SDK 7.4.1 and Bugsee Gradle plugin 4.x.** Apply `com.bugsee.android.gradle`
  (4.0.7 or newer) to your Android application module; plugin 3.x does not work with SDK 7.x.
- **Android: network and Compose extensions are not added automatically in KMP modules.** The
  Bugsee Gradle plugin does not see dependencies declared in Kotlin Multiplatform source sets, so
  add the extensions you need to your app yourself (for example
  `com.bugsee:bugsee-android-ktor-3`, `com.bugsee:bugsee-android-okhttp`,
  `com.bugsee:bugsee-android-compose`).

## Android behavior changes

- `pause()` / `resume()` map to the SDK 7 blackout API. The `Paused` / `Resumed` lifecycle events
  are reported when a blackout starts / ends. `pause()` before `launch()` is now honored: capture
  starts paused and `Launched` is followed by `Paused`. A pause ends when the SDK stops (`Resumed`
  is reported before `Stopped`), so call `pause()` again before a relaunch.
- The lifecycle events `Started`, `BeforeFeedbackShown` and `AfterFeedbackShown` are no longer
  reported on Android. The SDK 7 events `Launching`, `Stopping` and `ReportAssemblyFailed` have no
  0.1.x counterpart and are not reported either.
- Messages sent with `Bugsee.log(...)` are not passed through the log filter set with
  `setLogFilter`: Android SDK 7 treats them as already sanitized by your app. Only captured
  Logcat output is filtered.
- `BugseeNetworkEventStage.Cancel` is now reported on Android for aborted requests. Network
  timing updates bypass the network filter.
- The network filter can receive two `Before` events for one request on Android; the second one
  carries the request body. Redact each event by what it carries rather than by its stage.
- `BugseeExtendedReport.screenshot` returns only an image you assigned to it; Android SDK 7
  exposes the captured screenshot asynchronously, so the getter no longer returns it.
- `appearance.notificationTitleResId` is resolved to a string when you assign it.
- `upload(..., includeVideo = false)` cannot be honored: Android SDK 7 always includes video.
- `deleteCollectedDataOnDevice` still deletes everything, including unsent reports. Call it after
  `stop()`; on Android the callback receives `false` when a report is being uploaded at that
  moment (that report is kept).
- `logException(ex, options)` uses only `exceptionDomain` and `rules.skipFrames` on Android.
  `labels`, `includeVideo = false` and custom merging rules have no Android SDK 7 equivalent and
  are ignored.
- `setAttribute` (global and on a report) ignores values that are not `java.io.Serializable`.
- Network event header values are stored as strings.
- A report fields post-filter now uses the `BugseeReportFields` it returns.
- The internal `endpoint` / `debug` custom options are now `com.bugsee.option.$$ENDPOINT` /
  `com.bugsee.option.$$DEBUG` on Android.

## Launch options

`BugseeLaunchOptions` is generated from the option manifests of the native SDKs (Android 7.4.1,
iOS 7.0.0-beta5) and follows the Android SDK 7 naming.

- **Keys** are the shared SDK 7 keys (`com.bugsee.option.*`), available as constants on the new
  `BugseeOptions` object. Use them with `setCustomOption` or the `Map` overload of `launch`.
- **Defaults** are the native SDK defaults of the current platform; a few differ between platforms
  (for example `maxDataSize`, `captureVideoAdaptive`).
- **Every option the platform knows is passed to the SDK on launch**, so on Android the launch
  options override options declared as `<meta-data>` in the app manifest.
- Options of the other platform can be set but are not sent; each property's KDoc names its
  platforms and, for Android extension options, the required module.
- Values are checked against the option type when set (numbers are converted between Int and
  Float, enums also accept their name as a String). A value that does not fit is ignored with an
  error in the log, and the option keeps its previous value.
- The `Map` overload of `launch` goes through `BugseeLaunchOptions` on both platforms, so it also
  gets the defaults and the type checks. Keys that are not options are passed through unchanged.
- iOS SDK 7 lets `captureScreenshot` follow `captureVideo` when it is not set. Because every option
  is sent, set `captureScreenshot = false` yourself when you turn video off.
- `BugseeLaunchOptionsAndroid` and `BugseeLaunchOptionsIos` are removed; all options live on
  `BugseeLaunchOptions`. The class is no longer `open`.

Renamed properties:

| 0.1.x | 0.2.0 |
|---|---|
| `maxRecordingTime` | `duration` |
| `shakeToReport` / `screenshotToReport` | `reportingTriggerByShake` / `reportingTriggerByScreenshot` |
| `notificationBarTrigger` (Android) | `reportingTriggerByNotification` |
| `crashReport` | `detectAndReportCrash` |
| `detectAppExit` | `detectAndReportExit` |
| `killDetection` (iOS) | `detectAndReportKill` |
| `ndkCrashReport` (Android) | `ndkDetectAndReport` (needs `bugsee-android-ndk`) |
| `videoEnabled` / `screenshotEnabled` | `captureVideo` / `captureScreenshot` |
| `videoMode` / `videoQuality` (Android) | `captureVideoMode` / `captureVideoQuality` |
| `videoScale` (iOS, `Double`) | `captureVideoScale` (`Float`) |
| `frameRate` / `minFrameRate` / `maxFrameRate` | `captureVideoFrameRate` / `captureVideoMinFrameRate` / `captureVideoMaxFrameRate` |
| `viewHierarchyEnabled` | `captureViewHierarchy` |
| `monitorNetwork` / `maxNetworkBodySize` | `captureNetwork` / `captureNetworkBodySizeLimit` |
| `monitorWebSocket` (iOS) | `captureWebSocket` |
| `monitorDiskSpace` / `monitorBluetoothStatus` (iOS) | `captureDiskSpace` / `captureBluetoothStatus` |
| `enableMachExceptions` / `statusBarInfo` (iOS) | `captureMachExceptions` / `captureStatusBarInfo` |
| `defaultCrashPriority` / `defaultBugPriority` / `defaultErrorPriority` | `reportingDefaultCrashPriority` / `reportingDefaultBugPriority` / `reportingDefaultErrorPriority` |
| `reportSummaryRequired` / `reportDescriptionRequired` / `reportEmailRequired` | `reportingUISummaryRequired` / `reportingUIDescriptionRequired` / `reportingUIEmailRequired` |
| `reportLabelsEnabled` / `reportLabelsRequired` | `reportingUILabelsEnabled` / `reportingUILabelsRequired` |
| `reportPrioritySelector` | `reportingUIPrioritySelectorEnabled` |
| `bugseeStyle` (iOS, `String`) | `reportingUIStyle` (`BugseeStyle`) |

Unchanged: `captureLogs`, `wifiOnlyUpload`, `maxDataSize`, `captureAVPlayer`, `captureOSLogs`,
`captureDeviceAndNetworkNames` (now iOS only).

Removed without a direct replacement: `serviceMode` and `fallbackVideoMode` (Android; use
`captureVideoMode`), `handleAnr` (Android; see the `detectAndReportExitNotResponding` family).

Enums: `BugseeFrameRate` gains `Raw` (Android); `BugseeVideoMode.V3` / `V4` are replaced by
`Fullscreen` / `DirectBuffers`. New: `BugseeMemoryLeakMode`, `BugseeDataEncryptionMode`, `BugseeStyle`.

## Temporarily unavailable (nextgen only)

These do not work yet; the API calls among them log an error:

- Feedback: `showFeedback`, `setOnNewFeedbackListener`, `setDefaultFeedbackGreeting`.
- `setEmail` / `getEmail` / `clearEmail` (replaced by the user identifier API).
- iOS: launch options use the SDK 7 keys, which the iOS SDK 6.x the library still links ignores.
- Android: wrapper identification (Android SDK 7 ignores `wrapper_info`; iOS reads it).
