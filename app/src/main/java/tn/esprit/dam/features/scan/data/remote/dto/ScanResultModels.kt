package tn.esprit.dam.features.scan.data.remote.dto

import tn.esprit.dam.features.scan.domain.model.AnalysisType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScanResultResponse(
    @SerialName("scanId")
    val scanId: String,
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
