package tn.esprit.dam.features.vault.biometric

import android.content.Context
import android.os.Build
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.Config
import tn.esprit.dam.data.TokenManager

// API service for biometric authentication endpoints.
// Handles communication with /auth/biometric routes on the NestJS backend.
class BiometricApiService private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "BiometricApiService"
        private const val BASE_URL = Config.ROOT_URL
        
        @Volatile
        private var INSTANCE: BiometricApiService? = null
        
        fun getInstance(context: Context): BiometricApiService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BiometricApiService(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }
    
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
            level = LogLevel.BODY
        }
        
        expectSuccess = false
        
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
        
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }
    
    // Register a new biometric device with the server.
    // Requires JWT authentication.
    suspend fun registerDevice(request: RegisterBiometricDeviceRequest): Result<RegisterBiometricDeviceResponse> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("Not authenticated. Please login first."))
            
            Log.d(TAG, "POST /auth/biometric/register")
            Log.d(TAG, "Device: ${request.deviceName} (${request.deviceId})")
            
            val response: HttpResponse = client.post("/auth/biometric/register") {
                bearerAuth(token)
                setBody(request)
            }
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val result = response.body<RegisterBiometricDeviceResponse>()
                    Log.d(TAG, "Device registered: ${result.message}")
                    Result.success(result)
                }
                HttpStatusCode.Conflict -> {
                    Log.w(TAG, "Device already registered")
                    Result.failure(Exception("Device already registered for biometric authentication"))
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e(TAG, "Unauthorized - token may be expired")
                    Result.failure(Exception("Session expired. Please login again."))
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "Registration failed: ${response.status} - $errorBody")
                    Result.failure(Exception("Registration failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during registration: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    // Request a challenge to sign for biometric authentication.
    // Does NOT require JWT - this is the first step of biometric login.
    suspend fun requestChallenge(deviceId: String): Result<ChallengeResponse> {
        return try {
            Log.d(TAG, "POST /auth/biometric/challenge")
            Log.d(TAG, "Device ID: $deviceId")
            
            val response: HttpResponse = client.post("/auth/biometric/challenge") {
                setBody(RequestChallengeRequest(deviceId))
            }
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val result = response.body<ChallengeResponse>()
                    Log.d(TAG, "Challenge received (expires: ${result.expiresAt})")
                    Result.success(result)
                }
                HttpStatusCode.NotFound -> {
                    Log.e(TAG, "Device not registered")
                    Result.failure(Exception("Device not registered. Please setup biometric authentication first."))
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "Challenge request failed: ${response.status} - $errorBody")
                    Result.failure(Exception("Failed to get challenge: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error requesting challenge: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    // Verify a signed challenge and get authentication tokens.
    // Does NOT require JWT - returns new tokens on success.
    suspend fun verifyBiometric(
        deviceId: String,
        challenge: String,
        signature: String
    ): Result<BiometricVerifyResponse> {
        return try {
            Log.d(TAG, "POST /auth/biometric/verify")
            Log.d(TAG, "Device ID: $deviceId")
            Log.d(TAG, "Signature length: ${signature.length}")
            
            val response: HttpResponse = client.post("/auth/biometric/verify") {
                setBody(VerifyBiometricRequest(
                    deviceId = deviceId,
                    challenge = challenge,
                    signature = signature
                ))
            }
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val result = response.body<BiometricVerifyResponse>()
                    Log.d(TAG, "Biometric authentication successful!")
                    Log.d(TAG, "User: ${result.user.email}")
                    
                    // Save the new tokens
                    TokenManager.saveTokens(
                        context,
                        result.accessToken,
                        result.refreshToken ?: ""
                    )
                    
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e(TAG, "Invalid signature or challenge expired")
                    Result.failure(Exception("Authentication failed. Please try again."))
                }
                HttpStatusCode.Forbidden -> {
                    Log.e(TAG, "Device is locked or revoked")
                    Result.failure(Exception("This device has been locked. Please use password login."))
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "Verification failed: ${response.status} - $errorBody")
                    Result.failure(Exception("Authentication failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error during verification: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    // Get list of user's registered biometric devices.
    // Requires JWT authentication.
    suspend fun getDevices(): Result<DevicesListResponse> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("Not authenticated"))
            
            Log.d(TAG, "GET /auth/biometric/devices")
            
            val response: HttpResponse = client.get("/auth/biometric/devices") {
                bearerAuth(token)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<DevicesListResponse>()
                    Log.d(TAG, "Retrieved ${result.totalDevices} devices")
                    Result.success(result)
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "Failed to get devices: ${response.status} - $errorBody")
                    Result.failure(Exception("Failed to get devices"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error getting devices: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    // Get biometric authentication status for the current user.
    // Requires JWT authentication.
    suspend fun getBiometricStatus(): Result<BiometricStatusResponse> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("Not authenticated"))
            
            Log.d(TAG, "GET /auth/biometric/status")
            
            val response: HttpResponse = client.get("/auth/biometric/status") {
                bearerAuth(token)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<BiometricStatusResponse>()
                    Log.d(TAG, "Biometric enabled: ${result.biometricEnabled}, devices: ${result.deviceCount}")
                    Result.success(result)
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "Failed to get status: ${response.status} - $errorBody")
                    Result.failure(Exception("Failed to get biometric status"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error getting status: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    // Revoke a biometric device (mark as inactive).
    // Requires JWT authentication.
    suspend fun revokeDevice(deviceId: String, reason: String? = null): Result<BiometricMessageResponse> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("Not authenticated"))
            
            Log.d(TAG, "POST /auth/biometric/devices/$deviceId/revoke")
            
            val response: HttpResponse = client.post("/auth/biometric/devices/$deviceId/revoke") {
                bearerAuth(token)
                setBody(RevokeDeviceRequest(reason))
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<BiometricMessageResponse>()
                    Log.d(TAG, "Device revoked: ${result.message}")
                    Result.success(result)
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "Failed to revoke device: ${response.status} - $errorBody")
                    Result.failure(Exception("Failed to revoke device"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error revoking device: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    // Permanently delete a biometric device.
    // Requires JWT authentication.
    suspend fun deleteDevice(deviceId: String): Result<BiometricMessageResponse> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("Not authenticated"))
            
            Log.d(TAG, "DELETE /auth/biometric/devices/$deviceId")
            
            val response: HttpResponse = client.delete("/auth/biometric/devices/$deviceId") {
                bearerAuth(token)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<BiometricMessageResponse>()
                    Log.d(TAG, "Device deleted: ${result.message}")
                    Result.success(result)
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "Failed to delete device: ${response.status} - $errorBody")
                    Result.failure(Exception("Failed to delete device"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error deleting device: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
    
    // Helper to build device registration request with system info.
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
