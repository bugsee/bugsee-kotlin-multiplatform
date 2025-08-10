package com.bugsee.kmp

public enum class BugseeSeverity(private val level: Int) {
    VeryLow(1),
    Medium(2),
    High(3),
    Critical(4),
    Blocker(5);

    public fun getLevel(): Int {
        return level
    }

    public fun getLevelLong(): ULong {
        return level.toULong()
    }

    public companion object {
        public fun fromLevel(level: Int?, defaultValue: BugseeSeverity = Medium): BugseeSeverity {
            return when (level) {
                1 -> VeryLow
                2 -> Medium
                3 -> High
                4 -> Critical
                5 -> Blocker
                else -> defaultValue
            }
        }
    }
}