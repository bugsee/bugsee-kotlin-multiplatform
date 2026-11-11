import com.bugsee.gradle.options.GenerateBugseeOptionsTask
import com.bugsee.gradle.options.UpdateBugseeOptionsManifestsTask
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.bugsee.gradle.LocalIosPackages
import com.bugsee.gradle.localIosPackage
import com.bugsee.gradle.refusePublishingWithLocalIosPackage
import com.bugsee.gradle.usesLocalIosPackage

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.buildKonfig)
}

// Maven coordinates for the published artifact:
//   group:    com.bugsee  (matches the existing com.bugsee:bugsee-android namespace on Maven Central)
//   version:  read from repo-root version.txt (e.g. 0.1.0) — a SNAPSHOT suffix is appended
//             automatically unless RELEASE=true is set in the environment
//             (or -PRELEASE=true on the Gradle CLI). Convention matches the legacy
//             bugsee-android SDK's release script. The BuildKonfig libraryVersion field below
//             reuses the resolved value.
val isReleaseBuild: Boolean =
    (System.getenv("RELEASE") ?: project.findProperty("RELEASE")?.toString() ?: "false").toBoolean()
val libraryVersionName: String = rootProject.file("version.txt").readLines().first().trim() +
        if (isReleaseBuild) "" else "-SNAPSHOT"

// Short git SHA of the current HEAD — surfaced through BuildKonfig so wrapper_info
// can report which commit produced the artifact. Falls back to "unknown" when the
// repo has no commits or git is unavailable.
val buildChecksum: String = runCatching {
    providers.exec {
        commandLine("git", "--git-dir=${rootProject.projectDir}/.git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
}.getOrElse { "unknown" }

group = "${project.properties["LIB_GROUP"]}"
version = libraryVersionName

println("[:library] Release build: $isReleaseBuild  —  version: $version  —  build: $buildChecksum")

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

// Launch options are generated from the native SDK option manifests. The snapshots in
// options/ are committed so builds are reproducible offline; refresh them with
// `./gradlew :library:updateBugseeOptionsManifests` after bumping an SDK version.
val bugseeOptionsDir = layout.projectDirectory.dir("options")
val bugseeAndroidVersion = libs.versions.bugsee.android.get()
val bugseeIosOptionsVersion = libs.versions.bugsee.ios.get()

tasks.register<UpdateBugseeOptionsManifestsTask>("updateBugseeOptionsManifests") {
    group = "bugsee"
    description = "Downloads the Bugsee SDK option manifests into library/options/."
    androidVersion.set(bugseeAndroidVersion)
    iosVersion.set(bugseeIosOptionsVersion)
    baseUrl.set("https://download.bugsee.com/sdk/options")
    outputDir.set(bugseeOptionsDir)
}

val generateBugseeOptions = tasks.register<GenerateBugseeOptionsTask>("generateBugseeOptions") {
    group = "bugsee"
    description = "Generates BugseeOptions and BugseeLaunchOptions from the option manifests."
    androidManifest.set(bugseeOptionsDir.file("android-$bugseeAndroidVersion.json"))
    iosManifest.set(bugseeOptionsDir.file("ios-$bugseeIosOptionsVersion.json"))
    config.set(bugseeOptionsDir.file("kmp-options.json"))
    outputDir.set(layout.buildDirectory.dir("generated/bugseeOptions/commonMain/kotlin"))
}

// A local iOS SDK build for SDK development: bugsee.ios.localPackage is the directory of a Swift
// package with the Bugsee product, e.g. a binaryTarget pointing at a locally built
// Bugsee.xcframework (see buildSrc LocalIosPackages and CLAUDE.md). A blank value means unset, so
// -Pbugsee.ios.localPackage= builds against the remote package.
val bugseeIosLocalPackage: String? = localIosPackage(LocalIosPackages.CORE)

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    androidTarget {
        // Publish only the `release` Android variant to Maven Central; debug variants are not shipped.
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    // Published iOS targets — device (arm64) + both simulator slices so
    // consumers can link against the artifact from the iOS simulator as well
    // as real devices.
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }

        all {
            languageSettings.apply {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("kotlinx.cinterop.UnsafeNumber")
                optIn("kotlin.experimental.ExperimentalNativeApi")
            }
        }

        val commonMain by getting {
            kotlin.srcDir(generateBugseeOptions)
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Core SDK only. Network-client extensions (OkHttp, Ktor, Cronet, Compose) are
                // added by the consumer app: the Bugsee Gradle plugin auto-adds them for plain
                // Android app modules, but does not see dependencies declared in KMP source sets.
                implementation(libs.bugsee.android)
            }
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.robolectric)
                implementation(libs.junit)
                implementation(libs.mockito.core)
                implementation(libs.mockito.kotlin)
                implementation(libs.bugsee.android)
                implementation(libs.androidx.fragment)
            }
        }
    }

    // Kotlin/Native cinterop is generated from the iOS SDK's SwiftPM package
    // (package swiftPMImport.com.bugsee.library). iOS SDK 7 is not published to CocoaPods.
    // The dependency is published with the library (the swiftpm-metadata variant), so a
    // consumer's Kotlin Gradle plugin links Bugsee into its tests and framework and adds the
    // package to the app through its linkage package (integrateLinkagePackage).
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("15.0")
        // .swiftpm-locks is shared by the whole build: with any local package it would record the
        // local path, so leave it alone.
        if (usesLocalIosPackage) {
            packageResolvedSynchronization = noSynchronization()
        }
        val localPackage = bugseeIosLocalPackage
        if (localPackage != null) {
            localSwiftPackage(
                directory = rootProject.layout.projectDirectory.dir(localPackage),
                products = listOf("Bugsee"),
            )
        } else {
            swiftPackage(
                url = url("https://github.com/bugsee/spm.git"),
                version = exact(libs.versions.bugsee.ios.get()),
                products = listOf(product("Bugsee")),
            )
        }
    }
}

// Generates a `com.bugsee.kmp.BuildKonfig` object in commonMain with compile-time
// constants. `libraryVersion` is consumed by BugseeLaunchOptions.getWrapperInfo()
// so the value reported to the Bugsee backend always matches the published artifact.
buildkonfig {
    packageName = "com.bugsee.kmp"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "libraryVersion", libraryVersionName)
        buildConfigField(FieldSpec.Type.STRING, "buildChecksum", buildChecksum)
    }
}

// Print each test method and its result in CI logs (applies to all test tasks).
// outputs.upToDateWhen { false } forces re-execution so results always appear,
// even when Gradle considers the task UP-TO-DATE from a prior build on CI.
tasks.withType<AbstractTestTask> {
    outputs.upToDateWhen { false }
    testLogging {
        events("passed", "skipped", "failed")
    }
}

android {
    namespace = "com.bugsee.kmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

// Maven Central publishing via the vanniktech maven-publish plugin.
//
// Required local setup (in ~/.gradle/gradle.properties — NOT this repo):
//   mavenCentralUsername=<central portal user-token name>
//   mavenCentralPassword=<central portal user-token password>
//   signing.keyId=<last 8 chars of the GPG key id>
//   signing.password=<GPG key passphrase>
//   signing.secretKeyRingFile=<absolute path to GPG secring.gpg>
//
// Generate the user token at https://central.sonatype.com/account
// Export a legacy secring.gpg with:  gpg --export-secret-keys -o ~/.gnupg/secring.gpg
//
// CI (GitHub Actions) cannot put a keyring file in a secret, so it signs in memory
// instead. The plugin picks these up and prefers them over the classic keys above:
//   ORG_GRADLE_PROJECT_signingInMemoryKey=<ASCII-armored GPG private key block>
//   ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=<GPG key passphrase>
// See .github/CI.md.
//
// Publish steps:
//   1. ./gradlew :library:publishToMavenLocal           (smoke test, writes to ~/.m2)
//   2. ./gradlew :library:publishAndReleaseToMavenCentral (uploads + auto-releases)
refusePublishingWithLocalIosPackage()

mavenPublishing {
    // Target the new Central Portal (https://central.sonatype.com), not the legacy OSSRH.
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // GPG-sign every publication (KMP root + android + iosarm64 + sources/javadoc).
    // Central Portal rejects unsigned artifacts, but local smoke tests
    // (./gradlew :library:publishToMavenLocal) shouldn't require GPG keys.
    // Sign only when a signing key is configured (classic keyring locally, in-memory key on CI).
    if (providers.gradleProperty("signing.keyId").isPresent ||
        providers.gradleProperty("signingInMemoryKey").isPresent
    ) {
        signAllPublications()
    }

    // Final coordinates: com.bugsee:bugsee-kotlin-multiplatform:<version>
    // Per-target artifacts derive from this base id automatically:
    //   *-android, *-iosarm64, *-kotlinMultiplatform (root metadata).
    coordinates("com.bugsee", "bugsee-kotlin-multiplatform", version.toString())

    pom {
        name.set("Bugsee Kotlin Multiplatform")
        description.set("Kotlin Multiplatform wrapper around the native Bugsee crash reporting SDKs for Android and iOS.")
        inceptionYear.set("2026")
        url.set("https://bugsee.com")

        licenses {
            license {
                name = "Proprietary"
                url = "https://bugsee.com/tos/"
            }
        }

        developers {
            developer {
                id.set("bugsee")
                name.set("Bugsee, Inc")
                email.set("support@bugsee.com")
                url.set("https://bugsee.com")
            }
        }

        scm {
            url = "https://bugsee.com/"
        }
    }
}
