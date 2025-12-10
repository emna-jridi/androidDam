package tn.esprit.dam.screens.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import tn.esprit.dam.data.repository.ScanRepository
import tn.esprit.dam.data.model.ScanItem
import tn.esprit.dam.data.model.AppAnalysisResult
import tn.esprit.dam.data.model.ScanResult
import javax.inject.Inject


sealed class ScanUiState {
    object Initial : ScanUiState()
    data class Scanning(val totalApps: Int) : ScanUiState()
    data class Success(val scan: ScanItem, val isFromCache: Boolean) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
    object LoadingLastScan : ScanUiState()
}

data class ScanStatsData(
    val totalApps: Int = 0,
    val criticalApps: Int = 0,
    val highRiskApps: Int = 0,
    val mediumRiskApps: Int = 0,
    val lowRiskApps: Int = 0,
    val avgScore: Int = 0
) {
    companion object {
        fun fromScan(scan: ScanItem): ScanStatsData {
            val results = scan.report.results

            return ScanStatsData(
                totalApps = scan.totalApps,
                criticalApps = results.count {
                    it.riskLevel.equals("critical", ignoreCase = true)
                },
                highRiskApps = results.count {
                    it.riskLevel.equals("high", ignoreCase = true)
                },
                mediumRiskApps = results.count {
                    it.riskLevel.equals("medium", ignoreCase = true)
                },
                lowRiskApps = results.count {
                    it.riskLevel.equals("low", ignoreCase = true)
                },
                avgScore = if (results.isNotEmpty()) {
                    results.map { it.finalScore !!}.average().toInt()
                } else {
                    0
                }
            )
        }
    }
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Initial)
    val uiState: StateFlow<ScanUiState> = _uiState

    private val _stats = MutableStateFlow(ScanStatsData())
    val stats: StateFlow<ScanStatsData> = _stats

    fun startScan(userHash: String) {
        viewModelScope.launch {
            // ✅ Utiliser createScanFlow au lieu de createScan
            scanRepository.createScanFlow(userHash).collect { result ->
                // Map flow ScanResult<T> to UI states
                _uiState.value = when (result) {
                    is ScanResult.Loading<*> -> {
                        // Show spinner / scanning UI. totalApps is unknown at this point.
                        Log.d("ScanViewModel", "⏳ Scan in progress (loading)")
                        ScanUiState.Scanning(totalApps = 0)
                    }

                    is ScanResult.Success -> {
                        val scanItem = result.data as ScanItem
                        Log.d("ScanViewModel", "✅ Scan success: id=${scanItem._id}, finalScore=${scanItem.finalScore}")
                        _stats.value = ScanStatsData.fromScan(scanItem)
                        ScanUiState.Success(
                            scan = scanItem,
                            isFromCache = false
                        )
                    }

                    is ScanResult.Failure -> {
                        Log.e("ScanViewModel", "❌ Scan error: ${result.exception.message}")
                        ScanUiState.Error(
                            message = result.exception.message ?: "Erreur inconnue"
                        )
                    }
                }
            }
        }
    }

    fun loadLastScan(userHash: String) {
        viewModelScope.launch {
            when (val result = scanRepository.getLatestScan(userHash)) {
                is ScanResult.Success -> {
                    val data = result.data as? ScanItem?
                    if (data != null) {
                        _stats.value = ScanStatsData.fromScan(data)
                        _uiState.value = ScanUiState.Success(
                            scan = data,
                            isFromCache = true
                        )
                    } else {
                        _uiState.value = ScanUiState.Initial
                    }
                }

                is ScanResult.Failure -> {
                    _uiState.value = ScanUiState.Error(
                        message = result.exception.message ?: "Erreur"
                    )
                }

                is ScanResult.Loading<*> -> {}
            }
        }
    }
}

