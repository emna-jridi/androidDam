package tn.esprit.dam.features.scan.presentation.detail

import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.repository.ScanRepository

data class AppDetailUIState(
    val app: AppDetailUi? = null,
    val history: List<AppScanHistoryUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null
)

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppDetailUIState())
    val uiState: StateFlow<AppDetailUIState> = _uiState.asStateFlow()

    fun loadAppDetails(packageName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                error = null
            ) }

            when (val result = repository.getAppDetails(packageName)) {
                is ApiResult.Success -> {
                    val data = result.data

                    _uiState.update { it.copy(
                        app = data.toAppDetailUi(packageName),
                        history = data.history.sortedByDescending { it.scanDate }.map { it.toUi() },
                        isLoading = false,
                        error = null
                    ) }
                }
                is ApiResult.ApiError -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = UiText.Res(R.string.error_app_not_found, result.message)
                    ) }
                }
                is ApiResult.NetworkError -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.exception.message?.let { UiText.Res(R.string.error_network_with_message, it) } ?: UiText.Res(R.string.error_network_check_connection)
                    ) }
                }
                is ApiResult.SerializationError -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = UiText.Res(R.string.error_data, result.exception.message.orEmpty())
                    ) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh(packageName: String) {
        loadAppDetails(packageName)
    }
}
