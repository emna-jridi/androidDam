package tn.esprit.dam.features.scan.data.remote.dto

import tn.esprit.dam.features.scan.domain.model.AnalysisType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("timestamp")
    val timestamp: String,
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
    @SerialName("analysisType")
    val analysisType: AnalysisType? = null
)
