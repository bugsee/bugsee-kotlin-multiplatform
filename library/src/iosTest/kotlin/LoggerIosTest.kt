package com.bugsee.kmp.internal

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import kotlin.test.Test
import kotlin.test.assertEquals

class LoggerIosTest {

    // NSLog formats its line as stringWithFormat does, so this checks the text NSLog prints.
    @Test
    fun `escaped lines format back to the original text`() {
        val lines = listOf(
            "file:///tmp/a%20b %@ %s %d 100%",
            "%% %%% %@%@%@",
            "%",
            "ünï 🚀 %@",
            "no specifiers",
        )
        for (line in lines) {
            assertEquals<String?>(line, NSString.stringWithFormat(escapeFormatSpecifiers(line)))
        }
    }

    // A '%' in a logged message (a file path, an exception text) used to make NSLog read
    // arguments that were never passed and crash.
    @Test
    fun `messages with format specifiers are logged without a crash`() {
        Logger.e("LoggerIosTest", "file:///tmp/a%20b %@ %s 100%", IllegalStateException("%@ %@"))
        Logger.e("LoggerIosTest", "%@ %@ %@ %@")
        Logger.d("LoggerIosTest", "%@ %s %d")
        Logger.i("LoggerIosTest", "%@%@%@")
    }
}
