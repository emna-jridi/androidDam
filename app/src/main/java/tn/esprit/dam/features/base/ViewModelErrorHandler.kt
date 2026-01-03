package tn.esprit.dam.features.base

import android.util.Log
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.utils.AppConstants

/**
 * ViewModelErrorHandler - Standardized error handling across all ViewModels
 * Reduces duplication and ensures consistent error messages
 */
object ViewModelErrorHandler {
    
    /**
     * Map API result to user-friendly error message
     */
    fun <T> handleApiResult(
        result: ApiResult<T>,
        tag: String = "ViewModel"
    ): String? {
        return when (result) {
            is ApiResult.Success -> null
            is ApiResult.ApiError -> {
                Log.e(tag, "API Error: ${result.message}")
                result.message
            }
            is ApiResult.NetworkError -> {
                Log.e(tag, "Network Error")
                AppConstants.ErrorMessages.ERROR_NETWORK_TIMEOUT
            }
            is ApiResult.SerializationError -> {
                Log.e(tag, "Serialization Error", result.exception)
                "Error processing data"
            }
        }
    }
    
    /**
     * Execute operation with standard error handling
     */
    inline fun <T> execute(
        operation: () -> ApiResult<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        tag: String = "ViewModel"
    ) {
        try {
            val result = operation()
            when (result) {
                is ApiResult.Success -> onSuccess(result.data)
                else -> {
                    val errorMsg = handleApiResult(result, tag) 
                        ?: AppConstants.ErrorMessages.ERROR_UNKNOWN
                    onError(errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error", e)
            onError(e.message ?: AppConstants.ErrorMessages.ERROR_UNKNOWN)
        }
    }
    
    /**
     * Execute suspend operation with standard error handling
     */
    suspend inline fun <T> executeSuspend(
        operation: suspend () -> ApiResult<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit,
        tag: String = "ViewModel"
    ) {
        try {
            val result = operation()
            when (result) {
                is ApiResult.Success -> onSuccess(result.data)
                else -> {
                    val errorMsg = handleApiResult(result, tag)
                        ?: AppConstants.ErrorMessages.ERROR_UNKNOWN
                    onError(errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Unexpected error", e)
            onError(e.message ?: AppConstants.ErrorMessages.ERROR_UNKNOWN)
        }
    }
}
