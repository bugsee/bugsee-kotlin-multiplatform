package com.bugsee.kmp.sample

interface Platform {
    val name: String
    /**
     * The app token from local.properties ([SampleConfig]), or null when it is not set: the sample
     * then does not launch Bugsee.
     */
    val token: String?
    /**
     * Custom launch options: the endpoint from local.properties, when it is set, and the SDK's
     * debug logging. Their keys differ per platform.
     */
    val endpointLaunchOptions: Map<String, Any>
    val tempDir: String
}

expect fun getPlatform(): Platform

expect fun writeTextFile(path: String, content: String)
