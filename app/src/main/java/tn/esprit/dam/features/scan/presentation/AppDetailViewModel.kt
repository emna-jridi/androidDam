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
import tn.esprit.dam.data.api.models.AppDetailsResponse
import tn.esprit.dam.data.api.models.AppScanHistoryDto
import tn.esprit.dam.data.repository.ScanRepository

data class AppDetailUIState(
    val details: AppDetailsResponse? = null,
    val packageName: String? = null,
    val appName: String? = null,
    val securityScore: Float? = null,
    val privacyScore: Float? = null,
    val overallScore: Float? = null,
    val globalRisk: String? = null,
    val trackerCount: Int = 0,
    val permissionCount: Int = 0,
    val permissions: List<String> = emptyList(),
    val trackers: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
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

            when (val result = repository.getAppDetails(packageName)) {
                is ApiResult.Success -> {
                    val data = result.data
                    Log.d(TAG, "Successfully loaded app details for $packageName")
                    Log.d(TAG, "  - securityScore: ${data.securityScore}")
                    Log.d(TAG, "  - privacyScore: ${data.privacyScore}")
                    Log.d(TAG, "  - trackers: ${data.trackers?.totalFound ?: 0}")
                    Log.d(TAG, "  - permissions: ${data.permissions.size}")
                    
                    _uiState.value = _uiState.value.copy(
                        details = data,
                        packageName = data.packageName ?: data.app?.packageName ?: packageName,
                        appName = data.appName ?: data.app?.displayName ?: packageName,
                        securityScore = data.securityScore ?: data.overallScore,
                        privacyScore = data.privacyScore,
                        overallScore = data.overallScore,
                        globalRisk = data.globalRisk,
                        trackerCount = data.trackers?.totalFound ?: data.app?.trackers?.size ?: 0,
                        permissionCount = data.permissions.size.takeIf { it > 0 } ?: data.app?.permissions?.size ?: 0,
                        permissions = data.permissions.takeIf { it.isNotEmpty() } ?: data.app?.permissions ?: emptyList(),
                        trackers = data.trackers?.trackers?.map { it.name } ?: data.app?.trackers?.map { it.name } ?: emptyList(),
                        recommendations = data.recommendations,
                        history = data.history.sortedByDescending { it.scanDate },
                        isLoading = false,
                        error = null
                    )
                }
                is ApiResult.ApiError -> {
                    Log.e(TAG, "API Error for $packageName: ${result.message} (code: ${result.code})")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Application non trouvée: ${result.message}"
                    )
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
