package com.bugsee.kmp

/**
 * Bits for [BugseeLaunchOptions.captureCrashSignals] ([BugseeOptions.CaptureCrashSignals]): the fatal
 * signals the crash reporter intercepts, one bit per signal. iOS only.
 *
 * Combine them with `or` to choose which ones:
 *
 * ```
 * options.captureCrashSignals = BugseeCrashSignals.ABRT or BugseeCrashSignals.ILL or BugseeCrashSignals.TRAP
 * ```
 *
 * A bad memory access is intercepted when either [SEGV] or [BUS] is selected; leave both out to
 * leave bad memory accesses alone. Leaving out [ABRT] also stops uncaught exceptions, which end in
 * `abort()`, from being reported.
 *
 * The values are those of the iOS SDK `BugseeCrashSignals`.
 */
public object BugseeCrashSignals {
    /** No signals. */
    public const val None: Int = 0

    /** SIGABRT: `abort()`, failed assertions, uncaught exceptions. */
    public const val ABRT: Int = 1 shl 0

    /** SIGSEGV: invalid memory access. */
    public const val SEGV: Int = 1 shl 1

    /** SIGBUS: misaligned or otherwise invalid memory access. */
    public const val BUS: Int = 1 shl 2

    /** SIGILL: illegal instruction. */
    public const val ILL: Int = 1 shl 3

    /** SIGFPE: arithmetic exception. */
    public const val FPE: Int = 1 shl 4

    /** SIGTRAP: breakpoint or trap instruction, including Swift runtime traps. */
    public const val TRAP: Int = 1 shl 5

    /** Every signal. */
    public const val All: Int = ABRT or SEGV or BUS or ILL or FPE or TRAP

    /**
     * Lets the SDK choose: every signal. This is the default. It is honoured only on its own;
     * combined with other bits it is ignored and the other bits are used as given.
     */
    public const val Default: Int = 1 shl 7
}
