package com.bugsee.kmp

/**
 * Visual style of the Bugsee reporting UI ([BugseeOptions.ReportingUIStyle]). iOS only.
 */
public enum class BugseeStyle(private val value: Int) {
    Light(0),
    Dusk(1),
    BasedOnStatusBar(2),
    System(3);

    public fun toIntValue(): Int {
        return value
    }

    public companion object {
        public fun fromIntValue(value: Int?, defaultValue: BugseeStyle = System): BugseeStyle {
            return entries.firstOrNull { it.value == value } ?: defaultValue
        }
    }
}
