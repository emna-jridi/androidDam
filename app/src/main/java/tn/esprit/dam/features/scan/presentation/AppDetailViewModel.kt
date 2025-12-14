package tn.esprit.dam.features.scan.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.api.models.AppInfoDto
import tn.esprit.dam.data.api.models.AppScanHistoryDto
import tn.esprit.dam.data.repository.ScanRepository

data class AppDetailUIState(
    val app: AppInfoDto? = null,
    val history: List<AppScanHistoryDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {

    private val TAG = "AppDetailViewModel"
    private val _uiState = MutableStateFlow(AppDetailUIState())
    val uiState: StateFlow<AppDetailUIState> = _uiState.asStateFlow()

    /**
     * Load app details from the backend
     */
    fun loadAppDetails(packageName: String) {
        viewModelScope.launch {
            Log.d(TAG, "Loading app details for: $packageName")
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            // Try to get full details from apps endpoint first
            when (val result = repository.getFullAppDetails(packageName)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Successfully loaded app details for $packageName")
                    _uiState.value = _uiState.value.copy(
                        app = result.data.app,
                        history = result.data.history.sortedByDescending { it.scanDate },
                        isLoading = false,
                        error = null
                    )
                }
                is ApiResult.ApiError -> {
                    Log.w(TAG, "API Error from /api/apps/$packageName: ${result.message} (code: ${result.code})")
                    // Fallback to scan endpoint
                    loadFromScanEndpoint(packageName, result.message)
                }
                is ApiResult.NetworkError -> {
                    Log.e(TAG, "Network error loading app details", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur réseau: ${result.exception.message ?: "vérifiez votre connexion"}"
                    )
                }
                is ApiResult.SerializationError -> {
                    Log.e(TAG, "Serialization error loading app details", result.exception)
                    Log.e(TAG, "Raw response: ${result.rawResponse?.take(500)}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur de format de données: ${result.exception.message}"
                    )
                }
            }
        }
    }

    /**
     * Fallback to load from scan endpoint
     */
    private suspend fun loadFromScanEndpoint(packageName: String, previousError: String) {
        Log.d(TAG, "Trying fallback endpoint /api/scan/app/$packageName (previous error: $previousError)")
        when (val result = repository.getAppDetails(packageName)) {
            is ApiResult.Success -> {
                Log.d(TAG, "Successfully loaded app details from scan endpoint for $packageName")
                _uiState.value = _uiState.value.copy(
                    app = result.data.app,
                    history = result.data.history.sortedByDescending { it.scanDate },
                    isLoading = false,
                    error = null
                )
            }
            is ApiResult.ApiError -> {
                Log.e(TAG, "API Error from /api/scan/app/$packageName: ${result.message} (code: ${result.code})")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Application non trouvée: ${result.message}"
                )
            }
            is ApiResult.NetworkError -> {
                Log.e(TAG, "Network error from fallback endpoint", result.exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur réseau: ${result.exception.message ?: "vérifiez votre connexion"}"
                )
            }
            is ApiResult.SerializationError -> {
                Log.e(TAG, "Serialization error from fallback endpoint", result.exception)
                Log.e(TAG, "Raw response: ${result.rawResponse?.take(500)}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur de format: ${result.exception.message}"
                )
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Refresh app details
     */
    fun refresh(packageName: String) {
        loadAppDetails(packageName)
    }
}
