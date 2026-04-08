package com.bugsee.kmp.sample.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@Composable
fun AdvancedNetworkScreen(onBackClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }

    TestScreenScaffold(title = "Advanced Network Tests", onBackClick = onBackClick) {
        TestButton("Slow Request (5s delay)") {
            status = "Loading..."
            scope.launch {
                try {
                    val client = HttpClient()
                    val response = client.get("https://httpbin.org/delay/5")
                    status = "Slow request: ${response.status}"
                    client.close()
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }

        TestButton("Large Response (100KB)") {
            status = "Loading..."
            scope.launch {
                try {
                    val client = HttpClient()
                    val response = client.get("https://httpbin.org/bytes/102400")
                    val body = response.bodyAsText()
                    status = "Large response: ${response.status}, ${body.length} chars"
                    client.close()
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }

        TestButton("404 Request") {
            status = "Loading..."
            scope.launch {
                try {
                    val client = HttpClient()
                    val response = client.get("https://httpbin.org/status/404")
                    status = "404 request: ${response.status}"
                    client.close()
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }

        TestButton("Redirect Chain (3 hops)") {
            status = "Loading..."
            scope.launch {
                try {
                    val client = HttpClient()
                    val response = client.get("https://httpbin.org/redirect/3")
                    status = "Redirect chain: ${response.status}"
                    client.close()
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }

        TestButton("POST with JSON Body") {
            status = "Loading..."
            scope.launch {
                try {
                    val client = HttpClient()
                    val response = client.post("https://httpbin.org/post") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"test": "bugsee"}""")
                    }
                    status = "POST: ${response.status}"
                    client.close()
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }

        TestButton("Multiple Concurrent Requests (5)") {
            status = "Loading..."
            scope.launch {
                try {
                    val client = HttpClient()
                    val results = (1..5).map {
                        async { client.get("https://httpbin.org/get") }
                    }.awaitAll()
                    status = "Concurrent: ${results.map { it.status }}"
                    client.close()
                } catch (e: Exception) {
                    status = "Error: ${e.message}"
                }
            }
        }

        if (status.isNotEmpty()) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}
