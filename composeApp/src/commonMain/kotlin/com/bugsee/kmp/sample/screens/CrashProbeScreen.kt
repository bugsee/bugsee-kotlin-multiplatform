package com.bugsee.kmp.sample.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bugsee.kmp.sample.runOnBackgroundThread

/**
 * CrashProbe — exercises the Bugsee crash-capture pipeline with a representative set of failures.
 *
 * Every test dispatches its crashing block via [runOnBackgroundThread] for one Android-specific
 * reason: Compose Multiplatform 1.9.x's `Modifier.clickable` runs the click lambda inside a
 * `pointerInput` coroutine (`SuspendingPointerInputModifierNode`), and that coroutine silently
 * absorbs throwables raised synchronously from the lambda — they never reach
 * `Thread.UncaughtExceptionHandler`, so Bugsee never sees them. Dispatching to a background
 * thread routes the exception to the platform's normal uncaught path.
 *
 * iOS does not have this problem: `ComposeSceneMediator.onTouchesEvent` calls
 * `ComposeScene.sendPointerEvent` synchronously, so a synchronous throw from a click lambda
 * propagates up to libc++abi and is captured by the native Bugsee iOS SDK. The bg-thread
 * wrapper is still used on iOS for uniformity — the throwable surfaces through the Kotlin/Native
 * unhandled-exception hook installed by `setBugseeUnhandledExceptionHook()`, which wraps it in
 * an `NSException` and hands it to the native SDK before `terminateWithUnhandledException`.
 *
 * On Android, captured crashes flow through `Thread.UncaughtExceptionHandler` -> Bugsee's
 * Android SDK.
 *
 * Two tests have notable platform differences (called out inline): "Stack Overflow" surfaces as
 * SIGSEGV on iOS rather than a Kotlin exception, and "Out of Memory" is silently jetsam-killed
 * by iOS and only detected post-mortem on the next launch.
 *
 * The "[Diagnostic]" button at the bottom intentionally throws on the main thread to demonstrate
 * the Android-only Compose absorption — on iOS, that button crashes and is captured normally.
 */
@Composable
fun CrashProbeScreen(onBackClick: () -> Unit) {
    TestScreenScaffold(title = "CrashProbe Tests", onBackClick = onBackClick) {
        Text(
            "These tests will crash the app. Reopen after each test and check the Bugsee dashboard for the captured crash. " +
                "Crashes are dispatched to a background thread because Compose Multiplatform's input pipeline silently absorbs " +
                "exceptions thrown synchronously from a click handler on the main thread.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TestButton("Null Pointer Crash") {
            runOnBackgroundThread {
                @Suppress("UNUSED_VARIABLE")
                val len = (null as String?)!!.length
            }
        }

        TestButton("Array Out of Bounds") {
            runOnBackgroundThread {
                val list = listOf(1, 2, 3)
                @Suppress("UNUSED_VARIABLE")
                val item = list[10]
            }
        }

        TestButton("Stack Overflow") {
            // iOS: surfaces as SIGSEGV when the recursion hits the stack guard page (GCD worker
            // stacks are ~512 KB), not as a Kotlin exception. Captured by the Bugsee iOS SDK's
            // signal/Mach exception handler rather than the Kotlin unhandled-exception hook;
            // recursion frames may be aggregated in the report.
            runOnBackgroundThread {
                fun recurse(): Int = recurse() + 1
                @Suppress("UNUSED_VARIABLE")
                val result = recurse()
            }
        }

        TestButton("Out of Memory") {
            // iOS: does not raise OutOfMemoryError. The OS sends memory-pressure warnings and
            // eventually jetsams the process via SIGKILL — there is no live crash report. The
            // Bugsee iOS SDK detects the OOM heuristically on the next launch (last session
            // ended without normal termination and without a crash signal).
            runOnBackgroundThread {
                val lists = mutableListOf<ByteArray>()
                while (true) {
                    lists.add(ByteArray(1024 * 1024 * 10)) // 10MB chunks
                }
            }
        }

        TestButton("Division by Zero") {
            // Kotlin/Native inserts a zero-check for integer division and throws
            // ArithmeticException to match JVM semantics, so iOS captures this through the
            // standard Kotlin unhandled-exception path (not as a SIGFPE).
            runOnBackgroundThread {
                val numerator = 50
                val denominator = 0
                @Suppress("UNUSED_VARIABLE")
                val result = numerator / denominator
            }
        }

        TestButton("Force Unwrap Optional") {
            runOnBackgroundThread {
                val nullStr: String? = null
                @Suppress("UNUSED_VARIABLE")
                val len = nullStr!!.length
            }
        }

        TestButton("Concurrent Modification") {
            runOnBackgroundThread {
                val list = mutableListOf(1, 2, 3, 4, 5)
                for (item in list) {
                    if (item == 3) list.remove(item)
                }
            }
        }

        TestButton("Custom Uncaught Exception") {
            runOnBackgroundThread {
                throw RuntimeException("Test uncaught exception from CrashProbe")
            }
        }

        TestButton("[Diagnostic] Main-thread throw (Android: swallowed; iOS: crashes)") {
            // Platform divergence in Compose Multiplatform 1.9.x:
            //   Android — Modifier.clickable runs onClick inside a pointerInput coroutine
            //     (SuspendingPointerInputModifierNode). The throwable here is absorbed by the
            //     input coroutine scope, never reaches Thread.UncaughtExceptionHandler, and
            //     Bugsee cannot capture it. The button appears to do nothing.
            //   iOS — ComposeSceneMediator.onTouchesEvent dispatches into
            //     ComposeScene.sendPointerEvent synchronously. The throwable propagates up the
            //     call chain to libc++abi and is captured by Bugsee's native iOS SDK normally.
            // Kept as a button so future contributors don't re-discover the Android behavior
            // and assume the SDK is broken.
            throw RuntimeException("Main-thread throw from a Compose click handler")
        }
    }
}
