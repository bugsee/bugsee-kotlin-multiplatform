package com.bugsee.kmp.internal

import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSException
import platform.Foundation.NSNull
import platform.Foundation.NSNumber
import platform.Foundation.NSURL

/**
 * A copy of [value] to hand to the iOS SDK. The SDK keeps what it is given by reference and reads
 * it later on its own threads: a trace value is serialized again every capture part, event params
 * are copied only at the top level, report attributes and network headers are read when the report
 * or the event is written. Kotlin/Native passes a Kotlin collection to Objective-C as a live view,
 * not a copy, so an app that changes the collection afterwards changes it under the SDK's
 * enumeration, and the Kotlin exception thrown on the SDK thread terminates the app.
 *
 * Lists, sets and maps are copied all the way down, and nothing else holds the copies. Strings,
 * numbers, booleans and the Foundation values the SDK serializes itself (dates, data, URLs, NSNull,
 * errors, exceptions) are kept. Any other object goes through [other], which keeps it by default.
 *
 * The copy is made on the caller's thread. Like any other read of the collection, it must not run
 * while another thread changes the collection: the copy would throw to the caller.
 */
internal fun snapshotForNative(value: Any, other: (Any) -> Any = { it }): Any = when (value) {
    // Unsigned numbers are not kotlin.Number, but reach Objective-C as NSNumber, as the signed ones do.
    is String, is Number, is Boolean, is UByte, is UShort, is UInt, is ULong -> value
    is List<*> -> value.map { it?.let { item -> snapshotForNative(item, other) } }
    is Set<*> -> value.mapTo(LinkedHashSet(value.size)) { it?.let { item -> snapshotForNative(item, other) } }
    is Map<*, *> -> value.entries.associate { (key, item) ->
        key?.let { snapshotForNative(it, other) } to item?.let { snapshotForNative(it, other) }
    }
    // Not `is NSObject` or `is ObjCObject`: on Kotlin/Native those are true for any Kotlin object.
    is NSNumber, is NSDate, is NSData, is NSURL, is NSNull, is NSError, is NSException -> value
    else -> other(value)
}

/**
 * [snapshotForNative] for a trace value or event params. The SDK writes an object it cannot
 * serialize as its `-description`, which for a Kotlin object is its `toString()`, called on an SDK
 * thread every time the value is written. The text is taken here instead, once, on the caller's
 * thread: the object may be changing (a class holding a mutable list), and a `toString` that
 * throws would terminate the app on the SDK thread.
 */
internal fun snapshotForCapture(value: Any): Any = snapshotForNative(value, ::describeValue)

private fun describeValue(value: Any): String =
    try {
        value.toString()
    } catch (e: Throwable) {
        Logger.e("NativeValues", "toString() of a ${value::class.simpleName} threw; its class name is recorded instead", e)
        value::class.simpleName ?: "Any"
    }
