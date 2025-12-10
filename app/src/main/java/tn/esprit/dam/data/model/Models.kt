package tn.esprit.dam.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Requests
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

// Responses
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

@Serializable
data class ApiError(
    @Serializable(with = FlexibleMessageSerializer::class)
    val message: String? = null,
    val error: String? = null,
    val statusCode: Int? = null,
)

object FlexibleMessageSerializer : KSerializer<String?> {
    override val descriptor = PrimitiveSerialDescriptor("FlexibleMessage", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) encoder.encodeString(value) else encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): String? {
        return try {
            decoder.decodeString()
        } catch (_: Exception) {
            null
        }
    }
}
