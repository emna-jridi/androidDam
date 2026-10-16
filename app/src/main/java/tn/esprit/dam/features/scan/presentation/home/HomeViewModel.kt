package tn.esprit.dam.features.scan.presentation.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.repository.ScanRepository

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ScanRepository,
    private val tokenRepository: TokenRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _homeState.value = _homeState.value.copy(loading = true, error = null)

                val user = tokenRepository.getUser()
                val userId = user?.id

                if (userId == null) {
                    _homeState.value = _homeState.value.copy(
                        loading = false,
                        error = "Utilisateur non connecté",
                        riskyApps = emptyList()
                    )
                    return@launch
                }

                val historyResult = repository.getScanHistory(userId, limit = 1, offset = 0)

                val hasHistory = historyResult is ApiResult.Success && historyResult.data.scans.isNotEmpty()
                val lastHistoryDate = if (hasHistory) historyResult.data.scans.first().createdAt else null

                if (!hasHistory) {
                    _homeState.value = _homeState.value.copy(
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
                    )
                    return@launch
                }

                when (val result = repository.getLatestScan(userId)) {
                    is ApiResult.Success -> {
                        val response = result.data
                        val apps = response.apps

                        if (apps.isEmpty()) {
                            _homeState.value = _homeState.value.copy(
                                lastScanDate = formatTimestamp(lastHistoryDate ?: response.createdAt),
                                overallScore = 0f,
                                highRiskCount = 0,
                                mediumRiskCount = 0,
                                lowRiskCount = 0,
                                riskyApps = emptyList(),
                                recentScans = emptyList(),
                                loading = false,
                                error = null,
                                totalApps = 0
                            )
                        } else {
                            val riskScores = apps.map { it.aiRiskScore ?: it.finalScore ?: 0f }
                            val averageScore = if (riskScores.isNotEmpty()) riskScores.average().toFloat() else 0f

                            val sortedByRisk = apps.sortedBy { it.aiRiskScore ?: it.finalScore ?: 0f }

                            val highRiskCount = riskScores.count { it < 40 }
                            val mediumRiskCount = riskScores.count { it in 40.0..69.99 }
                            val lowRiskCount = riskScores.count { it >= 70 }

                            _homeState.value = _homeState.value.copy(
                                lastScanDate = formatTimestamp(lastHistoryDate ?: response.createdAt),
                                overallScore = averageScore,
                                highRiskCount = highRiskCount,
                                mediumRiskCount = mediumRiskCount,
                                lowRiskCount = lowRiskCount,
                                riskyApps = sortedByRisk,
                                recentScans = emptyList(),
                                loading = false,
                                error = null,
                                totalApps = apps.size
                            )
                        }
                    }
                    is ApiResult.ApiError -> {
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = result.message,
                            riskyApps = emptyList()
                        )
                    }
                    is ApiResult.NetworkError -> {
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = "Erreur réseau: vérifiez votre connexion",
                            riskyApps = emptyList()
                        )
                    }
                    is ApiResult.SerializationError -> {
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = "Erreur de données: ${result.exception.message}",
                            riskyApps = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    loading = false,
                    error = e.message ?: "Impossible de charger le tableau de bord",
                    riskyApps = emptyList()
                )
            }
        }
    }

    fun refreshDashboard() {
        loadDashboard()
    }

    fun clearError() {
        _homeState.value = _homeState.value.copy(error = null)
    }

    fun scanApk(uri: Uri) {
        viewModelScope.launch {
            try {
                _homeState.value = _homeState.value.copy(loading = true, error = null)

                val user = tokenRepository.getUser()
                val userId = user?.id

                if (userId == null) {
                    _homeState.value = _homeState.value.copy(
                        loading = false,
                        error = "Utilisateur non connecté"
                    )
                    return@launch
                }

                when (val result = repository.scanApk(uri, userId, null)) {
                    is ApiResult.Success -> {
                        loadDashboard()
                    }
                    is ApiResult.ApiError -> {
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = "Erreur: ${result.message}"
                        )
                    }
                    is ApiResult.NetworkError -> {
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = "Erreur réseau: ${result.exception.message}"
                        )
                    }
                    is ApiResult.SerializationError -> {
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = "Erreur de données: ${result.exception.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    loading = false,
                    error = e.message ?: "Erreur lors de l'analyse APK"
                )
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
