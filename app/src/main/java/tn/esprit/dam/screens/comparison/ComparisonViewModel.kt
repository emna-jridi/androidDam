package tn.esprit.dam.screens.comparison
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import tn.esprit.dam.data.repository.ScanRepository
import tn.esprit.dam.data.model.*
import javax.inject.Inject

data class ComparisonUiState(
    val isLoading: Boolean = false,
    val comparison: ComparisonData? = null,
    val error: String? = null
)

/**
 * ✅ AJOUT: @HiltViewModel et @Inject constructor
 */
@HiltViewModel
class ComparisonViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ComparisonViewModel"
    }

    private val _uiState = MutableStateFlow(ComparisonUiState())
    val uiState: StateFlow<ComparisonUiState> = _uiState.asStateFlow()

    /**
     * ✅ Comparer deux scans
     */
    fun compareScans(
        token: String,
        userHash: String,
        scanId1: String,
        scanId2: String
    ) {
        /*viewModelScope.launch {
            Log.d(TAG, "🔄 Comparing scans: $scanId1 vs $scanId2")

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            repository.compareScanHistory(token, userHash, scanId1, scanId2)
                .onSuccess { response ->
                    Log.d(TAG, "✅ Comparison complete")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        comparison = response.data
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Comparison failed", error)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to compare scans"
                    )
                }
        }*/
    }

    /**
     * ✅ Réinitialiser
     */
    fun reset() {
        _uiState.value = ComparisonUiState()
    }
}
