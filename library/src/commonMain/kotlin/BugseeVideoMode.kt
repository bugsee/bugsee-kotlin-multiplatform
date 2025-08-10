package com.bugsee.kmp

public enum class BugseeVideoMode(private val value: Int) {
    None(0),
    /**
     * User is not asked to allow video recording, but frame rate is lower comparing to {@link VideoMode#V2} mode and some special views
     * like status bar, soft keyboard and views, which contain Surface (MapView, VideoView, GlSurfaceView, etc.) are not recorded.
     */
    V1(1),
    /**
     * All types of views are recorded, but user is asked to allow video recording.
     */
    V2(2),
    /**
     * User is not asked to allow video recording, but frame rate is lower comparing to {@link VideoMode#V2} and system views like status bar
     * and soft keyboard are not recorded. This mode is <b>experimental</b> and works only on Android API level 24 and higher. On lower API
     * levels video mode is automatically switched to {@link VideoMode#V1}.
     */
    V3(3),
    /**
     * Special for Unity.
     */
    V4(4);

    public fun toIntValue(): Int {
        return value
    }

    public companion object {
        public fun fromIntValue(value: Int?, defaultValue: BugseeVideoMode = V3): BugseeVideoMode {
            if (value == null) {
                return defaultValue
            }

            return when (value) {
                1 -> V1
                2 -> V2
                3 -> V3
                4 -> V4
                else -> defaultValue
            }
        }
    }
}