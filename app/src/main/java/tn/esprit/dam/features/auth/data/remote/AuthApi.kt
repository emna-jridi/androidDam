package tn.esprit.dam.features.auth.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.map
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.auth.data.model.GoogleLoginRequest
import tn.esprit.dam.features.auth.data.model.LoginRequest
import tn.esprit.dam.features.auth.data.model.LoginResponse
import tn.esprit.dam.features.auth.data.model.RegisterRequest
import tn.esprit.dam.features.auth.data.model.RegisterResponse
import tn.esprit.dam.features.auth.data.model.RequestPasswordResetRequest
import tn.esprit.dam.features.auth.data.model.RequestPasswordResetResponse
import tn.esprit.dam.features.auth.data.model.ResendOTPRequest
import tn.esprit.dam.features.auth.data.model.ResendOTPResponse
import tn.esprit.dam.features.auth.data.model.ResetPasswordResponse
import tn.esprit.dam.features.auth.data.model.VerifyEmailMessageResponse
import tn.esprit.dam.features.auth.data.model.VerifyPasswordResetOTPRequest
import tn.esprit.dam.features.auth.data.model.VerifyPasswordResetOTPResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApi @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        private const val AUTH_URL = "/api/auth"
        private const val RESET_REQUESTED_MESSAGE = "If this email exists, a reset code has been sent."
    }

    suspend fun register(request: RegisterRequest): ApiResult<RegisterResponse> =
        safeApiCall { client.post("$AUTH_URL/register") { setBody(request) } }

    suspend fun login(request: LoginRequest): ApiResult<LoginResponse> =
        safeApiCall { client.post("$AUTH_URL/login") { setBody(request) } }

    suspend fun googleLogin(idToken: String): ApiResult<LoginResponse> =
        safeApiCall { client.post("$AUTH_URL/google") { setBody(GoogleLoginRequest(idToken)) } }

    suspend fun verifyEmail(email: String, otp: String): ApiResult<String> =
        safeApiCall<VerifyEmailMessageResponse> {
            client.post("$AUTH_URL/verify-email") { setBody(mapOf("email" to email, "otp" to otp)) }
        }.map { it.message }

    suspend fun resendVerificationOTP(email: String): ApiResult<ResendOTPResponse> =
        safeApiCall { client.post("$AUTH_URL/resend-otp") { setBody(ResendOTPRequest(email)) } }

    /** Always succeeds with a neutral message so the UI never reveals whether an email exists. */
    suspend fun requestPasswordReset(email: String): ApiResult<RequestPasswordResetResponse> {
        val result = safeApiCall<RequestPasswordResetResponse> {
            client.post("$AUTH_URL/request-password-reset") { setBody(RequestPasswordResetRequest(email)) }
        }
        return result as? ApiResult.Success
            ?: ApiResult.Success(RequestPasswordResetResponse(message = RESET_REQUESTED_MESSAGE))
    }

    suspend fun verifyPasswordResetOTP(email: String, otp: String): ApiResult<VerifyPasswordResetOTPResponse> =
        safeApiCall { client.post("$AUTH_URL/verify-reset-code") { setBody(VerifyPasswordResetOTPRequest(email, otp)) } }

    suspend fun resetPassword(email: String, code: String, newPassword: String): ApiResult<ResetPasswordResponse> =
        safeApiCall {
            client.post("$AUTH_URL/reset-password") {
                setBody(mapOf("email" to email, "code" to code, "newPassword" to newPassword))
            }
        }
}
