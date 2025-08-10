package com.bugsee.kmp

public enum class BugseeReportType {
    Bug,
    Error,
    Crash;

    public fun toStringValue(): String {
        return when (this) {
            Bug -> "bug"
            Error -> "error"
            Crash -> "crash"
        }
    }

    public companion object {
        public fun fromString(value: String, defaultValue: BugseeReportType = Bug): BugseeReportType {
            return when (value) {
                "bug" -> Bug
                "error" -> Error
                "crash" -> Crash
                else -> defaultValue
            }
        }
    }
}