import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The shared UI of the sample: an Android-KMP library used by :androidApp, and the iOS framework
// that iosApp links. Since AGP 9 the Android application itself lives in :androidApp.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.bugsee.kmp.sample"
version = "1.0-SNAPSHOT"

kotlin {
    // SampleSettings and AppLogger are expect objects.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.bugsee.kmp.sample.shared"
        compileSdk = libs.versions.sample.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        // Compose resources (composeResources/) are packaged as Android assets.
        androidResources {
            enable = true
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
            // BackHandler for the system Back (SampleBackHandler).
            implementation(libs.androidx.activity.compose)
            implementation("io.ktor:ktor-client-android:${libs.versions.ktor3.get()}")
            // WebSockets: the Android engine has none (see SampleHttpClients).
            implementation("io.ktor:ktor-client-okhttp:${libs.versions.ktor3.get()}")
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
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
