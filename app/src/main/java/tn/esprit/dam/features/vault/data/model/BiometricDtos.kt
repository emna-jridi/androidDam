package tn.esprit.dam.features.vault.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class RegisterBiometricDeviceRequest(
    val deviceId: String,
    val publicKey: String,
    val keyType: String = "EC",
    val deviceName: String,
    val platform: String = "android",
    val osVersion: String? = null,
    val appVersion: String? = null
)

@Serializable
data class RequestChallengeRequest(
    val deviceId: String
)

@Serializable
data class VerifyBiometricRequest(
    val deviceId: String,
    val challenge: String,
    val signature: String
)

@Serializable
data class RevokeDeviceRequest(
    val reason: String? = null
)

@Serializable
data class RegisterBiometricDeviceResponse(
    val success: Boolean = true,
    val message: String,
    val deviceId: String
)

@Serializable
data class ChallengeResponse(
    val challenge: String,
    val expiresAt: String
)

@Serializable
data class BiometricVerifyResponse(
    val success: Boolean = true,
    val message: String? = null,
    val accessToken: String,
    val refreshToken: String? = null,
    val user: BiometricUser
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BiometricUser(
    @JsonNames("id", "_id")
    val id: String? = null,
    val userId: String? = null,
    val email: String,
    val name: String? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BiometricDeviceInfo(
    @JsonNames("id", "_id")
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val platform: String,
    val osVersion: String? = null,
    val isActive: Boolean,
    val lastUsedAt: String? = null,
    @JsonNames("registeredAt", "createdAt")
    val registeredAt: String? = null,
    val authCount: Int = 0,
    val revokedAt: String? = null,
    val revokedReason: String? = null
)

/** Built on the client: GET /auth/biometric/devices returns a plain array. */
data class DevicesListResponse(
    val devices: List<BiometricDeviceInfo>,
    val totalDevices: Int,
    val activeDevices: Int
)

@Serializable
data class BiometricStatusResponse(
    val biometricEnabled: Boolean,
    val deviceCount: Int,
    val devices: List<BiometricDeviceInfo>? = null
)

@Serializable
data class BiometricMessageResponse(
    val message: String
)

