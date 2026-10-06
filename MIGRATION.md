# Migrating from 0.1.x to 0.2.0

> Work in progress. This file is filled in as the `nextgen` branch moves the library to the
> Bugsee native SDKs 7.x. It is the source for the published migration guide.

## Requirements

- **iOS 15.0 or later.** The minimum iOS deployment target is raised from 12.0 to 15.0.
- **Kotlin 2.4 or later in your project.** The library is built with Kotlin 2.4.20 and depends on
  `kotlin-stdlib` 2.4.20. Its iOS klibs carry metadata version 2.4.0, which older compilers cannot
  read; Android consumers on Kotlin 2.3 may work but are not supported.
- **Android: Bugsee SDK 7.3.0 and Bugsee Gradle plugin 4.x.** Apply `com.bugsee.android.gradle`
  (4.0.7 or newer) to your Android application module; plugin 3.x does not work with SDK 7.x.
- **Android: network and Compose extensions are not added automatically in KMP modules.** The
  Bugsee Gradle plugin does not see dependencies declared in Kotlin Multiplatform source sets, so
  add the extensions you need to your app yourself (for example
  `com.bugsee:bugsee-android-ktor-3`, `com.bugsee:bugsee-android-okhttp`,
  `com.bugsee:bugsee-android-compose`).

## Android behavior changes

- `pause()` / `resume()` map to the SDK 7 blackout API. The `Paused` / `Resumed` lifecycle events
  are reported when a blackout starts / ends.
- The lifecycle events `Started`, `BeforeFeedbackShown` and `AfterFeedbackShown` are no longer
  reported on Android. The SDK 7 events `Launching`, `Stopping` and `ReportAssemblyFailed` have no
  0.1.x counterpart and are not reported either.
- Messages sent with `Bugsee.log(...)` are not passed through the log filter set with
  `setLogFilter`: Android SDK 7 treats them as already sanitized by your app. Only captured
  Logcat output is filtered.
- `BugseeNetworkEventStage.Cancel` is now reported on Android for aborted requests. Network
  timing updates bypass the network filter.
- `BugseeExtendedReport.screenshot` returns only an image you assigned to it; Android SDK 7
  exposes the captured screenshot asynchronously, so the getter no longer returns it.
- `appearance.notificationTitleResId` is resolved to a string when you assign it.
- `upload(..., includeVideo = false)` cannot be honored: Android SDK 7 always includes video.
- `deleteCollectedDataOnDevice` also deletes intermediate capture data.
- `setAttribute` (global and on a report) ignores values that are not `java.io.Serializable`.
- Network event header values are stored as strings.
- A report fields post-filter now uses the `BugseeReportFields` it returns.
- The internal `endpoint` / `debug` custom options are now `com.bugsee.option.$$ENDPOINT` /
  `com.bugsee.option.$$DEBUG` on Android.

## Temporarily unavailable on Android (nextgen only)

These are no-ops that log an error until the corresponding API lands on `nextgen`:

- Feedback: `showFeedback`, `setOnNewFeedbackListener`, `setDefaultFeedbackGreeting`.
- `setEmail` / `getEmail` / `clearEmail` (replaced by the user identifier API).
- Typed `BugseeLaunchOptions` values (still emitted with 6.x keys, which SDK 7 ignores).
- Wrapper identification (`wrapper_info` is dropped by SDK 7).
