package com.bugsee.kmp.internal

import com.bugsee.library.contracts.options.LogLevel

private const val TAG = "BugseeFilter"

// Bugsee.log lines are not passed through the log filter. The logcat line is read back by the
// SDK and filtered again, but the reporter has already reported, so nothing more is written.
// The failure is reported once, so the logcat line is written even if Bugsee.log throws.
internal actual fun reportFilterFailure(message: String) {
    try {
        com.bugsee.library.Bugsee.log(message, LogLevel.Error)
    } finally {
        Logger.w(TAG, message)
    }
}
