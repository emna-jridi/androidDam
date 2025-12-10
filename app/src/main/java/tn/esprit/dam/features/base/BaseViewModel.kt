// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
// FILE: app/src/main/java/com/example/shadowguard/features/base/BaseViewModel.kt
// BaseViewModel - Pattern de base pour tous les ViewModels
// â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

package tn.esprit.dam.features.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.pow

private const val TAG = "BaseViewModel"

/**
 * Sealed class pour les Ã©tats UI
 */
sealed class UiState<out T> {
    object Initial : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}

/**
 * Sealed class pour les Ã©vÃ©nements UI
 */
sealed class UiEvent {
    data class ShowMessage(val message: String) : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    data class Navigate(val destination: String) : UiEvent()
}

/**
 * Base ViewModel avec patterns communs
 * - Retry logic avec exponential backoff
 * - Loading states
 * - Error handling
 * - Polling utilities
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Event channel pour les événements ponctuels
     */
    protected val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    /**
     * Flow pour les messages d'erreur
     */
    protected val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    /**
     * Execute operation with loading state and error handling
     */
    protected fun <T> executeWithLoading(
        stateFlow: MutableStateFlow<UiState<T>>,
        operation: suspend () -> T,
    ) {
        viewModelScope.launch {
            try {
                stateFlow.value = UiState.Loading
                Log.d(TAG, "Operation started")

                val result = operation()
                stateFlow.value = UiState.Success(result)
                Log.d(TAG, "âœ“ Operation succeeded")
            } catch (e: Exception) {
                Log.e(TAG, "âœ— Operation failed", e)
                stateFlow.value = UiState.Error(
                    e.message ?: "Une erreur est survenue",
                    e,
                )
                _events.emit(UiEvent.ShowError(e.message ?: "Erreur inconnue"))
            }
        }
    }

    /**
     * Execute operation with retry logic and exponential backoff
     * Backoff: 1s, 2s, 4s, 8s...
     */
    protected suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        backoffMultiplier: Float = 2f,
    ): T {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                Log.d(TAG, "Attempt ${attempt + 1}/$maxRetries")
                return operation()
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Attempt ${attempt + 1} failed: ${e.message}")

                if (attempt < maxRetries - 1) {
                    val delayMs = (initialDelayMs * (backoffMultiplier.powExp(attempt))).toLong()
                    Log.d(TAG, "Waiting ${delayMs}ms before retry...")
                    delay(delayMs)
                }
            }
        }

        throw lastException ?: Exception("Max retries exceeded")
    }

    /**
     * Poll for updates until condition is met or timeout.
     */
    protected fun <T> pollUntil(
        intervalMs: Long = 3000,
        maxDurationMs: Long = 300000, // 5 minutes default
        condition: (T) -> Boolean,
        operation: suspend () -> T,
        onUpdate: (T) -> Unit,
        onComplete: (T) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var pollCount = 0

            while (System.currentTimeMillis() - startTime < maxDurationMs) {
                try {
                    pollCount++
                    Log.d(TAG, "Poll #$pollCount")

                    val result = operation()
                    onUpdate(result)

                    if (condition(result)) {
                        Log.d(TAG, "âœ“ Condition met after $pollCount polls")
                        onComplete(result)
                        return@launch
                    }

                    delay(intervalMs)
                } catch (e: Exception) {
                    Log.e(TAG, "Poll error", e)
                    onError(e.message ?: "Erreur lors du polling")
                    return@launch
                }
            }

            Log.w(TAG, "Polling timeout after ${System.currentTimeMillis() - startTime}ms")
            onError("Timeout: L'opÃ©ration a pris trop de temps (${maxDurationMs}ms)")
        }
    }

    /**
     * Debounce user input (e.g., search query)
     */
    protected fun <T> Flow<T>.debounceInput(
        waitMs: Long = 500,
    ): Flow<T> = debounce(waitMs)

    /**
     * Map Result<T> to UiState<T>
     */
    protected fun <T> Result<T>.toUiState(): UiState<T> {
        return fold(
            onSuccess = { UiState.Success(it) },
            onFailure = { UiState.Error(it.message ?: "Unknown error", it) },
        )
    }

    /**
     * Execute and handle Result<T>
     */
    protected fun <T> executeResult(
        stateFlow: MutableStateFlow<UiState<T>>,
        operation: suspend () -> Result<T>,
    ) {
        viewModelScope.launch {
            try {
                stateFlow.value = UiState.Loading
                val result = operation()
                stateFlow.value = result.toUiState()
            } catch (e: Exception) {
                Log.e(TAG, "Error", e)
                stateFlow.value = UiState.Error(e.message ?: "Unknown error", e)
                _events.emit(UiEvent.ShowError(e.message ?: "Error"))
            }
        }
    }

    /**
     * Emit event
     */
    protected suspend fun emitEvent(event: UiEvent) {
        _events.emit(event)
    }

    /**
     * Show message
     */
    protected fun showMessage(message: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowMessage(message))
        }
    }

    /**
     * Show error
     */
    protected fun showError(message: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.ShowError(message))
        }
    }

    /**
     * Navigate to destination
     */
    protected fun navigate(destination: String) {
        viewModelScope.launch {
            _events.emit(UiEvent.Navigate(destination))
        }
    }

    /**
     * Measure operation duration
     */
    protected suspend fun <T> measureDuration(
        operation: suspend () -> T,
        operationName: String = "Operation",
    ): Pair<T, Long> {
        val startMs = System.currentTimeMillis()
        val result = operation()
        val durationMs = System.currentTimeMillis() - startMs

        Log.d(TAG, "[$operationName] Completed in ${durationMs}ms")

        return Pair(result, durationMs)
    }

    companion object {
        /**
         * Power function for exponential backoff
         */
        private fun Float.powExp(exponent: Int): Float {
            val doubleValue = this.toDouble()
            val result = doubleValue.pow(exponent.toDouble())
            return result.toFloat()
        }
    }
}
