import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

// Match :library exactly so the artifacts ship in lockstep.
// RELEASE=true flips off the -SNAPSHOT suffix; see :library/build.gradle.kts for the rationale.

val isReleaseBuild: Boolean =
    (System.getenv("RELEASE") ?: project.findProperty("RELEASE")?.toString() ?: "false").toBoolean()
val libraryVersionName: String = rootProject.file("version.txt").readLines().first().trim() +
        if (isReleaseBuild) "" else "-SNAPSHOT"

group = "${project.properties["LIB_GROUP"]}"
version = libraryVersionName

println("[:library-feedback] Release build: $isReleaseBuild  —  version: $version")

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    androidTarget {
        // Publish only the `release` variant — debug variants are not shipped.
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
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
        val androidUnitTest by getting {
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
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    swiftPMDependencies {
        iosMinimumDeploymentTarget.set("15.0")
        swiftPackage(
            url = url("https://github.com/bugsee/feedback-spm.git"),
            version = exact(libs.versions.bugsee.ios.get()),
            products = listOf(product("BugseeFeedback")),
        )
    }
}

android {
    namespace = "com.bugsee.kmp.feedback"
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
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

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
