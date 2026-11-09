import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.bugsee.gradle.plugin)
}

group = "com.bugsee.kmp.sample"
version = "1.0-SNAPSHOT"

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Built into iosApp by the embedAndSignAppleFrameworkForXcode build phase. Bugsee comes in
    // transitively from :library's SwiftPM dependency, through the linkage package in iosApp.
    iosArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
//    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("io.ktor:ktor-client-android:${libs.versions.ktor3.get()}")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(project(":library"))
            implementation(project(":library-protect"))
            implementation(project(":library-feedback"))
            implementation(libs.ktor3.client.core)
            implementation(libs.ktor3.client.websockets)
            implementation(libs.kotlinx.coroutines.core)
        }

        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:${libs.versions.ktor3.get()}")
        }

        all {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }
}

android {
    namespace = "com.bugsee.kmp.sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

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

    buildTypes {
        debug {
            // Enables code-related app optimization.
//            isMinifyEnabled = true

            // Enables resource shrinking.
//            isShrinkResources = true

//            proguardFiles(
//                // Default file with automatically generated optimization rules.
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//            )
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
        ndk {                                 // upload NDK debug symbols
            enabled.set(true)
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}