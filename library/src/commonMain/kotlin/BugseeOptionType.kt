package com.bugsee.kmp

/**
 * Value type of a launch option, used to accept only values the native SDKs can read.
 * [BugseeOptionsSpec.types] assigns one to every option key.
 */
internal sealed class BugseeOptionType {
    /** Returns [value] converted to this type, or `null` when it cannot be. */
    abstract fun coerce(value: Any, isAndroid: Boolean): Any?

    /** The form the native SDK of the platform expects. */
    open fun toNative(value: Any, isAndroid: Boolean): Any = value

    object BooleanType : BugseeOptionType() {
        override fun coerce(value: Any, isAndroid: Boolean): Any? = value as? Boolean
    }

    object IntType : BugseeOptionType() {
        override fun coerce(value: Any, isAndroid: Boolean): Any? = (value as? Number)?.toInt()
    }

    object FloatType : BugseeOptionType() {
        override fun coerce(value: Any, isAndroid: Boolean): Any? = (value as? Number)?.toFloat()
    }

    object StringType : BugseeOptionType() {
        override fun coerce(value: Any, isAndroid: Boolean): Any? = value as? String
    }

    /**
     * An enum option. Accepts a KMP entry, a name ([names], lower-cased: KMP entry names and the
     * native constant names of both SDKs) or a native Int value ([values]).
     *
     * [android] and [ios] map the entries a platform's SDK accepts to the native value it expects
     * (Android: constant name, iOS: Int); `null` when the platform has no such option. An entry
     * the current platform does not accept is rejected.
     */
    class EnumType(
        private val names: Map<String, Enum<*>>,
        private val values: Map<Int, Enum<*>>,
        private val android: Map<Enum<*>, Any>?,
        private val ios: Map<Enum<*>, Any>?,
    ) : BugseeOptionType() {
        override fun coerce(value: Any, isAndroid: Boolean): Any? {
            val entry = when (value) {
                is Enum<*> -> value.takeIf { it in names.values }
                is String -> names[value.lowercase()]
                is Number -> values[value.toInt()]
                else -> null
            } ?: return null

            val accepted = if (isAndroid) android else ios
            return entry.takeIf { accepted == null || it in accepted }
        }

        override fun toNative(value: Any, isAndroid: Boolean): Any =
            (if (isAndroid) android else ios)?.get(value) ?: (value as Enum<*>).name
    }
}
