package com.bugsee.kmp.sample.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CrashProbeScreen(onBackClick: () -> Unit) {
    TestScreenScaffold(title = "CrashProbe Tests", onBackClick = onBackClick) {
        Text(
            "These tests will crash the app. Reopen after each test and check the Bugsee dashboard for the captured crash.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TestButton("Null Pointer Crash") {
            @Suppress("UNUSED_VARIABLE")
            val len = (null as String?)!!.length
        }

        TestButton("Array Out of Bounds") {
            val list = listOf(1, 2, 3)
            @Suppress("UNUSED_VARIABLE")
            val item = list[10]
        }

        TestButton("Stack Overflow") {
            fun recurse(): Int = recurse() + 1
            @Suppress("UNUSED_VARIABLE")
            val result = recurse()
        }

        TestButton("Out of Memory") {
            val lists = mutableListOf<ByteArray>()
            while (true) {
                lists.add(ByteArray(1024 * 1024 * 10)) // 10MB chunks
            }
        }

        TestButton("Division by Zero") {
            val numerator = 50
            val denominator = 0
            @Suppress("UNUSED_VARIABLE")
            val result = numerator / denominator
        }

        TestButton("Force Unwrap Optional") {
            val nullStr: String? = null
            @Suppress("UNUSED_VARIABLE")
            val len = nullStr!!.length
        }

        TestButton("Concurrent Modification") {
            val list = mutableListOf(1, 2, 3, 4, 5)
            for (item in list) {
                if (item == 3) list.remove(item)
            }
        }

        TestButton("Custom Uncaught Exception") {
            throw RuntimeException("Test uncaught exception from CrashProbe")
        }
    }
}
