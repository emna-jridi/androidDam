package tn.esprit.dam.features.scan.presentation

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
            val userApps = localApps.filter { !isSystemApp(it.packageName) }
            _availableApps.value = userApps
            _scanState.value = _scanState.value.copy(
                status = "IDLE",
                totalApps = userApps.size
            )
        }
    }

    // System apps to filter out
    private fun isSystemApp(packageName: String): Boolean {
        val systemPackages = setOf(
            "android",
            "com.android",
            "com.google.android",
            "com.sec.android",
            "com.samsung.android",
            "com.huawei",
            "com.miui",
            "com.oneplus",
            "com.oppo",
            "com.vivo",
            "com.xiaomi"
        )
        return systemPackages.any { packageName.startsWith(it) }
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
                _scanState.value = _scanState.value.copy(
                    status = "ANALYZING",
                    error = null
                )

                val selectedPackages = _scanState.value.selectedApps.map { it.packageName }

                // Start scan on server
                when (val result = repository.startScan(selectedPackages, userId!!, deviceId!!, false)) {
                    is ApiResult.Success -> {
                        val scanId = result.data.scanId
                        Log.d(TAG, "🚀 Scan started with ID: $scanId")
                        
                        _scanState.value = _scanState.value.copy(
                            status = "ANALYZING",
                            scanId = scanId,
                            scannedApps = 0,
                            highRiskCount = 0,
                            mediumRiskCount = 0,
                            lowRiskCount = 0,
                            averageScore = 0f
                        )
                        
                        // Start polling for scan status
                        startPolling(scanId)
                    }
                    is ApiResult.ApiError -> {
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = result.message
                        )
                    }
                    is ApiResult.NetworkError -> {
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = "Erreur réseau: vérifiez votre connexion"
                        )
                    }
                    is ApiResult.SerializationError -> {
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = "Erreur de données"
                        )
                    }
                }
            } catch (e: Exception) {
                _scanState.value = _scanState.value.copy(
                    status = "FAILED",
                    error = e.message ?: "Erreur inconnue"
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
                        Log.d(TAG, "📊 Status: ${status.status}, Progress: ${status.progress}%, Scanned: ${status.scannedApps}/${status.totalApps}")
                        
                        _scanState.value = _scanState.value.copy(
                            scannedApps = status.scannedApps ?: 0,
                            totalApps = status.totalApps ?: _scanState.value.totalApps,
                            highRiskCount = status.results?.highRiskApps ?: 0,
                            mediumRiskCount = status.results?.mediumRiskApps ?: 0,
                            lowRiskCount = status.results?.lowRiskApps ?: 0,
                            averageScore = status.results?.averageScore ?: 0f
                        )
                        
                        when (status.status) {
                            "completed" -> {
                                Log.d(TAG, "✅ Scan completed!")
                                _scanState.value = _scanState.value.copy(
                                    status = "COMPLETED",
                                    averageScore = status.results?.averageScore ?: 0f,
                                    highRiskCount = status.results?.highRiskApps ?: 0,
                                    mediumRiskCount = status.results?.mediumRiskApps ?: 0,
                                    lowRiskCount = status.results?.lowRiskApps ?: 0
                                )
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
