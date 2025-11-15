package tn.esprit.dam.data

import android.content.Context
import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class ApiClient private constructor(private val context: Context) {

    companion object {
        private const val BASE_URL = "http://192.168.1.115:3000"
        private const val TAG = "ApiClient"

        @Volatile
        private var INSTANCE: ApiClient? = null

        // ⭐ Méthode getInstance
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
                    val refreshToken = runBlocking {
                        TokenManager.getRefreshToken(context)
                    }

                    if (refreshToken != null) {
                        // TODO: Appeler endpoint /auth/refresh
                        null
                    } else {
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
            Log.d(TAG, "📤 POST /auth/register - Email: $email")

            val response: HttpResponse = client.post("/auth/register") {
                setBody(RegisterRequest(email, password, name))
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val registerResponse = response.body<RegisterResponse>()
                    Log.d(TAG, "✅ Registration successful:")
                    registerResponse
                }
                HttpStatusCode.Conflict -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "❌ Conflict: ${error.message}")
                    throw AuthException(error.message ?: "Email already exists")
                }
                else -> {
                    val rawBody = response.bodyAsText()
                    Log.e(TAG, "❌ Error ${response.status}: $rawBody")
                    throw ApiException("Registration failed", response.status)
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Registration error: ${e.message}", e)
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

    /**
     * 🔄 Resend OTP
     */
    suspend fun resendVerificationOTP(email: String): ResendOTPResponse {
        return try {
            Log.d(TAG, "📤 POST /auth/resend-otp - Email: $email")

            val response: HttpResponse = client.post("/auth/resend-otp") {
                setBody(ResendOTPRequest(email))
            }

            when (response.status) {
                HttpStatusCode.OK ,  HttpStatusCode.Created -> {
                    val result = response.body<ResendOTPResponse>()
                    Log.d(TAG, "✅ OTP resent successfully")
                    result
                }
                else -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "❌ Resend failed: ${error.message}")
                    throw AuthException(error.message ?: "Failed to resend OTP")
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Resend OTP error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    /**
     * 🔐 Login
     */
    suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val cleanEmail = email.trim().lowercase()
            Log.d(TAG, "📤 POST /auth/login - Email: $cleanEmail")

            val response: HttpResponse = client.post("/auth/login") {
                setBody(LoginRequest(cleanEmail, password))
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val loginResponse = response.body<LoginResponse>()
                    Log.d(TAG, "✅ Login successful")

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
                    Log.e(TAG, "❌ Unauthorized: ${error.message}")
                    throw AuthException(error.message ?: "Invalid email or password")
                }
                else -> {
                    val rawBody = response.bodyAsText()
                    Log.e(TAG, "❌ Error ${response.status}: $rawBody")
                    throw ApiException("Login failed", response.status)
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Login error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    /**
     * 🔑 Request Password Reset
     */
    suspend fun requestPasswordReset(email: String): RequestPasswordResetResponse {
        return try {
            Log.d(TAG, "📤 POST /auth/request-password-reset - Email: $email")

            val response: HttpResponse = client.post("/auth/request-password-reset") {
                setBody(RequestPasswordResetRequest(email))
            }

            when (response.status) {
                HttpStatusCode.OK , HttpStatusCode.Created  -> {
                    val result = response.body<RequestPasswordResetResponse>()
                    Log.d(TAG, "✅ Reset code sent")
                    result
                }
                else -> {
                    Log.d(TAG, "⚠️ Response ${response.status}, returning generic message")
                    RequestPasswordResetResponse(
                        message = "If this email exists, a reset code has been sent."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Request reset error: ${e.message}", e)
            RequestPasswordResetResponse(
                message = "If this email exists, a reset code has been sent."
            )
        }
    }

    /**
     * ✅ Verify Password Reset OTP
     */
    suspend fun verifyPasswordResetOTP(email: String, otp: String): VerifyPasswordResetOTPResponse {
        return try {
            Log.d(TAG, "📤 POST /auth/verify-reset-otp - Email: $email, OTP: $otp")

            val response: HttpResponse = client.post("/auth/verify-reset-code") {
                setBody(VerifyPasswordResetOTPRequest(email, otp))
            }

            when (response.status) {
                HttpStatusCode.OK , HttpStatusCode.Accepted , HttpStatusCode.Created-> {
                    val result = response.body<VerifyPasswordResetOTPResponse>()
                    Log.d(TAG, "✅ Reset OTP verified")
                    result
                }
                else -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "❌ Verification failed: ${error.message}")
                    throw AuthException(error.message ?: "Invalid reset code")
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Verify reset OTP error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    /**
     *  Reset Password
     */
    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): ResetPasswordResponse {
        return try {
            Log.d(TAG, "📤 POST /auth/reset-password")

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
                    Log.d(TAG, "✅ Password reset successful")
                    result
                }
                else -> {
                    val error = response.body<ApiError>()
                    Log.e(TAG, "❌ Reset failed: ${error.message}")
                    throw AuthException(error.message ?: "Invalid email or code")
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Reset password error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    /**
     * 🔐 Google Login
     */
    suspend fun googleLogin(idToken: String): LoginResponse {
        return try {
            Log.d(TAG, "📤 POST /auth/google")

            val response: HttpResponse = client.post("/auth/google") {
                setBody(GoogleLoginRequest(idToken))
            }

            when (response.status) {
                HttpStatusCode.OK ,    HttpStatusCode.Created-> {
                    val loginResponse = response.body<LoginResponse>()
                    Log.d(TAG, "✅ Google login successful")

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
                    Log.e(TAG, "❌ Google login failed: ${error.message}")
                    throw AuthException(error.message ?: "Google login failed")
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Google login error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    /**
     * 🚪 Logout
     */
    suspend fun logout() {
        Log.d(TAG, "🚪 Logging out...")
        TokenManager.clearAll(context)
        Log.d(TAG, "✅ Logged out successfully")
    }

    /**
     * 👤 Get Profile
     */
    suspend fun getProfile(): User {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: throw AuthException("No access token. Please login again.")

            Log.d(TAG, "📤 GET /users/profile")

            val response: HttpResponse = client.get("/users/profile") {
                bearerAuth(token)
            }

            when (response.status) {
                HttpStatusCode.OK ,  HttpStatusCode.Created  -> {
                    val user = response.body<User>()
                    Log.d(TAG, "✅ Profile retrieved")
                    TokenManager.saveUser(context, user)
                    user
                }
                HttpStatusCode.Unauthorized -> {
                    Log.e(TAG, "❌ Unauthorized - Token expired")
                    TokenManager.clearAll(context)
                    throw AuthException("Session expired. Please login again.")
                }
                else -> {
                    val rawBody = response.bodyAsText()
                    Log.e(TAG, "❌ Error ${response.status}: $rawBody")
                    throw ApiException("Failed to get profile", response.status)
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get profile error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    /**
     * ✏️ Update Profile
     */
    suspend fun updateProfile(name: String? = null, email: String? = null): User {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: throw AuthException("No access token")

            Log.d(TAG, "📤 PATCH /users/me")

            val response: HttpResponse = client.patch("/users/me") {
                bearerAuth(token)
                setBody(UpdateUserRequest(name = name, email = email))
            }

            when (response.status) {
                HttpStatusCode.OK ,  HttpStatusCode.Created -> {
                    val user = response.body<User>()
                    Log.d(TAG, "✅ Profile updated")
                    TokenManager.saveUser(context, user)
                    user
                }
                HttpStatusCode.Unauthorized -> {
                    TokenManager.clearAll(context)
                    throw AuthException("Session expired")
                }
                else -> {
                    throw ApiException("Failed to update profile", response.status)
                }
            }
        } catch (e: AuthException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "❌ Update profile error: ${e.message}", e)
            throw AuthException("Network error: ${e.message}")
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return TokenManager.isLoggedIn(context)
    }

    /**
     * 👤 Get current user from cache
     */
    suspend fun getCurrentUser(): User? {
        return TokenManager.getUser(context)
    }
}