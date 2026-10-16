package tn.esprit.dam.features.scan.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val averageScore: Float? = null,
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
    val duration: Long? = null,
    @SerialName("packageName")
    val packageName: String? = null,
    @SerialName("appName")
    val appName: String? = null,
    @SerialName("globalRisk")
    val globalRisk: String? = null,
    @SerialName("overallScore")
    val overallScore: Float? = null
)
