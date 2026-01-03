package tn.esprit.dam.data

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.model.ApiError
import tn.esprit.dam.data.model.GoogleLoginRequest
import tn.esprit.dam.data.model.LoginRequest
import tn.esprit.dam.data.model.LoginResponse
import tn.esprit.dam.data.model.RegisterRequest
import tn.esprit.dam.data.model.RegisterResponse
import tn.esprit.dam.data.model.RequestPasswordResetRequest
import tn.esprit.dam.data.model.RequestPasswordResetResponse
import tn.esprit.dam.data.model.ResendOTPRequest
import tn.esprit.dam.data.model.ResendOTPResponse
import tn.esprit.dam.data.model.ResetPasswordResponse
import tn.esprit.dam.data.model.UpdateUserRequest
import tn.esprit.dam.data.model.User
import tn.esprit.dam.data.model.VerifyEmailMessageResponse
import tn.esprit.dam.data.model.VerifyPasswordResetOTPRequest
import tn.esprit.dam.data.model.VerifyPasswordResetOTPResponse

class ApiClient private constructor(private val context: Context) {
    companion object {
        private const val BASE_URL = Config.ROOT_URL
        private const val TAG = "ApiClient"
        @Volatile
        private var INSTANCE: ApiClient? = null

        // STAR Method getInstance
        fun getInstance(context: Context): ApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiClient(context.applicationContext).also {
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
        prettyPrint = true
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("KtorClient", message)
                }
            }
            level = LogLevel.ALL
        }

        install(Auth) {
            bearer {
                loadTokens {
                    runBlocking {
                        TokenManager.getAccessToken(context)?.let {
                            BearerTokens(it, "")
                        }
                    }
                }

                refreshTokens {
                    Log.d(TAG, "[REFRESH] Token refresh triggered")
                    val refreshToken = runBlocking {
                        TokenManager.getRefreshToken(context)
                    }

                    if (refreshToken != null) {
                        try {
                            Log.d(TAG, "📧 Attempting to refresh token...")
                            val refreshResponse = runBlocking {
                                client.post("/auth/refresh") {
                                    setBody(mapOf("refreshToken" to refreshToken))
                                }.body<LoginResponse>()
                            }
                            Log.d(TAG, "âœ… Token refreshed successfully")
                            runBlocking {
                                TokenManager.saveTokens(
                                    context,
                                    refreshResponse.accessToken,
                                    refreshResponse.refreshToken
                                )
                            }
                            BearerTokens(refreshResponse.accessToken, "")
                        } catch (e: Exception) {
                            Log.e(TAG, "âŒ Token refresh failed: ${e.message}")
                            runBlocking {
                                TokenManager.clearAll(context)
                            }
                            null
                        }
                    } else {
                        Log.w(TAG, "[WARN] No refresh token available")
                        null
                    }
                }
            }
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

    // ================== EXCEPTIONS ==================

    class AuthException(message: String) : Exception(message)
    class ApiException(message: String, val statusCode: HttpStatusCode) : Exception(message)

    // ================== AUTH ==================

    suspend fun register(email: String, password: String, name: String): RegisterResponse {
        return try {
            Log.d(TAG, "🔤 POST /auth/register - Email: $email")

            val response: HttpResponse = client.post("/auth/register") {
                setBody(RegisterRequest(email, password, name))
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val registerResponse = response.body<RegisterResponse>()
                    Log.d(TAG, "âœ… Registration successful:")
                    registerResponse
                }
                HttpStatusCode.Conflict -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "âŒ Conflict: ${error.message}")
                    throw AuthException(error.message ?: "Email already exists")
                }
                else -> {
                    val rawBody = response.bodyAsText()
                    Log.e(TAG, "âŒ Error ${response.status}: $rawBody")
                    throw ApiException("Registration failed", response.status)
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Registration error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    suspend fun verifyEmail(email: String , otp: String): String {
        val response = client.post("auth/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "otp" to otp))
        }

        if (response.status.value in 200..299) {
            val body = response.body<VerifyEmailMessageResponse>()
            return body.message
        } else {
            throw ApiException("Verification failed: ${response.body<String>()}", response.status)
        }
    }

    suspend fun resendVerificationOTP(email: String): ResendOTPResponse {
        return try {
            Log.d(TAG, "🔤 POST /auth/resend-otp - Email: $email")

            val response: HttpResponse = client.post("/auth/resend-otp") {
                setBody(ResendOTPRequest(email))
            }

            when (response.status) {
                HttpStatusCode.OK ,  HttpStatusCode.Created -> {
                    val result = response.body<ResendOTPResponse>()
                    Log.d(TAG, "âœ… OTP resent successfully")
                    result
                }
                else -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "âŒ Resend failed: ${error.message}")
                    throw AuthException(error.message ?: "Failed to resend OTP")
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Resend OTP error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }


    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val cleanEmail = email.trim().lowercase()
            Log.d(TAG, "🔤 POST /auth/login - Email: $cleanEmail")

            val response: HttpResponse = client.post("/auth/login") {
                setBody(LoginRequest(cleanEmail, password))
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val loginResponse = response.body<LoginResponse>()
                    Log.d(TAG, "âœ… Login successful")

                    // Sauvegarder tokens et user
                    TokenManager.saveTokens(
                        context,
                        loginResponse.accessToken,
                        loginResponse.refreshToken
                    )
                    TokenManager.saveUser(context, loginResponse.user)

                    loginResponse
                }
                HttpStatusCode.Unauthorized -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "âŒ Unauthorized: ${error.message}")
                    throw AuthException(error.message ?: "Invalid email or password")
                }
                else -> {
                    val rawBody = response.bodyAsText()
                    Log.e(TAG, "âŒ Error ${response.status}: $rawBody")
                    throw ApiException("Login failed", response.status)
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Login error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    suspend fun requestPasswordReset(email: String): RequestPasswordResetResponse {
        return try {
            Log.d(TAG, "🔤 POST /auth/request-password-reset - Email: $email")

            val response: HttpResponse = client.post("/auth/request-password-reset") {
                setBody(RequestPasswordResetRequest(email))
            }

            when (response.status) {
                HttpStatusCode.OK , HttpStatusCode.Created  -> {
                    val result = response.body<RequestPasswordResetResponse>()
                    Log.d(TAG, "âœ… Reset code sent")
                    result
                }
                else -> {
                    Log.d(TAG, "âš ï¸ Response ${response.status}, returning generic message")
                    RequestPasswordResetResponse(
                        message = "If this email exists, a reset code has been sent."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Request reset error: ${e.message}", e)
            RequestPasswordResetResponse(
                message = "If this email exists, a reset code has been sent."
            )
        }
    }


    suspend fun verifyPasswordResetOTP(email: String, otp: String): VerifyPasswordResetOTPResponse {
        return try {
            Log.d(TAG, "🔤 POST /auth/verify-reset-otp - Email: $email, OTP: $otp")

            val response: HttpResponse = client.post("/auth/verify-reset-code") {
                setBody(VerifyPasswordResetOTPRequest(email, otp))
            }

            when (response.status) {
                HttpStatusCode.OK , HttpStatusCode.Accepted , HttpStatusCode.Created-> {
                    val result = response.body<VerifyPasswordResetOTPResponse>()
                    Log.d(TAG, "âœ… Reset OTP verified")
                    result
                }
                else -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "âŒ Verification failed: ${error.message}")
                    throw AuthException(error.message ?: "Invalid reset code")
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Verify reset OTP error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }


    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): ResetPasswordResponse {
        return try {
            Log.d(TAG, "🔤 POST /auth/reset-password")

            // Build request body according to backend DTO
            val requestBody = mapOf(
                "email" to email,
                "code" to code,
                "newPassword" to newPassword
            )

            val response: HttpResponse = client.post("/auth/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            when (response.status) {
                HttpStatusCode.OK ,  HttpStatusCode.Created-> {
                    val result = response.body<ResetPasswordResponse>()
                    Log.d(TAG, "âœ… Password reset successful")
                    result
                }
                else -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "âŒ Reset failed: ${error.message}")
                    throw AuthException(error.message ?: "Invalid email or code")
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Reset password error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }


    suspend fun googleLogin(idToken: String): LoginResponse {
        return try {
            Log.d(TAG, "🔤 POST /auth/google")

            val response: HttpResponse = client.post("/auth/google") {
                setBody(GoogleLoginRequest(idToken))
            }

            when (response.status) {
                HttpStatusCode.OK ,    HttpStatusCode.Created-> {
                    val loginResponse = response.body<LoginResponse>()
                    Log.d(TAG, "âœ… Google login successful")

                    // Sauvegarder tokens et user
                    TokenManager.saveTokens(
                        context,
                        loginResponse.accessToken,
                        loginResponse.refreshToken
                    )
                    TokenManager.saveUser(context, loginResponse.user)

                    loginResponse
                }
                else -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "âŒ Google login failed: ${error.message}")
                    throw AuthException(error.message ?: "Google login failed")
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Google login error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    suspend fun logout() {
        Log.d(TAG, "🚪 Logging out...")
        TokenManager.clearAll(context)
        Log.d(TAG, "✅ Logged out successfully")
    }



    suspend fun isLoggedIn(): Boolean {
        return TokenManager.isLoggedIn(context)
    }

    // ================== USER PROFILE ==================

    /**
     * 👤 Get user profile
     */
    suspend fun getUserProfile(): User {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: throw AuthException("No access token")

            Log.d(TAG, "🔤 GET /users/profile")

            val response: HttpResponse = client.get("/users/profile") {
                bearerAuth(token)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val user = response.body<User>()
                    Log.d(TAG, "âœ… Profile retrieved: ${user.name}")
                    TokenManager.saveUser(context, user) // Mettre Ã  jour le cache
                    user
                }
                HttpStatusCode.Unauthorized -> {
                    TokenManager.clearAll(context)
                    throw AuthException("Session expired")
                }
                else -> {
                    val error = response.body<ApiError>()
                    throw ApiException(error.message ?: "Failed to get profile", response.status)
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Get profile error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }


    suspend fun updateUserProfile(request: UpdateUserRequest): User {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: throw AuthException("No access token")

            Log.d(TAG, "🔤 PATCH /users/me - Request: $request")

            val response: HttpResponse = client.patch("/users/me") {
                bearerAuth(token)
                setBody(request)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val user = response.body<User>()
                    Log.d(TAG, "âœ… Profile updated: ${user.name}")
                    TokenManager.saveUser(context, user)
                    user
                }
                HttpStatusCode.Unauthorized -> {
                    TokenManager.clearAll(context)
                    throw AuthException("Session expired")
                }
                else -> {
                    val error = response.body<ApiError>()
                    throw ApiException(error.message ?: "Failed to update profile", response.status)
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "âŒ Update profile error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }
    suspend fun getCurrentUser(): User? {
        return TokenManager.getUser(context)
    }

    suspend fun getAlertHistory(): List<tn.esprit.dam.data.model.Alert> {
        return try {
            val token = TokenManager.getAccessToken(context) ?: return emptyList()

            Log.d(TAG, "📤 GET /alerts (Token: ${token.take(10)}...)")

            val response: HttpResponse = client.get("/alerts") {
                bearerAuth(token)
            }

            if (response.status == HttpStatusCode.OK) {
                val alerts = response.body<List<tn.esprit.dam.data.model.Alert>>()
                Log.d(TAG, "✅ Loaded ${alerts.size} alerts")
                alerts
            } else {
                Log.e(TAG, "❌ Failed to load alerts: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading alerts", e)
            emptyList()
        }
    }
    suspend fun getAppSafetyReport(packageName: String): tn.esprit.dam.data.model.AppSafetyReport? {
        return try {
            // ⚠️ REPLACE WITH YOUR PC'S LOCAL IP (e.g., 192.168.1.5)
            // Do not use "localhost" because that refers to the phone itself!
            val response: HttpResponse = client.get("http://172.18.1.18:3000/report/$packageName")

            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else {
                Log.e(TAG, "❌ Server error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Network error fetching report", e)
            null
        }
    }
    suspend fun sendAlertEvent(packageName: String, event: String, details: Map<String, String>) {
        try {
            val token = TokenManager.getAccessToken(context) ?: return
            Log.d(TAG, "📤 POST /alerts/event")

            val alertDto = tn.esprit.dam.data.model.AlertEventDto(
                packageName = packageName,
                event = event,
                timestamp = System.currentTimeMillis(),
                details = details
            )

            val response = client.post("/alerts/event") {
                bearerAuth(token)
                setBody(alertDto)
            }

            if (response.status == HttpStatusCode.Created) {
                Log.d(TAG, "✅ Alert event sent successfully")
            } else {
                Log.e(TAG, "❌ Failed to send alert event: ${response.status}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sending alert event", e)
        }
    }
}
