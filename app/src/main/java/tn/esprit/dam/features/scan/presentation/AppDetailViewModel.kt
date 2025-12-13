package tn.esprit.dam.features.scan.presentation

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

    private val _uiState = MutableStateFlow(AppDetailUIState())
    val uiState: StateFlow<AppDetailUIState> = _uiState.asStateFlow()

    /**
     * Load app details from the backend
     */
    fun loadAppDetails(packageName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            // Try to get full details from apps endpoint first
            when (val result = repository.getFullAppDetails(packageName)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        app = result.data.app,
                        history = result.data.history,
                        isLoading = false,
                        error = null
                    )
                }
                is ApiResult.ApiError -> {
                    // Fallback to scan endpoint
                    loadFromScanEndpoint(packageName, result.message)
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur réseau: vérifiez votre connexion"
                    )
                }
                is ApiResult.SerializationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur de données"
                    )
                }
            }
        }
    }

    /**
     * Fallback to load from scan endpoint
     */
    private suspend fun loadFromScanEndpoint(packageName: String, previousError: String) {
        when (val result = repository.getAppDetails(packageName)) {
            is ApiResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    app = result.data.app,
                    history = result.data.history,
                    isLoading = false,
                    error = null
                )
            }
            is ApiResult.ApiError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
            is ApiResult.NetworkError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur réseau: vérifiez votre connexion"
                )
            }
            is ApiResult.SerializationError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur de données"
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
