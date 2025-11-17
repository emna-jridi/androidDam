// data/Models.kt

package tn.esprit.dam.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

// ================== USER ===================



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
    val avatarUrl: String? = null
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
    override val descriptor = PrimitiveSerialDescriptor(
        "FlexibleMessage",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: String?) {
        value?.let { encoder.encodeString(it) } ?: encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): String? {
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
    val name: String,
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
data class ScanSummaryResponse(
    val avgScore: Double? = null,
    val riskDistribution: RiskDistribution? = null,
    val totalAlerts: Int? = null,
    val mostDangerousApps: List<DangerousApp>? = null
)



@Serializable
data class DangerousApp(
    val packageName: String,
    val name: String,
    val score: Int
)


@Serializable
data class BreakdownInfo(
    val permissions: PermissionBreakdown? = null,
    val trackers: TrackerBreakdown? = null,
    val debug: DebugBreakdown? = null,
    val community: CommunityBreakdown? = null,
    val unknownTrackers: UnknownTrackersBreakdown? = null
)

@Serializable
data class PermissionBreakdown(
    val penalty: Int = 0,
    val count: Int = 0,
    val list: List<String> = emptyList()
)
@Serializable
data class TrackerBreakdown(
    val penalty: Int = 0,
    val count: Int = 0
)
@Serializable
data class DebugBreakdown(
    val penalty: Int = 0,
    val isDebuggable: Boolean = false
)

@Serializable
data class CommunityBreakdown(
    val bonus: Int = 0,
    val score: Double? = null
)

@Serializable
data class UnknownTrackersBreakdown(
    val penalty: Int = 0,
    val hasUnknown: Boolean = false
)

// ================== SCAN STORAGE ===================

@Serializable
data class SavedScan(
    val _id: String? = null,
    val userHash: String,
    val scanDate: String,
    val totalApps: Int,
    val results: List<AppAnalysisResult>,
    val summary: ScanSummary? = null,
    val scanId: String,
    val createdAt: String? = null
)

@Serializable
data class SaveScanRequest(
    val userHash: String,
    val scanId: String,
    val totalApps: Int,
    val results: List<AppAnalysisResult>,
    val summary:ScanSummary? = null
)

@Serializable
data class SaveScanResponse(
    val success: Boolean,
    val scan: SavedScan,
    val message: String? = null
)

@Serializable
data class GetScansResponse(
    val success: Boolean,
    val data: ScanDataWrapper
)
@Serializable
data class ScanDataWrapper(
    val scans: List<SavedScan>,
    val pagination: Pagination,
    val stats: ScanStats
)
@Serializable
data class Pagination(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
)

@Serializable
data class ScanStats(
    val totalScans: Int,
    val avgAppsPerScan: Int,
    val avgScore: Int,
    val totalAppsScanned: Int
)
@Serializable
data class ScanResultResponse(
    val scanId: String,
    val userHash: String? = null,
    val totalApps: Int,
    val results: List<AppAnalysisResult>,
    val summary: ScanSummary,
    val createdAt: String? = null
)
@Serializable
data class AppAnalysisResult(
    val packageName: String,
    val name: String,
    val version: String? = null,
    val score: Int,
    val riskLevel: String,
    val alerts: List<String> = emptyList(),
    val breakdown: BreakdownInfo? = null,
    val trackers: List<String> = emptyList(),
    val permissions: PermissionDetails,
    val error: String? = null
)
@Serializable
data class PermissionDetails(
    val dangerous: List<String> = emptyList(),
    val total: Int = 0
)
@Serializable
data class TrackerInfo(
    val _id: String? = null,  // ✅ Ajouter l'ID MongoDB si nécessaire
    val name: String? = null,
    val company: String? = null,
    val category: String? = null,
    val websiteUrl: String? = null,
    val networkSignature: String? = null,
    val codeSignature: String? = null,
    val description: String? = null
)

@Serializable
data class ScanSummary(
    val avgScore: Int = 0,
    val riskDistribution: RiskDistribution = RiskDistribution(),
    val totalAlerts: Int = 0,
    val totalTrackers: Int = 0,
    val mostDangerousApps: List<DangerousApp> = emptyList()
)
@Serializable
data class RiskDistribution(
    val critical: Int = 0,
    val high: Int = 0,
    val medium: Int = 0,
    val low: Int = 0
)
@Serializable
data class AddPackageMappingDto(
    val packageName: String,
    val trackers: List<String>
)
@Serializable
data class ExodusStatsResponse(
    val cacheSize: Int,
    val mappingsSize: Int
)
@Serializable
data class MetadataDto(
    val packageName: String,
    val permissions: List<String> = emptyList(),
    val isDebuggable: Boolean = false
)
@Serializable
data class ScanItem(
    val _id: String,
    val type: String,
    val userHash: String,
    val totalApps: Int,
    val report: ScanReport,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
@Serializable
data class LatestScanResponse(
    val success: Boolean,
    val data: ScanItem? = null,
    val message: String? = null
)
@Serializable
data class SingleScanResponse(
    val success: Boolean,
    val data: ScanItem
)
@Serializable
data class ScanReport(
    val results: List<AppAnalysisResult> = emptyList()
)

/**
 * ✅ Convertir ScanItem en SavedScan
 */
fun ScanItem.toSavedScan(): SavedScan {
    return SavedScan(
        _id = this._id,
        userHash = this.userHash,
        scanDate = this.createdAt ?: "",
        totalApps = this.totalApps,
        results = this.report.results,
        summary = null, // Calculer si nécessaire
        scanId = this._id,
        createdAt = this.createdAt
    )
}

/**
 * ✅ Convertir SavedScan en ScanItem
 */
fun SavedScan.toScanItem(): ScanItem {
    return ScanItem(
        _id = this._id ?: this.scanId,
        type = "batch_installed",
        userHash = this.userHash,
        totalApps = this.totalApps,
        report = ScanReport(results = this.results),
        createdAt = this.createdAt,
        updatedAt = null
    )
}