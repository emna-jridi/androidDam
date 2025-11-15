// data/Models.kt

package tn.esprit.dam.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

// ================== USER ===================

@Serializable
data class User(
    @SerialName("_id")
    val id: String? = null,
    val email: String,
    val name: String,
    val role: String = "user",
    val avatarUrl: String? = null,
    val userHash: String? = null,
    val provider: String = "local",
    val isEmailVerified: Boolean = false,
    val isVerified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ================== AUTH REQUESTS ===================

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class VerifyEmailRequest(
    val email: String,
    val otp: String
)

@Serializable
data class ResendOTPRequest(
    val email: String
)

@Serializable
data class RequestPasswordResetRequest(
    val email: String
)

@Serializable
data class VerifyPasswordResetOTPRequest(
    val email: String,
    val otp: String
)

@Serializable
data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)

@Serializable
data class GoogleLoginRequest(
    val idToken: String
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val avatar: String? = null
)

// ================== AUTH RESPONSES ===================

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)
@Serializable
data class RegisterResponse(
    val user: UserDto
)



@Serializable
data class LoginResponse(
    val message: String? = null,
    val user: User,
    val accessToken: String,
    val refreshToken: String? = null
)

@Serializable
data class VerifyEmailResponse(
    val message: String,
    val user: User,
    val accessToken: String
)
@Serializable
data class VerifyEmailMessageResponse(
    val message: String
)

@Serializable
data class ResendOTPResponse(
    val message: String,
    val expiresIn: String
)

@Serializable
data class RequestPasswordResetResponse(
    val message: String
)

@Serializable
data class VerifyPasswordResetOTPResponse(
    val message: String,
    val verified: Boolean
)

@Serializable
data class ResetPasswordResponse(
    val message: String
)

// ================== API RESPONSES ===================

@Serializable
data class ApiError(
    @Serializable(with = FlexibleMessageSerializer::class)
    val message: String? = null,
    val error: String? = null,
    val statusCode: Int? = null
)

object FlexibleMessageSerializer : KSerializer<String?> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor(
        "FlexibleMessage",
        kotlinx.serialization.descriptors.PrimitiveKind.STRING
    )

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: String?) {
        value?.let { encoder.encodeString(it) } ?: encoder.encodeNull()
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): String? {
        val element = decoder.decodeSerializableValue(JsonElement.serializer())

        return when {
            // Si c'est un string direct
            element is JsonPrimitive && element.isString -> element.content

            // Si c'est un array, prendre le premier message
            element is JsonArray && element.isNotEmpty() ->
                element.first().jsonPrimitive.content

            // Sinon null
            else -> null
        }
    }
}

@Serializable
data class ApiResponse(
    val message: String
)

// ================== APP SCANNING ===================

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

    // Détails de sécurité
    val permissions: PermissionsInfo? = null,
    val trackers: TrackersInfo? = null,
    val flags: FlagsInfo? = null,
    val recommendations: List<String>? = null,
    val alternatives: List<AlternativeApp>? = null,
    val stats: AppStats? = null
)

@Serializable
data class PermissionsInfo(
    val dangerous: List<String> = emptyList(),
    val total: Int = 0
)

@Serializable
data class TrackersInfo(
    val total: Int = 0,
    val list: List<String> = emptyList()
)

@Serializable
data class FlagsInfo(
    val isDebuggable: Boolean = false,
    val hasUnknownTrackers: Boolean = false
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
    val totalScans: Int = 0,
    val lastScanned: String? = null,
    val avgScoreFromCommunity: Int? = null
)

// ================== INSTALLED APPS ANALYSIS ===================

@Serializable
data class InstalledAppDto(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Int,
    val permissions: List<String> = emptyList()
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

// Extensions pour ScanResult
val ScanResult.appName: String
    get() = name

val ScanResult.privacyScore: Int
    get() = score

// ================== SEARCH & COMPARE ===================

@Serializable
data class SearchResponse(
    val results: List<AppDetails>
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

// ================== SCAN HISTORY ===================

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