package com.bugsee.kmp.sample

/**
 * Runs [block] on a background thread.
 *
 * Why this exists: on Android, Compose Multiplatform 1.9.x's `Modifier.clickable` invokes the
 * click lambda inside a `pointerInput` coroutine (`SuspendingPointerInputModifierNode`).
 * Throwables raised synchronously from that lambda are absorbed by the input coroutine scope
 * and never propagate to `Thread.UncaughtExceptionHandler`, so Bugsee cannot capture them.
 * Dispatching to a background thread sidesteps the input coroutine entirely.
 *
 * iOS does not have this problem in 1.9.x — `ComposeSceneMediator.onTouchesEvent` dispatches
 * pointer events synchronously into `ComposeScene.sendPointerEvent`, so a synchronous throw
 * from a click lambda propagates up through the call chain, terminates via libc++abi, and is
 * captured by the native Bugsee iOS SDK. The bg-thread wrapper still works on iOS (the
 * throwable surfaces via the Kotlin/Native unhandled-exception hook installed by
 * `setBugseeUnhandledExceptionHook()`); it's used here purely to keep CrashProbe behavior
 * uniform across platforms.
 *
 * Android: `Thread { block() }.start()` — exception reaches `AndroidRuntime` and Bugsee.
 * iOS: `dispatch_async` on a global GCD queue — exception triggers Kotlin/Native's
 * unhandled-exception hook, which converts it to an `NSException` for the iOS SDK before
 * terminating the process.
 */
expect fun runOnBackgroundThread(block: () -> Unit)
