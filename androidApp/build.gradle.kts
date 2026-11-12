import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The Android app of the sample. Since AGP 9 an Android application module cannot also apply
// Kotlin Multiplatform, so the shared UI lives in :composeApp (an Android-KMP library that also
// builds the iOS framework) and this module holds only the Android entry point. Kotlin is built
// into AGP 9, so no Kotlin Android plugin is applied.
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.bugsee.gradle.plugin)
}

// The NDK crash handler, which captureCrashSignals configures on Android. It is in the sample by
// default; set bugsee.sample.ndk=false (-P, or in local.properties) to build the app without it.
// The Bugsee Gradle plugin adds bugsee-android-ndk as a version range when its ndk block is
// enabled; the catalog dependency below keeps it on the core SDK's version.
val sampleWithNdk: Boolean = run {
    val local = rootProject.file("local.properties").takeIf { it.isFile }
        ?.let { file -> Properties().apply { file.inputStream().use { load(it) } } }
    (providers.gradleProperty("bugsee.sample.ndk").orNull ?: local?.getProperty("bugsee.sample.ndk"))
        ?.trim()?.toBoolean() ?: true
}


android {
    namespace = "com.bugsee.kmp.sample"
    compileSdk = libs.versions.sample.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bugsee.kmp.sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    bugsee {
        appToken("629d2d4a-8804-4210-8951-3b3c3f83761a")
        endpoint = "https://apidev.bugsee.com"  // custom endpoint (optional) "https://apidev.bugsee.com"
        debug = true                         // enable plugin debug logging
        ndk {                                 // adds bugsee-android-ndk, uploads NDK debug symbols
            enabled.set(sampleWithNdk)
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    if (sampleWithNdk) {
        implementation(libs.bugsee.android.ndk)
    }
    implementation(project(":composeApp"))
    // OkHttp capture, including the WebSocket test's OkHttp WebSockets. The Bugsee Gradle plugin
    // adds network extensions only for dependencies it sees in this module, not in :composeApp.
    implementation(libs.bugsee.android.okhttp)
    // Compose support of the Android SDK: masks Compose password fields in the video and hides
    // taps on them. The Bugsee Gradle plugin adds it only when this module declares an
    // androidx.compose dependency itself, and the sample's Compose comes through :composeApp.
    implementation(libs.bugsee.android.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
