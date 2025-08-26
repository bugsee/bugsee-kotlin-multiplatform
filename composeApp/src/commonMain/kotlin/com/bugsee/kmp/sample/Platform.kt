package com.bugsee.kmp.sample

interface Platform {
    val name: String

    val token: String
}

expect fun getPlatform(): Platform