package tn.esprit.dam.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val _id: String? = null,
    val email: String,
    val name: String?,
    val phone: String?,
    val address: String?,
    val image: String? = null,
    val role: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null,
    val resetPasswordCode: String? = null,
    val resetPasswordExpires: String? = null,
    val avatar: String? = null
)

//  Register request
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    val address: String,
    val avatar: String? = null
)

// Login request
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

//  Response returned after successful login
@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

//  Forgot password
@Serializable
data class ForgotPasswordRequest(
    val email: String
)

//  Reset password
@Serializable
data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)

//  Update user request
@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val avatar: String? = null
)

//  Generic message response
@Serializable
data class ApiResponse(
    val message: String
)
@Serializable
data class RegisterResponse(
    val user: User
)
@Serializable
data class AppInfo(
    val packageName: String,
    val name: String,
    val version: String = "",
    val permissions: List<String> = emptyList()
)

@Serializable
data class ScanRequest(
    val deviceId: String,
    val apps: List<AppInfo>
)

@Serializable

data class AppDetails(
    val packageName: String,
    val name: String,
    val developer: String = "",
    val category: String = "",
    val version: String = "",
    val iconUrl: String = "",
    val description: String = "",

    // Scores
    val privacyScore: Int = 50,
    val riskLevel: String = "MEDIUM",
    val riskColor: String = "#F39C12",
    val communityScore: Double = 0.0,

    // Détails de sécurité (présents seulement dans getAppDetails)
    val permissions: PermissionsInfo? = null,
    val trackers: TrackersInfo? = null,
    val flags: FlagsInfo? = null,
    val recommendations: List<String>? = null,
    val alternatives: List<AlternativeApp>? = null,
    val stats: AppStats? = null
)
@Serializable

data class FlagsInfo(
    val isDebuggable: Boolean,
    val hasUnknownTrackers: Boolean
)
@Serializable

data class AlternativeApp(
    val packageName: String,
    val name: String,
    val privacyScore: Int,
    val improvement: Int
)
@Serializable
data class AppStats(
    val totalScans: Int,
    val lastScanned: String?,
    val avgScoreFromCommunity: Int?
)
@Serializable
data class TrackersInfo(    
    val total: Int,
    val list: List<String>
)

@Serializable
data class CompareRequest(
    val packageNames: List<String>
)

@Serializable
data class CompareResult(
    val apps: List<AppComparison>
)

@Serializable
data class AppComparison(
    val packageName: String,
    val name: String,
    val privacyScore: Int,
    val trackerCount: Int,
    val permissionCount: Int
)

@Serializable
data class ScanHistory(
    val scans: List<HistoryItem>
)

@Serializable
data class HistoryItem(
    val id: String,
    val deviceId: String,
    val timestamp: String,
    val appsScanned: Int,
    val averageScore: Int
)

@Serializable
data class InstalledAppDto(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val permissions: List<String>
)

@Serializable
data class AnalyzeInstalledAppsDto(
    val userHash: String,
    val apps: List<InstalledAppDto>
)

@Serializable
data class AnalyzeInstalledAppsResponse(
    val scanId: String,
    val totalApps: Int,
    val results: List<ScanResult>,
    val summary: ScanSummary? = null
)

@Serializable
data class ScanSummary(
    val avgPrivacyScore: Double? = null,
    val totalTrackers: Int? = null,
    val totalPermissions: Int? = null,
    val riskDistribution: Map<String, Int>? = null
)

@Serializable
data class ScanResult(
    val packageName: String,
    val name: String,
    val score: Int,
    val riskLevel: String,
    val alerts: List<String> = emptyList(),
    val breakdown: BreakdownInfo? = null,
    val trackers: List<String> = emptyList(),
    val permissions: PermissionsInfo
)
@Serializable
data class PermissionsInfo(
    val dangerous: List<String>,
    val total: Int
)

@Serializable
data class BreakdownInfo(
    val permissions: PermissionBreakdown,
    val trackers: TrackerBreakdown
)

@Serializable
data class PermissionBreakdown(
    val penalty: Int,
    val count: Int,
    val list: List<String> = emptyList()
)

@Serializable
data class TrackerBreakdown(
    val penalty: Int,
    val count: Int
)
val ScanResult.appName: String
    get() = name

val ScanResult.privacyScore: Int
    get() = score
@Serializable
data class SearchResponse(
    val results: List<AppDetails>
)
@Serializable
data class CompareAppsRequest(
    val packageNames: List<String>
)
@Serializable

data class CompareAppsResponse(
    val apps: List<AppDetails>,
    val comparison: ComparisonResult
)
@Serializable

data class ComparisonResult(
    val bestChoice: AppDetails,
    val worstChoice: AppDetails,
    val avgScore: Double,
    val comparison: List<ComparisonItem>
)
@Serializable

data class ComparisonItem(
    val packageName: String,
    val name: String,
    val score: Int,
    val trackers: Int,
    val dangerousPermissions: Int
)

