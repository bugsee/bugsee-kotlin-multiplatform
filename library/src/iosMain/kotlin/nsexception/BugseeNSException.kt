package com.bugsee.kmp.nsexception

import kotlinx.cinterop.convert
import platform.Foundation.NSException
import platform.Foundation.NSNumber
import platform.darwin.NSUInteger

public class BugseeNSException(
    name: String,
    reason: String?,
    private val stackFrameAddresses: List<NSNumber>,
) : NSException(name, reason, null) {
    public constructor(throwable: Throwable) : this(
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

private val Throwable.name: String
    get() = "KmpManagedException: " + (this::class.qualifiedName ?: this::class.simpleName ?: "Throwable")
