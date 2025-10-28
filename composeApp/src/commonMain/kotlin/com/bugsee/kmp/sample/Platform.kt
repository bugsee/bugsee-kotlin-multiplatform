package com.bugsee.kmp.sample

interface Platform {
    val name: String

    val token: String

    val appdevEndpoint: String
}

expect fun getPlatform(): Platform