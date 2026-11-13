package com.bugsee.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.withType

/**
 * Local iOS SDK packages for SDK development. Each is the directory (absolute, or relative to
 * the repository root) of a Swift package, set in local.properties, which the Xcode build
 * phase also reads, or with -P. A blank value means unset.
 */
object LocalIosPackages {
    /** The core SDK: a package with the `Bugsee` product. */
    const val CORE = "bugsee.ios.localPackage"

    /** The Feedback add-on: a package with the `BugseeFeedback` product. */
    const val FEEDBACK = "bugsee.ios.localFeedbackPackage"
}

/** The value of [name] from -P or local.properties, or null when it is unset or blank. */
fun Project.localIosPackage(name: String): String? = localProperty(name)

/** True when either local iOS package is set. */
val Project.usesLocalIosPackage: Boolean
    get() = localIosPackage(LocalIosPackages.CORE) != null ||
        localIosPackage(LocalIosPackages.FEEDBACK) != null

/**
 * Fails every Maven publication of this project while a local iOS package is set: the SwiftPM
 * metadata published with it would carry a machine-local path.
 */
fun Project.refusePublishingWithLocalIosPackage() {
    if (!usesLocalIosPackage) return
    tasks.withType<AbstractPublishToMaven>().configureEach {
        doFirst {
            throw GradleException(
                "${LocalIosPackages.CORE} or ${LocalIosPackages.FEEDBACK} is set; " +
                    "unset it before publishing."
            )
        }
    }
}
