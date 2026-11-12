package com.bugsee.kmp.sample

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets

actual fun sampleHttpClient(): HttpClient = HttpClient(Android)

actual fun sampleWebSocketClient(): HttpClient = HttpClient(OkHttp) { install(WebSockets) }
