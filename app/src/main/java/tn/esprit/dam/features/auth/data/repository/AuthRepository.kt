package tn.esprit.dam.features.auth.data.repository

import tn.esprit.dam.core.model.User
import tn.esprit.dam.features.auth.data.model.LoginResponse
import tn.esprit.dam.features.auth.data.model.RegisterResponse
import tn.esprit.dam.features.auth.data.model.RequestPasswordResetResponse
import tn.esprit.dam.features.auth.data.model.ResendOTPResponse
import tn.esprit.dam.features.auth.data.model.ResetPasswordResponse
import tn.esprit.dam.features.auth.data.model.VerifyPasswordResetOTPResponse

interface AuthRepository {
    suspend fun register(email: String, password: String, name: String): Result<RegisterResponse>

    suspend fun verifyEmail(email: String, otp: String): Result<String>

    suspend fun resendVerificationOTP(email: String): Result<ResendOTPResponse>

    suspend fun login(email: String, password: String): Result<LoginResponse>

    suspend fun googleLogin(idToken: String): Result<LoginResponse>

    suspend fun logout()

    suspend fun requestPasswordReset(email: String): Result<RequestPasswordResetResponse>

    suspend fun verifyPasswordResetOTP(email: String, otp: String): Result<VerifyPasswordResetOTPResponse>

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<ResetPasswordResponse>

    suspend fun isLoggedIn(): Boolean

    suspend fun getCurrentUser(): User?
}
