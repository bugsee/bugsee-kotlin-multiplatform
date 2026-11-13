package com.bugsee.gradle

import org.gradle.api.Project

/**
 * The Bugsee apps the sample reports to, set in local.properties or with -P, so that no app token
 * is committed. Without the token of a platform the sample does not launch Bugsee there, the
 * Android Gradle plugin uploads no mapping and the iOS scheme uploads no dSYMs.
 */
object SampleBugseeConfig {
    /** App token of the Android sample. */
    const val ANDROID_TOKEN = "bugsee.sample.android.token"

    /** App token of the iOS sample. */
    const val IOS_TOKEN = "bugsee.sample.ios.token"

    /**
     * API base of both apps, e.g. `https://apidev.bugsee.com`; unset means the SDKs' default
     * (production). The iOS SDK endpoint is this base plus `/v2`.
     */
    const val ENDPOINT = "bugsee.sample.endpoint"
}

/** [SampleBugseeConfig.ENDPOINT] without a trailing slash or `/v2`, or null when unset. */
val Project.sampleBugseeEndpoint: String?
    get() = localProperty(SampleBugseeConfig.ENDPOINT)?.trim()?.removeSuffix("/")?.removeSuffix("/v2")
