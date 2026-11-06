package com.bugsee.kmp

/**
 * How the crash reporter intercepts crashes ([BugseeOptions.CaptureCrashHandlerType]). iOS only.
 *
 * The signals are chosen with [BugseeOptions.CaptureCrashSignals].
 */
public enum class BugseeCrashHandlerType(private val value: Int) {
    /**
     * BSD signal handlers for the selected signals.
     */
    BSD(1),

    /**
     * A Mach exception server, plus a BSD signal handler for SIGABRT when it is selected. Any other
     * signal raised by software (`raise()`, `kill()`, `pthread_kill()`) is not reported.
     */
    Mach(2),

    /**
     * A Mach exception server and BSD signal handlers for the selected signals, so both hardware
     * faults and signals raised by software are reported. This is the default.
     */
    MachAndBSD(3);

    public fun toIntValue(): Int {
        return value
    }

    public companion object {
        public fun fromIntValue(value: Int?, defaultValue: BugseeCrashHandlerType = MachAndBSD): BugseeCrashHandlerType {
            return entries.firstOrNull { it.value == value } ?: defaultValue
        }
    }
}
