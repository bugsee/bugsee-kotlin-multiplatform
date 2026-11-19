package com.bugsee.kmp.internal

import android.util.Log

internal actual object Logger {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            // Called from catch blocks: the stack trace of an app's throwable calls its toString,
            // which can throw.
            try {
                Log.e(tag, message, throwable)
            } catch (ignored: Throwable) {
                Log.e(tag, "$message: ${describeThrowable(throwable)}")
            }
        } else {
            Log.e(tag, message)
        }
    }

    actual fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    actual fun i(tag: String, message: String) {
        Log.i(tag, message)
    }
}