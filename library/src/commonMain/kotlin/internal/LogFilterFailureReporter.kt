@file:OptIn(ExperimentalAtomicApi::class)

package com.bugsee.kmp.internal

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Reports a failure of the app's log filter, once per installed filter.
 *
 * Both SDKs pass the app's console output through the log filter: logcat on Android, and on iOS
 * stdout/stderr, read by a single SDK thread that also calls the filter. A failure written to the
 * console from inside the filter comes back to the same filter, so a filter that always throws
 * would run without end; on iOS that write can also block on the pipe only that thread drains,
 * which stops every console writer, the main thread included. So the failure is reported once,
 * through [report]: the platform writes it to the Bugsee log, which neither SDK passes through the
 * filter, and to the console without blocking the filter's thread.
 */
internal class LogFilterFailureReporter(
    private val report: (String) -> Unit = ::reportLogFilterFailure,
) {
    private val reported = AtomicBoolean(false)

    fun onFailure(error: Throwable) {
        if (!reported.compareAndSet(false, true)) return
        try {
            report(
                "Bugsee: the log filter threw ${describeThrowable(error)}; the line was kept. " +
                    "Further failures of this filter are not reported."
            )
        } catch (ignored: Throwable) {
            // Nothing may be written to the console from the filter's thread.
        }
    }
}

/** Writes [message] to the Bugsee log and to the console, without logging from the filter's thread. */
internal expect fun reportLogFilterFailure(message: String)
