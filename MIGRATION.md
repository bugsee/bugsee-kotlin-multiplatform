# Migrating from 0.1.x to 0.2.0

> Work in progress. This file is filled in as the `nextgen` branch moves the library to the
> Bugsee native SDKs 7.x. It is the source for the published migration guide.

## Requirements

- **iOS 15.0 or later.** The minimum iOS deployment target is raised from 12.0 to 15.0.
- **Kotlin 2.4 or later in your project.** The library is built with Kotlin 2.4.20 and depends on
  `kotlin-stdlib` 2.4.20. Its iOS klibs carry metadata version 2.4.0, which older compilers cannot
  read; Android consumers on Kotlin 2.3 may work but are not supported.
- **iOS: Bugsee SDK 7.0.1 through Swift Package Manager; CocoaPods is not supported.** iOS SDK 7
  is published only as the Swift package `https://github.com/bugsee/spm`, not to CocoaPods, so
  remove `pod 'Bugsee'` from your Podfile. The library declares the package as a SwiftPM
  dependency (Kotlin SwiftPM import) and publishes it with the library, so you do not add Bugsee
  to your app yourself:
  - Build your shared module into the app with `embedAndSignAppleFrameworkForXcode` (direct
    integration). The Kotlin Gradle plugin links Bugsee into your Kotlin tests and framework. The
    first Xcode build fails with the command that adds the Kotlin linkage package, which brings
    Bugsee into the app, to your Xcode project (`integrateLinkagePackage`); run it, build again,
    and commit `KotlinMultiplatformLinkedPackage`, the project and the `Package.resolved` that
    Xcode writes.
  - The linkage package names its subpackage after the library version. After you update the
    library, the first Xcode build regenerates the package and fails with "Synthetic project
    regenerated"; resolve the packages in Xcode (File > Packages > Resolve Package Versions) and
    build again. Delete the previous version's folder under
    `KotlinMultiplatformLinkedPackage/subpackages`, which is left behind.
  - The Kotlin CocoaPods plugin (`kotlin("native.cocoapods")`) does not work with the library: it
    does not accept SwiftPM dependencies, and its `syncFramework` task, which the Xcode build
    runs, fails with "You are using CocoaPods integration with SwiftPM dependencies". Move the
    shared module to direct integration before upgrading
    ([migration guide](https://kotl.in/cocoapods-to-swiftpm-migration)).
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
- The lifecycle event `Started` is no longer reported on Android. `BeforeFeedbackShown` and
  `AfterFeedbackShown` are reported only when the app uses `bugsee-kotlin-multiplatform-feedback`
  (see [Feedback](#feedback)). The SDK 7 events `Launching`, `Stopping` and `ReportAssemblyFailed` have no
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

## iOS behavior changes

- `pause()` / `resume()` map to the SDK 7 blackout API. The `Paused` / `Resumed` lifecycle events
  are reported when a blackout starts / ends. Stopping the SDK ends a blackout (`Resumed` is
  reported before `Stopped`); unlike on Android, a relaunch restores it.
- `pause()` no longer stops logs and network capture. iOS SDK 6 paused the video and the loggers;
  a blackout hides only what shows the screen (video, report screenshot, touches, keyboard and
  focus breadcrumbs, view hierarchy), while logs, network events and traces keep being recorded.
  Redact sensitive data with the log and network filters, or call `stop()` to halt all capture.
- Messages sent with `Bugsee.log(...)` are not passed through the log filter set with
  `setLogFilter`, as on Android; iOS SDK 6 filtered them. Only captured console logs are
  filtered.
- `pause()` before `launch()` is now honored: the session starts paused, and `Paused` is reported
  right away, before `Launched`. iOS SDK 6 ignored it.
- The lifecycle events `Started`, `BeforeFeedbackShown` and `AfterFeedbackShown` are no longer
  reported on iOS. The SDK 7 event `ReportAssemblyFailed` has no 0.1.x counterpart and is not
  reported.
- The report fields pre- and post-filters (`setReportFieldsPreFilter` / `setReportFieldsPostFilter`)
  and the attachments provider run on the SDK 7 report handler, as on Android:
  - the filters run for every report (`logException`, `upload`, crashes), not only for the report
    dialog;
  - the pre-filter receives the report's current fields (for `showReportDialog(summary, ...)`, the
    ones you passed) instead of empty ones, and a field it leaves unchanged is kept;
  - the report dialog opens with the severity you pass or `reportingDefaultBugPriority` (High by
    default); 0.1.x always opened it at Medium.
- `upload(..., includeVideo = false)` cannot be honored: iOS SDK 7 always includes video.
- `logException(ex, options)` uses only `exceptionDomain` and `rules.skipFrames` on iOS, as on
  Android. `labels`, `includeVideo = false` and custom merging rules are ignored.
- The Kotlin bindings of the native SDK moved from the package `cocoapods.Bugsee` to
  `swiftPMImport.com.bugsee.library`, and `com.bugsee.kmp.internal.ExceptionLoggingOptions` is
  removed. This matters only for code that calls the native SDK directly.

## User identifier

`setEmail` / `getEmail` / `clearEmail` are removed: the native SDKs 7 replaced the email with a
user identifier. Use `setUserIdentifier` / `getUserIdentifier` / `clearUserIdentifier` instead.
The identifier is attached to every report and kept across app restarts; if it is an email
address, the server may link the reports to that user's account. `setUserIdentifier(null)` and
`setUserIdentifier("")` clear it on both platforms, and `getUserIdentifier()` returns `null` when
none is set. The email field of the report dialog edits the same value.

An email set with 0.1.x is carried over on iOS only: iOS SDK 7 reads the identifier from the
Keychain item iOS SDK 6 stored the email in, while Android SDK 7 does not read the email Android
SDK 6 stored. Call `setUserIdentifier` again after the upgrade, for example at app start, rather
than only when the user logs in.

## Feedback

The feedback chat is a separate artifact, as in the native SDKs 7, where it became an optional
add-on. Add `com.bugsee:bugsee-kotlin-multiplatform-feedback` (the same version as
`bugsee-kotlin-multiplatform`) and use `com.bugsee.kmp.feedback.BugseeFeedback`:

| 0.1.x | 0.2.0 |
|---|---|
| `Bugsee.showFeedback()` | `BugseeFeedback.show()` |
| `Bugsee.setDefaultFeedbackGreeting(greeting)` | `BugseeFeedback.setGreeting(greeting)` |
| `Bugsee.setOnNewFeedbackListener { messages -> … }` | `BugseeFeedback.setListener(object : BugseeFeedbackListener { override fun onNewMessagesReceived(messages: List<String>) { … } })` |

- `BugseeFeedbackEventListener` is removed. `BugseeFeedbackListener` also reports the messages
  the user sends (`onNewMessageSent`), and both callbacks run on the main thread.
- The artifact brings the Feedback add-ons: `com.bugsee:bugsee-android-feedback` on Android and
  the Swift package `github.com/bugsee/feedback-spm` on iOS. The Kotlin Gradle plugin adds the
  Swift package to the app through the linkage package, as it does for the core package; the
  first Xcode build after adding the artifact fails once with "Synthetic project regenerated".
- On Android the feedback screen does not open before `Bugsee.launch`.
- Android: the Feedback add-on needs `compileSdk` 35 or newer and Android Gradle plugin 8.1.1 or
  newer (its Compose UI 1.8 and `androidx.dynamicanimation` 1.1 dependencies require them).
- The greeting set with `setGreeting` is kept across app restarts on Android, but only until the
  app exits on iOS: set it at every start.

## Appearance

`BugseeAppearance` keeps its properties; each property's KDoc names the platforms it applies to.

- Setting a property of the other platform is still ignored, but now logged once per property;
  reading it still returns `null`.
- `feedbackEmailSkipTextColor` now also works on iOS (`BugseeTheme.feedbackEmailSkipColor`); reading
  it there returns the SDK default until it is set, like the other iOS colors.
- `reportSendButtonColor` and `reportCloseButtonColor` (iOS) are deprecated: iOS SDK 7 does not
  apply them.
- On iOS an unset placeholder (`reportSummaryPlaceholder`, …) reads as `null` instead of `""`.
- The feedback colors take effect only in apps that use `bugsee-kotlin-multiplatform-feedback`.
- The native SDKs do not apply every property they declare; the property KDoc says so:
  - the iOS SDK 7.0 feedback screens use only `feedbackBackgroundColor` and the feedback e-mail
    screen colors (`feedbackEmailBackgroundColor`, `feedbackEmailContinueActiveColor`,
    `feedbackEmailContinueNotActiveColor`, `feedbackEmailSkipTextColor`);
  - Android SDK 7.4 ignores `reportActionBarButtonBackgroundClickedColor`,
    `feedbackActionBarButtonBackgroundClickedColor`, `feedbackEmailSkipBackgroundClickedColor`,
    `feedbackEmailContinueClickedColor`, `feedbackLoadingBarBackgroundColor`,
    `feedbackLoadingTextColor` and `feedbackErrorTextColor`.

## Launch options

`BugseeLaunchOptions` is generated from the option manifests of the native SDKs (Android 7.4.1,
iOS 7.0.1) and follows the Android SDK 7 naming.

- **Keys** are the shared SDK 7 keys (`com.bugsee.option.*`), available as constants on the new
  `BugseeOptions` object. Use them with `setCustomOption` or the `Map` overload of `launch`.
- **Defaults** are the native SDK defaults of the current platform; they can differ between
  platforms (today only `captureVideoAdaptive`).
- **Every option the platform knows is passed to the SDK on launch**, so on Android the launch
  options override options declared as `<meta-data>` in the app manifest.
- Options of the other platform can be set but are not sent; each property's KDoc names its
  platforms and, for Android extension options, the required module.
- Values are checked against the option type when set: numbers are converted between Int and
  Float, and enum options also accept the native Int value or a native constant name of either SDK
  (`1`, `"Low"`, `"DEEP_DEBUG_ONLY"`). A value that does not fit, or an enum value the platform does
  not support (`BugseeFrameRate.Raw` on iOS), is ignored with an error in the log, and the option
  keeps its previous value.
- The `Map` overloads of `launch` and `relaunch` go through `BugseeLaunchOptions` on both platforms,
  so they also get the defaults and the type checks. Keys that are not options are passed through
  unchanged.
- Some defaults change with SDK 7.
  - `maxDataSize` is 150 (was 50).
  - `detectAndReportExit` is on (`detectAppExit` was off).
  - iOS: `reportingTriggerByShake` is on and `reportingTriggerByScreenshot` is off (0.1.x had
    `shakeToReport` off and `screenshotToReport` on). Set both to keep the 0.1.x gesture.
  - iOS: `detectAndReportEarlyCrash` and `captureVideoAdaptive` are on; iOS SDK 6 had neither.
  - iOS: `captureCrashHandlerType` is `MachAndBSD`. 0.1.x used BSD signal handlers only
    (`enableMachExceptions` was off).
- iOS SDK 7 lets `captureScreenshot` follow `captureVideo` when it is not set. Because every option
  is sent, set `captureScreenshot = false` yourself when you turn video off.
- `captureCrashSignals` (iOS) is a bit set of the signals the crash reporter handles; combine the
  `BugseeCrashSignals` constants with `or` (`BugseeCrashSignals.ABRT or BugseeCrashSignals.TRAP`).
  The default `BugseeCrashSignals.Default` lets the SDK choose (every signal); combined with other
  bits it is ignored. Leaving out `ABRT` also stops uncaught Objective-C and C++ exceptions from
  being reported.
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
| `ndkCrashReport` (Android) | `ndkDetectAndReport` (needs `bugsee-android-ndk`) |
| `videoEnabled` / `screenshotEnabled` | `captureVideo` / `captureScreenshot` |
| `videoMode` / `videoQuality` (Android) | `captureVideoMode` / `captureVideoQuality` |
| `videoScale` (iOS, `Double`) | `captureVideoScale` (`Float`) |
| `frameRate` / `minFrameRate` / `maxFrameRate` | `captureVideoFrameRate` / `captureVideoMinFrameRate` / `captureVideoMaxFrameRate` |
| `viewHierarchyEnabled` | `captureViewHierarchy` |
| `monitorNetwork` / `maxNetworkBodySize` | `captureNetwork` / `captureNetworkBodySizeLimit` |
| `monitorWebSocket` (iOS) | `captureWebSocket` |
| `monitorDiskSpace` / `monitorBluetoothStatus` (iOS) | `captureDiskSpace` / `captureBluetoothStatus` |
| `enableMachExceptions` (iOS, `Boolean`) | `captureCrashHandlerType` (`BugseeCrashHandlerType`): `true` → `Mach`, `false` → `BSD`; the new default is `MachAndBSD` |
| `statusBarInfo` (iOS) | `captureStatusBarInfo` |
| `defaultCrashPriority` / `defaultBugPriority` / `defaultErrorPriority` | `reportingDefaultCrashPriority` / `reportingDefaultBugPriority` / `reportingDefaultErrorPriority` |
| `reportSummaryRequired` / `reportDescriptionRequired` / `reportEmailRequired` | `reportingUISummaryRequired` / `reportingUIDescriptionRequired` / `reportingUIEmailRequired` |
| `reportLabelsEnabled` / `reportLabelsRequired` | `reportingUILabelsEnabled` / `reportingUILabelsRequired` |
| `reportPrioritySelector` | `reportingUIPrioritySelectorEnabled` |
| `bugseeStyle` (iOS, `String`) | `reportingUIStyle` (`BugseeStyle`) |

Unchanged: `captureLogs`, `wifiOnlyUpload`, `maxDataSize`, `captureAVPlayer`, `captureOSLogs`,
`captureDeviceAndNetworkNames` (now iOS only).

Removed without a direct replacement: `serviceMode` and `fallbackVideoMode` (Android; use
`captureVideoMode`), `handleAnr` (Android; see the `detectAndReportExitNotResponding` family),
`killDetection` (iOS; out-of-memory kills are reported through `detectAndReportExit`).

Enums: `BugseeFrameRate` gains `Raw` (Android); `BugseeVideoMode.V3` / `V4` are replaced by
`Fullscreen` / `DirectBuffers`. New: `BugseeMemoryLeakMode`, `BugseeDataEncryptionMode`, `BugseeStyle`,
`BugseeCrashHandlerType`, and the `BugseeCrashSignals` constants.
