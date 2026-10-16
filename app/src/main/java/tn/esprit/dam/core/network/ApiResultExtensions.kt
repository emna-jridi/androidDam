package tn.esprit.dam.core.network

import tn.esprit.dam.core.util.AppConstants

inline fun <T> ApiResult<T>.getOrElse(
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit,
    tag: String = "ApiResult"
) {
    when (this) {
        is ApiResult.Success -> onSuccess(this.data)
        is ApiResult.ApiError -> {
            onError(this.message)
        }
        is ApiResult.NetworkError -> {
            onError(AppConstants.ErrorMessages.ERROR_NETWORK_TIMEOUT)
        }
        is ApiResult.SerializationError -> {
            onError("Error processing data: ${this.exception.message}")
        }
    }
}

fun <T> ApiResult<T>.getErrorMessage(): String? {
    return when (this) {
        is ApiResult.Success -> null
        is ApiResult.ApiError -> this.message
        is ApiResult.NetworkError -> AppConstants.ErrorMessages.ERROR_NETWORK_TIMEOUT
        is ApiResult.SerializationError -> "Error processing data"
    }
}

fun <T> ApiResult<T>.isSuccess(): Boolean = this is ApiResult.Success

fun <T> ApiResult<T>.isError(): Boolean = this !is ApiResult.Success

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) {
        action(this.data)
    }
    return this
}

inline fun <T> ApiResult<T>.onError(action: (String) -> Unit): ApiResult<T> {
    val message = getErrorMessage()
    if (message != null) {
        action(message)
    }
    return this
}
