package com.bugsee.kmp.sample.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.bugsee.kmp.sample.sampleWebSocketClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun WebSocketScreen(onBackClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var log by remember { mutableStateOf("") }
    var connected by remember { mutableStateOf(false) }
    var wsJob by remember { mutableStateOf<Job?>(null) }
    var sendAction by remember { mutableStateOf<(suspend (String) -> Unit)?>(null) }
    var closeAction by remember { mutableStateOf<(suspend () -> Unit)?>(null) }

    // Runs [action] on the open connection, or notes that there is none. The connection can close
    // while the action runs (Disconnect during the 10 messages): that is logged, not thrown.
    fun withConnection(action: suspend (send: suspend (String) -> Unit) -> Unit) {
        val send = sendAction
        if (send == null) {
            log += "Not connected: tap Connect first\n"
            return
        }
        scope.launch {
            try {
                action(send)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log += "Send failed: ${e.message}\n"
            }
        }
    }

    TestScreenScaffold(title = "WebSocket", onBackClick = {
        wsJob?.cancel()
        onBackClick()
    }) {
        TestButton(if (connected) "Connected" else "Connect") {
            // Also while connecting, before the handshake has finished.
            if (wsJob?.isActive != true) {
                log = ""
                wsJob = scope.launch {
                    try {
                        sampleWebSocketClient().use { client ->
                            client.webSocket("wss://ws.postman-echo.com/raw") {
                                connected = true
                                log += "Connected to echo server\n"

                                sendAction = { message ->
                                    send(Frame.Text(message))
                                    log += "Sent: $message\n"
                                }

                                closeAction = {
                                    close()
                                }

                                for (frame in incoming) {
                                    if (frame is Frame.Text) {
                                        log += "Received: ${frame.readText()}\n"
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        log += "Error: ${e.message}\n"
                    } finally {
                        connected = false
                        sendAction = null
                        closeAction = null
                        log += "Disconnected\n"
                    }
                }
            }
        }

        TestButton("Send Text Message") {
            withConnection { send -> send("Hello from Bugsee KMP!") }
        }

        TestButton("Send Multiple Messages (10)") {
            withConnection { send ->
                for (i in 1..10) {
                    send("Message #$i")
                }
            }
        }

        TestButton("Disconnect") {
            val close = closeAction
            if (close == null) {
                log += "Not connected\n"
            } else {
                scope.launch {
                    try {
                        close()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        log += "Disconnect failed: ${e.message}\n"
                    }
                }
            }
        }

        if (log.isNotEmpty()) {
            Text(
                text = log,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp)
            )
        }
    }
}
