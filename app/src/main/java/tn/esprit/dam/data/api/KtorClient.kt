package tn.esprit.dam.data.api

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.Config
import tn.esprit.dam.data.TokenManager

object KtorClient {
    private var instance: HttpClient? = null

    fun getInstance(context: Context, tokenManager: TokenManager): HttpClient {
        if (instance == null) {
            instance = buildHttpClient(context, tokenManager)
        }
        return instance!!
    }

    private fun buildHttpClient(context: Context, tokenManager: TokenManager): HttpClient {
        return HttpClient(Android) {
            // Request timeout configuration
            install(HttpTimeout) {
                requestTimeoutMillis = 300_000  // 5 minutes for long scans
                connectTimeoutMillis = 120_000  // 2 minutes to establish connection
                socketTimeoutMillis = 300_000   // 5 minutes for data transfer
            }

            // JSON serialization with kotlinx
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = false
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    allowSpecialFloatingPointValues = true
                    encodeDefaults = true  // IMPORTANT: Include default values in request body
                })
            }

            // Comprehensive API Logging with custom logger
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        val tag = "API_LOG"
                        
                        when {
                            // Request lines - show full URL
                            message.contains("REQUEST:") || message.contains("-> ") -> {
                                Log.d(tag, "➡️  $message")
                            }
                            // Response lines  
                            message.contains("RESPONSE:") || message.contains("<- ") -> {
                                Log.d(tag, "⬅️  $message")
                            }
                            // Method and URL
                            message.startsWith("METHOD:") || message.startsWith("URL:") -> {
                                Log.d(tag, "   $message")
                            }
                            // Authorization header - mask token
                            message.contains("Authorization") -> {
                                val masked = message.replace(
                                    Regex("Bearer [a-zA-Z0-9._-]+"),
                                    "Bearer eyJhbGci...XXX"
                                )
                                Log.d(tag, "   $masked")
                            }
                            // Body content - show full JSON
                            message.contains("BODY") || message.startsWith("{") || message.startsWith("[") -> {
                                Log.d(tag, "   $message")
                            }
                            // Error messages
                            message.contains("Exception") || message.contains("ERROR") -> {
                                Log.e(tag, "❌  $message")
                            }
                            // Everything else
                            else -> {
                                Log.d(tag, "   $message")
                            }
                        }
                    }
                }
                level = LogLevel.ALL // Log everything including headers and body
            }

            // Retry policy with status code check
            install(HttpRequestRetry) {
                maxRetries = 2
                retryIf { request, response ->
                    !response.status.isSuccess() && response.status.value != 401
                }
                exponentialDelay()
            }

            // Default request configuration
            defaultRequest {
                url(Config.BASE_URL)
                contentType(ContentType.Application.Json)

                // Add authorization header with token if available
                val token = runBlocking { tokenManager.getAccessToken(context) }
                if (!token.isNullOrBlank()) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }

    fun close() {
        instance?.close()
        instance = null
    }
}
