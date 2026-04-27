package com.bugsee.kmp.sample

import kotlinx.cinterop.ExperimentalForeignApi
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

@OptIn(ExperimentalForeignApi::class)
actual fun runOnBackgroundThread(block: () -> Unit) {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
        block()
    }
}
