package tn.esprit.dam.features.auth.data.repository

import io.ktor.client.HttpClient
import tn.esprit.dam.core.model.User
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.invalidateBearerTokens
import tn.esprit.dam.core.network.toResult
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.features.auth.data.model.LoginRequest
import tn.esprit.dam.features.auth.data.model.LoginResponse
import tn.esprit.dam.features.auth.data.model.RegisterRequest
import tn.esprit.dam.features.auth.data.model.RegisterResponse
import tn.esprit.dam.features.auth.data.model.RequestPasswordResetResponse
import tn.esprit.dam.features.auth.data.model.ResendOTPResponse
import tn.esprit.dam.features.auth.data.model.ResetPasswordResponse
import tn.esprit.dam.features.auth.data.model.VerifyPasswordResetOTPResponse
import tn.esprit.dam.features.auth.data.remote.AuthApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
    private val httpClient: HttpClient
) {
    suspend fun register(email: String, password: String, name: String): Result<RegisterResponse> =
        authApi.register(RegisterRequest(email, password, name)).toResult()

    suspend fun verifyEmail(email: String, otp: String): Result<String> =
        authApi.verifyEmail(email, otp).toResult()

    suspend fun resendVerificationOTP(email: String): Result<ResendOTPResponse> =
        authApi.resendVerificationOTP(email).toResult()

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        val result = authApi.login(LoginRequest(email.trim().lowercase(), password))
        if (result is ApiResult.Success) persistAuthSession(result.data)
        return result.toResult()
    }

    suspend fun googleLogin(idToken: String): Result<LoginResponse> {
        val result = authApi.googleLogin(idToken)
        if (result is ApiResult.Success) persistAuthSession(result.data)
        return result.toResult()
    }

    private suspend fun persistAuthSession(response: LoginResponse) {
        tokenRepository.saveTokens(response.accessToken, response.refreshToken ?: "")
        tokenRepository.saveUser(response.user)
        httpClient.invalidateBearerTokens()
    }

    suspend fun logout() {
        runCatching { tokenRepository.clearTokens() }
        httpClient.invalidateBearerTokens()
    }

    suspend fun requestPasswordReset(email: String): Result<RequestPasswordResetResponse> =
        authApi.requestPasswordReset(email).toResult()

    suspend fun verifyPasswordResetOTP(email: String, otp: String): Result<VerifyPasswordResetOTPResponse> =
        authApi.verifyPasswordResetOTP(email, otp).toResult()

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<ResetPasswordResponse> =
        authApi.resetPassword(email, code, newPassword).toResult()

    suspend fun isLoggedIn(): Boolean {
        return tokenRepository.isLoggedIn() || tokenRepository.hasTokens()
    }

    suspend fun getCurrentUser(): User? {
        return tokenRepository.getUser()
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidOTP(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }
}
