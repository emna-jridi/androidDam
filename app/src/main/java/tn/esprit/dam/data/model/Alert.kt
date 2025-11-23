package tn.esprit.dam.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Alert(
    @SerialName("_id") val id: String,
    val packageName: String,
    val event: String,     // e.g., "Camera Accessed"
    val severity: String,  // "critical", "high", "info"
    val timestamp: Long,
    val notified: Boolean
)