package tn.esprit.dam.features.scan.data.remote.dto

import tn.esprit.dam.features.scan.domain.model.AnalysisType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppInfoDto(
    @SerialName("packageName")
    val packageName: String,
    @SerialName("displayName")
    val displayName: String? = null,
    @SerialName("category")
    val category: String? = null,
    @SerialName("permissions")
    val permissions: List<String> = emptyList(),
    @SerialName("trackers")
    val trackers: List<SimpleTrackerInfo> = emptyList(),
    @SerialName("storeData")
    val storeData: StoreDataDto? = null,
    @SerialName("scanResults")
    val scanResults: AnalysisResultDto? = null,
    @SerialName("finalScore")
    val finalScore: Float = 0f,
    @SerialName("lastScanned")
    val lastScanned: String? = null
)

@Serializable
data class SimpleTrackerInfo(
    @SerialName("name")
    val name: String,
    @SerialName("riskLevel")
    val riskLevel: String? = null
)

@Serializable
data class StoreDataDto(
    @SerialName("version")
    val version: String? = null,
    @SerialName("downloads")
    val downloads: String? = null,
    @SerialName("rating")
    val rating: Float? = null,
    @SerialName("developer")
    val developer: String? = null,
    @SerialName("icon")
    val icon: String? = null,
    @SerialName("lastUpdate")
    val lastUpdate: String? = null
)

@Serializable
data class AnalysisResultDto(
    @SerialName("aiRiskScore")
    val aiRiskScore: Float,
    @SerialName("aiRiskLevel")
    val aiRiskLevel: String,
    @SerialName("aiSummary")
    val aiSummary: String,
    @SerialName("aiRecommendations")
    val aiRecommendations: List<String> = emptyList(),
    @SerialName("permissionsScore")
    val permissionsScore: Float? = null,
    @SerialName("trackersScore")
    val trackersScore: Float? = null,
    @SerialName("aiStatus")
    val aiStatus: String? = "fallback"
)

@Serializable
data class MLAnalysisDetailsDto(
    @SerialName("permissionsAnalysis")
    val permissionsAnalysis: String? = null,
    @SerialName("trackersAnalysis")
    val trackersAnalysis: String? = null,
    @SerialName("behaviorAnalysis")
    val behaviorAnalysis: String? = null
)

@Serializable
data class MLAnalysisDto(
    @SerialName("explanation")
    val explanation: String? = null,
    @SerialName("recommendations")
    val recommendations: List<String> = emptyList(),
    @SerialName("riskFactors")
    val riskFactors: List<String> = emptyList(),
    @SerialName("safetyTips")
    val safetyTips: List<String> = emptyList(),
    @SerialName("analysisDetails")
    val analysisDetails: MLAnalysisDetailsDto? = null,
    @SerialName("analysisSource")
    val analysisSource: String? = null
)

@Serializable
data class AppDetailsResponse(
    @SerialName("scanId")
    val scanId: String? = null,
    @SerialName("packageName")
    val packageName: String? = null,
    @SerialName("appName")
    val appName: String? = null,
    @SerialName("analysisType")
    val analysisType: AnalysisType? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("securityScore")
    val securityScore: Float? = null,
    @SerialName("privacyScore")
    val privacyScore: Float? = null,
    @SerialName("globalRisk")
    val globalRisk: String? = null,
    @SerialName("overallScore")
    val overallScore: Float? = null,
    @SerialName("confidenceScore")
    val confidenceScore: Double? = null,
    @SerialName("ml")
    val ml: ScanMLResult? = null,
    @SerialName("mlAnalysis")
    val mlAnalysis: MLAnalysisDto? = null,
    @SerialName("trackers")
    val trackers: AppTrackersResult? = null,
    @SerialName("recommendations")
    val recommendations: List<String> = emptyList(),
    @SerialName("permissions")
    val permissions: List<String> = emptyList(),
    @SerialName("errors")
    val errors: List<String> = emptyList(),
    @SerialName("warnings")
    val warnings: List<String> = emptyList(),
    @SerialName("app")
    val app: AppInfoDto? = null,
    @SerialName("history")
    val history: List<AppScanHistoryDto> = emptyList()
)

@Serializable
data class AppTrackersResult(
    @SerialName("totalFound")
    val totalFound: Int? = null,
    @SerialName("categories")
    val categories: TrackerCategories? = null,
    @SerialName("trackers")
    val trackers: List<TrackerItemDto> = emptyList(),
    @SerialName("privacyScore")
    val privacyScore: Int? = null,
    @SerialName("apiUsed")
    val apiUsed: String? = null,
    @SerialName("cachingStatus")
    val cachingStatus: String? = null
)

@Serializable
data class TrackerCategories(
    @SerialName("advertising")
    val advertising: Int = 0,
    @SerialName("analytics")
    val analytics: Int = 0,
    @SerialName("crossapp")
    val crossapp: Int = 0,
    @SerialName("location")
    val location: Int = 0
)

@Serializable
data class TrackerItemDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String,
    @SerialName("category")
    val category: String? = null,
    @SerialName("found")
    val found: Boolean = true
)

@Serializable
data class AppScanHistoryDto(
    @SerialName("scanDate")
    val scanDate: String,
    @SerialName("score")
    val score: Float,
    @SerialName("riskLevel")
    val riskLevel: String
)
