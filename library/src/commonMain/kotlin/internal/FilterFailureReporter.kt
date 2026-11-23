@file:OptIn(ExperimentalAtomicApi::class)

package com.bugsee.kmp.internal

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/** The app's filters whose failure drops the event they were given, as the SDKs do. */
internal enum class SdkFilter(val title: String, val item: String) {
    Log("log filter", "line"),
    Network("network filter", "event"),
}

/**
 * Reports a failure of the app's log or network filter, once per installed filter. The event the
 * filter was given is dropped, as both SDKs drop an event whose filter fails: a filter that
 * redacts must not let through what it failed to redact.
 *
 * Once, because a filter that throws throws on every event. For the log filter it is also the only
 * safe way: both SDKs pass the app's console output through it, logcat on Android, and on iOS
 * stdout/stderr, read by a single SDK thread that also calls the filter. A failure written to the
 * console from inside the filter comes back to the same filter, so a filter that always throws
 * would run without end; on iOS that write can also block on the pipe only that thread drains,
 * which stops every console writer, the main thread included. So the failure goes through
 * [report]: the platform writes it to the Bugsee log, which neither SDK passes through the log
 * filter, and to the console without blocking the filter's thread.
 */
internal class FilterFailureReporter(
    private val filter: SdkFilter,
    private val report: (String) -> Unit = ::reportFilterFailure,
) {
    private val reported = AtomicBoolean(false)

    fun onFailure(error: Throwable) {
        if (!reported.compareAndSet(false, true)) return
        try {
            report(
                "Bugsee: the ${filter.title} threw ${describeThrowable(error)}; the ${filter.item} was dropped. " +
                    "Further failures of this filter are not reported."
            )
        } catch (ignored: Throwable) {
            // Nothing may be written to the console from the filter's thread.
        }
    }
}

/** Writes [message] to the Bugsee log and to the console, without logging from the filter's thread. */
internal expect fun reportFilterFailure(message: String)
