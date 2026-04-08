package com.bugsee.kmp.sample.screens

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeLogLevel

@Composable
fun LoggingTestsScreen(onBackClick: () -> Unit) {
    TestScreenScaffold(title = "Logging Tests", onBackClick = onBackClick) {
        TestSectionLabel("Log Levels")
        TestButton("Log Info") {
            Bugsee.log("Info message", BugseeLogLevel.Info)
        }
        TestButton("Log Warning") {
            Bugsee.log("Warning message", BugseeLogLevel.Warning)
        }
        TestButton("Log Error") {
            Bugsee.log("Error message", BugseeLogLevel.Error)
        }
        TestButton("Log Debug") {
            Bugsee.log("Debug message", BugseeLogLevel.Debug)
        }
        TestButton("Log Verbose") {
            Bugsee.log("Verbose message", BugseeLogLevel.Verbose)
        }
        TestButton("Log 100 Messages") {
            for (i in 1..100) {
                Bugsee.log("Batch log message #$i", BugseeLogLevel.Info)
            }
        }

        HorizontalDivider()
        TestSectionLabel("Events & Traces")
        TestButton("Send Event") {
            Bugsee.event("test_event", mapOf("key" to "value"))
        }
        TestButton("Send Event (no params)") {
            Bugsee.event("simple_event")
        }
        TestButton("Send Trace") {
            Bugsee.trace("test_trace", 42)
        }
        TestButton("Send Multiple Traces") {
            for (i in 1..5) {
                Bugsee.trace("multi_trace", i * 10)
            }
        }
    }
}
