package com.bugsee.kmp.sample

import io.ktor.client.HttpClient

// The Ktor clients of the sample, each with an explicit engine: with more than one engine on the
// classpath, `HttpClient()` would pick one by service-loader order.

/**
 * For the HTTP tests: the Android engine (HttpURLConnection) on Android, Darwin on iOS.
 *
 * Bugsee captures this client and [sampleWebSocketClient] without the Ktor extension
 * (`bugsee-android-ktor-3`): the Android engine through its HttpURLConnection capture, OkHttp
 * through `bugsee-android-okhttp`, Darwin through NSURLSession. Installing `BugseeKtor3Plugin` on
 * them would report each request twice.
 */
expect fun sampleHttpClient(): HttpClient

/**
 * For the WebSocket test, with the WebSockets plugin installed: OkHttp on Android (the Android
 * engine has no WebSocket support, and the Bugsee Gradle plugin captures OkHttp WebSockets),
 * Darwin on iOS.
 */
expect fun sampleWebSocketClient(): HttpClient
