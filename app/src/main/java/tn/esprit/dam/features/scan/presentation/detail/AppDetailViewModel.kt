package tn.esprit.dam.features.scan.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.AppScanHistoryDto
import tn.esprit.dam.features.scan.data.repository.ScanRepository

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
    private val _uiState = MutableStateFlow(AppDetailUIState())
    val uiState: StateFlow<AppDetailUIState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    fun loadAppDetails(packageName: String, userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = repository.getAppDetails(packageName, userId)) {
                is ApiResult.Success -> {
                    val data = result.data

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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Application non trouvée: ${result.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur réseau: ${result.exception.message ?: "vérifiez votre connexion"}"
                    )
                }
                is ApiResult.SerializationError -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur de format de données: ${result.exception.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh(packageName: String) {
        if (currentUserId != null) {
            loadAppDetails(packageName, currentUserId!!)
        } else {
        }
    }
}
