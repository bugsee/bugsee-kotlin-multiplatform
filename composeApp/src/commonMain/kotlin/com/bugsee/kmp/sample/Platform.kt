package com.bugsee.kmp.sample

interface Platform {
    val name: String

    val token: String

    val appdevEndpoint: String

    val tempDir: String
}

expect fun getPlatform(): Platform

expect fun writeTextFile(path: String, content: String)