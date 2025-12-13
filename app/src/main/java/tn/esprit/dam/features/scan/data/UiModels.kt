package tn.esprit.dam.features.scan.data

import tn.esprit.dam.data.api.models.LocalAppInfo as ApiLocalAppInfo
import tn.esprit.dam.data.api.models.AppInfoDto

/**
 * UI state wrapper for LocalAppInfo with selection tracking
 */
data class LocalAppInfo(
    val packageName: String,
    val displayName: String,
    val category: String? = null,
    val isSystemApp: Boolean = false,
    val permissions: List<String> = emptyList(),
    val trackers: List<tn.esprit.dam.data.api.models.SimpleTrackerInfo> = emptyList(),
    val isSelected: Boolean = false
)

/**
 * Extension to convert API model to UI model
 */
fun ApiLocalAppInfo.toUiModel(isSelected: Boolean = false): LocalAppInfo {
    return LocalAppInfo(
        packageName = this.packageName,
        displayName = this.displayName,
        category = this.category,
        isSystemApp = this.isSystemApp,
        permissions = this.permissions,
        trackers = this.trackers,
        isSelected = isSelected
    )
}

fun AppInfoDto.toUiModel(isSelected: Boolean = false): LocalAppInfo {
    return LocalAppInfo(
        packageName = this.packageName,
        displayName = this.displayName,
        category = this.category,
        isSystemApp = false,
        permissions = this.permissions,
        trackers = this.trackers,
        isSelected = isSelected
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
    val error: String? = null
)
