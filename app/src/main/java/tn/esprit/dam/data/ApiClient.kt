package tn.esprit.dam.data

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android  // ← Your built-in engine (no extra dependency!)
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ApiClient {
    // FIXED: No space! Clean URL
    private const val BASE_URL = "http://192.168.1.9:3000"

    // GLOBAL safe Json – perfect
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    // Your exact dependencies → Android engine + FULL logging
    val client = HttpClient(Android) {
        // JSON + Logging (you already have these deps)
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }

        install(Auth) {

        }
        expectSuccess = false
        defaultRequest {
            accept(ContentType.Any)
        }

        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }

    // ================== AUTH ==================
    @Serializable
    data class ApiError(
        val message: String? = null,
        val error: String? = null,
        val statusCode: Int? = null
    )

    class AuthException(message: String) : Exception(message)

    suspend fun login(request: LoginRequest): LoginResponse {
        val cleanRequest = LoginRequest(
            email = request.email.trim().lowercase(),
            password = request.password
        )

        val response: HttpResponse = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(cleanRequest)
        }

        val rawBody = response.bodyAsText()
        return when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.Created -> {
                response.body<LoginResponse>()
            }
            HttpStatusCode.Unauthorized -> {
                throw AuthException("Invalid email or password. Please check your credentials.")
            }
            else -> {
                val error = try {
                    response.body<ApiError>()
                } catch (e: Exception) {
                    ApiError(message = rawBody.take(100))
                }
                throw AuthException(error.message ?: "Login failed (${response.status})")
            }
        }
    }
    suspend fun register(request: RegisterRequest): User {
        val response = client.post("$BASE_URL/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        return when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.Created -> {
                val registerResponse: RegisterResponse = response.body()
                registerResponse.user
            }
            else -> throw AuthException("Register failed: ${response.status}")
        }
    }
    suspend fun forgotPassword(email: String): ApiResponse {
        return client.post("$BASE_URL/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(ForgotPasswordRequest(email))
        }.body()
    }

    suspend fun resetPassword(resetToken: String, newPassword: String): ApiResponse {
        return client.post("$BASE_URL/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(ResetPasswordRequest(resetToken, newPassword))
        }.body()
    }

    // ================== PROTECTED ==================
    suspend fun getProfile(token: String): User {
        val response = client.get("$BASE_URL/users/profile") {
            bearerAuth(token)
        }
        val raw = response.bodyAsText()
        println("Raw profile response: $raw")
        return response.body()
    }


    suspend fun updateProfile(token: String, request: UpdateUserRequest): User {
        return client.patch("$BASE_URL/users/me") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    suspend fun updateAvatar(token: String, avatarUrl: String): User {
        val response = client.patch("$BASE_URL/users/avatar") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(mapOf("avatar" to avatarUrl))
        }
        return response.body()
    }

}