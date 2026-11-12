package com.bugsee.kmp

import com.bugsee.kmp.internal.Logger

/**
 * Storage behind the generated [BugseeLaunchOptions].
 *
 * Options start at the native defaults of the current platform ([BugseeOptionsSpec.defaults]).
 * Values are converted to the option's type when stored, so what the getters return is what
 * the native SDK receives. [toMap] sends every option the current platform knows plus the custom
 * options; options of the other platform are kept for the getters but never sent.
 */
internal class BugseeLaunchOptionsStore(private val isAndroid: Boolean) {
    private val defaults = BugseeOptionsSpec.defaults(isAndroid)
    private val values = HashMap<String, Any>(defaults)
    private val customOptions = HashMap<String, Any>()

    fun put(key: String, value: Any?) {
        if (key == WRAPPER_INFO_KEY || key == ANDROID_WRAPPER_KEY) {
            return // the wrapper metadata comes from BugseeWrapperInfo only
        }

        val type = BugseeOptionsSpec.types[key]
        if (type == null) {
            if (value != null) customOptions[key] = value
            return
        }

        if (value == null) {
            // Back to the platform default; options without one are not sent at all.
            defaults[key]?.let { values[key] = it } ?: values.remove(key)
            return
        }

        val coerced = type.coerce(value, isAndroid)
        if (coerced == null) {
            Logger.e(TAG, "Ignoring launch option $key: ${value::class.simpleName} value '$value' does not fit the option on this platform")
            return
        }
        values[key] = coerced
    }

    fun putAll(options: Map<String, Any>) {
        options.forEach { (key, value) -> put(key, value) }
    }

    fun boolean(key: String, fallback: Boolean): Boolean = valueOf(key) as? Boolean ?: fallback

    fun int(key: String, fallback: Int): Int = valueOf(key) as? Int ?: fallback

    fun float(key: String, fallback: Float): Float = valueOf(key) as? Float ?: fallback

    fun string(key: String, fallback: String): String = valueOf(key) as? String ?: fallback

    fun stringOrNull(key: String, fallback: String?): String? = valueOf(key) as? String ?: fallback

    inline fun <reified E : Enum<E>> enum(key: String, fallback: E): E = valueOf(key) as? E ?: fallback

    /** The stored value, else the current platform's default; `null` for an unset option without one. */
    fun valueOf(key: String): Any? = values[key] ?: defaults[key]

    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>(customOptions)
        val known = BugseeOptionsSpec.knownKeys(isAndroid)
        values.forEach { (key, value) ->
            if (key in known) {
                result[key] = BugseeOptionsSpec.types.getValue(key).toNative(value, isAndroid)
            }
        }
        // iOS gets the wrapper metadata from the adapter registered with +[Bugsee setWrapper:]
        // instead; its SDK would read a wrapper_info option only when no adapter is registered.
        if (isAndroid) {
            result[ANDROID_WRAPPER_KEY] = androidWrapperInfo()
        }
        return result
    }

    // Hidden option of the Android SDK 7: {type, version, build, context}.
    private fun androidWrapperInfo(): HashMap<String, Any> = hashMapOf(
        "type" to BugseeWrapperInfo.TYPE,
        "version" to BugseeWrapperInfo.version,
        "build" to BugseeWrapperInfo.build,
        "context" to hashMapOf("runtime" to BugseeWrapperInfo.runtime),
    )

    private companion object {
        const val TAG = "BugseeLaunchOptions"
        // The legacy iOS key; not sent, and not accepted from the app either.
        const val WRAPPER_INFO_KEY = "wrapper_info"
        const val ANDROID_WRAPPER_KEY = "com.bugsee.option.\$\$WRAPPER"
    }
}
