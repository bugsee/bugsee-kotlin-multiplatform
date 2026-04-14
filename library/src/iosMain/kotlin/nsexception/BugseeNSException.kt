@file:OptIn(ExperimentalNativeApi::class)

package com.bugsee.kmp.nsexception

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.getStackTraceAddresses
import platform.Foundation.NSException
import platform.Foundation.NSNumber

public class BugseeNSException private constructor(
    name: String,
    reason: String?,
    private val stackFrameAddresses: List<NSNumber>,
    private val stackFrameSymbols: List<String>,
) : NSException(name, reason, null) {
    public constructor(throwable: Throwable) : this(
        name = throwable.name,
        reason = throwable.message,
        stackFrameAddresses = throwable.getStackTraceAddresses().map { address ->
            NSNumber(unsignedLongLong = address.toULong())
        },
        stackFrameSymbols = throwable.getStackTrace().toList(),
    )

    override fun callStackReturnAddresses(): List<*> = stackFrameAddresses

    override fun callStackSymbols(): List<*> = stackFrameSymbols
}

private val Throwable.name: String
    get() = "KmpManagedException: " + (this::class.qualifiedName ?: this::class.simpleName ?: "Throwable")
