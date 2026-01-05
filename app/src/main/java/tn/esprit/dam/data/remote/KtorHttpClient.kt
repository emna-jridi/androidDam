package tn.esprit.dam.data.remote

import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.utils.sanitizeForUI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorHttpClient @Inject constructor(
    @ApplicationContext private val context: android.content.Context
) {

    private val httpClient = HttpClient(Android) {
        engine {
            connectTimeout = 120_000  // 2 minutes
            socketTimeout = 300_000   // 5 minutes
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorClient", message)
                }
            }
            level = LogLevel.BODY
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 300_000  // 5 minutes for long scans
            connectTimeoutMillis = 120_000  // 2 minutes to establish connection
            socketTimeoutMillis = 300_000   // 5 minutes for data transfer
        }

        defaultRequest {
            header("Content-Type", "application/json; charset=UTF-8")
            header("Accept", "application/json")
            header("Accept-Charset", "UTF-8")
        }
    }

    internal suspend inline fun <reified T> get(
        url: String,
        maxRetries: Int = 3,
        delayMs: Long = 1000
    ): Result<T> = executeWithRetry(maxRetries, delayMs) {
        Log.d("KtorClient", "GET: ")
        val token = TokenManager.getAccessToken(context)
        val response = httpClient.get(url) {
            if (token != null) header("Authorization", "Bearer $token")
        }
        enforceStatus(response)
        response.body()
    }

    internal suspend inline fun <reified RequestType, reified T> post(
        url: String,
        body: RequestType,
        maxRetries: Int = 3,
        delayMs: Long = 1000
    ): Result<T> = executeWithRetry(maxRetries, delayMs) {
        Log.d("KtorClient", "POST: $url")
        val token = TokenManager.getAccessToken(context)
        val response = httpClient.post(url) {
            if (token != null) header("Authorization", "Bearer $token")
            setBody(body)
        }
        enforceStatus(response)
        response.body()
    }

    internal suspend inline fun <reified RequestType, reified T> put(
        url: String,
        body: RequestType,
        maxRetries: Int = 3,
        delayMs: Long = 1000
    ): Result<T> = executeWithRetry(maxRetries, delayMs) {
        Log.d("KtorClient", "PUT: $url")
        val token = TokenManager.getAccessToken(context)
        val response = httpClient.put(url) {
            if (token != null) header("Authorization", "Bearer $token")
            setBody(body)
        }
        enforceStatus(response)
        response.body()
    }

    internal suspend inline fun <reified T> delete(
        url: String,
        maxRetries: Int = 3,
        delayMs: Long = 1000
    ): Result<T> = executeWithRetry(maxRetries, delayMs) {
        Log.d("KtorClient", "DELETE: ")
        val token = TokenManager.getAccessToken(context)
        val response = httpClient.delete(url) {
            if (token != null) header("Authorization", "Bearer $token")
        }
        enforceStatus(response)
        response.body()
    }

    private suspend inline fun <reified T> executeWithRetry(
        maxRetries: Int,
        delayMs: Long,
        block: suspend () -> T
    ): Result<T> {
        repeat(maxRetries) { attempt ->
            try {
                Log.d("KtorClient", "Attempt ${attempt + 1}/$maxRetries")
                return Result.success(block())
            } catch (e: Exception) {
                if (e is ClientRequestException && e.response.status == HttpStatusCode.Unauthorized) {
                    Log.w("KtorClient", "401 Unauthorized - clearing tokens and aborting retries")
                    // Clear tokens so UI can redirect to login
                    TokenManager.clearAll(context)
                    return Result.failure(e)
                }

                Log.e("KtorClient", "Attempt ${attempt + 1} failed: ${e.message}")
                if (attempt == maxRetries - 1) {
                    return Result.failure(e)
                }
                delay(delayMs * (1L shl attempt))
            }
        }
        return Result.failure(Exception("Max retries exceeded"))
    }

    private suspend fun enforceStatus(response: HttpResponse) {
        if (response.status == HttpStatusCode.Unauthorized) {
            Log.w("KtorClient", "401 Unauthorized - clearing tokens")
            TokenManager.clearAll(context)
            throw ClientRequestException(response, "Unauthorized")
        }
        if (!response.status.isSuccess()) {
            throw ClientRequestException(response, "HTTP ${response.status}")
        }
    }

    fun close() {
        httpClient.close()
    }
}