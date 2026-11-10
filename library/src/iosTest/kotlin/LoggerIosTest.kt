package com.bugsee.kmp.internal

import kotlin.test.Test

class LoggerIosTest {

    // NSLog takes its first argument as a format string. A '%' in a logged message (a file
    // path, an exception text) used to make it read arguments that were never passed.
    @Test
    fun `messages with format specifiers are logged verbatim`() {
        Logger.e("LoggerIosTest", "file:///tmp/a%20b %@ %s 100%", IllegalStateException("%@ %@"))
        Logger.e("LoggerIosTest", "%@ %@ %@ %@")
        Logger.d("LoggerIosTest", "%@ %s %d")
        Logger.i("LoggerIosTest", "%@%@%@")
    }
}
