package tn.esprit.dam.features.scan.presentation.model

import android.graphics.drawable.Drawable
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.features.scan.domain.SecurityUtils
import tn.esprit.dam.features.scan.domain.ScanRiskResult
import tn.esprit.dam.features.scan.data.remote.dto.AppResult
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.ConfidenceLevel

data class MLAnalysisInfo(
    val explanation: String? = null,
    val recommendations: List<String> = emptyList(),
    val riskFactors: List<String> = emptyList(),
    val safetyTips: List<String> = emptyList(),
    val permissionsAnalysis: String? = null,
    val trackersAnalysis: String? = null,
    val behaviorAnalysis: String? = null,
    val analysisSource: String? = null
)

/** A tracker found in an app, as shown on the scan and detail screens. */
data class TrackerUi(
    val name: String,
    val riskLevel: String?
)

data class LocalAppInfo(
    val packageName: String,
    val displayName: String,
    val category: String? = null,
    val isSystemApp: Boolean = false,
    val permissions: List<String> = emptyList(),
    val trackers: List<TrackerUi> = emptyList(),
    val isSelected: Boolean = false,
    val riskResult: ScanRiskResult? = null,
    val mlAnalysis: MLAnalysisInfo? = null,
    val icon: Drawable? = null
)

fun AppResult.toUiModel(isSelected: Boolean = false): LocalAppInfo {
    val scoreFloat = this.aiRiskScore ?: this.finalScore ?: 0f
    val score = scoreFloat.toInt()

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
        permissionScore = 100,
        trackerScore = 100,
        codeScore = 100,
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

fun tn.esprit.dam.features.scan.data.remote.dto.AppInfoDto.toUiModel(isSelected: Boolean = false): LocalAppInfo {
    val simpleTrackers = this.trackers.map { TrackerUi(name = it.name, riskLevel = it.riskLevel) }

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

fun tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse.toLocalAppInfo(
    fallbackPackage: String,
    fallbackName: String
): LocalAppInfo {
    val simpleTrackers = this.trackers?.trackers?.map {
        TrackerUi(
            name = it.name,
            riskLevel = it.category
        )
    } ?: emptyList()

    val backendScore = this.overallScore ?: this.securityScore ?: 50f
    val score = backendScore.toInt().coerceIn(0, 100)

    val riskLevel = when (this.globalRisk?.uppercase()) {
        "LOW" -> RiskLevel.LOW
        "MEDIUM" -> RiskLevel.MEDIUM
        "HIGH" -> RiskLevel.HIGH
        "CRITICAL" -> RiskLevel.CRITICAL
        else -> when {
            score >= 85 -> RiskLevel.LOW
            score >= 70 -> RiskLevel.MEDIUM
            score >= 40 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }
    }

    val risk = ScanRiskResult(
        score = score,
        riskLevel = riskLevel,
        permissionScore = this.privacyScore?.toInt() ?: 100,
        trackerScore = this.trackers?.privacyScore ?: 100,
        codeScore = 100,
        criticalIssues = this.errors,
        warnings = this.warnings,
        confidence = when {
            (this.confidenceScore ?: 0.0) >= 80 -> ConfidenceLevel.HIGH
            (this.confidenceScore ?: 0.0) >= 60 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    )

    val mlAnalysisInfo = this.mlAnalysis?.let { ml ->
        MLAnalysisInfo(
            explanation = ml.explanation,
            recommendations = ml.recommendations,
            riskFactors = ml.riskFactors,
            safetyTips = ml.safetyTips,
            permissionsAnalysis = ml.analysisDetails?.permissionsAnalysis,
            trackersAnalysis = ml.analysisDetails?.trackersAnalysis,
            behaviorAnalysis = ml.analysisDetails?.behaviorAnalysis,
            analysisSource = ml.analysisSource
        )
    }

    return LocalAppInfo(
        packageName = this.packageName ?: fallbackPackage,
        displayName = this.appName ?: fallbackName,
        category = null,
        isSystemApp = false,
        permissions = this.permissions,
        trackers = simpleTrackers,
        isSelected = false,
        riskResult = risk,
        mlAnalysis = mlAnalysisInfo
    )
}

/** Lifecycle of a scan on the Scan screen. */
sealed interface ScanStatus {
    data object Idle : ScanStatus
    data object Loading : ScanStatus
    data object Analyzing : ScanStatus
    data object Completed : ScanStatus
    data object Failed : ScanStatus
}

data class ScanState(
    val scanId: String? = null,
    val status: ScanStatus = ScanStatus.Idle,
    val selectedApps: List<LocalAppInfo> = emptyList(),
    val totalApps: Int = 0,
    val scannedApps: Int = 0,
    val highRiskCount: Int = 0,
    val mediumRiskCount: Int = 0,
    val lowRiskCount: Int = 0,
    val averageScore: Float = 0f,
    val confidenceScore: Int? = null,
    val error: UiText? = null,
    val showSystemApps: Boolean = false,
    val analysisNote: UiText? = null,
    val mlExplanation: String? = null,
    val mlRecommendations: List<String> = emptyList(),
    val mlRiskFactors: List<String> = emptyList(),
    val mlSafetyTips: List<String> = emptyList(),
    val analysisSource: String? = null,
    val scanCompleted: Boolean = false
)
