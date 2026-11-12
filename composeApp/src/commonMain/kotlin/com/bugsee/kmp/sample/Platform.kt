package com.bugsee.kmp.sample

interface Platform {
    val name: String
    val token: String
    val appdevEndpoint: String
    /**
     * Custom launch options that send reports to [appdevEndpoint] and turn on the SDK's debug
     * logging. Their keys differ per platform.
     */
    val endpointLaunchOptions: Map<String, Any>
    val tempDir: String
}

expect fun getPlatform(): Platform

expect fun writeTextFile(path: String, content: String)
