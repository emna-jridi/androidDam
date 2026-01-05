package tn.esprit.dam.features.scan.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import tn.esprit.dam.R
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.api.models.AppDetailsResponse
import tn.esprit.dam.data.api.models.ScanLevel
import tn.esprit.dam.data.local.AppScanner
import tn.esprit.dam.data.repository.ScanRepository
import tn.esprit.dam.features.scan.data.LocalAppInfo
import tn.esprit.dam.features.scan.data.ScanState
import tn.esprit.dam.features.scan.data.toUiModel
import tn.esprit.dam.features.scan.domain.RiskLevel

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository,
    private val appScanner: AppScanner,
    @ApplicationContext private val context: Context
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
        val updatedApps = _availableApps.value.toList().map { it.copy(isSelected = true) }
        _availableApps.value = updatedApps
        _scanState.value = _scanState.value.copy(
            selectedApps = updatedApps,
            error = null
        )
    }

    fun deselectAllApps() {
        val updatedApps = _availableApps.value.toList().map { it.copy(isSelected = false) }
        _availableApps.value = updatedApps
        _scanState.value = _scanState.value.copy(
            selectedApps = emptyList(),
            error = null
        )
    }

    fun toggleSystemApps() {
        _scanState.value = _scanState.value.copy(
            showSystemApps = !_scanState.value.showSystemApps
        )
        loadAvailableApps()
    }

    fun setScanLevel(level: ScanLevel) {
        _scanState.value = _scanState.value.copy(scanLevel = level)
    }

    fun startScan() {
        if (_scanState.value.selectedApps.isEmpty()) {
            _scanState.value = _scanState.value.copy(
                error = context.getString(R.string.error_select_app)
            )
            return
        }

        if (userId == null || deviceId == null) {
            _scanState.value = _scanState.value.copy(
                error = context.getString(R.string.error_user_not_initialized)
            )
            return
        }

        viewModelScope.launch {
            try {
                val currentSelectedApps = _scanState.value.selectedApps
                
                Log.d(TAG, "🚀 startScan called with ${currentSelectedApps.size} apps")
                Log.d(TAG, "   userId: $userId")
                Log.d(TAG, "   deviceId: $deviceId")
                
                _scanState.value = _scanState.value.copy(
                    status = "ANALYZING",
                    error = null,
                    analysisNote = null,
                    selectedApps = currentSelectedApps
                )

                val selectedPackages = currentSelectedApps.map { it.packageName }
                Log.d(TAG, "📦 Selected packages: ${selectedPackages.take(3).joinToString()}")

                // Start scan on server
                when (val result = repository.startScan(
                    apps = selectedPackages,
                    userId = userId!!,
                    deviceId = deviceId!!,
                    includeSystemApps = false,
                    level = _scanState.value.scanLevel
                )) {
                    is ApiResult.Success -> {
                        val scanId = result.data?.scanId
                        if (scanId == null) {
                            Log.e(TAG, "❌ Null scanId in response")
                            _scanState.value = _scanState.value.copy(
                                status = "FAILED",
                                error = context.getString(R.string.error_scan_id_empty),
                                selectedApps = currentSelectedApps
                            )
                            return@launch
                        }
                        Log.d(TAG, "🚀 Scan started with ID: $scanId")
                        
                        _scanState.value = _scanState.value.copy(
                            status = "ANALYZING",
                            scanId = scanId,
                            selectedApps = currentSelectedApps,
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
                        Log.e(TAG, "âŒ API Error: ${result.message} (code: ${result.code})")
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = result.message,
                            selectedApps = currentSelectedApps
                        )
                    }
                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "❌ Network Error: ${result.exception.message}", result.exception)
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = "Erreur réseau: ${result.exception.message ?: "vérifiez votre connexion"}",
                            selectedApps = currentSelectedApps
                        )
                    }
                    is ApiResult.SerializationError -> {
                        Log.e(TAG, "❌ Serialization Error: ${result.exception.message}", result.exception)
                        Log.e(TAG, "   Raw response: ${result.rawResponse?.take(500)}")
                        _scanState.value = _scanState.value.copy(
                            status = "FAILED",
                            error = "Erreur de données: ${result.exception.message}",
                            selectedApps = currentSelectedApps
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "âŒ Unexpected error in startScan: ${e.message}", e)
                val currentSelectedApps = _scanState.value.selectedApps
                val errorMsg = "${e.javaClass.simpleName}: ${e.message ?: "Erreur inconnue"}"
                _scanState.value = _scanState.value.copy(
                    status = "FAILED",
                    error = errorMsg,
                    selectedApps = currentSelectedApps
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
                // For APK upload, force DEEP scan mode for thorough analysis
                _scanState.value = _scanState.value.copy(
                    status = "ANALYZING",
                    error = null,
                    scanLevel = ScanLevel.DEEP, // APK uploads always use DEEP
                    analysisNote = "Validation et analyse APK en cours...",
                    selectedApps = emptyList(),
                    totalApps = 1,
                    scannedApps = 0
                )

                when (val result = repository.scanApk(uri, userId!!, deviceId)) {
                    is ApiResult.Success -> {
                        val appData = result.data?.app
                        if (appData == null) {
                            _scanState.value = _scanState.value.copy(
                                status = "FAILED",
                                error = "Erreur serveur: aucune donnée d'application"
                            )
                            return@launch
                        }
                        val app = appData
                        val localApp = app.toUiModel(isSelected = false)
                        // Use calculated risk result
                        val riskResult = localApp.riskResult
                        val score = riskResult?.score?.toFloat() ?: 0f
                        val level = riskResult?.riskLevel ?: RiskLevel.LOW
                        
                        val note = if (app.scanResults?.aiStatus == "ok") {
                            "Analyse APK complète: MobSF + IA + Heuristiques"
                        } else {
                            "Analyse APK: MobSF + Heuristiques locales"
                        }

                        _scanState.value = _scanState.value.copy(
                            status = "COMPLETED",
                            selectedApps = listOf(localApp),
                            totalApps = 1,
                            scannedApps = 1,
                            highRiskCount = if (level == RiskLevel.HIGH || level == RiskLevel.CRITICAL) 1 else 0,
                            mediumRiskCount = if (level == RiskLevel.MEDIUM) 1 else 0,
                            lowRiskCount = if (level == RiskLevel.LOW || level == RiskLevel.SAFE) 1 else 0,
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
     * For DEEP scans: 5 minute timeout (100 attempts)
     * For SMART scans: 2 minute timeout (40 attempts)
     */
    private fun startPolling(scanId: String) {
        // Cancel any existing polling job
        pollingJob?.cancel()
        
        pollingJob = viewModelScope.launch {
            var attempts = 0
            val maxAttempts = if (_scanState.value.scanLevel == ScanLevel.DEEP) {
                100 // DEEP: 5 minutes max (100 * 3s)
            } else {
                40 // SMART: 2 minutes max (40 * 3s)
            }
            
            while (attempts < maxAttempts) {
                delay(3000) // Poll every 3 seconds
                attempts++
                
                Log.d(TAG, "📊 Polling scan status #$attempts/$maxAttempts for $scanId")

                when (val result = repository.getScanStatus(scanId)) {
                    is ApiResult.Success -> {
                        val status = result.data
                        val derivedStatus = when {
                            status.status?.equals("completed", ignoreCase = true) == true -> "completed"
                            status.status?.equals("failed", ignoreCase = true) == true -> "failed"
                            status.steps.isNotEmpty() && status.steps.all { it.status?.equals("COMPLETED", ignoreCase = true) == true } -> "completed"
                            (status.percentage ?: status.progress ?: 0) >= 100 -> "completed"
                            else -> status.status?.lowercase() ?: "processing"
                        }
                        val currentSelectedApps = _scanState.value.selectedApps
                        val recommendationNote = if (status.recommendDeepAnalysis == true && _scanState.value.scanLevel == ScanLevel.SMART) {
                            "Suggestion: lancer un scan DEEP pour approfondir (confiance ${status.confidenceScore?.roundToInt() ?: 0}%)."
                        } else null

                        // Update progress
                        if (derivedStatus != "completed" && derivedStatus != "failed") {
                             _scanState.value = _scanState.value.copy(
                                scannedApps = status.scannedApps ?: _scanState.value.scannedApps,
                                totalApps = status.totalApps ?: _scanState.value.totalApps,
                                selectedApps = currentSelectedApps,
                                highRiskCount = status.results?.highRiskApps ?: 0,
                                mediumRiskCount = status.results?.mediumRiskApps ?: 0,
                                lowRiskCount = status.results?.lowRiskApps ?: 0,
                                averageScore = status.results?.averageScore ?: 0f,
                                confidenceScore = status.results?.confidenceScore?.toInt(),
                                recommendDeepAnalysis = status.results?.recommendDeepAnalysis ?: false,
                                analysisNote = recommendationNote ?: _scanState.value.analysisNote
                            )
                        } else if (derivedStatus == "completed") {
                            // Completed! Get full scan result by scanId
                            Log.d(TAG, "✅ Scan completed, fetching result for $scanId")
                            val scanResult = repository.getScanResult(scanId)

                            if (scanResult is ApiResult.Success) {
                                val result = scanResult.data
                                val pkgName = result.packageName ?: "unknown"
                                
                                // Determine score from result
                                val score = result.overallScore?.toInt() 
                                    ?: result.securityScore?.toInt() 
                                    ?: 0
                                
                                // Determine risk level from globalRisk or score
                                val riskLevel = when (result.globalRisk?.lowercase()) {
                                    "low", "safe" -> RiskLevel.LOW
                                    "medium" -> RiskLevel.MEDIUM
                                    "high" -> RiskLevel.HIGH
                                    "critical" -> RiskLevel.CRITICAL
                                    else -> if (score >= 85) RiskLevel.LOW
                                        else if (score >= 70) RiskLevel.MEDIUM
                                        else if (score >= 40) RiskLevel.HIGH
                                        else RiskLevel.CRITICAL
                                }
                                
                                val risk = tn.esprit.dam.features.scan.domain.ScanRiskResult(
                                    score = score,
                                    riskLevel = riskLevel,
                                    permissionScore = 100,
                                    trackerScore = 100 - ((result.trackers?.count ?: 0) * 10),
                                    codeScore = ((1f - (result.ml?.malwareProbability ?: 0f)) * 100).toInt(),
                                    criticalIssues = result.errors,
                                    warnings = result.warnings,
                                    confidence = if ((result.confidenceScore ?: 0.0) > 80) 
                                        tn.esprit.dam.features.scan.domain.ConfidenceLevel.HIGH
                                    else if ((result.confidenceScore ?: 0.0) > 50)
                                        tn.esprit.dam.features.scan.domain.ConfidenceLevel.MEDIUM
                                    else tn.esprit.dam.features.scan.domain.ConfidenceLevel.LOW
                                )
                                
                                val uiApp = LocalAppInfo(
                                    packageName = pkgName,
                                    displayName = result.appName ?: pkgName,
                                    category = null,
                                    isSystemApp = false,
                                    permissions = result.permissions,
                                    trackers = emptyList(),
                                    isSelected = false,
                                    riskResult = risk
                                )
                                
                                val high = if (riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL) 1 else 0
                                val med = if (riskLevel == RiskLevel.MEDIUM) 1 else 0
                                val low = if (riskLevel == RiskLevel.LOW || riskLevel == RiskLevel.SAFE) 1 else 0

                                _scanState.value = _scanState.value.copy(
                                    status = "COMPLETED",
                                    selectedApps = listOf(uiApp),
                                    totalApps = 1,
                                    scannedApps = 1,
                                    averageScore = score.toFloat(),
                                    confidenceScore = result.confidenceScore?.toInt(),
                                    recommendDeepAnalysis = result.recommendDeepAnalysis ?: false,
                                    highRiskCount = high,
                                    mediumRiskCount = med,
                                    lowRiskCount = low
                                )
                                Log.d(TAG, "✅ Scan result displayed: $pkgName score=$score risk=$riskLevel")
                            } else {
                                Log.e(TAG, "❌ Failed to fetch scan result: $scanResult")
                                // Still mark as completed but without detailed results
                                _scanState.value = _scanState.value.copy(
                                    status = "COMPLETED",
                                    analysisNote = "Analyse terminée. Détails non disponibles."
                                )
                            }
                            return@launch
                        } else if (derivedStatus == "failed") {
                            _scanState.value = _scanState.value.copy(
                                status = "FAILED",
                                error = "L'analyse a échoué sur le serveur"
                            )
                            return@launch
                        }
                    }
                    is ApiResult.ApiError -> {
                        Log.e(TAG, "âŒ Polling error: ${result.message}")
                    }
                    is ApiResult.NetworkError -> {
                        Log.e(TAG, "âŒ Network error during polling")
                    }
                    is ApiResult.SerializationError -> {
                        Log.e(TAG, "âŒ Serialization error during polling")
                    }
                }
            }
            
            // Max attempts reached (timeout)
            val timeoutMsg = if (_scanState.value.scanLevel == ScanLevel.DEEP) {
                "Délai d'expiration: l'analyse cloud prend plus de 5 minutes. Vérifiez l'état du serveur N8N."
            } else {
                "Délai d'expiration: l'analyse prend plus de 2 minutes. Vérifiez votre connexion réseau."
            }
            Log.w(TAG, "⚠️ Max polling attempts reached ($attempts/$maxAttempts)")
            _scanState.value = _scanState.value.copy(
                status = "FAILED",
                error = timeoutMsg
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
