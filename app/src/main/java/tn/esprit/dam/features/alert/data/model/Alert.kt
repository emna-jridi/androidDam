package tn.esprit.dam.features.alert.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Alert(
    @SerialName("_id") val id: String,
    val packageName: String,
    val event: String,
    val severity: String,
    val timestamp: String,
    val notified: Boolean,
    val read: Boolean = false
)
