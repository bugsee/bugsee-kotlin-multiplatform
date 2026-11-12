package com.bugsee.kmp.sample

import io.ktor.client.HttpClient

/**
 * The Ktor clients of the sample, each with an explicit engine: with more than one engine on the
 * classpath, `HttpClient()` would pick one by service-loader order.
 */

/** For the HTTP tests: the Android engine (HttpURLConnection) on Android, Darwin on iOS. */
expect fun sampleHttpClient(): HttpClient

/**
 * For the WebSocket test, with the WebSockets plugin installed: OkHttp on Android (the Android
 * engine has no WebSocket support, and the Bugsee Gradle plugin captures OkHttp WebSockets),
 * Darwin on iOS.
 */
expect fun sampleWebSocketClient(): HttpClient
