package com.bugsee.kmp.sample.Network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class NetworkHelper {
    suspend fun ktorGetRequest(url: String): NetworkResult {
        val client = HttpClient()

        return try {
            val response: HttpResponse = client.get(url)
            val responseBody = response.bodyAsText()
            
            NetworkResult.Success(
                statusCode = response.status.value,
                body = responseBody,
                headers = response.headers.entries().associate { it.key to it.value }
            )
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
