package com.bugsee.kmp.internal

import com.bugsee.library.contracts.options.LogLevel

private const val TAG = "BugseeLogFilter"

// Bugsee.log lines are not passed through the log filter. The logcat line is read back by the
// SDK and filtered again, but the reporter has already reported, so nothing more is written.
internal actual fun reportLogFilterFailure(message: String) {
    com.bugsee.library.Bugsee.log(message, LogLevel.Error)
    Logger.w(TAG, message)
}
