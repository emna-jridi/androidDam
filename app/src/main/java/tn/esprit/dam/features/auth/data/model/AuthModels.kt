package tn.esprit.dam.features.auth.data.model

import kotlinx.serialization.Serializable
import tn.esprit.dam.core.model.User

@Serializable
data class RegisterRequest(val email: String, val password: String, val name: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class GoogleLoginRequest(val idToken: String)

@Serializable
data class ResendOTPRequest(val email: String)

@Serializable
data class RequestPasswordResetRequest(val email: String)

@Serializable
data class VerifyPasswordResetOTPRequest(val email: String, val otp: String)

@Serializable
data class UpdateUserRequest(val name: String? = null)

@Serializable
data class RegisterResponse(val user: User)

@Serializable
data class LoginResponse(
    val message: String? = null,
    val user: User,
    val accessToken: String,
    val refreshToken: String? = null,
)

@Serializable
data class VerifyEmailMessageResponse(val message: String)

@Serializable
data class ResendOTPResponse(val message: String, val expiresIn: String? = null)

@Serializable
data class RequestPasswordResetResponse(val message: String)

@Serializable
data class VerifyPasswordResetOTPResponse(val message: String, val verified: Boolean = false)

@Serializable
data class ResetPasswordResponse(val message: String)
