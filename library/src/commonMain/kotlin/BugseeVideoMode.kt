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
     * All windows on the screen are recorded, including system UI, but user is asked to allow video
     * recording. This mode may capture windows of other applications, so consider privacy and
     * security before using it.
     */
    Fullscreen(20),
    /**
     * Video is recorded from raw pixel buffers supplied from outside of the SDK.
     */
    DirectBuffers(21);

    public fun toIntValue(): Int {
        return value
    }

    public companion object {
        public fun fromIntValue(value: Int?, defaultValue: BugseeVideoMode = V2): BugseeVideoMode {
            if (value == null) {
                return defaultValue
            }

            return when (value) {
                0 -> None
                1 -> V1
                2 -> V2
                20 -> Fullscreen
                21 -> DirectBuffers
                else -> defaultValue
            }
        }
    }
}