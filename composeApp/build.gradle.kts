import com.bugsee.gradle.SampleBugseeConfig
import com.bugsee.gradle.localProperty
import com.bugsee.gradle.sampleBugseeEndpoint
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The shared UI of the sample: an Android-KMP library used by :androidApp, and the iOS framework
// that iosApp links. Since AGP 9 the Android application itself lives in :androidApp.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildKonfig)
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

// The Bugsee apps the sample reports to come from local.properties (see SampleBugseeConfig), so no
// app token is committed. A blank value means unset.
val sampleAndroidToken: String = localProperty(SampleBugseeConfig.ANDROID_TOKEN).orEmpty()
val sampleIosToken: String = localProperty(SampleBugseeConfig.IOS_TOKEN).orEmpty()
val sampleEndpoint: String = sampleBugseeEndpoint.orEmpty()

// Generates `com.bugsee.kmp.sample.SampleConfig` in commonMain; Platform.*.kt read it.
buildkonfig {
    packageName = "com.bugsee.kmp.sample"
    objectName = "SampleConfig"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "androidToken", sampleAndroidToken)
        buildConfigField(FieldSpec.Type.STRING, "iosToken", sampleIosToken)
        buildConfigField(FieldSpec.Type.STRING, "endpoint", sampleEndpoint)
    }
}

// The iosApp scheme's Build post-action uploads the dSYMs with the token and API base in this
// file. It is written when Xcode builds the Kotlin framework, which comes before the post-action,
// so the upload follows local.properties like the app does.
val writeIosSymbolUploadConfig = tasks.register("writeIosSymbolUploadConfig") {
    val token = sampleIosToken
    val endpoint = sampleEndpoint
    val output = layout.buildDirectory.file("bugsee/ios-symbol-upload.properties")
    inputs.property("token", token)
    inputs.property("endpoint", endpoint)
    outputs.file(output)
    doLast {
        output.get().asFile.writeText("token=$token\nendpoint=$endpoint\n")
    }
}

tasks.matching { it.name == "embedAndSignAppleFrameworkForXcode" }.configureEach {
    dependsOn(writeIosSymbolUploadConfig)
}
