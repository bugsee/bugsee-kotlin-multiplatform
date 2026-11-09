# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin Multiplatform (KMP) wrapper library around native Bugsee crash reporting SDKs for Android and iOS. Provides a unified Kotlin API via expect/actual pattern. Includes a Compose Multiplatform sample app.

## Modules

- **library/** — Core KMP library (`com.bugsee.kmp`). Targets: `androidTarget`, `iosArm64`. Uses explicit API mode and `-Xexpect-actual-classes` compiler flag.
- **composeApp/** — Sample Compose Multiplatform app (`com.bugsee.kmp.sample`) demonstrating library usage.
- **iosApp/** — Native iOS app wrapper. Builds the composeApp framework through direct integration (an Xcode build phase runs `:composeApp:embedAndSignAppleFrameworkForXcode`) and gets Bugsee from the SwiftPM linkage package `iosApp/KotlinMultiplatformLinkedPackage`.

## Build Commands

```bash
# Build library
./gradlew :library:build

# Build sample app
./gradlew :composeApp:build

# Regenerate the iOS cinterop from the Bugsee SwiftPM package
./gradlew :library:cinteropSwiftPMImportIosSimulatorArm64

# Re-wire iosApp after changing the SwiftPM dependencies (rarely needed: the linkage package updates itself)
XCODEPROJ_PATH="$PWD/iosApp/iosApp.xcodeproj" ./gradlew :composeApp:integrateEmbedAndSign :composeApp:integrateLinkagePackage

# Run all library tests
./gradlew :library:test

# Run Android unit tests (Robolectric)
./gradlew :library:testDebugUnitTest

# Run a single test class
./gradlew :library:testDebugUnitTest --tests "BugseeAndroidSimpleTest"

# Full test suite via script (auto-detects JDK 21/17/11)
./scripts/test.sh
```

iOS app is built via the Xcode project `iosApp/iosApp.xcodeproj` (no workspace, no CocoaPods). Its "Compile Kotlin Framework" build phase runs Gradle.

## Architecture

### Expect/Actual Pattern

The library's core abstraction. Common code defines `expect` classes/declarations; each platform provides `actual` implementations:

| Common (expect) | Android (actual) | iOS (actual) |
|---|---|---|
| `BugseeInternal` | Wraps `com.bugsee.library.Bugsee` | Wraps `swiftPMImport.com.bugsee.library` via cinterop |
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
- iOS SDK 7 comes from the SwiftPM package `github.com/bugsee/spm` (not on CocoaPods) through Kotlin SwiftPM import: `kotlin { swiftPMDependencies { ... } }` in `library/build.gradle.kts`, version `bugsee-ios` in the catalog. The cinterop package is `swiftPMImport.com.bugsee.library` (from the Gradle group and project name). The dependency is published with the library (`*-swiftpm-metadata.json`), so consumers' Kotlin Gradle plugin picks it up transitively. CocoaPods is not used anywhere (library, sample) and not supported for consumers: the Kotlin CocoaPods plugin rejects SwiftPM dependencies, so a consumer's shared module must be integrated directly (`embedAndSignAppleFrameworkForXcode` + `integrateLinkagePackage`). Resolved versions are locked in `.swiftpm-locks/default/swiftImport/Package.resolved` (Gradle) and `iosApp/iosApp.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved` (Xcode); after bumping `bugsee-ios`, commit both together with `iosApp/KotlinMultiplatformLinkedPackage`.
- Local iOS SDK build: set `bugsee.ios.localPackage=<dir>` in `local.properties` (the Xcode build phase reads it too) or pass `-Pbugsee.ios.localPackage=<dir>`. `<dir>` (absolute, or relative to the repository root) is a Swift package with a `Bugsee` product, e.g. `.binaryTarget(name: "Bugsee", path: "Bugsee.xcframework")`. While it is set, `.swiftpm-locks` is left alone and publishing fails. The first Xcode build fails once with "Synthetic project regenerated" and writes the local path into `iosApp/KotlinMultiplatformLinkedPackage/subpackages/_library/Package.swift`: do not commit that file (`scripts/check-swiftpm-remote.sh`, run by `pr.yml`, fails on it). To go back, remove the property and `git checkout iosApp/KotlinMultiplatformLinkedPackage`.

### Launch Options

`BugseeOptions` (keys), `BugseeLaunchOptions` (typed builder) and the internal `BugseeOptionsSpec` (per-platform defaults/known keys) are **generated** into `library/build/generated/bugseeOptions/` by `:library:generateBugseeOptions` (sources in `buildSrc/`). Inputs live in `library/options/`: the committed native SDK option manifests (`android-<version>.json`, `ios-<version>.json`, refreshed with `./gradlew :library:updateBugseeOptionsManifests` after bumping `bugsee-android` / `bugsee-ios` in the version catalog) and the hand-maintained `kmp-options.json` (key → name in Android `Options.*` style, native enum → KMP enum, optional key → extra KDoc paragraph). The build fails on a manifest key without a name; a KMP enum missing a manifest value fails compilation of the generated `BugseeOptionsSpec`. Option enums (`BugseeFrameRate`, `BugseeVideoMode`, …) and the storage (`BugseeLaunchOptionsStore`) are hand-written. `toMap()` sends every option the current platform knows (enums as Android constant names / iOS Ints) plus custom options and `wrapper_info`.

### Callback Types (Types.kt)

Filters transform events in-place (return modified or null to suppress). Listeners receive one-way notifications. Providers supply data on demand (attachments, extended reports). All defined as type aliases: `EventHandler<T>`, `TransformHandler<T>`, `ProducerHandler<T>`, `ProducerArgHandler<T, R>`.

## Key Build Configuration

- Kotlin: 2.4.20, Compose Multiplatform: 1.9.3
- Gradle wrapper: 8.14.5 (Kotlin 2.5 requires ≥ 8.14.4; Gradle 9 deferred to an AGP upgrade), AGP 8.6.0
- Android: minSdk 24, compileSdk 35, JVM target 1.8 (library) / 11 (sample)
- iOS: deployment target 15.0 (library), 16.6 (sample)
- `kotlin.mpp.enableCInteropCommonization=true`
- Language opt-ins (iOS/native code): `kotlinx.cinterop.ExperimentalForeignApi`, `kotlinx.cinterop.UnsafeNumber`, `kotlin.experimental.ExperimentalNativeApi`
- Publishing: Maven Central via `com.vanniktech:maven-publish` plugin (Sonatype Central Portal). Coordinates: `com.bugsee:bugsee-kotlin-multiplatform` and `com.bugsee:bugsee-kotlin-multiplatform-protect`. Version sourced from `version.txt` (`-SNAPSHOT` appended unless `RELEASE=true`). Release via the **Deploy (production)** GitHub Actions workflow from the `release` branch (or `RELEASE=true ./scripts/deploy.sh` locally).
- CI/CD: GitHub Actions on hosted `macos-26` runners with Xcode 26.4.1 pinned (Kotlin/Native 2.4.20's Xcode) — `pr.yml` (build + test, every PR), `main.yml` (build + test + mavenLocal publish on push to `main` / `nextgen`), `deploy-production.yml` (manual release). See `.github/CI.md`.

## Testing

- Common tests in `library/src/commonTest/`
- Android tests use Robolectric (`library/src/androidUnitTest/`) with SDK 28
- iOS tests in `library/src/iosTest/`
- Dependencies: JUnit 4.13.2, Mockito 5.8.0, Mockito-Kotlin 5.3.1, Robolectric 4.12.1
