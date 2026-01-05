package tn.esprit.dam.data.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Scan mode (aligned to backend SMART/DEEP only)
@Serializable
enum class ScanLevel {
    @SerialName("SMART") SMART,
    @SerialName("DEEP") DEEP
}

// Scan analysis type (installed app vs direct APK upload)
@Serializable
enum class AnalysisType {
    @SerialName("installed_app") INSTALLED_APP,
    @SerialName("apk_upload") APK_UPLOAD
}

// ============= Safe API Result Wrapper =============
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class ApiError(val message: String, val code: Int) : ApiResult<Nothing>()
    data class NetworkError(val exception: Throwable) : ApiResult<Nothing>()
    data class SerializationError(val exception: Throwable, val rawResponse: String?) : ApiResult<Nothing>()
}

// ============= API Response Wrapper =============
@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    @SerialName("data")
    val data: T? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("error")
    val error: String? = null,  // âœ… Backend uses 'error' field for errors
    @SerialName("timestamp")
    val timestamp: String? = null
)

@Serializable
data class ScanAppsResponse(
    @SerialName("apps")
    val apps: List<AppInfoDto> = emptyList(),
    @SerialName("total")
    val total: Int = 0,
    @SerialName("timestamp")
    val timestamp: String? = null
)

@Serializable
data class AppResult(
    @SerialName("packageName")
    val packageName: String? = null,
    @SerialName("appName")
    val appName: String? = null,
    @SerialName("finalScore")
    val finalScore: Float? = null,
    @SerialName("aiRiskScore")
    val aiRiskScore: Float? = null, // Backend uses this sometimes
    @SerialName("riskLevel")
    val riskLevel: String? = null,
    @SerialName("aiRiskLevel")
    val aiRiskLevel: String? = null,
    @SerialName("lastScanned")
    val lastScanned: String? = null
)

@Serializable
data class LatestScanResponse(
    @SerialName("results")
    val results: LatestScanResultsDto? = null,
    @SerialName("createdAt")
    val createdAt: String? = null,
    // Keep backward compatibility if backend sends flat list sometimes
    @SerialName("apps")
    val apps: List<AppResult> = emptyList(), 
    @SerialName("globalScore")
    val globalScore: Int = 0
)

@Serializable
data class LatestScanResultsDto(
    @SerialName("apps")
    val apps: List<AppResult> = emptyList(),
    @SerialName("globalScore")
    val globalScore: Int = 0,
    @SerialName("maxRiskLevel")
    val maxRiskLevel: String? = null,
    @SerialName("confidenceScore")
    val confidenceScore: Double? = null,
    @SerialName("recommendDeepAnalysis")
    val recommendDeepAnalysis: Boolean? = null
)

// ============= Scan Request/Response =============
@Serializable
data class StartScanRequest(
    @SerialName("deviceId")
    val deviceId: String,
    @SerialName("platform")
    val platform: String = "android",
    @SerialName("includeSystemApps")
    val includeSystemApps: Boolean = false,
    @SerialName("apps")
    val apps: List<StartScanAppDto>,
    @SerialName("userId")
    val userId: String,
    @SerialName("timestamp")
    val timestamp: String,  // ISO 8601 format: 2025-12-12T16:52:07.123Z
    @SerialName("level")
    val level: ScanLevel = ScanLevel.SMART,
    @SerialName("analysisType")
    val analysisType: AnalysisType = AnalysisType.INSTALLED_APP
)

@Serializable
data class StartScanAppDto(
    @SerialName("packageName")
    val packageName: String,
    @SerialName("displayName")
    val displayName: String,
    @SerialName("category")
    val category: String? = null
)

@Serializable
data class StartScanResponse(
    @SerialName("scanId")
    val scanId: String,
    @SerialName("status")
    val status: String,
    @SerialName("userId")
    val userId: String? = null,
    @SerialName("deviceId")
    val deviceId: String? = null,
    @SerialName("platform")
    val platform: String? = null,
    @SerialName("createdAt")
    val createdAt: String? = null,
    @SerialName("level")
    val level: ScanLevel? = null,
    @SerialName("analysisType")
    val analysisType: AnalysisType? = null
)

// ============= Scan Status =============
@Serializable
data class ScanStatusResponse(
    @SerialName("scanId")
    val scanId: String,
    @SerialName("status")
    val status: String? = null,
    @SerialName("analysisType")
    val analysisType: AnalysisType? = null,
    @SerialName("level")
    val level: ScanLevel? = null,
    @SerialName("progress")
    val progress: Int? = null,
    @SerialName("percentage")
    val percentage: Int? = null,
    @SerialName("currentStep")
    val currentStep: String? = null,
    @SerialName("steps")
    val steps: List<ScanStepProgress> = emptyList(),
    @SerialName("estimatedTimeRemaining")
    val estimatedTimeRemaining: Int? = null,
    @SerialName("elapsed")
    val elapsed: Int? = null,
    @SerialName("totalApps")
    val totalApps: Int? = null,
    @SerialName("scannedApps")
    val scannedApps: Int? = null,
    @SerialName("results")
    val results: ScanResultsDto? = null,
    @SerialName("confidenceScore")
    val confidenceScore: Double? = null,
    @SerialName("recommendDeepAnalysis")
    val recommendDeepAnalysis: Boolean? = null
)

@Serializable
data class ScanStepProgress(
    @SerialName("name")
    val name: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("progress")
    val progress: Int? = null,
    @SerialName("startTime")
    val startTime: String? = null,
    @SerialName("duration")
    val duration: Int? = null,
    @SerialName("endTime")
    val endTime: String? = null
)

@Serializable
data class ScanResultsDto(
    @SerialName("totalScanned")
    val totalScanned: Int,
    @SerialName("highRiskApps")
    val highRiskApps: Int,
    @SerialName("mediumRiskApps")
    val mediumRiskApps: Int,
    @SerialName("lowRiskApps")
    val lowRiskApps: Int,
    @SerialName("averageScore")
    val averageScore: Float,
    @SerialName("confidenceScore")
    val confidenceScore: Double? = null,
    @SerialName("recommendDeepAnalysis")
    val recommendDeepAnalysis: Boolean? = null
)

// ============= App Information =============
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

// ============= App Details (from /scan/app/{packageName}) =============
@Serializable
data class AppDetailsResponse(
    @SerialName("scanId")
    val scanId: String? = null,
    @SerialName("packageName")
    val packageName: String? = null,
    @SerialName("appName")
    val appName: String? = null,
    @SerialName("level")
    val level: ScanLevel? = null,
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
    @SerialName("recommendDeepAnalysis")
    val recommendDeepAnalysis: Boolean? = null,
    @SerialName("ml")
    val ml: ScanMLResult? = null,
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
    // Legacy fields for backward compatibility
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

// ============= Local Domain Models =============
@Serializable
data class LocalAppInfo(
    @SerialName("packageName")
    val packageName: String,
    @SerialName("displayName")
    val displayName: String,
    @SerialName("category")
    val category: String? = null,
    @SerialName("isSystemApp")
    val isSystemApp: Boolean = false,
    @SerialName("permissions")
    val permissions: List<String> = emptyList(),
    @SerialName("trackers")
    val trackers: List<SimpleTrackerInfo> = emptyList()
)

// ============= Search Request/Response =============
@Serializable
data class SearchAppRequest(
    @SerialName("query")
    val query: String,
    @SerialName("platform")
    val platform: String = "android",
    @SerialName("limit")
    val limit: Int = 10
)

@Serializable
data class SearchAppResponse(
    @SerialName("results")
    val results: List<SearchResultDto> = emptyList()
)

@Serializable
data class SearchResultDto(
    @SerialName("packageName")
    val packageName: String,
    @SerialName("appName")
    val appName: String,
    @SerialName("icon")
    val icon: String = "",
    @SerialName("rating")
    val rating: Float = 0f,
    @SerialName("downloads")
    val downloads: String = "Unknown",
    @SerialName("developer")
    val developer: String = "Unknown",
    @SerialName("predictedRiskScore")
    val predictedRiskScore: Int = 0,
    @SerialName("predictedRiskLevel")
    val predictedRiskLevel: String = "low",
    @SerialName("predictedRecommendations")
    val predictedRecommendations: List<String> = emptyList()
)

// ============= Scan History =============
@Serializable
data class ScanHistoryResponse(
    @SerialName("scans")
    val scans: List<ScanHistoryItemDto> = emptyList(),
    @SerialName("total")
    val total: Int = 0,
    @SerialName("limit")
    val limit: Int = 10,
    @SerialName("offset")
    val offset: Int = 0
)

@Serializable
data class ScanHistoryItemDto(
    @SerialName("scanId")
    val scanId: String,
    @SerialName("status")
    val status: String,
    @SerialName("totalApps")
    val totalApps: Int = 0,
    @SerialName("scannedApps")
    val scannedApps: Int = 0,
    @SerialName("averageScore")
    val averageScore: Int? = null,
    @SerialName("highRiskApps")
    val highRiskApps: Int? = null,
    @SerialName("mediumRiskApps")
    val mediumRiskApps: Int? = null,
    @SerialName("lowRiskApps")
    val lowRiskApps: Int? = null,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("completedAt")
    val completedAt: String? = null,
    @SerialName("duration")
    val duration: Long? = null
)

// ============= Permission Details =============
@Serializable
data class PermissionDetailDto(
    @SerialName("name")
    val name: String,
    @SerialName("translation")
    val translation: String = "",
    @SerialName("riskLevel")
    val riskLevel: String = "normal",
    @SerialName("explanation")
    val explanation: String = ""
)

// ============= Scan Result (from /scan/{scanId}) =============
@Serializable
data class ScanResultResponse(
    @SerialName("scanId")
    val scanId: String,
    @SerialName("packageName")
    val packageName: String? = null,
    @SerialName("appName")
    val appName: String? = null,
    @SerialName("level")
    val level: ScanLevel? = null,
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
    @SerialName("recommendDeepAnalysis")
    val recommendDeepAnalysis: Boolean? = null,
    @SerialName("ml")
    val ml: ScanMLResult? = null,
    @SerialName("trackers")
    val trackers: ScanTrackersResult? = null,
    @SerialName("recommendations")
    val recommendations: List<String> = emptyList(),
    @SerialName("permissions")
    val permissions: List<String> = emptyList(),
    @SerialName("errors")
    val errors: List<String> = emptyList(),
    @SerialName("warnings")
    val warnings: List<String> = emptyList()
)

@Serializable
data class ScanMLResult(
    @SerialName("malwareProbability")
    val malwareProbability: Float? = null,
    @SerialName("verdict")
    val verdict: String? = null,
    @SerialName("confidence")
    val confidence: Float? = null
)

@Serializable
data class ScanTrackersResult(
    @SerialName("count")
    val count: Int? = null,
    @SerialName("categories")
    val categories: Map<String, Int> = emptyMap()
)

// ============= Tracker Details =============
@Serializable
data class TrackerDetailDto(
    @SerialName("name")
    val name: String,
    @SerialName("category")
    val category: String = "",
    @SerialName("risk")
    val risk: String = "low"
)

// ============= Final Score =============
@Serializable
data class FinalScoreDto(
    @SerialName("score")
    val score: Int,
    @SerialName("storeWeight")
    val storeWeight: Int = 30,
    @SerialName("ollamaWeight")
    val ollamaWeight: Int = 70,
    @SerialName("breakdown")
    val breakdown: String = ""
)

