package tn.esprit.dam.features.scan.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchAppResponse(
    @SerialName("results")
    val results: List<SearchResultDto> = emptyList()
)

@Serializable
data class SearchResultDto(
    @SerialName("packageName")
    val packageName: String,
    @SerialName("name")
    val appName: String,
    @SerialName("iconUrl")
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
