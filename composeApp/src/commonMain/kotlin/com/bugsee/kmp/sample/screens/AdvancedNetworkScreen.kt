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
import com.bugsee.kmp.sample.sampleHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun AdvancedNetworkScreen(onBackClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }

    // Runs [request] with a new client, closed afterwards also on failure, and shows its result.
    fun runRequest(request: suspend (HttpClient) -> String) {
        status = "Loading..."
        scope.launch {
            status = try {
                sampleHttpClient().use { client -> request(client) }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    TestScreenScaffold(title = "Advanced Network Tests", onBackClick = onBackClick) {
        TestButton("Slow Request (5s delay)") {
            runRequest { client ->
                "Slow request: ${client.get("https://httpbin.org/delay/5").status}"
            }
        }

        TestButton("Large Response (100KB)") {
            runRequest { client ->
                val response = client.get("https://httpbin.org/bytes/102400")
                "Large response: ${response.status}, ${response.bodyAsBytes().size} bytes"
            }
        }

        TestButton("404 Request") {
            runRequest { client ->
                "404 request: ${client.get("https://httpbin.org/status/404").status}"
            }
        }

        TestButton("Redirect Chain (3 hops)") {
            runRequest { client ->
                "Redirect chain: ${client.get("https://httpbin.org/redirect/3").status}"
            }
        }

        // The Authorization header is a fake secret for the redaction: the SDK's built-in one, or
        // the network filter of Filters & Listeners, which replaces it.
        TestButton("POST with JSON Body and Authorization") {
            runRequest { client ->
                val response = client.post("https://httpbin.org/post") {
                    header(HttpHeaders.Authorization, "Bearer sample-secret-token")
                    contentType(ContentType.Application.Json)
                    setBody("""{"test": "bugsee"}""")
                }
                "POST: ${response.status}"
            }
        }

        TestButton("Multiple Concurrent Requests (5)") {
            runRequest { client ->
                val results = coroutineScope {
                    (1..5).map {
                        async { client.get("https://httpbin.org/get") }
                    }.awaitAll()
                }
                "Concurrent: ${results.map { it.status }}"
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
