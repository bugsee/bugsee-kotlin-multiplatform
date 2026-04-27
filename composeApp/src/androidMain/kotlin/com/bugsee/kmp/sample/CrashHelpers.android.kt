package com.bugsee.kmp.sample

actual fun runOnBackgroundThread(block: () -> Unit) {
    Thread { block() }.start()
}
