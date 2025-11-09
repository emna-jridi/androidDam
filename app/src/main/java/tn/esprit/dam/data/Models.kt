package tn.esprit.dam.data

import kotlinx.serialization.Serializable

// ✅ Matches your backend /users/me and /auth/login responses
@Serializable
data class User(
    val _id: String? = null,
    val email: String,
    val name: String,
    val phone: String,
    val address: String,
    val image: String? = null,
    val role: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null,
    val resetPasswordCode: String? = null,
    val resetPasswordExpires: String? = null,
    val avatar: String? = null
)

// ✅ Register request
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
    val address: String,
    val avatar: String? = null
)

// ✅ Login request
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

// ✅ Response returned after successful login
@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

// ✅ Forgot password
@Serializable
data class ForgotPasswordRequest(
    val email: String
)

// ✅ Reset password
@Serializable
data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)

// ✅ Update user request
@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val image: String? = null
)

// ✅ Generic message response
@Serializable
data class ApiResponse(
    val message: String
)
@Serializable
data class RegisterResponse(
    val user: User
)
