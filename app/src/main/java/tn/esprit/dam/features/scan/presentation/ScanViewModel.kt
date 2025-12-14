package tn.esprit.dam.features.scan.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.api.models.AppDetailsResponse
import tn.esprit.dam.data.local.AppScanner
import tn.esprit.dam.data.repository.ScanRepository
import tn.esprit.dam.features.scan.data.LocalAppInfo
import tn.esprit.dam.features.scan.data.ScanState
import tn.esprit.dam.features.scan.data.toUiModel

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository,
    private val appScanner: AppScanner
) : ViewModel() {

    private val _scanState = MutableStateFlow(ScanState())
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _availableApps = MutableStateFlow<List<LocalAppInfo>>(emptyList())
    val availableApps: StateFlow<List<LocalAppInfo>> = _availableApps.asStateFlow()

    private val _selectedAppDetails = MutableStateFlow<AppDetailsUIState?>(null)
    val selectedAppDetails: StateFlow<AppDetailsUIState?> = _selectedAppDetails.asStateFlow()

    private var userId: String? = null
    private var deviceId: String? = null
    
    // Polling job for scan status
    private var pollingJob: Job? = null
    private val TAG = "ScanViewModel"

    fun initialize(userId: String, deviceId: String) {
        this.userId = userId
        this.deviceId = deviceId
        loadAvailableApps()
    }

    private fun loadAvailableApps() {
        viewModelScope.launch {
            _scanState.value = _scanState.value.copy(status = "LOADING")
            
            // Load locally installed apps from device
            val localApps = appScanner.getInstalledApps()
            
            // Filter system apps based on toggle state
            val filteredApps = if (_scanState.value.showSystemApps) {
                localApps
            } else {
                localApps.filter { app ->
                    !app.isSystemApp && !isSystemPackage(app.packageName)
                }
            }
            
            _availableApps.value = filteredApps
            _scanState.value = _scanState.value.copy(
                status = "IDLE",
                totalApps = filteredApps.size
            )
        }
    }

    /**
     * Check if package is a system app by package name prefix
     * Filters out Google, Android, MIUI, Samsung, and other OEM system apps
     */
    private fun isSystemPackage(packageName: String): Boolean {
        val systemPrefixes = listOf(
            "android",
            "com.android.",
            "com.google.android.",
            "com.samsung.android.",
            "com.sec.android.",
            "com.huawei.",
            "com.miui.",
            "com.xiaomi.",
            "com.oneplus.",
            "com.oppo.",
            "com.vivo.",
            "com.coloros.",
            "com.bbk.",
            "com.qualcomm.",
            "com.mediatek."
        )
        return systemPrefixes.any { packageName.startsWith(it) }
    }

    // DEPRECATED: Keeping for backward compatibility, use isSystemPackage instead
    private fun isSystemApp(packageName: String): Boolean {
        return isSystemPackage(packageName)
    }

    fun toggleAppSelection(packageName: String) {
        val updatedApps = _availableApps.value.map { app ->
            if (app.packageName == packageName) app.copy(isSelected = !app.isSelected)
            else app
        }
        _availableApps.value = updatedApps
        _scanState.value = _scanState.value.copy(
            selectedApps = updatedApps.filter { it.isSelected }
        )
    }

    fun selectAllApps() {
        val updatedApps = _availableApps.value.map { it.copy(isSelected = true) }
        _availableApps.value = updatedApps
        _scanState.value = _scanState.value.copy(
            selectedApps = updatedApps
        )
    }

    fun deselectAllApps() {
        val updatedApps = _availableApps.value.map { it.copy(isSelected = false) }
        _availableApps.value = updatedApps
        _scanState.value = _scanState.value.copy(
            selectedApps = emptyList()
        )
    }

    fun toggleSystemApps() {
        _scanState.value = _scanState.value.copy(
            showSystemApps = !_scanState.value.showSystemApps
        )
        loadAvailableApps()
    }

    fun startScan() {
        if (_scanState.value.selectedApps.isEmpty()) {
            _scanState.value = _scanState.value.copy(
                error = "Sélectionnez au moins une application à analyser"
            )
            return
        }

        if (userId == null || deviceId == null) {
            _scanState.value = _scanState.value.copy(
                error = "ID utilisateur ou appareil non initialisé"
            )
            return
        }

        viewModelScope.launch {
            try {
                val currentSelectedApps = _scanState.value.selectedApps  // ✅ PRESERVE selectedApps
                
                Log.d(TAG, "🚀 startScan called with ${currentSelectedApps.size} apps")
                Log.d(TAG, "   userId: $userId")
                Log.d(TAG, "   deviceId: $deviceId")
                
                _scanState.value = _scanState.value.copy(
                    status = "ANALYZING",
                    error = null,
                    analysisNote = null,
                    selectedApps = currentSelectedApps  // ✅ KEEP selectedApps in state
                )

                val selectedPackages = currentSelectedApps.map { it.packageName }
                Log.d(TAG, "📦 Selected packages: ${selectedPackages.take(3).joinToString()}") 

                // Start scan on server
                when (val result = repository.startScan(selectedPackages, userId!!, deviceId!!, false)) {
                    is ApiResult.Success -> {
                        val scanId = result.data.scanId
                        Log.d(TAG, "🚀 Scan started with ID: $scanId")
                        
                        _scanState.value = _scanState.value.copy(
                            status = "ANALYZING",
                            scanId = scanId,
                            selectedApps = currentSelectedApps,  // ✅ PRESERVE
                            scannedApps = 0,
                            highRiskCount = 0,
                            mediumRiskCount = 0,
                            lowRiskCount = 0,
                            averageScore = 0f,
                            analysisNote = null
                        )
                        
                        // Start polling for scan status
                        startPolling(scanId)
                    }
                    is ApiResult.ApiError -> {
                        Log.e(TAG, "❌ API Error: ${result.message} (code: ${result.code})")
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = result.message,
                            selectedApps = currentSelectedApps  // ✅ PRESERVE
                        )
                    }
                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "❌ Network Error: ${result.exception.message}", result.exception)
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = "Erreur réseau: ${result.exception.message ?: "vérifiez votre connexion"}",
                            selectedApps = currentSelectedApps  // ✅ PRESERVE
                        )
                    }
                    is ApiResult.SerializationError -> {
                        Log.e(TAG, "❌ Serialization Error: ${result.exception.message}", result.exception)
                        Log.e(TAG, "   Raw response: ${result.rawResponse?.take(500)}")
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = "Erreur de données: ${result.exception.message}",
                            selectedApps = currentSelectedApps  // ✅ PRESERVE
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error in startScan: ${e.message}", e)
                val currentSelectedApps = _scanState.value.selectedApps
                val errorMsg = "${e.javaClass.simpleName}: ${e.message ?: "Erreur inconnue"}"
                _scanState.value = _scanState.value.copy(
                    status = "FAILED",
                    error = errorMsg,
                    selectedApps = currentSelectedApps  // ✅ PRESERVE
                )
            }
        }
    }

    fun scanApk(uri: Uri) {
        if (userId == null) {
            _scanState.value = _scanState.value.copy(error = "Utilisateur non connecté")
            return
        }

        viewModelScope.launch {
            try {
                _scanState.value = _scanState.value.copy(
                    status = "ANALYZING",
                    error = null,
                    analysisNote = "Téléversement et analyse APK en cours...",
                    selectedApps = emptyList(),
                    totalApps = 1,
                    scannedApps = 0
                )

                when (val result = repository.scanApk(uri, userId!!, deviceId)) {
                    is ApiResult.Success -> {
                        val app = result.data.app
                        val localApp = app.toUiModel(isSelected = false)
                        val score = app.finalScore ?: app.scanResults?.aiRiskScore?.toFloat() ?: 0f
                        val level = app.scanResults?.aiRiskLevel ?: "low"
                        val note = if (app.scanResults?.aiStatus == "ok") {
                            "Analyse basée sur MobSF + IA"
                        } else {
                            "Analyse basée sur MobSF (IA indisponible)"
                        }

                        _scanState.value = _scanState.value.copy(
                            status = "COMPLETED",
                            selectedApps = listOf(localApp),
                            totalApps = 1,
                            scannedApps = 1,
                            highRiskCount = if (level == "high") 1 else 0,
                            mediumRiskCount = if (level == "medium") 1 else 0,
                            lowRiskCount = if (level == "low") 1 else 0,
                            averageScore = score,
                            analysisNote = note
                        )
                    }
                    is ApiResult.ApiError -> {
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = result.message,
                            analysisNote = null
                        )
                    }
                    is ApiResult.NetworkError -> {
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = "Erreur réseau: ${result.exception.message}",
                            analysisNote = null
                        )
                    }
                    is ApiResult.SerializationError -> {
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = "Erreur de données: ${result.exception.message}",
                            analysisNote = null
                        )
                    }
                }
            } catch (e: Exception) {
                _scanState.value = _scanState.value.copy(
                    status = "FAILED",
                    error = e.message ?: "Erreur inconnue",
                    analysisNote = null
                )
            }
        }
    }
    
    /**
     * Poll scan status every 3 seconds until completed or failed
     */
    private fun startPolling(scanId: String) {
        // Cancel any existing polling job
        pollingJob?.cancel()
        
        pollingJob = viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 100 // Max 5 minutes (100 * 3s)
            
            while (attempts < maxAttempts) {
                delay(3000) // Poll every 3 seconds
                attempts++
                
                Log.d(TAG, "📊 Polling scan status #$attempts for $scanId")
                
                when (val result = repository.getScanStatus(scanId)) {
                    is ApiResult.Success -> {
                        val status = result.data
                        val currentSelectedApps = _scanState.value.selectedApps  // ✅ PRESERVE
                        Log.d(TAG, "📊 Status: ${status.status}, Progress: ${status.progress}%, Scanned: ${status.scannedApps}/${status.totalApps}")
                        
                        _scanState.value = _scanState.value.copy(
                            scannedApps = status.scannedApps ?: 0,
                            totalApps = status.totalApps ?: _scanState.value.totalApps,
                            selectedApps = currentSelectedApps,  // ✅ PRESERVE
                            highRiskCount = status.results?.highRiskApps ?: 0,
                            mediumRiskCount = status.results?.mediumRiskApps ?: 0,
                            lowRiskCount = status.results?.lowRiskApps ?: 0,
                            averageScore = status.results?.averageScore ?: 0f
                        )
                        
                        when (status.status) {
                            "completed" -> {
                                Log.d(TAG, "✅ Scan completed!")
                                val latestResult = repository.getLatestScan(userId!!)

                                if (latestResult is ApiResult.Success) {
                                    val apps = latestResult.data.apps
                                    val riskScores = apps.map { it.finalScore }
                                    val avgScore = if (riskScores.isNotEmpty()) riskScores.average().toFloat() else status.results?.averageScore ?: 0f
                                    val high = apps.count { it.finalScore >= 85 }
                                    val med = apps.count { it.finalScore in 70.0..84.99 }
                                    val low = apps.size - high - med

                                    _scanState.value = _scanState.value.copy(
                                        status = "COMPLETED",
                                        selectedApps = apps.map {
                                            LocalAppInfo(
                                                packageName = it.packageName,
                                                displayName = it.appName,
                                                permissions = emptyList(),
                                                trackers = emptyList(),
                                                isSelected = false
                                            )
                                        },
                                        totalApps = apps.size,
                                        scannedApps = apps.size,
                                        averageScore = avgScore,
                                        highRiskCount = high,
                                        mediumRiskCount = med,
                                        lowRiskCount = low
                                    )
                                } else {
                                    _scanState.value = _scanState.value.copy(
                                        status = "COMPLETED",
                                        selectedApps = currentSelectedApps,  // ✅ PRESERVE
                                        averageScore = status.results?.averageScore ?: 0f,
                                        highRiskCount = status.results?.highRiskApps ?: 0,
                                        mediumRiskCount = status.results?.mediumRiskApps ?: 0,
                                        lowRiskCount = status.results?.lowRiskApps ?: 0
                                    )
                                }
                                return@launch // Stop polling
                            }
                            "failed" -> {
                                Log.e(TAG, "❌ Scan failed!")
                                _scanState.value = _scanState.value.copy(
                                    status = "FAILED",
                                    error = "L'analyse a échoué"
                                )
                                return@launch // Stop polling
                            }
                            else -> {
                                // Still analyzing - continue polling
                            }
                        }
                    }
                    is ApiResult.ApiError -> {
                        Log.e(TAG, "❌ Polling error: ${result.message}")
                        // Continue polling despite error
                    }
                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "❌ Network error during polling")
                        // Continue polling despite error
                    }
                    is ApiResult.SerializationError -> {
                        Log.e(TAG, "❌ Serialization error during polling")
                        // Continue polling despite error
                    }
                }
            }
            
            // Max attempts reached
            Log.w(TAG, "⚠️ Max polling attempts reached")
            _scanState.value = _scanState.value.copy(
                status = "FAILED",
                error = "Timeout: l'analyse prend trop de temps"
            )
        }
    }
    
    /**
     * Stop polling (called when ViewModel is cleared or scan is reset)
     */
    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    fun getAppDetails(packageName: String) {
        viewModelScope.launch {
            try {
                _selectedAppDetails.value = AppDetailsUIState.Loading

                when (val result = repository.getAppDetails(packageName)) {
                    is ApiResult.Success -> {
                        _selectedAppDetails.value = AppDetailsUIState.Success(result.data)
                    }
                    is ApiResult.ApiError -> {
                        _selectedAppDetails.value = AppDetailsUIState.Error(result.message)
                    }
                    is ApiResult.NetworkError -> {
                        _selectedAppDetails.value = AppDetailsUIState.Error(
                            "Erreur réseau: vérifiez votre connexion"
                        )
                    }
                    is ApiResult.SerializationError -> {
                        _selectedAppDetails.value = AppDetailsUIState.Error(
                            "Erreur de données: ${result.exception.message ?: "format invalide"}"
                        )
                    }
                }
            } catch (e: Exception) {
                _selectedAppDetails.value = AppDetailsUIState.Error(
                    e.message ?: "Erreur lors du chargement des détails"
                )
            }
        }
    }

    fun clearError() {
        _scanState.value = _scanState.value.copy(error = null)
    }

    fun resetScan() {
        stopPolling()
        _scanState.value = ScanState(totalApps = _availableApps.value.size)
        _availableApps.value = _availableApps.value.map { it.copy(isSelected = false) }
    }
}

// UI State for app details
sealed class AppDetailsUIState {
    object Loading : AppDetailsUIState()
    data class Success(val data: AppDetailsResponse) : AppDetailsUIState()
    data class Error(val message: String) : AppDetailsUIState()
}
