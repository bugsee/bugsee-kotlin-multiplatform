@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package com.bugsee.kmp.nsexception

import kotlinx.cinterop.convert
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSException
import platform.Foundation.NSNumber
import platform.darwin.NSUInteger
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicReference
import kotlin.experimental.ExperimentalNativeApi

private typealias BugseeSDK = cocoapods.Bugsee.Bugsee

/**
 * Wraps the unhandled exception hook such that the provided hook is invoked
 * before the currently set unhandled exception hook is invoked.
 * Note: once the unhandled exception hook returns the program will be terminated.
 * @see setUnhandledExceptionHook
 * @see terminateWithUnhandledException
 */
@OptIn(ExperimentalNativeApi::class)
internal fun setBugseeUnhandledExceptionHook() {
    val unhandledExceptionCrashed = AtomicInt(0)
    val prevHook = AtomicReference<ReportUnhandledExceptionHook?>(null)
    val wrappedHook: ReportUnhandledExceptionHook = { throwable ->
        // We only handle a single Kotlin crash
        if (unhandledExceptionCrashed.compareAndSet(0, 1)) {
            // TODO: Implement NSException instance from Throwable
            val exception = BugseeNSException(throwable)
            BugseeSDK.logUnhandledException(exception, null)
        }

        prevHook.value?.invoke(throwable)
        terminateWithUnhandledException(throwable)
    }

    prevHook.value = setUnhandledExceptionHook(wrappedHook)
}

private val Throwable.name: String
    get() = this::class.qualifiedName ?: this::class.simpleName ?: "Throwable"

internal class BugseeNSException(
    name: String,
    reason: String?,
    private val stackFrameAddresses: List<NSNumber>,
) : NSException(name, reason, null) {
    constructor(throwable: Throwable) : this(
        throwable.name,
        throwable.message,
        throwable.getStackTraceAddresses().map { address ->
            NSNumber(unsignedInteger = address.convert<NSUInteger>())
        },
    )

    override fun callStackReturnAddresses(): List<*> {
        return stackFrameAddresses
    }
}