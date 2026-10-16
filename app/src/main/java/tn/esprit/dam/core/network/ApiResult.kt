package tn.esprit.dam.core.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class ApiError(val message: String, val code: Int) : ApiResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ApiResult<Nothing>()
    data class SerializationError(val exception: Throwable, val rawResponse: String?) : ApiResult<Nothing>()
}
