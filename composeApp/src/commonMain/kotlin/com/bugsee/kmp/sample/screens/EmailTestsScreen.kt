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
fun EmailTestsScreen(onBackClick: () -> Unit) {
    var customEmail by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    TestScreenScaffold(title = "Email Tests", onBackClick = onBackClick) {
        TestButton("Set Email (test@bugsee.com)") {
            Bugsee.setEmail("test@bugsee.com")
            status = "Email set to: test@bugsee.com"
        }

        OutlinedTextField(
            value = customEmail,
            onValueChange = { customEmail = it },
            label = { Text("Custom email") },
            modifier = Modifier.fillMaxWidth()
        )
        TestButton("Set Custom Email") {
            if (customEmail.isNotBlank()) {
                Bugsee.setEmail(customEmail)
                status = "Email set to: $customEmail"
            }
        }

        TestButton("Get Email") {
            val email = Bugsee.getEmail()
            status = "Current email: ${email ?: "(not set)"}"
        }

        TestButton("Clear Email") {
            Bugsee.clearEmail()
            status = "Email cleared"
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
