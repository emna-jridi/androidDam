package tn.esprit.dam.features.scan.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
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
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.repository.ScanRepository

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ScanRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()
    
    private val TAG = "HomeViewModel"

    fun loadDashboard() {
        viewModelScope.launch {
            try {
                _homeState.value = _homeState.value.copy(loading = true, error = null)

                // Get userId from TokenManager
                val user = TokenManager.getUser(context)
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

                // If no history, hide score card
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

                // Latest scan details for score + risk breakdown
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
                            // Calculate risk scores handling nullable fields
                            val riskScores = apps.map { it.aiRiskScore ?: it.finalScore ?: 0f }
                            val averageScore = if (riskScores.isNotEmpty()) riskScores.average().toFloat() else 0f
                            
                            val sortedByRisk = apps.sortedBy { it.aiRiskScore ?: it.finalScore ?: 0f }
                            
                            // ScanViewModel logic: score >= 85 is LOW RISK (Safe)
                            // We are counting "High Risk" apps (Low Score)
                            val highRiskCount = riskScores.count { it < 40 } // Critical/High
                            val mediumRiskCount = riskScores.count { it in 40.0..69.99 }
                            val lowRiskCount = riskScores.count { it >= 70 } // Safe
                            // I adjusted logic to match standard MobSF (0-100 where 100 is safe)
                            // 0-39 High Risk
                            // 40-69 Medium Risk
                            // 70-100 Low Risk

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

                val user = TokenManager.getUser(context)
                val userId = user?.id

                if (userId == null) {
                    _homeState.value = _homeState.value.copy(
                        loading = false,
                        error = "Utilisateur non connecté"
                    )
                    return@launch
                }

                Log.d(TAG, "Starting APK scan for user: $userId")

                when (val result = repository.scanApk(uri, userId, null)) {
                    is ApiResult.Success -> {
                        Log.d(TAG, "APK scan successful")
                        // Reload dashboard to show new results
                        loadDashboard()
                    }
                    is ApiResult.ApiError -> {
                        Log.e(TAG, "APK scan API error: ${result.message}")
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = "Erreur: ${result.message}"
                        )
                    }
                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "APK scan network error", result.exception)
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = "Erreur réseau: ${result.exception.message}"
                        )
                    }
                    is ApiResult.SerializationError -> {
                        Log.e(TAG, "APK scan serialization error", result.exception)
                        _homeState.value = _homeState.value.copy(
                            loading = false,
                            error = "Erreur de données: ${result.exception.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "APK scan unexpected error", e)
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
