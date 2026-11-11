package com.bugsee.kmp.sample

import platform.Foundation.NSLog

actual object AppLogger {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            log("ERROR: [$tag] $message. Throwable: $throwable CAUSE ${throwable.cause}")
        } else {
            log("ERROR: [$tag] $message")
        }
    }

    actual fun d(tag: String, message: String) {
        log("DEBUG: [$tag] $message")
    }

    actual fun i(tag: String, message: String) {
        log("INFO: [$tag] $message")
    }

    // NSLog takes its first argument as a format string: a '%' in a message (feedback text,
    // a file path) would make it read arguments that were never passed and crash.
    private fun log(line: String) {
        NSLog(line.replace("%", "%%"))
    }
}
