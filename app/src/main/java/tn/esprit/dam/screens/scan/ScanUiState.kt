package tn.esprit.dam.screens.scan

import tn.esprit.dam.data.model.SavedScan
import tn.esprit.dam.data.model.AppAnalysisResult

sealed class ScanUiState {
    object Initial : ScanUiState()
    object LoadingLastScan : ScanUiState()
    data class Scanning(
        val totalApps: Int = 0,
        val progress: String = "Collecte des applications..."
    ) : ScanUiState()
    data class Success(
        val scan: SavedScan,
        val isFromCache: Boolean = false
    ) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

data class ScanStatsData(
    val totalApps: Int = 0,
    val criticalApps: Int = 0,
    val highRiskApps: Int = 0,
    val mediumRiskApps: Int = 0,
    val lowRiskApps: Int = 0,
    val totalTrackers: Int = 0,
    val totalPermissions: Int = 0,
    val avgScore: Double = 0.0
) {
    companion object {
        private fun calculateScore(totalTrackers: Int): Int {
            return when {
                totalTrackers > 10 -> 20
                totalTrackers > 5 -> 50
                totalTrackers > 2 -> 70
                else -> 90
            }
        }

        // ✅ Fonction helper pour déterminer le niveau de risque
        private fun calculateRiskLevel(score: Int): String {
            return when {
                score < 30 -> "critical"
                score < 50 -> "high"
                score < 70 -> "medium"
                else -> "low"
            }
        }

        fun fromResults(results: List<AppAnalysisResult>): ScanStatsData { // ✅ CHANGÉ le type
            // Calculer les scores pour chaque app
            val appsWithScores = results.map { app ->
                val score = calculateScore(app.totalTrackers)
                val riskLevel = calculateRiskLevel(score)
                Triple(app, score, riskLevel)
            }

            return ScanStatsData(
                totalApps = results.size,
                criticalApps = appsWithScores.count { it.third == "critical" },
                highRiskApps = appsWithScores.count { it.third == "high" },
                mediumRiskApps = appsWithScores.count { it.third == "medium" },
                lowRiskApps = appsWithScores.count { it.third == "low" },
                totalTrackers = results.sumOf { it.totalTrackers }, // ✅ CHANGÉ
                totalPermissions = results.sumOf { it.permissions.size }, // ✅ CHANGÉ
                avgScore = if (appsWithScores.isNotEmpty()) {
                    appsWithScores.map { it.second }.average()
                } else 0.0
            )
        }

        fun fromScan(scan: SavedScan): ScanStatsData {
            // ✅ Calculer directement depuis les résultats car ScanSummary est simplifié
            val appsWithScores = scan.results.map { app ->
                val score = calculateScore(app.totalTrackers)
                val riskLevel = calculateRiskLevel(score)
                Triple(app, score, riskLevel)
            }

            return ScanStatsData(
                totalApps = scan.totalApps,
                criticalApps = appsWithScores.count { it.third == "critical" },
                highRiskApps = appsWithScores.count { it.third == "high" },
                mediumRiskApps = appsWithScores.count { it.third == "medium" },
                lowRiskApps = appsWithScores.count { it.third == "low" },
                totalTrackers = scan.summary?.totalTrackers ?: scan.results.sumOf { it.totalTrackers },
                totalPermissions = scan.results.sumOf { it.permissions.size },
                avgScore = if (appsWithScores.isNotEmpty()) {
                    appsWithScores.map { it.second }.average()
                } else 0.0
            )
        }
    }
}