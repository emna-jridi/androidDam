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
    /** Play Store rating (0–5), null when the store doesn't give one. */
    @SerialName("rating")
    val rating: Float? = null,
    @SerialName("developer")
    val developer: String? = null,
    /** Search results are not analyzed yet: the backend sends "UNKNOWN". */
    @SerialName("riskLevel")
    val riskLevel: String? = null
)
