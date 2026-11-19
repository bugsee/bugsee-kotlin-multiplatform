package com.bugsee.kmp.internal

/**
 * [error] as text, for a log line written while handling it. An app's exception can throw from
 * `toString`, and that would escape the catch block that is logging it.
 */
internal fun describeThrowable(error: Throwable): String =
    try {
        error.toString()
    } catch (ignored: Throwable) {
        error::class.simpleName ?: "Throwable"
    }
