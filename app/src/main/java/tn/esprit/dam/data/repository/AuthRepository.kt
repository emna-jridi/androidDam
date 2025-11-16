package tn.esprit.dam.data.repository
import android.content.Context
import android.util.Log
import tn.esprit.dam.data.*
import tn.esprit.dam.data.model.LoginResponse
import tn.esprit.dam.data.model.RegisterResponse
import tn.esprit.dam.data.model.RequestPasswordResetResponse
import tn.esprit.dam.data.model.ResendOTPResponse
import tn.esprit.dam.data.model.ResetPasswordResponse
import tn.esprit.dam.data.model.User
import tn.esprit.dam.data.model.VerifyPasswordResetOTPResponse

class AuthRepository(private val context: Context) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    private val apiClient = ApiClient.getInstance(context)

    // ========================================
    // INSCRIPTION & VÉRIFICATION
    // ========================================

    suspend fun register(
        email: String,
        password: String,
        name: String
    ): Result<RegisterResponse> {
        return try {
            Log.d(TAG, " Repository: Registering user $email")

            val response = apiClient.register(email, password, name)

            Log.d(TAG, "Repository: Registration successful")
            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, " Repository: Registration failed - ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun verifyEmail(email: String, otp: String): Result<String> {
        return try {
            Log.d(TAG, "✉ Repository: Verifying email $email with OTP")
            val message = apiClient.verifyEmail(email, otp)
            Log.d(TAG, "Repository: Email verified successfully: $message")
            Result.success(message)
        } catch (e: Exception) {
            Log.e(TAG, "Repository: Email verification failed - ${e.message}", e)
            Result.failure(e)
        }
    }



    suspend fun resendVerificationOTP(email: String): Result<ResendOTPResponse> {
        return try {
            Log.d(TAG, " Repository: Resending OTP to $email")

            val response = apiClient.resendVerificationOTP(email)

            Log.d(TAG, " Repository: OTP resent successfully")
            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, " Repository: Resend OTP failed - ${e.message}", e)
            Result.failure(e)
        }
    }

    // ========================================
    // CONNEXION & DÉCONNEXION
    // ========================================


    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResponse> {
        return try {
            Log.d(TAG, " Repository: Logging in user $email")

            val response = apiClient.login(email, password)

            Log.d(TAG, "Repository: Login successful")
            Log.d(TAG, "User: ${response.user.name} (${response.user.role})")

            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, " Repository: Login failed - ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun googleLogin(idToken: String): Result<LoginResponse> {
        return try {
            Log.d(TAG, " Repository: Google login")

            val response = apiClient.googleLogin(idToken)

            Log.d(TAG, " Repository: Google login successful")
            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, " Repository: Google login failed - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            Log.d(TAG, " Repository: Logging out")
            apiClient.logout()
            Log.d(TAG, " Repository: Logout successful")
        } catch (e: Exception) {
            Log.e(TAG, " Repository: Logout failed - ${e.message}", e)
            TokenManager.clearAll(context)
        }
    }

    // ========================================
    // RÉINITIALISATION MOT DE PASSE
    // ========================================

    suspend fun requestPasswordReset(email: String): Result<RequestPasswordResetResponse> {
        return try {
            Log.d(TAG, " Repository: Requesting password reset for $email")

            val response = apiClient.requestPasswordReset(email)

            Log.d(TAG, " Repository: Password reset requested")
            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, " Repository: Password reset request failed - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun verifyPasswordResetOTP(
        email: String,
        otp: String
    ): Result<VerifyPasswordResetOTPResponse> {
        return try {
            Log.d(TAG, " Repository: Verifying password reset OTP for $email")

            val response = apiClient.verifyPasswordResetOTP(email, otp)

            Log.d(TAG, " Repository: Password reset OTP verified")
            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, " Repository: Password reset OTP verification failed - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): Result<ResetPasswordResponse> {
        return try {
            Log.d(TAG, "📤 Repository: Resetting password for $email")

            // Call the Ktor API function with the updated parameters
            val response = apiClient.resetPassword(email, code, newPassword)

            Log.d(TAG, "✅ Repository: Password reset successful")
            Result.success(response)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Repository: Password reset failed - ${e.message}", e)
            Result.failure(e)
        }
    }


    // ========================================
    // ÉTAT & PROFIL
    // ========================================

    suspend fun isLoggedIn(): Boolean {
        return try {
            val isLoggedIn = apiClient.isLoggedIn()
            Log.d(TAG, "🔍 Repository: User logged in status = $isLoggedIn")
            isLoggedIn
        } catch (e: Exception) {
            Log.e(TAG, "❌ Repository: Error checking login status - ${e.message}", e)
            false
        }
    }
    suspend fun getCurrentUser(): User? {
        return try {
            val user = apiClient.getCurrentUser()

            if (user != null) {
                Log.d(TAG, " Repository: Current user = ${user.email}")
            } else {
                Log.d(TAG, " Repository: No current user")
            }

            user

        } catch (e: Exception) {
            Log.e(TAG, "Repository: Error getting current user - ${e.message}", e)
            null
        }
    }


    // ========================================
    // UTILITAIRES
    // ========================================


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