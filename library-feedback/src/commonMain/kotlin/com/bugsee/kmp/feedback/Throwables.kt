package com.bugsee.kmp.feedback

/**
 * [error] as text, for a log line written while handling it. An app's exception can throw from
 * `toString`, and that would escape the catch block that is logging it. The same as the core
 * library's helper, which is internal to that module.
 */
internal fun describeThrowable(error: Throwable): String =
    try {
        error.toString()
    } catch (ignored: Throwable) {
        error::class.simpleName ?: "Throwable"
    }
