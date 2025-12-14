package tn.esprit.dam.data.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val error: String? = null,  // ✅ Backend uses 'error' field for errors
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
    val packageName: String,
    @SerialName("appName")
    val appName: String,
    @SerialName("finalScore")
    val finalScore: Float,
    @SerialName("lastScanned")
    val lastScanned: String? = null
)

@Serializable
data class LatestScanResponse(
    @SerialName("apps")
    val apps: List<AppResult> = emptyList(),
    @SerialName("globalScore")
    val globalScore: Int = 0,
    @SerialName("createdAt")
    val createdAt: String
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
    val timestamp: String  // ISO 8601 format: 2025-12-12T16:52:07.123Z
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
    val userId: String,
    @SerialName("deviceId")
    val deviceId: String,
    @SerialName("platform")
    val platform: String,
    @SerialName("createdAt")
    val createdAt: String
)

// ============= Scan Status =============
@Serializable
data class ScanStatusResponse(
    @SerialName("scanId")
    val scanId: String,
    @SerialName("status")
    val status: String,
    @SerialName("progress")
    val progress: Int? = null,
    @SerialName("totalApps")
    val totalApps: Int? = null,
    @SerialName("scannedApps")
    val scannedApps: Int? = null,
    @SerialName("results")
    val results: ScanResultsDto? = null
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
    val averageScore: Float
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

// ============= App Details =============
@Serializable
data class AppDetailsResponse(
    @SerialName("app")
    val app: AppInfoDto,
    @SerialName("history")
    val history: List<AppScanHistoryDto> = emptyList()
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

