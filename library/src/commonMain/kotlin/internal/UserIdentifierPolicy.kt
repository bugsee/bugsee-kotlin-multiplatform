package com.bugsee.kmp.internal

/**
 * Gives the user identifier the same meaning on both platforms, as the React Native and Unity
 * wrappers do: iOS SDK 7 treats setting an empty identifier as a clear, while Android SDK 7
 * stores the empty string.
 */
internal object UserIdentifierPolicy {
    /** The identifier to set, or `null` to clear it instead. */
    fun forSet(userIdentifier: String?): String? = userIdentifier?.takeIf { it.isNotEmpty() }

    /** The identifier to return: `null` in place of an empty one. */
    fun forGet(userIdentifier: String?): String? = userIdentifier?.takeIf { it.isNotEmpty() }
}
