package tn.esprit.dam.features.scan.presentation.home

import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import kotlinx.coroutines.flow.update
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.repository.ScanRepository

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _homeState.update { it.copy(loading = true, error = null) }

                val historyResult = repository.getScanHistory(limit = 1, offset = 0)

                val hasHistory = historyResult is ApiResult.Success && historyResult.data.scans.isNotEmpty()
                val lastHistoryDate = if (hasHistory) historyResult.data.scans.first().createdAt else null

                if (!hasHistory) {
                    _homeState.update { it.copy(
                        lastScanDate = null,
                        overallScore = 0f,
                        highRiskCount = 0,
                        mediumRiskCount = 0,
                        lowRiskCount = 0,
                        riskyApps = emptyList(),
                        recentScans = emptyList(),
                        loading = false,
                        error = null,
                        totalApps = 0
                    ) }
                    return@launch
                }

                when (val result = repository.getLatestScan()) {
                    is ApiResult.Success -> {
                        val response = result.data
                        val apps = response.results

                        if (apps.isEmpty()) {
                            _homeState.update { it.copy(
                                lastScanDate = formatTimestamp(lastHistoryDate ?: response.scanDate),
                                overallScore = 0f,
                                highRiskCount = 0,
                                mediumRiskCount = 0,
                                lowRiskCount = 0,
                                riskyApps = emptyList(),
                                recentScans = emptyList(),
                                loading = false,
                                error = null,
                                totalApps = 0
                            ) }
                        } else {
                            val riskScores = apps.map { it.aiRiskScore ?: it.finalScore ?: 0f }
                            val averageScore = if (riskScores.isNotEmpty()) riskScores.average().toFloat() else 0f

                            val sortedByRisk = apps.sortedBy { it.aiRiskScore ?: it.finalScore ?: 0f }

                            val highRiskCount = riskScores.count { it < 40 }
                            val mediumRiskCount = riskScores.count { it in 40.0..69.99 }
                            val lowRiskCount = riskScores.count { it >= 70 }

                            _homeState.update { it.copy(
                                lastScanDate = formatTimestamp(lastHistoryDate ?: response.scanDate),
                                overallScore = averageScore,
                                highRiskCount = highRiskCount,
                                mediumRiskCount = mediumRiskCount,
                                lowRiskCount = lowRiskCount,
                                riskyApps = sortedByRisk.map { it.toRiskyAppUi() },
                                recentScans = emptyList(),
                                loading = false,
                                error = null,
                                totalApps = apps.size
                            ) }
                        }
                    }
                    is ApiResult.ApiError -> {
                        _homeState.update { it.copy(
                            loading = false,
                            error = UiText.Raw(result.message),
                            riskyApps = emptyList()
                        ) }
                    }
                    is ApiResult.NetworkError -> {
                        _homeState.update { it.copy(
                            loading = false,
                            error = UiText.Res(R.string.error_network_check_connection),
                            riskyApps = emptyList()
                        ) }
                    }
                    is ApiResult.SerializationError -> {
                        _homeState.update { it.copy(
                            loading = false,
                            error = UiText.Res(R.string.error_data, result.exception.message.orEmpty()),
                            riskyApps = emptyList()
                        ) }
                    }
                }
            } catch (e: Exception) {
                _homeState.update { it.copy(
                    loading = false,
                    error = e.message?.let { UiText.Raw(it) } ?: UiText.Res(R.string.error_dashboard_load),
                    riskyApps = emptyList()
                ) }
            }
        }
    }

    fun refreshDashboard() {
        loadDashboard()
    }

    fun clearError() {
        _homeState.update { it.copy(error = null) }
    }

    fun scanApk(uri: Uri) {
        viewModelScope.launch {
            try {
                _homeState.update { it.copy(loading = true, error = null) }

                when (val result = repository.scanApk(uri, null)) {
                    is ApiResult.Success -> {
                        loadDashboard()
                    }
                    is ApiResult.ApiError -> {
                        _homeState.update { it.copy(
                            loading = false,
                            error = UiText.Res(R.string.error_with_message, result.message)
                        ) }
                    }
                    is ApiResult.NetworkError -> {
                        _homeState.update { it.copy(
                            loading = false,
                            error = UiText.Res(R.string.error_network_with_message, result.exception.message.orEmpty())
                        ) }
                    }
                    is ApiResult.SerializationError -> {
                        _homeState.update { it.copy(
                            loading = false,
                            error = UiText.Res(R.string.error_data, result.exception.message.orEmpty())
                        ) }
                    }
                }
            } catch (e: Exception) {
                _homeState.update { it.copy(
                    loading = false,
                    error = e.message?.let { UiText.Raw(it) } ?: UiText.Res(R.string.error_apk_scan)
                ) }
            }
        }
    }

    @Suppress("NewApi")
    private fun formatTimestamp(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return try {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'à' HH:mm", Locale.FRENCH)
            formatter.format(Instant.parse(raw).atZone(ZoneId.systemDefault()))
        } catch (e: Exception) {
            raw
        }
    }
}
