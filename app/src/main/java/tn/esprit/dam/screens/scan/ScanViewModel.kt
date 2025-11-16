package tn.esprit.dam.screens.scan


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.repository.ScanRepository

class ScanViewModel : ViewModel() {

    companion object {
        private const val TAG = "ScanViewModel"
    }

    private lateinit var repository: ScanRepository

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Initial)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _stats = MutableStateFlow(ScanStatsData())
    val stats: StateFlow<ScanStatsData> = _stats.asStateFlow()

    /**
     * Initialiser le repository
     */
    fun initialize(context: Context) {
        if (!::repository.isInitialized) {
            repository = ScanRepository(context)
            Log.d(TAG, "✅ Repository initialized")
        }
    }

    /**
     * Charger le dernier scan
     */
    fun loadLastScan(context: Context, userHash: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ScanUiState.LoadingLastScan

                val result = repository.getLatestScan(userHash)

                result.onSuccess { scan ->
                    if (scan != null) {
                        _uiState.value = ScanUiState.Success(
                            scan = scan,
                            isFromCache = true
                        )
                        _stats.value = ScanStatsData.fromScan(scan)
                        Log.d(TAG, "✅ Loaded last scan: ${scan.totalApps} apps")
                    } else {
                        _uiState.value = ScanUiState.Initial
                        Log.d(TAG, "ℹ️ No previous scan found")
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
     * Démarrer un nouveau scan
     */
    fun startScan(context: Context, userHash: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔍 Starting scan...")

                // Étape 1: Récupérer les apps installées
                _uiState.value = ScanUiState.Scanning(
                    totalApps = 0,
                    progress = "Collecte des applications..."
                )

                val apps = repository.getInstalledApps()

                if (apps.isEmpty()) {
                    _uiState.value = ScanUiState.Error("Aucune application trouvée")
                    return@launch
                }

                // Étape 2: Analyse en cours
                _uiState.value = ScanUiState.Scanning(
                    totalApps = apps.size,
                    progress = "Analyse de ${apps.size} applications..."
                )

                // Étape 3: Créer le scan (analyse + sauvegarde)
                val result = repository.createScan(userHash, apps)

                result.onSuccess { scan ->
                    _uiState.value = ScanUiState.Success(
                        scan = scan,
                        isFromCache = false
                    )
                    _stats.value = ScanStatsData.fromScan(scan)
                    Log.d(TAG, "✅ Scan completed: ${scan.totalApps} apps")
                }.onFailure { error ->
                    val errorMessage = when {
                        error.message?.contains("timeout", true) == true ->
                            "Timeout - Le serveur met trop de temps à répondre (>3 min)"
                        error.message?.contains("connect", true) == true ->
                            "Impossible de se connecter au serveur\n\n• Vérifiez que le backend est démarré\n• Même réseau WiFi"
                        error.message?.contains("No access token") == true ->
                            "Session expirée. Veuillez vous reconnecter"
                        else ->
                            "Erreur: ${error.message}"
                    }
                    _uiState.value = ScanUiState.Error(errorMessage)
                    Log.e(TAG, "❌ Scan failed: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error: ${e.message}", e)
                _uiState.value = ScanUiState.Error("Erreur inattendue: ${e.message}")
            }
        }
    }

    /**
     * Effacer l'erreur et revenir à l'état précédent
     */
    fun clearError() {
        if (_uiState.value is ScanUiState.Error) {
            _uiState.value = ScanUiState.Initial
        }
    }

    /**
     * Réinitialiser l'état
     */
    fun reset() {
        _uiState.value = ScanUiState.Initial
        _stats.value = ScanStatsData()
    }
}