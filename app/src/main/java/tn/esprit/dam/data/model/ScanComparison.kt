package tn.esprit.dam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ComparisonResponse(
    val success: Boolean,
    val data: ComparisonData
)

@Serializable
data class ComparisonData(
    val scan1: ScanInfo,
    val scan2: ScanInfo,
    val differences: Differences,
    val summary: ComparisonSummary
)

@Serializable
data class ScanInfo(
    val scanId: String,
    val scanDate: String,
    val totalApps: Int,
    val avgScore: Int
)

@Serializable
data class Differences(
    val newApps: List<String>,
    val removedApps: List<String>,
    val unchangedApps: List<String>,
    val scoreChanges: List<ScoreChange>
)

@Serializable
data class ScoreChange(
    val packageName: String,
    val name: String,
    val oldScore: Int,
    val newScore: Int,
    val change: Int
)

@Serializable
data class ComparisonSummary(
    val totalChanges: Int,
    val appsAdded: Int,
    val appsRemoved: Int,
    val avgScoreChange: Int
)