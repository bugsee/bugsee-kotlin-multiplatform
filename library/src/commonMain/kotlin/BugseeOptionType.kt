package com.bugsee.kmp

/**
 * Value type of a launch option, used to accept only values the native SDKs can read.
 * [BugseeOptionsSpec.types] assigns one to every option key.
 */
internal sealed class BugseeOptionType {
    /** Returns [value] converted to this type, or `null` when it cannot be. */
    abstract fun coerce(value: Any): Any?

    object BooleanType : BugseeOptionType() {
        override fun coerce(value: Any): Any? = value as? Boolean
    }

    object IntType : BugseeOptionType() {
        override fun coerce(value: Any): Any? = (value as? Number)?.toInt()
    }

    object FloatType : BugseeOptionType() {
        override fun coerce(value: Any): Any? = (value as? Number)?.toFloat()
    }

    object StringType : BugseeOptionType() {
        override fun coerce(value: Any): Any? = value as? String
    }

    /** Accepts one of [entries], or a String naming one (case-insensitive, as the Android SDK does). */
    class EnumType(private val entries: List<Enum<*>>) : BugseeOptionType() {
        override fun coerce(value: Any): Any? = when (value) {
            is Enum<*> -> value.takeIf { it in entries }
            is String -> entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
            else -> null
        }
    }
}
