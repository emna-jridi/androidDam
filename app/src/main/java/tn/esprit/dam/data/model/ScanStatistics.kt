package tn.esprit.dam.data.model


import kotlinx.serialization.Serializable

@Serializable
data class StatisticsResponse(
    val success: Boolean,
    val data: StatisticsData
)

@Serializable
data class StatisticsData(
    val totalScans: Int,
    val firstScan: String?,
    val lastScan: String?,
    val avgAppsPerScan: Int,
    val avgScore: Int,
    val scoreEvolution: List<ScoreEvolution>,
    val appsEvolution: List<AppsEvolution>
)

@Serializable
data class ScoreEvolution(
    val date: String,
    val avgScore: Int,
    val scanId: String
)

@Serializable
data class AppsEvolution(
    val date: String,
    val totalApps: Int,
    val scanId: String
)