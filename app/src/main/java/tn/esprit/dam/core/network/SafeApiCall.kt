package tn.esprit.dam.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Thrown when an [ApiResult] failure is bridged to a [kotlin.Result] or rethrown. */
class ApiException(message: String, val code: Int? = null, cause: Throwable? = null) : Exception(message, cause)

private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

/**
 * The single entry point for HTTP calls. Never throws (except cancellation):
 * - 2xx -> [ApiResult.Success] with the decoded body
 * - non-2xx -> [ApiResult.ApiError] with the server's message
 * - IO failure -> [ApiResult.NetworkError]
 * - bad payload -> [ApiResult.SerializationError]
 *
 * 401 handling (token refresh / logout) is owned by the Ktor Auth plugin, and retries
 * of idempotent GETs by HttpRequestRetry, both configured in NetworkModule.
 */
suspend inline fun <reified T> safeApiCall(block: () -> HttpResponse): ApiResult<T> {
    val response = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        return ApiResult.NetworkError(e)
    }

    if (!response.status.isSuccess()) {
        return ApiResult.ApiError(extractErrorMessage(response), response.status.value)
    }

    return try {
        ApiResult.Success(response.body<T>())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ApiResult.SerializationError(e, runCatching { response.bodyAsText() }.getOrNull())
    }
}

@PublishedApi
internal suspend fun extractErrorMessage(response: HttpResponse): String {
    val raw = runCatching { response.bodyAsText() }.getOrNull().orEmpty()
    val parsed = try {
        errorJson.decodeFromString(ApiError.serializer(), raw)
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
    return parsed?.message?.takeIf { it.isNotBlank() }
        ?: parsed?.error?.takeIf { it.isNotBlank() }
        ?: raw.take(200).ifBlank { "HTTP ${response.status.value}" }
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.ApiError -> this
    is ApiResult.NetworkError -> this
    is ApiResult.SerializationError -> this
}

inline fun <T, R> ApiResult<T>.flatMap(transform: (T) -> ApiResult<R>): ApiResult<R> = when (this) {
    is ApiResult.Success -> transform(data)
    is ApiResult.ApiError -> this
    is ApiResult.NetworkError -> this
    is ApiResult.SerializationError -> this
}

fun <T> ApiResult<T>.toException(): ApiException? = when (this) {
    is ApiResult.Success -> null
    is ApiResult.ApiError -> ApiException(message, code)
    is ApiResult.NetworkError -> ApiException(exception.message ?: "Network error", cause = exception)
    is ApiResult.SerializationError -> ApiException("Error processing data", cause = exception)
}

fun <T> ApiResult<T>.getOrThrow(): T = when (this) {
    is ApiResult.Success -> data
    else -> throw toException()!!
}

/** Bridge for layers that still expose [kotlin.Result]. */
fun <T> ApiResult<T>.toResult(): Result<T> = when (this) {
    is ApiResult.Success -> Result.success(data)
    else -> Result.failure(toException()!!)
}

/** Unwraps a `{ success, data, message }` envelope into its data. */
fun <T> ApiResult<ApiEnvelope<T>>.unwrapEnvelope(): ApiResult<T> = flatMap { envelope ->
    val data = envelope.data
    if (envelope.success && data != null) ApiResult.Success(data)
    else ApiResult.ApiError(envelope.message ?: "API call failed", 200)
}

/** Drops the Auth plugin's cached bearer tokens so the next request reloads them from storage. */
fun HttpClient.invalidateBearerTokens() {
    plugin(Auth).providers.filterIsInstance<BearerAuthProvider>().forEach { it.clearToken() }
}
