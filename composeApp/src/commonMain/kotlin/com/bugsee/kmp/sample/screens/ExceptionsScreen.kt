package com.bugsee.kmp.sample.screens

import androidx.compose.runtime.Composable
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeExceptionLoggingOptions

@Composable
fun ExceptionsScreen(onBackClick: () -> Unit) {
    TestScreenScaffold(title = "Exceptions", onBackClick = onBackClick) {
        TestButton("Log NullPointer Exception") {
            try {
                @Suppress("UNUSED_VARIABLE")
                val len = (null as String?)!!.length
            } catch (ex: Exception) {
                Bugsee.logException(ex)
            }
        }

        TestButton("Log Exception with Options") {
            try {
                @Suppress("UNUSED_VARIABLE")
                val len = (null as String?)!!.length
            } catch (ex: Exception) {
                // Both SDKs read only the domain and skipFrames; labels and includeVideo are
                // ignored (labels can be added to a report in a report filter).
                val options = BugseeExceptionLoggingOptions()
                options.exceptionDomain = "KMPSampleDomain"
                options.rules.skipFrames = 2
                Bugsee.logException(ex, options)
            }
        }

        TestButton("Log IndexOutOfBounds") {
            try {
                val list = listOf(1, 2, 3)
                @Suppress("UNUSED_VARIABLE")
                val item = list[10]
            } catch (ex: Exception) {
                Bugsee.logException(ex)
            }
        }

        TestButton("Log Runtime Exception") {
            try {
                throw RuntimeException("Runtime test exception")
            } catch (ex: Exception) {
                Bugsee.logException(ex)
            }
        }

        TestButton("Log Nested Exception") {
            try {
                throw RuntimeException(
                    "Outer exception",
                    IllegalStateException("Inner cause")
                )
            } catch (ex: Exception) {
                Bugsee.logException(ex)
            }
        }
    }
}
