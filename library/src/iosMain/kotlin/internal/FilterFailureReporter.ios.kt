package com.bugsee.kmp.internal

import com.bugsee.kmp.BugseeLogLevel
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

private const val TAG = "BugseeFilter"

// Bugsee.log lines are not passed through the log filter. The console line is written from
// another queue: the log filter runs on the SDK thread that reads stderr, and a write from that
// thread blocks for good once the pipe is full. The failure is reported once, so the console
// line is written even if Bugsee.log throws.
internal actual fun reportFilterFailure(message: String) {
    try {
        swiftPMImport.com.bugsee.library.Bugsee.log(message, BugseeLogLevel.Error.getLevelLong())
    } finally {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            Logger.w(TAG, message)
        }
    }
}
