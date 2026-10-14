package com.bugsee.kmp

/**
 * Encryption of the data Bugsee collects ([BugseeOptions.DataEncryption]). iOS only.
 */
public enum class BugseeDataEncryptionMode(private val value: Int) {
    None(0),
    OnDevice(1),
    EndToEnd(2);

    public fun toIntValue(): Int {
        return value
    }

    public companion object {
        public fun fromIntValue(value: Int?, defaultValue: BugseeDataEncryptionMode = None): BugseeDataEncryptionMode {
            return entries.firstOrNull { it.value == value } ?: defaultValue
        }
    }
}
