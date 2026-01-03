package tn.esprit.dam.features.base

import android.util.Log
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.utils.AppConstants

/**
 * Extension functions for common ViewModel patterns
 * Reduces duplication in error handling and state management
 */

/**
 * Handle ApiResult with common error mapping
 */
inline fun <T> ApiResult<T>.getOrElse(
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit,
    tag: String = "ApiResult"
) {
    when (this) {
        is ApiResult.Success -> onSuccess(this.data)
        is ApiResult.ApiError -> {
            Log.e(tag, "API Error: ${this.message}")
            onError(this.message)
        }
        is ApiResult.NetworkError -> {
            Log.e(tag, "Network Error")
            onError(AppConstants.ErrorMessages.ERROR_NETWORK_TIMEOUT)
        }
        is ApiResult.SerializationError -> {
            Log.e(tag, "Serialization Error: ${this.exception.message}")
            onError("Error processing data: ${this.exception.message}")
        }
    }
}

/**
 * Convert ApiResult to a user-friendly error message
 */
fun <T> ApiResult<T>.getErrorMessage(): String? {
    return when (this) {
        is ApiResult.Success -> null
        is ApiResult.ApiError -> this.message
        is ApiResult.NetworkError -> AppConstants.ErrorMessages.ERROR_NETWORK_TIMEOUT
        is ApiResult.SerializationError -> "Error processing data"
    }
}

/**
 * Check if result is successful
 */
fun <T> ApiResult<T>.isSuccess(): Boolean = this is ApiResult.Success

/**
 * Check if result is an error
 */
fun <T> ApiResult<T>.isError(): Boolean = this !is ApiResult.Success

/**
 * Extract data or return null on error
 */
fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

/**
 * Map success result to another type
 */
inline fun <T, R> ApiResult<T>.mapSuccess(transform: (T) -> R): ApiResult<R> {
    return when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(this.data))
        is ApiResult.ApiError -> ApiResult.ApiError(this.message, this.code)
        is ApiResult.NetworkError -> ApiResult.NetworkError(this.exception)
        is ApiResult.SerializationError -> ApiResult.SerializationError(this.exception, this.rawResponse)
    }
}

/**
 * Execute side effect on success
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) {
        action(this.data)
    }
    return this
}

/**
 * Execute side effect on error
 */
inline fun <T> ApiResult<T>.onError(action: (String) -> Unit): ApiResult<T> {
    val message = getErrorMessage()
    if (message != null) {
        action(message)
    }
    return this
}
