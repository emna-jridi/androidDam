package tn.esprit.dam.features.scan.data.remote.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AppResult(
    @SerialName("packageName")
    val packageName: String? = null,
    // /scan/latest sends "name" and "score"
    @JsonNames("appName", "name")
    val appName: String? = null,
    @JsonNames("finalScore", "score")
    val finalScore: Float? = null,
    @SerialName("aiRiskScore")
    val aiRiskScore: Float? = null,
    @SerialName("riskLevel")
    val riskLevel: String? = null,
    @SerialName("aiRiskLevel")
    val aiRiskLevel: String? = null,
    @SerialName("lastScanned")
    val lastScanned: String? = null
)

/** GET /scan/latest — status is "NONE" (and results empty) when the user has no scans. */
@Serializable
data class LatestScanResponse(
    @SerialName("scanId")
    val scanId: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("overallScore")
    val overallScore: Float? = null,
    @SerialName("globalRisk")
    val globalRisk: String? = null,
    @SerialName("totalApps")
    val totalApps: Int = 0,
    @SerialName("riskyAppsCount")
    val riskyAppsCount: Int = 0,
    @SerialName("scanDate")
    val scanDate: String? = null,
    @SerialName("results")
    val results: List<AppResult> = emptyList()
)
