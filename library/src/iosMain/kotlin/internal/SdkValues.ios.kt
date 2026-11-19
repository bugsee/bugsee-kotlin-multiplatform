package com.bugsee.kmp.internal

import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSNull
import platform.Foundation.NSURL

// The types both writers of the SDK handle: the message packer (trace values, event params) and the
// JSON writer of report attributes, which writes any other object as null. Not `is NSObject` or
// `is ObjCObject`: on Kotlin/Native those are true for any Kotlin object. Not NSNumber either: an
// NSNumber reaches Kotlin as a Kotlin number or Boolean.
internal actual fun platformValue(value: Any): Any? = when (value) {
    is NSDate, is NSData, is NSURL, is NSNull -> value
    else -> null
}
