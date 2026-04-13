import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlinCocoapods)
}

// Maven coordinates for the published artifact:
//   group:    com.bugsee  (matches the existing com.bugsee:bugsee-android namespace on Maven Central)
//   version:  bumped here for every release; the cocoapods{} block below reuses this same value
group = "${project.properties["LIB_GROUP"]}"
version = "${project.properties["LIB_VERSION"]}"

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

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

    // Published iOS targets. Currently device-only (real iPhones, arm64).
    // Note: without iosSimulatorArm64 / iosX64, consumers cannot run their app
    // in the iOS simulator against this artifact — they must use a real device.
    iosArm64()

    // Simulator targets — intentionally disabled for now.
    // Adding a second iOS target forces Kotlin/Native's platform-library
    // commonizer to run across iosArm64 + iosSimulatorArm64, which on Xcode
    // 16.x fails with "Unresolved classifier: platform/Metal/…" — a known
    // Kotlin platform-library bug, unrelated to our Bugsee cinterop. Until
    // that's resolved upstream, simulator tests need a different path (see
    // docs / discuss with the team).
    // iosX64()
    // iosSimulatorArm64()

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
                 implementation(libs.bugsee.android)
                 
                 // Compile-only dependencies for Bugsee SDK integration
                 compileOnly(libs.okhttp3)
                 compileOnly(libs.animal.sniffer.annotations)
                 compileOnly(libs.jsr305)
                 compileOnly(libs.conscrypt.openjdk.uber)
                 
                 compileOnly(libs.okhttp2)
                 compileOnly(libs.material)
                 compileOnly(libs.picasso)
                 
                 compileOnly(libs.kotlin.stdlib.jdk8)
                 compileOnly(libs.kotlinx.coroutines.core)
                 compileOnly(libs.ktor.client.core)
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

    // Kotlin/Native cinterop is generated from the Bugsee CocoaPod at build time.
    // The published .klib only contains Objective-C bindings — consumers must
    // supply the actual Bugsee framework themselves (e.g. by adding
    // `pod 'Bugsee', '~> 6.1.2'` to their own Podfile, or via the Kotlin
    // cocoapods plugin in their KMP project).
    cocoapods {
        summary = "Bugsee Kotlin Multiplatform Library"
        homepage = "https://bugsee.com"
        // Reuse the Maven version so the auto-generated library.podspec stays in sync.
        version = project.version.toString()

        pod("Bugsee") {
            version = libs.versions.bugsee.ios.get()
            // -fmodules is required so clang can resolve Bugsee's @import statements
            // when generating the cinterop klib.
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        /*
         * If you want to debug Bugsee XCF,
         * then create local Bugsee pod,
         * and use below pod module description
         */
//        pod("Bugsee") {
//            version = "26.0.0"
//            source = path(project.file("/Users/dsheikherev/WorkDir/Cocoapods/Local/Bugsee"))
//            extraOpts += listOf("-compiler-option", "-fmodules")
//        }

        ios.deploymentTarget = "12.0"
    }

    // Fix for Xcode 16.4 compatibility issues
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            // Remove problematic compiler args
        }
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
//   signingInMemoryKey=<ASCII-armored GPG private key body>
//   signingInMemoryKeyId=<last 8 chars of the GPG key id>
//   signingInMemoryKeyPassword=<GPG key passphrase>
//
// Generate the user token at https://central.sonatype.com/account
// Export the GPG key with:  gpg --export-secret-keys --armor <key-id>
//
// Publish steps:
//   1. ./gradlew :library:publishToMavenLocal           (smoke test, writes to ~/.m2)
//   2. ./gradlew :library:publishAndReleaseToMavenCentral (uploads + auto-releases)
mavenPublishing {
    // Target the new Central Portal (https://central.sonatype.com), not the legacy OSSRH.
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // GPG-sign every publication (KMP root + android + iosarm64 + sources/javadoc).
    // Central Portal rejects unsigned artifacts, but local smoke tests
    // (./gradlew :library:publishToMavenLocal) shouldn't require GPG keys.
    // Sign only when a signing key is configured.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
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
