package com.bugsee.kmpsample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform