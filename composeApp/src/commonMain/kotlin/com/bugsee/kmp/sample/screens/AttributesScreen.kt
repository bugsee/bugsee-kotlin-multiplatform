package com.bugsee.kmp.sample.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bugsee.kmp.Bugsee
import com.bugsee.kmp.BugseeLogLevel

@Composable
fun AttributesScreen(onBackClick: () -> Unit) {
    var statusText by remember { mutableStateOf("") }

    TestScreenScaffold(title = "Attributes", onBackClick = onBackClick) {
        if (statusText.isNotEmpty()) {
            Text(statusText)
        }

        TestButton("Set Attribute") {
            Bugsee.setAttribute("test_key", "test_value")
            statusText = "Attribute set: test_key = test_value"
        }

        TestButton("Get Attribute") {
            val value = Bugsee.getAttribute("test_key")
            Bugsee.log("getAttribute(test_key) = $value", BugseeLogLevel.Info)
            statusText = "test_key = $value"
        }

        TestButton("Clear All Attributes") {
            Bugsee.clearAllAttributes()
            statusText = "All attributes cleared"
        }
    }
}
