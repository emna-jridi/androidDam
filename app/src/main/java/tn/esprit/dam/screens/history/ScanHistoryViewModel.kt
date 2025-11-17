package tn.esprit.dam.screens.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.model.ScanHistoryItem
import tn.esprit.dam.data.repository.ScanRepository
import javax.inject.Inject

data class ScanHistoryUiState(
    val isLoading: Boolean = false,
    val scans: List<ScanHistoryItem> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true
)

@HiltViewModel
class ScanHistoryViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ScanHistoryViewModel"
        private const val PAGE_SIZE = 10
    }

    private val _uiState = MutableStateFlow(ScanHistoryUiState())
    val uiState: StateFlow<ScanHistoryUiState> = _uiState.asStateFlow()

    fun loadScans(token: String, userHash: String) {
        viewModelScope.launch {
            Log.d(TAG, "📜 Loading scans (page ${_uiState.value.currentPage})")

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            repository.getScanHistory(
                token = token,
                userHash = userHash,
                page = _uiState.value.currentPage,
                limit = PAGE_SIZE
            ).onSuccess { response ->
                Log.d(TAG, "✅ Scans loaded: ${response.data.scans.size} items")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scans = if (_uiState.value.currentPage == 1) {
                        response.data.scans
                    } else {
                        _uiState.value.scans + response.data.scans
                    },
                    hasMore = response.data.scans.size == PAGE_SIZE
                )
            }.onFailure { error ->
                Log.e(TAG, "❌ Load scans failed", error)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load scans"
                )
            }
        }
    }

    fun refresh(token: String, userHash: String) {
        _uiState.value = ScanHistoryUiState()
        loadScans(token, userHash)
    }

    fun loadMore(token: String, userHash: String) {
        if (!_uiState.value.isLoading && _uiState.value.hasMore) {
            _uiState.value = _uiState.value.copy(
                currentPage = _uiState.value.currentPage + 1
            )
            loadScans(token, userHash)
        }
    }

    /**
     * ✅ CORRIGÉ: Utiliser deleteScan au lieu de deleteScanHistory
     */
    fun deleteScan(token: String, userHash: String, scanId: String) {
        viewModelScope.launch {
            Log.d(TAG, "🗑️ Deleting scan: $scanId")

            // ✅ CORRECTION: repository.deleteScan (pas deleteScanHistory)
            repository.deleteScan(token, scanId, userHash)
                .onSuccess { success ->
                    if (success) {
                        Log.d(TAG, "✅ Scan deleted")

                        _uiState.value = _uiState.value.copy(
                            scans = _uiState.value.scans.filter { it._id != scanId }
                        )
                    } else {
                        Log.e(TAG, "❌ Delete scan returned false")
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to delete scan"
                        )
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Delete scan failed", error)

                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete scan"
                    )
                }
        }
    }

    /**
     * ✅ Effacer l'erreur
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}