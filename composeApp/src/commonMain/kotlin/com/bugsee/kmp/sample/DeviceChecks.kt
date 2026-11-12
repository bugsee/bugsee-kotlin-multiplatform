package com.bugsee.kmp.sample

import com.bugsee.kmp.BugseeCrashSignals

/**
 * Settings the sample applies when it launches Bugsee, kept across app restarts so a device check
 * can change a launch option, restart the app and see its effect.
 */
expect object SampleSettings {
    /** The [BugseeCrashSignals] bit set for `captureCrashSignals`; [BugseeCrashSignals.Default] if never set. */
    var crashSignals: Int
}

/** The `captureCrashSignals` value this process launched Bugsee with (set at launch). */
var launchedCrashSignals: Int = BugseeCrashSignals.Default

/** Fatal signals the Crash Signals screen can send to the app's own process. */
enum class NativeSignal(val bit: Int) {
    ABRT(BugseeCrashSignals.ABRT),
    SEGV(BugseeCrashSignals.SEGV),
    BUS(BugseeCrashSignals.BUS),
    ILL(BugseeCrashSignals.ILL),
    FPE(BugseeCrashSignals.FPE),
    TRAP(BugseeCrashSignals.TRAP),
}

/**
 * Sends [signal] to this process as a software signal (`kill`, `raise`). On iOS that reaches only the
 * BSD handler of a selected signal, never the Mach exception handler; see [causeBadMemoryAccess].
 */
expect fun raiseNativeSignal(signal: NativeSignal)

/** Whether [causeBadMemoryAccess] is available: iOS only, the Android sample has no native code. */
expect val badMemoryAccessAvailable: Boolean

/**
 * Writes through an invalid pointer: a real bad memory access, which iOS reports through its Mach
 * exception handler. Only where [badMemoryAccessAvailable].
 */
expect fun causeBadMemoryAccess()

/** Which native crash handler this build has, shown on the Crash Signals screen. */
expect fun nativeCrashHandlerInfo(): String

/** Readable form of a [BugseeCrashSignals] bit set. */
fun describeCrashSignals(value: Int): String = when (value) {
    BugseeCrashSignals.Default -> "Default (SDK choice: every signal)"
    BugseeCrashSignals.All -> "All"
    BugseeCrashSignals.None -> "None"
    else -> NativeSignal.entries.filter { value and it.bit != 0 }.joinToString(" | ") { it.name }.ifEmpty { "None" } + " ($value)"
}
