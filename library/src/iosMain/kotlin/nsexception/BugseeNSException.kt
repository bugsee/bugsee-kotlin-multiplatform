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
        reason = throwable.reasonWithCauses(),
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

// Causes listed after the message, at most this many.
private const val MAX_CAUSES = 10

/**
 * The message, then a "Caused by: <cause>" line for each cause, as Kotlin's `stackTraceToString`
 * prints them. An NSException has no cause, and the iOS SDK reports only its name, reason and
 * frames, so the causes go into the reason; the frames stay those of this throwable.
 */
private fun Throwable.reasonWithCauses(): String? {
    val lines = mutableListOf<String>()
    message?.let { lines += it }

    val seen = mutableListOf<Throwable>(this)
    var next = cause
    while (next != null && seen.none { it === next } && seen.size <= MAX_CAUSES) {
        lines += "Caused by: $next"
        seen += next
        next = next.cause
    }

    return if (lines.isEmpty()) null else lines.joinToString("\n")
}
