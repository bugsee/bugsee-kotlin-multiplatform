package com.bugsee.kmp.sample

import android.content.Context
import android.content.SharedPreferences
import android.system.Os
import android.system.OsConstants
import com.bugsee.kmp.BugseeCrashSignals

actual object SampleSettings {
    private const val KEY_CRASH_SIGNALS = "crash_signals"
    private var preferences: SharedPreferences? = null

    /** Called from the Application before Bugsee is launched. */
    fun init(context: Context) {
        preferences = context.getSharedPreferences("bugsee-sample", Context.MODE_PRIVATE)
    }

    actual var crashSignals: Int
        get() = preferences?.getInt(KEY_CRASH_SIGNALS, BugseeCrashSignals.Default) ?: BugseeCrashSignals.Default
        // commit(), not apply(): the next step of a check is to kill the app.
        set(value) {
            preferences?.edit()?.putInt(KEY_CRASH_SIGNALS, value)?.commit()
        }

    actual fun isHandlerOn(handler: SampleHandler): Boolean =
        preferences?.getBoolean(handlerKey(handler), false) ?: false

    actual fun setHandlerOn(handler: SampleHandler, on: Boolean) {
        preferences?.edit()?.putBoolean(handlerKey(handler), on)?.commit()
    }

    private fun handlerKey(handler: SampleHandler) = "handler_${handler.name}"
}

actual fun raiseNativeSignal(signal: NativeSignal) {
    val signo = when (signal) {
        NativeSignal.ABRT -> OsConstants.SIGABRT
        NativeSignal.SEGV -> OsConstants.SIGSEGV
        NativeSignal.BUS -> OsConstants.SIGBUS
        NativeSignal.ILL -> OsConstants.SIGILL
        NativeSignal.FPE -> OsConstants.SIGFPE
        NativeSignal.TRAP -> OsConstants.SIGTRAP
    }
    Os.kill(Os.getpid(), signo)
}

// The sample has no native code to fault in; a sent signal reaches the NDK handler like a fault.
actual val badMemoryAccessAvailable: Boolean = false

actual fun causeBadMemoryAccess() {
    throw UnsupportedOperationException("No native code to fault in on Android")
}

// captureCrashSignals takes effect only through the NDK crash handler (bugsee-android-ndk).
actual fun nativeCrashHandlerInfo(): String {
    val hasNdk = runCatching { Class.forName("com.bugsee.library.BugseeNdk") }.isSuccess
    return if (hasNdk) {
        "bugsee-android-ndk is in this build: captureCrashSignals applies (read once per process)."
    } else {
        "bugsee-android-ndk is NOT in this build: native crashes are not reported and captureCrashSignals has no effect."
    }
}
