package tn.esprit.dam.features.vault.data.biometric

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val message: String,
    val deviceId: String,
    val registeredAt: String
)

@Serializable
data class ChallengeResponse(
    val challenge: String,
    val expiresAt: String
)

@Serializable
data class BiometricVerifyResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val user: BiometricUser
)

@Serializable
data class BiometricUser(
    @SerialName("_id")
    val id: String? = null,
    val userId: String? = null,
    val email: String,
    val name: String? = null
)

@Serializable
data class BiometricDeviceInfo(
    @SerialName("_id")
    val id: String,
    val deviceId: String,
    val deviceName: String,
    val platform: String,
    val isActive: Boolean,
    val lastUsedAt: String? = null,
    val registeredAt: String,
    val authCount: Int = 0,
    val revokedAt: String? = null,
    val revokedReason: String? = null
)

@Serializable
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

