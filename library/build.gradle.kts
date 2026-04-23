import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.buildKonfig)
}

// Maven coordinates for the published artifact:
//   group:    com.bugsee  (matches the existing com.bugsee:bugsee-android namespace on Maven Central)
//   version:  read from repo-root version.txt (e.g. 0.1.0) — a SNAPSHOT suffix is appended
//             automatically unless RELEASE=true is set in the environment
//             (or -PRELEASE=true on the Gradle CLI). Convention matches the legacy
//             bugsee-android SDK's release script. The cocoapods{} block and BuildKonfig
//             libraryVersion field below both reuse the resolved value.
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
    // `pod 'Bugsee'` to their own Podfile, or via the Kotlin
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
    if (providers.gradleProperty("signing.keyId").isPresent) {
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
