package tn.esprit.dam.features.alert.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AlertEventDto(
    val packageName: String,
    val event: String,
    val timestamp: Long,
    val details: Map<String, String>
)

@Serializable
data class DeviceTokenDto(
    val token: String,
    val platform: String
)
