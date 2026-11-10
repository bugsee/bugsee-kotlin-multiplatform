package com.bugsee.kmp.internal

import platform.Foundation.NSLog

internal actual object Logger {
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

    private fun log(line: String) {
        NSLog(escapeFormatSpecifiers(line))
    }
}

/**
 * NSLog takes its first argument as a format string, so a '%' in a message (a file path, an
 * exception text) would make it read arguments that were never passed and crash. The line is
 * escaped rather than passed as an argument: a Kotlin String is not an Objective-C object in this
 * variadic call, so `NSLog("%@", line)` crashes too, and `NSLog("%s", line)` mangles non-ASCII
 * text (it reads the UTF-8 bytes in the system encoding).
 */
internal fun escapeFormatSpecifiers(line: String): String = line.replace("%", "%%")
