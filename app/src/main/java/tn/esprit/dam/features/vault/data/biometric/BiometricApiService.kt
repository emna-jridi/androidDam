package tn.esprit.dam.features.vault.data.biometric

import android.content.Context
import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import dagger.hilt.android.qualifiers.ApplicationContext
import tn.esprit.dam.core.network.ApiException
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.invalidateBearerTokens
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.core.network.toResult
import tn.esprit.dam.core.session.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricApiService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: HttpClient,
    private val tokenRepository: TokenRepository
) {
    suspend fun registerDevice(request: RegisterBiometricDeviceRequest): Result<RegisterBiometricDeviceResponse> =
        safeApiCall<RegisterBiometricDeviceResponse> {
            client.post("/api/auth/biometric/register") { setBody(request) }
        }.toResultWithMessages("Registration failed") { code ->
            when (code) {
                409 -> "Device already registered for biometric authentication"
                401 -> "Session expired. Please login again."
                else -> null
            }
        }

    suspend fun requestChallenge(deviceId: String): Result<ChallengeResponse> =
        safeApiCall<ChallengeResponse> {
            client.post("/api/auth/biometric/challenge") { setBody(RequestChallengeRequest(deviceId)) }
        }.toResultWithMessages("Failed to get challenge") { code ->
            if (code == 404) "Device not registered. Please setup biometric authentication first." else null
        }

    suspend fun verifyBiometric(
        deviceId: String,
        challenge: String,
        signature: String
    ): Result<BiometricVerifyResponse> {
        val result = safeApiCall<BiometricVerifyResponse> {
            client.post("/api/auth/biometric/verify") {
                setBody(VerifyBiometricRequest(deviceId = deviceId, challenge = challenge, signature = signature))
            }
        }
        if (result is ApiResult.Success) {
            tokenRepository.saveTokens(result.data.accessToken, result.data.refreshToken ?: "")
            client.invalidateBearerTokens()
        }
        return result.toResultWithMessages("Authentication failed") { code ->
            when (code) {
                401 -> "Authentication failed. Please try again."
                403 -> "This device has been locked. Please use password login."
                else -> null
            }
        }
    }

    suspend fun getDevices(): Result<DevicesListResponse> =
        safeApiCall<DevicesListResponse> { client.get("/api/auth/biometric/devices") }
            .toResultWithMessages("Failed to get devices")

    suspend fun getBiometricStatus(): Result<BiometricStatusResponse> =
        safeApiCall<BiometricStatusResponse> { client.get("/api/auth/biometric/status") }
            .toResultWithMessages("Failed to get biometric status")

    suspend fun revokeDevice(deviceId: String, reason: String? = null): Result<BiometricMessageResponse> =
        safeApiCall<BiometricMessageResponse> {
            client.post("/api/auth/biometric/devices/$deviceId/revoke") { setBody(RevokeDeviceRequest(reason)) }
        }.toResultWithMessages("Failed to revoke device")

    suspend fun deleteDevice(deviceId: String): Result<BiometricMessageResponse> =
        safeApiCall<BiometricMessageResponse> { client.delete("/api/auth/biometric/devices/$deviceId") }
            .toResultWithMessages("Failed to delete device")

    /** Maps HTTP errors to user-facing messages; network errors keep the "Network error" prefix. */
    private inline fun <T> ApiResult<T>.toResultWithMessages(
        fallback: String,
        messageFor: (Int) -> String? = { null }
    ): Result<T> = when (this) {
        is ApiResult.ApiError -> Result.failure(ApiException(messageFor(code) ?: "$fallback: HTTP $code", code))
        is ApiResult.NetworkError -> Result.failure(ApiException("Network error: ${exception.message}", cause = exception))
        else -> toResult()
    }

    fun buildRegistrationRequest(
        deviceId: String,
        publicKeyPem: String,
        deviceName: String
    ): RegisterBiometricDeviceRequest {
        return RegisterBiometricDeviceRequest(
            deviceId = deviceId,
            publicKey = publicKeyPem,
            keyType = "EC",
            deviceName = deviceName,
            platform = "android",
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = getAppVersion()
        )
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
