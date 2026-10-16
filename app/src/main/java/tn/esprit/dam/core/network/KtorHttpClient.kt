package tn.esprit.dam.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JSON convenience wrapper over the shared [HttpClient]. Auth, 401 refresh and GET retries
 * come from the client's plugins (see NetworkModule); this class only maps to [ApiResult].
 */
@Singleton
class KtorHttpClient @Inject constructor(
    @PublishedApi internal val httpClient: HttpClient
) {
    suspend inline fun <reified T> get(url: String): ApiResult<T> =
        safeApiCall { httpClient.get(url) { longTimeout() } }

    suspend inline fun <reified RequestType, reified T> post(url: String, body: RequestType): ApiResult<T> =
        safeApiCall { httpClient.post(url) { longTimeout(); setBody(body) } }

    suspend inline fun <reified RequestType, reified T> put(url: String, body: RequestType): ApiResult<T> =
        safeApiCall { httpClient.put(url) { longTimeout(); setBody(body) } }

    suspend inline fun <reified T> delete(url: String): ApiResult<T> =
        safeApiCall { httpClient.delete(url) { longTimeout() } }

    // Vault AI analysis and dark-web checks call slow upstream services.
    @PublishedApi
    internal fun HttpRequestBuilder.longTimeout() {
        timeout {
            requestTimeoutMillis = Config.Timeouts.LONG_REQUEST_TIMEOUT_MS
            socketTimeoutMillis = Config.Timeouts.LONG_REQUEST_TIMEOUT_MS
        }
    }
}
