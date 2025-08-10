package com.bugsee.kmp

public enum class BugseeFrameRate(private val value: Int) {
    Low(1),
    Medium(2),
    High(3);


    public fun getIntValue(): Int {
        return value
    }

    public companion object {
        public fun fromIntValue(value: Int?, defaultValue: BugseeFrameRate = High): BugseeFrameRate {
            if (value == null) {
                return defaultValue
            }

            return when (value) {
                1 -> Low
                2 -> Medium
                3 -> High
                else -> defaultValue
            }
        }
    }
}