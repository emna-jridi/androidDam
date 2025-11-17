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
import tn.esprit.dam.data.model.ScanItem  // ✅ CHANGÉ: SavedScan -> ScanItem
import tn.esprit.dam.data.model.AppAnalysisResult
import javax.inject.Inject

// ============================================================
// UI STATES
// ============================================================

sealed class ScanUiState {
    object Initial : ScanUiState()
    object LoadingLastScan : ScanUiState()

    data class Scanning(
        val progress: String = "Analyse en cours...",
        val totalApps: Int = 0
    ) : ScanUiState()

    data class Success(
        val scan: ScanItem,  // ✅ CHANGÉ: SavedScan -> ScanItem
        val isFromCache: Boolean = false
    ) : ScanUiState()

    data class Error(val message: String) : ScanUiState()
}

// ============================================================
// STATS DATA
// ============================================================

data class ScanStatsData(
    val totalApps: Int = 0,
    val criticalApps: Int = 0,
    val highRiskApps: Int = 0,
    val mediumRiskApps: Int = 0,
    val lowRiskApps: Int = 0,
    val avgScore: Int = 0
) {
    companion object {
        fun fromScan(scan: ScanItem): ScanStatsData {  // ✅ CHANGÉ: SavedScan -> ScanItem
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
                    results.map { it.score }.average().toInt()
                } else {
                    0
                }
            )
        }
    }
}

// ============================================================
// VIEWMODEL
// ============================================================

@HiltViewModel
class ScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ScanRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ScanViewModel"
    }

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Initial)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _stats = MutableStateFlow(ScanStatsData())
    val stats: StateFlow<ScanStatsData> = _stats.asStateFlow()

    /**
     * ✅ Charger le dernier scan
     */
    fun loadLastScan(userHash: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📌 Loading last scan for: $userHash")
                _uiState.value = ScanUiState.LoadingLastScan

                val result = repository.getLatestScan(userHash)

                result.onSuccess { scan: ScanItem? ->
                    if (scan != null) {
                        Log.d(TAG, "✅ Loaded last scan: ${scan.totalApps} apps")

                        _uiState.value = ScanUiState.Success(
                            scan = scan,  // ✅ Type correct: ScanItem
                            isFromCache = true
                        )

                        _stats.value = ScanStatsData.fromScan(scan)  // ✅ Type correct: ScanItem
                    } else {
                        Log.d(TAG, "ℹ️ No previous scan found")
                        _uiState.value = ScanUiState.Initial
                    }
                }.onFailure { error ->
                    Log.e(TAG, "❌ Load last scan failed: ${error.message}")
                    _uiState.value = ScanUiState.Initial
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error: ${e.message}", e)
                _uiState.value = ScanUiState.Initial
            }
        }
    }

    /**
     * ✅ Démarrer un nouveau scan
     */
    fun startScan(userHash: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔍 Starting scan...")

                _uiState.value = ScanUiState.Scanning(
                    totalApps = 0,
                    progress = "Collecte des applications..."
                )

                // Récupérer les applications installées
                val apps = repository.getInstalledApps()

                if (apps.isEmpty()) {
                    _uiState.value = ScanUiState.Error("Aucune application trouvée")
                    return@launch
                }

                Log.d(TAG, "📱 Found ${apps.size} apps")

                _uiState.value = ScanUiState.Scanning(
                    totalApps = apps.size,
                    progress = "Analyse de ${apps.size} applications..."
                )

                // Créer le scan
                val result = repository.createScan(userHash, apps)

                result.onSuccess { scan: ScanItem ->  // ✅ Type explicite: ScanItem
                    Log.d(TAG, "✅ Scan completed: ${scan.totalApps} apps")

                    _uiState.value = ScanUiState.Success(
                        scan = scan,  // ✅ Type correct: ScanItem
                        isFromCache = false
                    )

                    _stats.value = ScanStatsData.fromScan(scan)  // ✅ Type correct: ScanItem

                }.onFailure { error ->
                    Log.e(TAG, "❌ Scan failed: ${error.message}")

                    val errorMessage = when {
                        error.message?.contains("timeout", true) == true ->
                            "Timeout - Le serveur met trop de temps à répondre (>3 min)"

                        error.message?.contains("connect", true) == true ->
                            "Impossible de se connecter au serveur\n\n• Vérifiez que le backend est démarré\n• Vérifiez que vous êtes sur le même réseau WiFi"

                        error.message?.contains("No access token") == true ->
                            "Session expirée. Veuillez vous reconnecter"

                        else ->
                            "Erreur: ${error.message}"
                    }

                    _uiState.value = ScanUiState.Error(errorMessage)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error: ${e.message}", e)
                _uiState.value = ScanUiState.Error("Erreur inattendue: ${e.message}")
            }
        }
    }

    /**
     * ✅ Effacer l'erreur
     */
    fun clearError() {
        if (_uiState.value is ScanUiState.Error) {
            _uiState.value = ScanUiState.Initial
        }
    }

    /**
     * ✅ Réinitialiser l'état
     */
    fun reset() {
        _uiState.value = ScanUiState.Initial
        _stats.value = ScanStatsData()
    }

    /**
     * ✅ Obtenir les apps par niveau de risque
     */
    fun getAppsByRiskLevel(riskLevel: String): List<AppAnalysisResult> {
        val currentState = _uiState.value
        return if (currentState is ScanUiState.Success) {
            currentState.scan.report.results.filter {
                it.riskLevel.equals(riskLevel, ignoreCase = true)
            }
        } else {
            emptyList()
        }
    }

    /**
     * ✅ Obtenir toutes les apps analysées
     */
    fun getAllApps(): List<AppAnalysisResult> {
        val currentState = _uiState.value
        return if (currentState is ScanUiState.Success) {
            currentState.scan.report.results
        } else {
            emptyList()
        }
    }

    /**
     * ✅ Obtenir une app par son package name
     */
    fun getAppByPackage(packageName: String): AppAnalysisResult? {
        val currentState = _uiState.value
        return if (currentState is ScanUiState.Success) {
            currentState.scan.report.results.find {
                it.packageName == packageName
            }
        } else {
            null
        }
    }

    /**
     * ✅ Vérifier si un scan est en cache
     */
    fun isFromCache(): Boolean {
        val currentState = _uiState.value
        return currentState is ScanUiState.Success && currentState.isFromCache
    }

    /**
     * ✅ Obtenir le scan actuel
     */
    fun getCurrentScan(): ScanItem? {
        val currentState = _uiState.value
        return if (currentState is ScanUiState.Success) {
            currentState.scan
        } else {
            null
        }
    }
}