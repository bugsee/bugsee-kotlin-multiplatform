package com.bugsee.kmp

/**
 * How this library identifies itself to the native SDKs, which attach it to each session as
 * `sdk.wrapper`. Both read it from `BugseeWrapperAdapter`, which `BugseeInternal` registers with
 * the SDK's `setWrapper` before any launch.
 */
internal object BugseeWrapperInfo {
    const val TYPE: String = "kmp"

    val version: String get() = BuildKonfig.libraryVersion

    val build: String get() = BuildKonfig.buildChecksum

    /** Reported under `context`, as `runtime`. */
    val runtime: String get() = KotlinVersion.CURRENT.toString()
}
