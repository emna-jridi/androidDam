package tn.esprit.dam.features.scan.data

import tn.esprit.dam.features.scan.domain.SecurityUtils
import tn.esprit.dam.features.scan.domain.ScanRiskResult
import tn.esprit.dam.features.scan.data.AppDto
import tn.esprit.dam.data.api.models.SimpleTrackerInfo
import tn.esprit.dam.data.api.models.AppResult
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.ConfidenceLevel

/**
 * UI state wrapper for LocalAppInfo with selection tracking
 */
data class LocalAppInfo(
    val packageName: String,
    val displayName: String,
    val category: String? = null,
    val isSystemApp: Boolean = false,
    val permissions: List<String> = emptyList(),
    val trackers: List<SimpleTrackerInfo> = emptyList(),
    val isSelected: Boolean = false,
    val riskResult: ScanRiskResult? = null
)

/**
 * Extension to convert AppDto (from Scan response) to UI model
 */
fun AppDto.toUiModel(isSelected: Boolean = false): LocalAppInfo {
    // Convert String trackers to SimpleTrackerInfo
    val simpleTrackers = this.trackers.map { name -> 
        SimpleTrackerInfo(name = name, riskLevel = "unknown") 
    }

    // Calculate risk
    val risk = SecurityUtils.calculateAppRisk(
        permissions = this.permissions,
        trackers = this.trackers,
        isSystemApp = false
    )

    return LocalAppInfo(
        packageName = this.packageName,
        displayName = this.displayName,
        category = this.category,
        isSystemApp = false,
        permissions = this.permissions,
        trackers = simpleTrackers,
        isSelected = isSelected,
        riskResult = risk
    )
}

/**
 * Extension to convert AppResult (from Latest Scan) to UI model
 * Note: AppResult lacks permissions/trackers details, so we use the final score.
 */
fun AppResult.toUiModel(isSelected: Boolean = false): LocalAppInfo {
    // We don't have details, but we have final score.
    // We create a synthetic RiskResult from the score.
    val scoreFloat = this.aiRiskScore ?: this.finalScore ?: 0f
    val score = scoreFloat.toInt()
    
    // Determine risk level (prefer AI level, fallback to calculated)
    val riskStr = this.aiRiskLevel ?: this.riskLevel
    val level = when (riskStr?.lowercase()) {
        "low", "safe" -> RiskLevel.LOW
        "medium" -> RiskLevel.MEDIUM
        "high" -> RiskLevel.HIGH
        "critical" -> RiskLevel.CRITICAL
        else -> if (score >= 85) RiskLevel.LOW
            else if (score >= 70) RiskLevel.MEDIUM
            else if (score >= 40) RiskLevel.HIGH
            else RiskLevel.CRITICAL
    }
    
    val risk = ScanRiskResult(
        score = score,
        riskLevel = level,
        permissionScore = 100, // Placeholder
        trackerScore = 100, // Placeholder
        codeScore = 100, // Placeholder
        criticalIssues = emptyList(),
        warnings = emptyList(),
        confidence = ConfidenceLevel.LOW
    )

    return LocalAppInfo(
        packageName = this.packageName ?: "unknown.package",
        displayName = this.appName ?: this.packageName ?: "Unknown App",
        category = null,
        isSystemApp = false,
        permissions = emptyList(),
        trackers = emptyList(),
        isSelected = isSelected,
        riskResult = risk
    )
}

/**
 * Extension to convert AppInfoDto (from ApiModels) to UI model
 */
fun tn.esprit.dam.data.api.models.AppInfoDto.toUiModel(isSelected: Boolean = false): LocalAppInfo {
    // Convert String trackers to SimpleTrackerInfo
    val simpleTrackers = this.trackers

    // Provide default risk calculation if not unavailable
    val risk = SecurityUtils.calculateAppRisk(
        permissions = this.permissions,
        trackers = this.trackers.map { it.name },
        isSystemApp = false
    )

    return LocalAppInfo(
        packageName = this.packageName,
        displayName = this.displayName ?: this.packageName,
        category = this.category,
        isSystemApp = false,
        permissions = this.permissions,
        trackers = simpleTrackers,
        isSelected = isSelected,
        riskResult = risk
    )
}

/**
 * Scan state for UI
 */
data class ScanState(
    val scanId: String? = null,
    val status: String = "IDLE", // IDLE, LOADING, ANALYZING, COMPLETED, FAILED
    val selectedApps: List<LocalAppInfo> = emptyList(),
    val totalApps: Int = 0,
    val scannedApps: Int = 0,
    val highRiskCount: Int = 0,
    val mediumRiskCount: Int = 0,
    val lowRiskCount: Int = 0,
    val averageScore: Float = 0f,
    val error: String? = null,
    val showSystemApps: Boolean = false,
    val analysisNote: String? = null
)
