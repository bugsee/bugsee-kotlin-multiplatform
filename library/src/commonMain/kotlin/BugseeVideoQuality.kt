package com.bugsee.kmp

public enum class BugseeVideoQuality(private val value: Int) {
    /**
     * Default video quality suitable for most scenarios. The resulting video is
     * encoded into container with resolution of up to 640x640
     */
    Default(0),
    /**
     * Suitable for visual contents with medium original resolution (up to 2K pixels).
     * The resulting video is encoded into container with resolution of up to 960x960
     */
    Medium(1),
    /**
     * Suitable for visual contents with high original resolution (more than 2K pixels).
     * The resulting video is encoded into container with resolution of up to 1280x1280
     */
    High(2);

    public fun toIntValue(): Int {
        return value
    }

    public companion object {
        public fun fromIntValue(value: Int?, defaultValue: BugseeVideoQuality = Default): BugseeVideoQuality {
            if (value == null) {
                return defaultValue
            }

            return when (value) {
                0 -> Default
                1 -> Medium
                2 -> High
                else -> defaultValue
            }
        }
    }
}