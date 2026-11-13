import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    // Only the Compose compiler: the Compose Multiplatform plugin would tie the Compose
    // dependencies to the sample's version (see compose-multiplatform-protect in the catalog).
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

// Match :library exactly so the two artifacts ship in lockstep.
// RELEASE=true flips off the -SNAPSHOT suffix; see :library/build.gradle.kts for the rationale.

val isReleaseBuild: Boolean =
    (System.getenv("RELEASE") ?: project.findProperty("RELEASE")?.toString() ?: "false").toBoolean()
val libraryVersionName: String = rootProject.file("version.txt").readLines().first().trim() +
        if (isReleaseBuild) "" else "-SNAPSHOT"

group = providers.gradleProperty("LIB_GROUP").get()
version = libraryVersionName

println("[:library-protect] Release build: $isReleaseBuild  —  version: $version")

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    // The Android-KMP library plugin builds and publishes a single Android variant.
    android {
        namespace = "com.bugsee.kmp.protect"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    // iOS target set MUST match :library (this module depends on it transitively),
    // so device + both simulator slices are all published.
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }

        commonMain {
            dependencies {
                // Depends on the main library for com.bugsee.kmp.Bugsee.addSecureView().
                // `implementation` (not `api`): consumers must add bugsee-kotlin-multiplatform
                // themselves to call Bugsee.launch(); the protect library does not re-export it.
                implementation(project(":library"))

                implementation(libs.compose.protect.runtime)
                implementation(libs.compose.protect.foundation)
                implementation(libs.compose.protect.ui)
            }
        }
        androidMain {
            dependencies {
                // Modifier.bugseeSecure(), which BugseeProtect uses on Android. It also gives every
                // app with this artifact the SDK's Compose support (Compose password fields are
                // masked); its Compose dependency is compileOnly, so it brings no Compose version.
                implementation(libs.bugsee.android.compose)
            }
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
    publishToMavenCentral()

    // Sign only when a signing key is configured, so local smoke tests
    // (./gradlew :library-protect:publishToMavenLocal) work without GPG keys.
    // Classic keyring locally, in-memory key on CI — see :library/build.gradle.kts.
    if (providers.gradleProperty("signing.keyId").isPresent ||
        providers.gradleProperty("signingInMemoryKey").isPresent
    ) {
        signAllPublications()
    }

    // Final coordinates: com.bugsee:bugsee-kotlin-multiplatform-protect:<version>
    coordinates("com.bugsee", "bugsee-kotlin-multiplatform-protect", version.toString())

    pom {
        name.set("Bugsee Kotlin Multiplatform Protect")
        description.set("Compose Multiplatform helper that hides content from Bugsee video recordings and screenshots. Requires bugsee-kotlin-multiplatform.")
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
