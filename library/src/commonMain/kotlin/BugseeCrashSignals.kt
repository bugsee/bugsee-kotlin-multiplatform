package com.bugsee.kmp

/**
 * Bits for [BugseeLaunchOptions.captureCrashSignals] ([BugseeOptions.CaptureCrashSignals]): the fatal
 * signals the native crash handler intercepts, one bit per signal. On Android the handler is the
 * NDK one, so the option takes effect only with the `bugsee-android-ndk` module in the app.
 *
 * Combine them with `or` to choose which ones:
 *
 * ```
 * options.captureCrashSignals = BugseeCrashSignals.ABRT or BugseeCrashSignals.ILL or BugseeCrashSignals.TRAP
 * ```
 *
 * A signal left out keeps the handler it had before, so a runtime that turns that signal into an
 * exception of its own keeps doing so. The value is read once per process, when the crash handler is
 * first set up, so a later launch or relaunch with another value takes effect only after the app
 * restarts.
 *
 * iOS: with a Mach handler ([BugseeCrashHandlerType.Mach], [BugseeCrashHandlerType.MachAndBSD]) a
 * bad memory access is intercepted when either [SEGV] or [BUS] is selected; leave both out to leave
 * bad memory accesses alone. With [BugseeCrashHandlerType.BSD] only the selected one of the two is.
 * Leaving out [ABRT] also stops uncaught Objective-C and C++ exceptions, which end in `abort()`,
 * from being reported. A signal raised by software (`raise()`, `kill()`) never becomes a Mach
 * exception, so only the BSD handler of a selected signal reports it. The SDK sets up no crash handler
 * on the simulator or while a debugger is attached.
 *
 * Android: SIGQUIT and SIGSYS are always intercepted.
 *
 * The values are those of the iOS SDK `BugseeCrashSignals` and of the Android SDK `CrashSignals`.
 */
public object BugseeCrashSignals {
    /**
     * No signals: native crashes (on iOS also uncaught Objective-C and C++ exceptions) are not
     * reported, even with [BugseeLaunchOptions.detectAndReportCrash] on, apart from the signals the
     * Android handler always intercepts (SIGQUIT and SIGSYS). Uncaught Kotlin and Java exceptions
     * are still reported; turn [BugseeLaunchOptions.detectAndReportCrash] off to stop those too.
     */
    public const val None: Int = 0

    /** SIGABRT: `abort()`, failed assertions, uncaught C++ (and on iOS Objective-C) exceptions. */
    public const val ABRT: Int = 1 shl 0

    /** SIGSEGV: invalid memory access. */
    public const val SEGV: Int = 1 shl 1

    /** SIGBUS: misaligned or otherwise invalid memory access. */
    public const val BUS: Int = 1 shl 2

    /** SIGILL: illegal instruction. */
    public const val ILL: Int = 1 shl 3

    /** SIGFPE: arithmetic exception. */
    public const val FPE: Int = 1 shl 4

    /** SIGTRAP: breakpoint or trap instruction, on iOS including Swift runtime traps. */
    public const val TRAP: Int = 1 shl 5

    /** Every signal. */
    public const val All: Int = ABRT or SEGV or BUS or ILL or FPE or TRAP

    /**
     * Lets the SDK choose: every signal. This is the default. It is honoured only on its own;
     * combined with other bits it is ignored and the other bits are used as given.
     */
    public const val Default: Int = 1 shl 7
}
