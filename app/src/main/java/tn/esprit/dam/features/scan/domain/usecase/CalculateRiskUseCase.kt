package tn.esprit.dam.features.scan.domain.usecase

import android.adservices.ondevicepersonalization.AppInfo
import javax.inject.Inject
import kotlin.collections.sortedBy
import kotlin.collections.sortedByDescending

/**
 * Data class for risk analysis results
 */
data class RiskAnalysisResult(
    val averageScore: Float,
    val highRiskCount: Int,
    val mediumRiskCount: Int,
    val lowRiskCount: Int,
    val criticalApps: List<AppInfo>,
    val safeApps: List<AppInfo>
)

/**
 * CalculateRiskUseCase - Extracts risk calculation logic
 * Replaces duplicated risk score calculations in HomeViewModel, ScanViewModel, SearchViewModel
 * Single responsibility: Calculate and categorize risk levels
 */
class CalculateRiskUseCase @Inject constructor() {

    /**
     * Extract risk score from app (prefers AI score, falls back to final score)
     */
    private fun extractRiskScore(app: AppInfo): Float {
        return try {
            val riskScoreField = app::class.java.getDeclaredField("riskScore")
            riskScoreField.isAccessible = true
            (riskScoreField.get(app) as? Float) ?: 0f
        } catch (e: Exception) {
            (app as? Any)?.let {
                try {
                    val field = it::class.java.getDeclaredField("finalScore")
                    field.isAccessible = true
                    (field.get(it) as? Float) ?: 0f
                } catch (e2: Exception) {
                    0f
                }
            } ?: 0f
        }
    }

    /**
     * Categorize risk score into level
     * - < 40: Critical/High
     * - 40-69.99: Medium
     * - >= 70: Low/Safe
     */
    fun getRiskLevel(score: Float): String {
        return when {
            score < 40f -> "Critical"
            score < 70f -> "Medium"
            else -> "Safe"
        }
    }

    /**
     * Calculate comprehensive risk analysis for list of apps
     */
    fun analyzeRisks(apps: List<AppInfo>): RiskAnalysisResult {
        if (apps.isEmpty()) {
            return RiskAnalysisResult(
                averageScore = 0f,
                highRiskCount = 0,
                mediumRiskCount = 0,
                lowRiskCount = 0,
                criticalApps = emptyList(),
                safeApps = emptyList()
            )
        }

        val riskScores = apps.map { extractRiskScore(it) }
        val averageScore = riskScores.average().toFloat()

        // Categorize apps by risk level
        val highRiskCount = riskScores.count { it < 40f }
        val mediumRiskCount = riskScores.count { it in 40f..69.99f }
        val lowRiskCount = riskScores.count { it >= 70f }

        // Sort by risk for detailed views
        val criticalApps = apps.sortedBy { extractRiskScore(it) }.take(5)
        val safeApps = apps.sortedByDescending { extractRiskScore(it) }.take(5)

        return RiskAnalysisResult(
            averageScore = averageScore,
            highRiskCount = highRiskCount,
            mediumRiskCount = mediumRiskCount,
            lowRiskCount = lowRiskCount,
            criticalApps = criticalApps,
            safeApps = safeApps
        )
    }

    /**
     * Get risk color (for UI display)
     */
    fun getRiskColor(score: Float): String {
        return when {
            score < 40f -> "#FF6B6B" // Red - Critical
            score < 70f -> "#FFA500" // Orange - Medium
            else -> "#4CAF50" // Green - Safe
        }
    }
}
