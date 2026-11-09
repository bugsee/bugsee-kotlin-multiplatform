package com.bugsee.kmp.sample.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bugsee.kmp.Bugsee

@Composable
fun UserIdentifierTestsScreen(onBackClick: () -> Unit) {
    var customIdentifier by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    TestScreenScaffold(title = "User Identifier Tests", onBackClick = onBackClick) {
        TestButton("Set Identifier (test@bugsee.com)") {
            Bugsee.setUserIdentifier("test@bugsee.com")
            status = "User identifier set to: test@bugsee.com"
        }

        OutlinedTextField(
            value = customIdentifier,
            onValueChange = { customIdentifier = it },
            label = { Text("Custom user identifier") },
            modifier = Modifier.fillMaxWidth()
        )
        // An empty identifier clears it, as clearUserIdentifier() does.
        TestButton("Set Custom Identifier") {
            Bugsee.setUserIdentifier(customIdentifier)
            status = if (customIdentifier.isEmpty()) "User identifier cleared" else "User identifier set to: $customIdentifier"
        }

        TestButton("Get Identifier") {
            val userIdentifier = Bugsee.getUserIdentifier()
            status = "Current user identifier: ${userIdentifier ?: "(not set)"}"
        }

        TestButton("Clear Identifier") {
            Bugsee.clearUserIdentifier()
            status = "User identifier cleared"
        }

        if (status.isNotEmpty()) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
