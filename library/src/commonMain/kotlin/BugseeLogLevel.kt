package com.bugsee.kmp

public enum class BugseeLogLevel(private val level: Int) {
    Error(1),
    Warning(2),
    Info(3),
    Debug(4),
    Verbose(5);

    public fun getLevel(): Int {
        return level
    }

    public fun getLevelLong(): ULong {
        return level.toULong()
    }

    public fun fromLevel(level: Int, defaultValue: BugseeLogLevel = Info): BugseeLogLevel {
        return when (level) {
            1 -> Error
            2 -> Warning
            3 -> Info
            4 -> Debug
            5 -> Verbose
            else -> defaultValue
        }
    }
}