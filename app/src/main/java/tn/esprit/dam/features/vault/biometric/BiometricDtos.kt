package tn.esprit.dam.features.vault.biometric

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DTOs for biometric authentication API endpoints.
// These map to the NestJS backend endpoints at /auth/biometric

// ==================== REQUEST DTOs ====================

// Request to register a new biometric device.
// POST /auth/biometric/register
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

// Request to get a challenge for biometric authentication.
// POST /auth/biometric/challenge
@Serializable
data class RequestChallengeRequest(
    val deviceId: String
)

// Request to verify a signed challenge and authenticate.
// POST /auth/biometric/verify
@Serializable
data class VerifyBiometricRequest(
    val deviceId: String,
    val challenge: String,
    val signature: String
)

// Request to revoke a biometric device.
// POST /auth/biometric/devices/:deviceId/revoke
@Serializable
data class RevokeDeviceRequest(
    val reason: String? = null
)

// ==================== RESPONSE DTOs ====================

// Response from device registration.
@Serializable
data class RegisterBiometricDeviceResponse(
    val message: String,
    val deviceId: String,
    val registeredAt: String
)

// Response containing a challenge to sign.
@Serializable
data class ChallengeResponse(
    val challenge: String,
    val expiresAt: String
)

// Response from successful biometric verification.
@Serializable
data class BiometricVerifyResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val user: BiometricUser
)

// User info returned after biometric auth.
@Serializable
data class BiometricUser(
    @SerialName("_id")
    val id: String? = null,
    val userId: String? = null,
    val email: String,
    val name: String? = null
)

// Information about a registered biometric device.
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

// Response containing list of user's devices.
@Serializable
data class DevicesListResponse(
    val devices: List<BiometricDeviceInfo>,
    val totalDevices: Int,
    val activeDevices: Int
)

// Response for biometric status check.
@Serializable
data class BiometricStatusResponse(
    val biometricEnabled: Boolean,
    val deviceCount: Int,
    val devices: List<BiometricDeviceInfo>? = null
)

// Generic success message response.
@Serializable
data class BiometricMessageResponse(
    val message: String
)

// Error response from biometric endpoints.
@Serializable
data class BiometricErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val statusCode: Int? = null
)
