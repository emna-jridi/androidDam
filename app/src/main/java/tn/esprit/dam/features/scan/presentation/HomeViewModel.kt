package tn.esprit.dam.features.scan.presentation

import android.content.Context
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

                // Use getLatestScan - the ONLY endpoint for Home screen
                when (val result = repository.getLatestScan(userId)) {
                    is ApiResult.Success -> {
                        val response = result.data
                        val apps = response.apps
                        
                        // If apps is empty, this is NOT an error - it's first-time user state
                        if (apps.isEmpty()) {
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
                        } else {
                            val averageScore = apps.map { it.finalScore }.average().toFloat()
                            val sortedByRisk = apps.sortedBy { it.finalScore }
                            val highRiskCount = apps.count { it.finalScore < 40f }
                            val mediumRiskCount = apps.count { it.finalScore in 40f..69f }
                            val lowRiskCount = apps.count { it.finalScore >= 70f }

                            _homeState.value = _homeState.value.copy(
                                lastScanDate = formatTimestamp(response.createdAt),
                                overallScore = response.globalScore.toFloat(),
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
