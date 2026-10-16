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
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenRepository: TokenRepository,
    private val httpClient: HttpClient
) : AuthRepository {
    override suspend fun register(email: String, password: String, name: String): Result<RegisterResponse> =
        authApi.register(RegisterRequest(email, password, name)).toResult()

    override suspend fun verifyEmail(email: String, otp: String): Result<String> =
        authApi.verifyEmail(email, otp).toResult()

    override suspend fun resendVerificationOTP(email: String): Result<ResendOTPResponse> =
        authApi.resendVerificationOTP(email).toResult()

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        val result = authApi.login(LoginRequest(email.trim().lowercase(), password))
        if (result is ApiResult.Success) persistAuthSession(result.data)
        return result.toResult()
    }

    override suspend fun googleLogin(idToken: String): Result<LoginResponse> {
        val result = authApi.googleLogin(idToken)
        if (result is ApiResult.Success) persistAuthSession(result.data)
        return result.toResult()
    }

    private suspend fun persistAuthSession(response: LoginResponse) {
        tokenRepository.saveTokens(response.accessToken, response.refreshToken ?: "")
        tokenRepository.saveUser(response.user)
        httpClient.invalidateBearerTokens()
    }

    override suspend fun logout() {
        runCatching { tokenRepository.clearTokens() }
        httpClient.invalidateBearerTokens()
    }

    override suspend fun requestPasswordReset(email: String): Result<RequestPasswordResetResponse> =
        authApi.requestPasswordReset(email).toResult()

    override suspend fun verifyPasswordResetOTP(email: String, otp: String): Result<VerifyPasswordResetOTPResponse> =
        authApi.verifyPasswordResetOTP(email, otp).toResult()

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<ResetPasswordResponse> =
        authApi.resetPassword(email, code, newPassword).toResult()

    override suspend fun isLoggedIn(): Boolean {
        return tokenRepository.isLoggedIn() || tokenRepository.hasTokens()
    }

    override suspend fun getCurrentUser(): User? {
        return tokenRepository.getUser()
    }

}
