package com.bugsee.kmp.internal

import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSNull
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import kotlin.math.floor

// NSData, NSURL and NSNull are kept: both writers of the SDK handle them, the message packer (trace
// values, event params) and the JSON writer of report attributes, which writes any other object as
// null. An NSDate is passed as an ISO 8601 string in UTC, to the millisecond, as on Android: both
// writers would write it as seconds since 1970. Not `is NSObject` or `is ObjCObject`: on
// Kotlin/Native those are true for any Kotlin object. Not NSNumber either: an NSNumber reaches Kotlin
// as a Kotlin number or Boolean.
internal actual fun platformValue(value: Any): Any? = when (value) {
    is NSDate -> isoDate(floor(value.timeIntervalSince1970 * 1000).toLong())
    is NSData, is NSURL, is NSNull -> value
    else -> null
}
