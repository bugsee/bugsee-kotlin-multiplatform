import com.bugsee.gradle.LocalIosPackages
import com.bugsee.gradle.localIosPackage
import com.bugsee.gradle.refusePublishingWithLocalIosPackage
import com.bugsee.gradle.usesLocalIosPackage
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

// Match :library exactly so the artifacts ship in lockstep.
// RELEASE=true flips off the -SNAPSHOT suffix; see :library/build.gradle.kts for the rationale.

val isReleaseBuild: Boolean =
    (System.getenv("RELEASE") ?: project.findProperty("RELEASE")?.toString() ?: "false").toBoolean()
val libraryVersionName: String = rootProject.file("version.txt").readLines().first().trim() +
        if (isReleaseBuild) "" else "-SNAPSHOT"

group = providers.gradleProperty("LIB_GROUP").get()
version = libraryVersionName

println("[:library-feedback] Release build: $isReleaseBuild  —  version: $version")

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    // The Android-KMP library plugin builds and publishes a single Android variant.
    android {
        namespace = "com.bugsee.kmp.feedback"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        // Robolectric tests (androidHostTest).
        withHostTest {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // iOS target set MUST match :library (this module depends on it), so device + both
    // simulator slices are all published.
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        commonMain.dependencies {
            // `implementation` (not `api`), as in :library-protect: the app adds
            // bugsee-kotlin-multiplatform itself to launch Bugsee; this module only keeps the
            // two artifacts on the same version.
            implementation(project(":library"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            // The Feedback add-on of Android SDK 7, in lockstep with the core SDK :library links.
            implementation(libs.bugsee.android.feedback)
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.robolectric)
                implementation(libs.junit)
            }
        }
    }

    // The Feedback add-on of iOS SDK 7: the Swift package github.com/bugsee/feedback-spm, whose
    // @objc API is imported as swiftPMImport.com.bugsee.library.feedback. Each release pins the
    // core package (github.com/bugsee/spm) to the same exact version, so it follows bugsee-ios.
    // The dependency is published with the module, so a consumer's Kotlin Gradle plugin adds the
    // package to the app through its linkage package, as for :library.
    //
    // Local iOS SDK builds (see buildSrc LocalIosPackages and CLAUDE.md):
    // - bugsee.ios.localFeedbackPackage replaces feedback-spm with a local BugseeFeedback package;
    // - otherwise, with bugsee.ios.localPackage, feedback-spm's core dependency must resolve to
    //   the local core. SwiftPM does that only for a local package with the same identity, so
    //   the local core's folder must be named "spm".
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("15.0")
        if (usesLocalIosPackage) {
            packageResolvedSynchronization = noSynchronization()
        }
        val localFeedbackPackage = localIosPackage(LocalIosPackages.FEEDBACK)
        val localCorePackage = localIosPackage(LocalIosPackages.CORE)
        if (localFeedbackPackage != null) {
            localSwiftPackage(
                directory = rootProject.layout.projectDirectory.dir(localFeedbackPackage),
                products = listOf("BugseeFeedback"),
            )
        } else {
            val localCoreFolder = localCorePackage?.let { rootProject.file(it).name }
            if (localCoreFolder != null && !localCoreFolder.equals("spm", ignoreCase = true)) {
                // Fails the builds that resolve feedback-spm (this module's and the sample's Swift
                // package tasks, IDE sync included): SwiftPM would fail them anyway, on targets
                // named Bugsee in both the local core and github.com/bugsee/spm, with a less clear
                // error. Android builds and :library's iOS builds keep working with any folder name.
                val swiftPMWork = Regex("swiftpm|syntheticimport|syntheticlinkage|linkagepackage|embedandsign", RegexOption.IGNORE_CASE)
                val feedbackProjects = setOf(project.path, ":composeApp")
                gradle.taskGraph.whenReady {
                    if (allTasks.any { it.project.path in feedbackProjects && swiftPMWork.containsMatchIn(it.name) }) {
                        throw GradleException(
                            "${LocalIosPackages.CORE} points to '$localCoreFolder': feedback-spm depends on " +
                                "github.com/bugsee/spm, and SwiftPM uses a local core for it only when the " +
                                "local package's folder is named 'spm'. Rename the folder, or set " +
                                "${LocalIosPackages.FEEDBACK} to a local BugseeFeedback package that depends " +
                                "on your local core by path."
                        )
                    }
                }
            }
            swiftPackage(
                url = url("https://github.com/bugsee/feedback-spm.git"),
                version = exact(libs.versions.bugsee.ios.get()),
                products = listOf(product("BugseeFeedback")),
            )
        }
    }
}

refusePublishingWithLocalIosPackage()

tasks.withType<AbstractTestTask> {
    outputs.upToDateWhen { false }
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// Maven Central publishing — same conventions as :library.
// Required local setup (~/.gradle/gradle.properties keys, GPG key export, publish
// commands) is documented at the top of the mavenPublishing block in :library/build.gradle.kts.
mavenPublishing {
    publishToMavenCentral()

    // Sign only when a signing key is configured, so local smoke tests
    // (./gradlew :library-feedback:publishToMavenLocal) work without GPG keys.
    if (providers.gradleProperty("signing.keyId").isPresent ||
        providers.gradleProperty("signingInMemoryKey").isPresent
    ) {
        signAllPublications()
    }

    // Final coordinates: com.bugsee:bugsee-kotlin-multiplatform-feedback:<version>
    coordinates("com.bugsee", "bugsee-kotlin-multiplatform-feedback", version.toString())

    pom {
        name.set("Bugsee Kotlin Multiplatform Feedback")
        description.set("In-app feedback chat for Bugsee on Android and iOS: wraps the Bugsee SDK 7 Feedback add-ons. Requires bugsee-kotlin-multiplatform.")
        inceptionYear.set("2026")
        url.set("https://bugsee.com")

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
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
