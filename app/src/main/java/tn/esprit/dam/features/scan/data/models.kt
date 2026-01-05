package tn.esprit.dam.features.scan.data

import com.google.gson.annotations.SerializedName

// API Request Models
data class StartScanRequestDto(
  @SerializedName("deviceId")
  val deviceId: String,
  @SerializedName("platform")
  val platform: String = "android",
  @SerializedName("includeSystemApps")
  val includeSystemApps: Boolean = false,
  @SerializedName("apps")
  val apps: List<StartScanAppDto>,
  @SerializedName("userId")
  val userId: String,
  @SerializedName("timestamp")
  val timestamp: Long = System.currentTimeMillis()
)

data class StartScanAppDto(
  @SerializedName("packageName")
  val packageName: String,
  @SerializedName("displayName")
  val displayName: String,
  @SerializedName("category")
  val category: String? = null
)

// API Response Models
data class ApiResponse<T>(
  @SerializedName("success")
  val success: Boolean,
  @SerializedName("data")
  val data: T,
  @SerializedName("timestamp")
  val timestamp: String
)

data class ScanResponseDto(
  @SerializedName("scanId")
  val scanId: String,
  @SerializedName("status")
  val status: String,
  @SerializedName("userId")
  val userId: String,
  @SerializedName("deviceId")
  val deviceId: String,
  @SerializedName("platform")
  val platform: String,
  @SerializedName("apps")
  val apps: List<AppDto>,
  @SerializedName("results")
  val results: ScanResultsDto,
  @SerializedName("createdAt")
  val createdAt: String,
  @SerializedName("updatedAt")
  val updatedAt: String,
  @SerializedName("confidenceScore")
  val confidenceScore: Int? = null,
  @SerializedName("recommendDeepAnalysis")
  val recommendDeepAnalysis: Boolean? = false
)

data class ScanResultsDto(
  @SerializedName("totalScanned")
  val totalScanned: Int,
  @SerializedName("highRiskApps")
  val highRiskApps: Int,
  @SerializedName("mediumRiskApps")
  val mediumRiskApps: Int,
  @SerializedName("lowRiskApps")
  val lowRiskApps: Int,
  @SerializedName("averageScore")
  val averageScore: Float,
  @SerializedName("confidenceScore")
  val confidenceScore: Int? = null,
  @SerializedName("recommendDeepAnalysis")
  val recommendDeepAnalysis: Boolean? = false
)

data class AppDto(
  @SerializedName("packageName")
  val packageName: String,
  @SerializedName("displayName")
  val displayName: String,
  @SerializedName("category")
  val category: String? = null,
  @SerializedName("permissions")
  val permissions: List<String> = emptyList(),
  @SerializedName("trackers")
  val trackers: List<String> = emptyList(),
  @SerializedName("storeData")
  val storeData: StoreDataDto? = null,
  @SerializedName("scanResults")
  val scanResults: AnalysisResultDto? = null,
  @SerializedName("finalScore")
  val finalScore: Float = 0f,
  @SerializedName("lastScanned")
  val lastScanned: String? = null
)

data class StoreDataDto(
  @SerializedName("version")
  val version: String? = null,
  @SerializedName("downloads")
  val downloads: String? = null,
  @SerializedName("rating")
  val rating: Float? = null,
  @SerializedName("developer")
  val developer: String? = null,
  @SerializedName("icon")
  val icon: String? = null,
  @SerializedName("lastUpdate")
  val lastUpdate: String? = null
)

data class AnalysisResultDto(
  @SerializedName("aiRiskScore")
  val aiRiskScore: Float,
  @SerializedName("aiRiskLevel")
  val aiRiskLevel: String, // HIGH, MEDIUM, LOW
  @SerializedName("aiSummary")
  val aiSummary: String,
  @SerializedName("aiRecommendations")
  val aiRecommendations: List<String>,
  @SerializedName("permissions")
  val permissions: List<PermissionInfo> = emptyList(),
  @SerializedName("trackers")
  val trackers: List<TrackerInfo> = emptyList(),
  @SerializedName("permissionsScore")
  val permissionsScore: Float? = null,
  @SerializedName("trackersScore")
  val trackersScore: Float? = null
)

data class PermissionInfo(
  @SerializedName("name")
  val name: String
)

data class TrackerInfo(
  @SerializedName("name")
  val name: String
)

// Status check response
data class ScanStatusResponseDto(
  @SerializedName("scanId")
  val scanId: String,
  @SerializedName("status")
  val status: String, // PENDING, ANALYZING, COMPLETED, FAILED
  @SerializedName("progress")
  val progress: Int? = null,
  @SerializedName("results")
  val results: ScanResultsDto? = null
)

// App info response
data class AppInfoResponseDto(
  @SerializedName("app")
  val app: AppDto,
  @SerializedName("history")
  val history: List<AppScanHistoryDto> = emptyList()
)

data class AppScanHistoryDto(
  @SerializedName("scanDate")
  val scanDate: String,
  @SerializedName("score")
  val score: Float,
  @SerializedName("riskLevel")
  val riskLevel: String
)

