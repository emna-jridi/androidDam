package tn.esprit.dam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenDto(
    val token: String,
    val platform: String
)

@Serializable
data class AlertEventDto(
    val packageName: String,
    val event: String,
    val timestamp: Long,
    val details: Map<String, String>
)
