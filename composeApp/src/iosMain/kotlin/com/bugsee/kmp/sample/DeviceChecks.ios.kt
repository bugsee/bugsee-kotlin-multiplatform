package com.bugsee.kmp.sample

import com.bugsee.kmp.BugseeCrashSignals
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.value
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUserDefaults
import platform.posix.SIGABRT
import platform.posix.SIGBUS
import platform.posix.SIGFPE
import platform.posix.SIGILL
import platform.posix.SIGSEGV
import platform.posix.SIGTRAP
import platform.posix.raise

actual object SampleSettings {
    private const val KEY_CRASH_SIGNALS = "bugsee-sample.crash_signals"

    actual var crashSignals: Int
        get() {
            val defaults = NSUserDefaults.standardUserDefaults
            return if (defaults.objectForKey(KEY_CRASH_SIGNALS) == null) {
                BugseeCrashSignals.Default
            } else {
                defaults.integerForKey(KEY_CRASH_SIGNALS).toInt()
            }
        }
        set(value) {
            NSUserDefaults.standardUserDefaults.setInteger(value.toLong(), KEY_CRASH_SIGNALS)
        }
}

actual fun raiseNativeSignal(signal: NativeSignal) {
    val signo = when (signal) {
        NativeSignal.ABRT -> SIGABRT
        NativeSignal.SEGV -> SIGSEGV
        NativeSignal.BUS -> SIGBUS
        NativeSignal.ILL -> SIGILL
        NativeSignal.FPE -> SIGFPE
        NativeSignal.TRAP -> SIGTRAP
    }
    raise(signo)
}

actual val badMemoryAccessAvailable: Boolean = true

@OptIn(ExperimentalForeignApi::class)
actual fun causeBadMemoryAccess() {
    // Address 8 is never mapped: EXC_BAD_ACCESS, as a native null dereference.
    8L.toCPointer<IntVar>()!!.pointed.value = 1
}

actual fun nativeCrashHandlerInfo(): String =
    if (NSProcessInfo.processInfo.environment["SIMULATOR_UDID"] != null) {
        "Simulator: the iOS SDK installs no crash handler here, so no crash is reported. Use a device."
    } else {
        "The iOS SDK's crash handler is built in: BSD signal handlers for the selected signals plus Mach " +
            "exceptions (captureCrashHandlerType MachAndBSD). It is not installed while a debugger is attached."
    }
