package com.bugsee.kmp.internal

import kotlin.time.Instant

/*
 * The values the wrapper hands to the native SDKs, under one rule on both platforms. Each SDK keeps
 * only some types and drops or changes the others without telling the app, and the two SDKs differ.
 *
 * A scalar is a String, a Boolean or a number: Byte, Short, Int, Long, Float or Double, or an
 * unsigned number that fits in a Long. Byte, Short, UByte and UShort are passed as Int, UInt and
 * ULong as Long: the Android SDK keeps an Int attribute but drops a Byte or a Short.
 *
 * Global attributes and custom launch options take scalars only: the Android SDK stores nothing
 * else as an attribute. Trace values, event params and report attributes also take arrays,
 * collections and maps with String keys of such values, at any depth, with null elements. Arrays and
 * collections are passed as lists: the iOS SDK writes a Kotlin array as text, and the Android SDK
 * drops a collection that is not Serializable. They are copied, so the SDK never holds the app's
 * collection: the SDKs write values later on their own threads, and Kotlin/Native would hand the iOS
 * SDK the app's collection itself.
 *
 * A date, a kotlin.time.Instant or a date of the platform, is passed as an ISO 8601 string in UTC,
 * as Instant writes itself: the iOS SDK would write an NSDate as seconds since 1970 and an Instant as
 * null in a report attribute, the Android SDK a Date as its toString() in the device time zone.
 *
 * The copy is made on the caller's thread. Like any other read of the collection, it must not run
 * while another thread changes the collection: the copy would throw to the caller.
 */

private const val TAG = "BugseeValues"

/** The value of a global attribute; `null`, logged, when [value] is not a scalar. */
internal fun attributeValue(name: String, value: Any): Any? =
    sdkScalar(value) ?: null.also {
        Logger.e(TAG, "setAttribute: ignoring '$name': a value of type ${typeName(value)} is not a String, Boolean or number")
    }

/** The value of a custom launch option; `null`, logged, when [value] is not a scalar. */
internal fun customOptionValue(key: String, value: Any): Any? =
    sdkScalar(value) ?: null.also {
        Logger.e(TAG, "Ignoring launch option $key: a value of type ${typeName(value)} is not a String, Boolean or number")
    }

/** The value of a report attribute; `null`, logged, when [value] is or holds a value of another type. */
internal fun reportAttributeValue(name: String, value: Any): Any? =
    when (val converted = convert(value, Other.Reject)) {
        is Rejected -> null.also { Logger.e(TAG, "setAttribute: ignoring report attribute '$name': ${converted.reason}") }
        else -> converted
    }

/**
 * A trace value. A value of another type, or a map key that is not a String, is replaced by its
 * `toString()`, taken here, on the caller's thread: the iOS SDK would write its description, which
 * for a Kotlin object is `toString()`, on its own thread every time it writes the value; the Android
 * SDK would drop an object that is not `Serializable`.
 */
internal fun traceValue(value: Any): Any = convert(value, Other.Describe)

/** Event params, converted as [traceValue] converts each value. */
@Suppress("UNCHECKED_CAST")
internal fun eventParams(params: Map<String, Any>): LinkedHashMap<String, Any?> =
    convert(params, Other.Describe) as LinkedHashMap<String, Any?>

/** A copy of [value]'s collections, with any other value kept as it is: for the iOS network headers. */
internal fun copiedValue(value: Any): Any = convert(value, Other.Keep)

/**
 * A value of a platform type in trace values, event params and report attributes: a date as
 * [isoDate], or a value the SDK writes well, kept as it is. `null` for any other value.
 */
internal expect fun platformValue(value: Any): Any?

/** A date as an ISO 8601 string in UTC, such as `2026-01-31T09:30:00.250Z`, as kotlin.time.Instant writes it. */
internal fun isoDate(epochMilliseconds: Long): String = Instant.fromEpochMilliseconds(epochMilliseconds).toString()

/** What happens to a value of another type. */
private enum class Other {
    /** Replaced by its `toString()`. */
    Describe,

    /** The whole value is rejected. */
    Reject,

    /** Kept as it is. */
    Keep,
}

/** [convert]'s result when [Other.Reject] rejects the value, at any depth: [reason] says what was rejected. */
private class Rejected(val reason: String)

private fun convert(value: Any, other: Other): Any {
    sdkScalar(value)?.let { return it }
    if (value is Instant) return value.toString()
    platformValue(value)?.let { return it }
    elements(value)?.let { items ->
        val result = ArrayList<Any?>(items.size)
        for (item in items) {
            val converted = item?.let { convert(it, other) }
            if (converted is Rejected) return converted
            result.add(converted)
        }
        return result
    }
    return when (value) {
        is Map<*, *> -> {
            val result = LinkedHashMap<Any?, Any?>(value.size)
            for ((key, item) in value) {
                val convertedKey = when {
                    key is String || other == Other.Keep -> key
                    other == Other.Describe -> key?.let(::describe) ?: "null"
                    else -> return Rejected("a map key of type ${key?.let(::typeName)} is not a String")
                }
                val converted = item?.let { convert(it, other) }
                if (converted is Rejected) return converted
                result[convertedKey] = converted
            }
            result
        }
        else -> when (other) {
            Other.Describe -> describe(value)
            Other.Reject -> Rejected("a value of type ${typeName(value)} is not a String, Boolean, number, array, collection or map")
            Other.Keep -> value
        }
    }
}

/** The elements of an array or a collection, which is passed as a list; `null` for any other value. */
@OptIn(ExperimentalUnsignedTypes::class)
private fun elements(value: Any): Collection<*>? = when (value) {
    is Collection<*> -> value
    is Array<*> -> value.asList()
    is ByteArray -> value.asList()
    is ShortArray -> value.asList()
    is IntArray -> value.asList()
    is LongArray -> value.asList()
    is FloatArray -> value.asList()
    is DoubleArray -> value.asList()
    is BooleanArray -> value.asList()
    is CharArray -> value.asList()
    is UByteArray -> value.asList()
    is UShortArray -> value.asList()
    is UIntArray -> value.asList()
    is ULongArray -> value.asList()
    else -> null
}

private fun sdkScalar(value: Any): Any? = when (value) {
    is String, is Boolean, is Int, is Long, is Float, is Double -> value
    is Byte -> value.toInt()
    is Short -> value.toInt()
    is UByte -> value.toInt()
    is UShort -> value.toInt()
    is UInt -> value.toLong()
    is ULong -> if (value <= Long.MAX_VALUE.toULong()) value.toLong() else null
    else -> null
}

private fun describe(value: Any): String =
    try {
        value.toString()
    } catch (e: Throwable) {
        Logger.e(TAG, "toString() of a ${typeName(value)} threw; its class name is recorded instead", e)
        typeName(value)
    }

// The class of a list or map from listOf / mapOf has no simple name on Kotlin/Native.
private fun typeName(value: Any): String = when (value) {
    is List<*> -> "List"
    is Set<*> -> "Set"
    is Collection<*> -> "Collection"
    is Map<*, *> -> "Map"
    else -> value::class.simpleName ?: "(anonymous)"
}
