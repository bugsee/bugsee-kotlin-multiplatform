package com.bugsee.kmp.sample.screens

import com.bugsee.kmp.sample.sampleHttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*

class NetworkHelper {
    suspend fun ktorGetRequest(url: String): NetworkResult {
        // A client per request, closed after it.
        return try {
            sampleHttpClient().use { client ->
                val response: HttpResponse = client.get(url)
                NetworkResult.Success(
                    statusCode = response.status.value,
                    body = response.bodyAsText(),
                    headers = response.headers.entries().associate { it.key to it.value }
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}

sealed class NetworkResult {
    data class Success(
        val statusCode: Int,
        val body: String,
        val headers: Map<String, List<String>>
    ) : NetworkResult()

    data class Error(val message: String) : NetworkResult()
}
