@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package com.bugsee.kmp.nsexception

import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicReference
import kotlin.experimental.ExperimentalNativeApi

private typealias BugseeSDK = swiftPMImport.com.bugsee.library.Bugsee

// Set once the hook is installed: every successful launch and relaunch asks for it, and a second
// hook would wrap the first, so one Kotlin crash would be logged once per launch.
private val hookInstalled = AtomicInt(0)

/**
 * Wraps the unhandled exception hook such that the provided hook is invoked
 * before the currently set unhandled exception hook is invoked. Only the first call per process
 * installs it; later calls (another successful launch or relaunch) do nothing.
 * Note: once the unhandled exception hook returns the program will be terminated.
 * @see setUnhandledExceptionHook
 * @see terminateWithUnhandledException
 */
@OptIn(ExperimentalNativeApi::class)
internal fun setBugseeUnhandledExceptionHook() {
    if (!hookInstalled.compareAndSet(0, 1)) return

    val unhandledExceptionCrashed = AtomicInt(0)
    val prevHook = AtomicReference<ReportUnhandledExceptionHook?>(null)
    val wrappedHook: ReportUnhandledExceptionHook = { throwable ->
        // We only handle a single Kotlin crash
        if (unhandledExceptionCrashed.compareAndSet(0, 1)) {
            val exception = BugseeNSException(throwable)
            BugseeSDK.logUnhandledException(exception, null)
        }

        prevHook.value?.invoke(throwable)
        terminateWithUnhandledException(throwable)
    }

    prevHook.value = setUnhandledExceptionHook(wrappedHook)
}
