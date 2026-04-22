import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktech.mavenPublish)
}

// Match :library exactly so the two artifacts ship in lockstep.
group = "${project.properties["LIB_GROUP"]}"
version = "${project.properties["LIB_VERSION"]}"

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

        val commonMain by getting {
            dependencies {
                // Depends on the main library for com.bugsee.kmp.Bugsee.addSecureView().
                // `implementation` (not `api`): consumers must add bugsee-kotlin-multiplatform
                // themselves to call Bugsee.launch(); the protect library does not re-export it.
                implementation(project(":library"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
            }
        }
    }
}

android {
    namespace = "com.bugsee.kmp.protect"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    // (./gradlew :library-protect:publishToMavenLocal) work without GPG keys.
    if (providers.gradleProperty("signing.keyId").isPresent) {
        signAllPublications()
    }

    // Final coordinates: com.bugsee:bugsee-kotlin-multiplatform-protect:<version>
    coordinates("com.bugsee", "bugsee-kotlin-multiplatform-protect", version.toString())

    pom {
        name.set("Bugsee Kotlin Multiplatform Protect")
        description.set("Compose Multiplatform helper that wraps content with a Bugsee secure overlay, hiding it from Bugsee video recordings and screenshots. Requires bugsee-kotlin-multiplatform.")
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
