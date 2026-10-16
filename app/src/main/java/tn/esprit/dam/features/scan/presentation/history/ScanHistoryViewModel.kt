package tn.esprit.dam.features.scan.presentation.history

import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import kotlinx.coroutines.flow.update
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.repository.ScanRepository
import javax.inject.Inject

data class ScanHistoryUiState(
    val isLoading: Boolean = false,
    val scans: List<ScanHistoryItemUi> = emptyList(),
    val error: UiText? = null,
    val total: Int = 0,
    val hasMore: Boolean = true,
    val currentPage: Int = 0
)

@HiltViewModel
class ScanHistoryViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {
    companion object {
        private const val PAGE_SIZE = 10
    }

    private val _uiState = MutableStateFlow(ScanHistoryUiState())
    val uiState: StateFlow<ScanHistoryUiState> = _uiState.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                error = null
            ) }

            val offset = _uiState.value.currentPage * PAGE_SIZE
            val result = repository.getScanHistory(
                limit = PAGE_SIZE,
                offset = offset
            )

            when (result) {
                is ApiResult.Success -> {
                    val historyData = result.data

                    val merged = if (_uiState.value.currentPage == 0) {
                        historyData.scans.map { it.toUi() }
                    } else {
                        _uiState.value.scans + historyData.scans.map { it.toUi() }
                    }

                    val sorted = merged
                        .distinctBy { it.scanId }
                        .sortedByDescending { parseDateToMillis(it.createdAt) }

                    _uiState.update { it.copy(
                        isLoading = false,
                        scans = sorted,
                        total = historyData.total,
                        hasMore = historyData.scans.size == PAGE_SIZE,
                        error = null
                    ) }
                }
                is ApiResult.ApiError -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = UiText.Res(R.string.error_with_message, result.message)
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

    fun loadMore() {
        if (!_uiState.value.isLoading && _uiState.value.hasMore) {
            _uiState.update { it.copy(
                currentPage = it.currentPage + 1
            ) }
            loadHistory()
        }
    }

    fun refresh() {
        _uiState.value = ScanHistoryUiState()
        loadHistory()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun parseDateToMillis(value: String): Long {
        return runCatching {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            inputFormat.parse(value)?.time ?: 0L
        }.getOrDefault(0L)
    }
}
