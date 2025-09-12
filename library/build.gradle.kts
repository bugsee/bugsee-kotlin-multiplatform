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

group = "com.bugsee.kmp"
version = "1.0.0"

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    iosArm64() // Declares a target that corresponds to 64-bit iPhones

    // These are simulators targets. Commented out
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

    cocoapods {
        summary = "Bugsee Kotlin Multiplatform Library"
        homepage = "https://bugsee.com"
        version = "0.0.1"

        pod("Bugsee") {
            version = libs.versions.bugsee.ios.get()
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

        ios.deploymentTarget = "11.0"
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "library", version.toString())

    pom {
        name = "Bugsee"
        description = "A library."
        inceptionYear = "2024"
        url = "https://github.com/kotlin/multiplatform-library-template/"
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }
        developers {
            developer {
                id = "XXX"
                name = "YYY"
                url = "ZZZ"
            }
        }
        scm {
            url = "XXX"
            connection = "YYY"
            developerConnection = "ZZZ"
        }
    }
}
