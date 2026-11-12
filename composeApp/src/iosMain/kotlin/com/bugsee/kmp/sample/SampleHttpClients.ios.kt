package com.bugsee.kmp.sample

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.websocket.WebSockets

actual fun sampleHttpClient(): HttpClient = HttpClient(Darwin)

actual fun sampleWebSocketClient(): HttpClient = HttpClient(Darwin) { install(WebSockets) }
