package tn.esprit.dam.features.scan.data.remote.dto

import tn.esprit.dam.features.scan.domain.model.AnalysisType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
