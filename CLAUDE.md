# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform (KMP) wrapper library around native Bugsee crash reporting SDKs for Android and iOS. Provides a unified Kotlin API via expect/actual pattern. Includes a Compose Multiplatform sample app.

## Modules

- **library/** — Core KMP library (`com.bugsee.kmp`). Targets: `androidTarget`, `iosArm64`. Uses explicit API mode and `-Xexpect-actual-classes` compiler flag.
- **composeApp/** — Sample Compose Multiplatform app (`com.bugsee.kmp.sample`) demonstrating library usage.
- **iosApp/** — Native iOS app wrapper using CocoaPods to consume composeApp framework.

## Build Commands

```bash
# Build library
./gradlew :library:build

# Build sample app
./gradlew :composeApp:build

# Generate iOS framework (used by Xcode via CocoaPods)
./gradlew :library:syncFramework

# Run all library tests
./gradlew :library:test

# Run Android unit tests (Robolectric)
./gradlew :library:testDebugUnitTest

# Run a single test class
./gradlew :library:testDebugUnitTest --tests "BugseeAndroidSimpleTest"

# Full test suite via script (auto-detects JDK 21/17/11)
./scripts/test.sh
```

iOS app is built via Xcode workspace (`iosApp/iosApp.xcworkspace`). CocoaPods triggers Gradle's `:library:syncFramework` during Xcode builds.

## Architecture

### Expect/Actual Pattern

The library's core abstraction. Common code defines `expect` classes/declarations; each platform provides `actual` implementations:

| Common (expect) | Android (actual) | iOS (actual) |
|---|---|---|
| `BugseeInternal` | Wraps `com.bugsee.library.Bugsee` | Wraps `cocoapods.Bugsee` via cinterop |
| `PlatformInfo` | Returns `Platform.ANDROID` | Returns `Platform.IOS` |
| `BugseeAppearance` | Maps to Android SDK appearance | Converts ARGB Int ↔ UIColor |
| `BugseeLogEvent` | Wraps Android log event | Wraps iOS log event |
| `BugseeNetworkEvent` | Wraps Android network event | Wraps iOS network event |
| `BugseeExtendedReport` | Wraps Android report | Wraps iOS report |

### Public API

`Bugsee.kt` is the single public entry point — a singleton object delegating all calls to `BugseeInternal`. All public API surfaces are in `commonMain`. Explicit API mode is enabled, so all new public members must have explicit visibility modifiers.

### iOS-Specific

- `BugseeDelegateWrapper` implements `BugseeDelegateProtocol` for native callbacks (filters, listeners, providers). Uses weak references to prevent retain cycles.
- `nsexception/` package handles `NSException` ↔ Kotlin exception bridging.
- Color values are ARGB `Int` in common code, converted to/from `UIColor` on iOS.
- CocoaPods: Bugsee pod sourced from a local path (`/Users/dsheikherev/WorkDir/Cocoapods/Local/Bugsee`), not a public spec repo. The cinterop is auto-generated from the pod — no `.def` files.

### Launch Options

`BugseeLaunchOptions` provides typed configuration that converts to `Map<String, Any>` for native SDKs. Platform-aware defaults exist (e.g., `shakeToReport` defaults true on Android, `screenshotToReport` defaults true on iOS). Wrapper info (`type: "kmp"`) is injected automatically.

### Callback Types (Types.kt)

Filters transform events in-place (return modified or null to suppress). Listeners receive one-way notifications. Providers supply data on demand (attachments, extended reports). All defined as type aliases: `EventHandler<T>`, `TransformHandler<T>`, `ProducerHandler<T>`, `ProducerArgHandler<T, R>`.

## Key Build Configuration

- Kotlin: 2.2.21, Compose Multiplatform: 1.9.3
- Android: minSdk 24, compileSdk 35, JVM target 1.8 (library) / 11 (sample)
- iOS: deployment target 12.0 (library), 16.6 (sample)
- `kotlin.native.cacheKind=none` for Xcode 16.4 compatibility
- `kotlin.mpp.enableCInteropCommonization=true`
- Language opt-ins (iOS/native code): `kotlinx.cinterop.ExperimentalForeignApi`, `kotlinx.cinterop.UnsafeNumber`, `kotlin.experimental.ExperimentalNativeApi`
- Publishing: Maven Central via `com.vanniktech:maven-publish` plugin (Sonatype Central Portal). Coordinates: `com.bugsee:bugsee-kotlin-multiplatform` and `com.bugsee:bugsee-kotlin-multiplatform-protect`. Version sourced from `LIB_VERSION` in `gradle.properties`. Release via `./scripts/deploy.sh`.

## Testing

- Common tests in `library/src/commonTest/`
- Android tests use Robolectric (`library/src/androidUnitTest/`) with SDK 28
- iOS tests in `library/src/iosTest/`
- Dependencies: JUnit 4.13.2, Mockito 5.8.0, Mockito-Kotlin 5.3.1, Robolectric 4.12.1
