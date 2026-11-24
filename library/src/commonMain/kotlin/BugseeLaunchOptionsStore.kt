package com.bugsee.kmp

import com.bugsee.kmp.internal.Logger
import com.bugsee.kmp.internal.customOptionValue
import com.bugsee.kmp.internal.legacyLaunchOptionKeys
import com.bugsee.kmp.internal.legacyLaunchOptionMessage

/**
 * Storage behind the generated [BugseeLaunchOptions].
 *
 * Options start at the native defaults of the current platform ([BugseeOptionsSpec.defaults]).
 * Values are converted to the option's type when stored, so what the getters return is what
 * the native SDK receives. [toMap] sends every option the current platform knows plus the custom
 * options; options of the other platform are kept for the getters but never sent. Keys from before
 * 0.2.0 ([legacyLaunchOptionKeys]) are ignored with an error that names the new key.
 */
internal class BugseeLaunchOptionsStore(
    private val isAndroid: Boolean,
    private val logError: (String) -> Unit = { Logger.e(TAG, it) },
    private val logWarning: (String) -> Unit = { Logger.w(TAG, it) },
) {
    private val defaults = BugseeOptionsSpec.defaults(isAndroid)
    private val values = HashMap<String, Any>(defaults)
    private val customOptions = HashMap<String, Any>()

    fun put(key: String, value: Any?) {
        if (key == WRAPPER_INFO_KEY || key == ANDROID_WRAPPER_KEY) {
            return // the wrapper metadata comes from BugseeWrapperAdapter only
        }

        val legacy = legacyLaunchOptionKeys[key]
        if (legacy != null) {
            logError(legacyLaunchOptionMessage(key, legacy))
            return
        }

        val type = BugseeOptionsSpec.types[key]
        if (type == null) {
            if (value != null) putCustom(key, value)
            return
        }

        if (value == null) {
            // Back to the platform default; options without one are not sent at all.
            defaults[key]?.let { values[key] = it } ?: values.remove(key)
            return
        }

        val coerced = type.coerce(value, isAndroid)
        if (coerced == null) {
            logError("Ignoring launch option $key: ${value::class.simpleName} value '$value' does not fit the option on this platform")
            return
        }
        values[key] = coerced
    }

    fun putAll(options: Map<String, Any>) {
        options.forEach { (key, value) -> put(key, value) }
    }

    private fun putCustom(key: String, value: Any) {
        // Strings, Booleans and numbers only: the iOS SDK writes every option into the session as JSON.
        customOptions[key] = customOptionValue(key, value) ?: return
        if (!key.startsWith(OPTION_KEY_PREFIX)) {
            // Android SDK 7 drops a key it has no option for (OptionsContainerImpl.java:59-63).
            logWarning("Launch option '$key' is not a Bugsee option key ($OPTION_KEY_PREFIX*); it is passed to the native SDK as is")
        }
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
        // No wrapper metadata: both SDKs get it from the adapter BugseeInternal registers with
        // setWrapper (BugseeWrapperAdapter), which they prefer to the $$WRAPPER / wrapper_info option.
        return result
    }

    private companion object {
        const val TAG = "BugseeLaunchOptions"
        const val OPTION_KEY_PREFIX = "com.bugsee.option."
        // The legacy iOS key; not sent, and not accepted from the app either.
        const val WRAPPER_INFO_KEY = "wrapper_info"
        const val ANDROID_WRAPPER_KEY = "com.bugsee.option.\$\$WRAPPER"
    }
}
