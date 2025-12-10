package tn.esprit.dam.screens.auth.forgotpassword
import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.repository.AuthRepository

/**
 * Étapes du flow de reset password
 */
enum class ResetPasswordStep {
    ENTER_EMAIL,
    VERIFY_OTP,
    NEW_PASSWORD,
    SUCCESS
}


data class ForgotPasswordUiState(
    val currentStep: ResetPasswordStep = ResetPasswordStep.ENTER_EMAIL,
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val resetToken: String? = null,

    val emailError: String? = null,
    val otpError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    val canResend: Boolean = false,
    val resendCountdown: Int = 60,

    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false
)

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ForgotPasswordVM"
        private const val OTP_LENGTH = 6
        private const val COUNTDOWN_SECONDS = 60
    }

    private val repository = AuthRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    // ÉTAPE 1 : ENTRER EMAIL


    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            errorMessage = null
        )
    }

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email.trim()

        val error = when {
            email.isEmpty() -> "Email requis"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email invalide"
            else -> null
        }

        _uiState.value = _uiState.value.copy(emailError = error)
        return error == null
    }

    fun requestPasswordReset() {
        if (!validateEmail()) {
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "🔑 Requesting password reset for: ${_uiState.value.email}")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val result = repository.requestPasswordReset(_uiState.value.email.trim())

                result.onSuccess { response ->
                    Log.d(TAG, "✅ Reset code sent")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = ResetPasswordStep.VERIFY_OTP,
                        successMessage = response.message
                    )

                    // Démarrer le countdown
                    startResendCountdown()

                }.onFailure { error ->
                    Log.e(TAG, "❌ Request failed: ${error.message}", error)

                    // Même si ça échoue, on passe à l'étape suivante (sécurité)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = ResetPasswordStep.VERIFY_OTP,
                        successMessage = "Si cet email existe, un code a été envoyé."
                    )

                    startResendCountdown()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur: ${e.message}"
                )
            }
        }
    }

    // ÉTAPE 2 : VÉRIFIER OTP

    fun onOtpChange(otp: String) {
        if (otp.length <= OTP_LENGTH && otp.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                otp = otp,
                otpError = null,
                errorMessage = null
            )
        }
    }

    fun verifyResetOTP() {
        val otp = _uiState.value.otp

        if (otp.length != OTP_LENGTH) {
            _uiState.value = _uiState.value.copy(
                otpError = "Code à 6 chiffres requis"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "✉️ Verifying reset OTP")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val result = repository.verifyPasswordResetOTP(
                    email = _uiState.value.email,
                    otp = otp
                )

                result.onSuccess { response ->
                    Log.d(TAG, "✅ OTP verified, reset token received")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = ResetPasswordStep.NEW_PASSWORD,

                    )

                    // Arrêter le countdown
                    countdownJob?.cancel()

                }.onFailure { error ->
                    Log.e(TAG, "❌ OTP verification failed: ${error.message}", error)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Code invalide",
                        otp = ""
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur: ${e.message}",
                    otp = ""
                )
            }
        }
    }

    fun resendResetOTP() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Resending reset OTP")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val result = repository.requestPasswordReset(_uiState.value.email)

                result.onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otp = "",
                        successMessage = "Code renvoyé avec succès",
                        canResend = false,
                        resendCountdown = COUNTDOWN_SECONDS
                    )

                    startResendCountdown()

                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Échec de renvoi"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur: ${e.message}"
                )
            }
        }
    }

    private fun startResendCountdown() {
        countdownJob?.cancel()

        countdownJob = viewModelScope.launch {
            var seconds = COUNTDOWN_SECONDS

            while (seconds > 0) {
                _uiState.value = _uiState.value.copy(
                    resendCountdown = seconds,
                    canResend = false
                )
                delay(1000)
                seconds--
            }

            _uiState.value = _uiState.value.copy(
                canResend = true,
                resendCountdown = 0
            )
        }
    }

    // ÉTAPE 3 : NOUVEAU MOT DE PASSE

    fun onNewPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            newPassword = password,
            passwordError = null,
            errorMessage = null
        )
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = password,
            confirmPasswordError = null,
            errorMessage = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            passwordVisible = !_uiState.value.passwordVisible
        )
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            confirmPasswordVisible = !_uiState.value.confirmPasswordVisible
        )
    }

    private fun validateNewPassword(): Boolean {
        val password = _uiState.value.newPassword
        val confirmPassword = _uiState.value.confirmPassword

        var isValid = true

        // Valider password
        val passwordError = when {
            password.isEmpty() -> "Mot de passe requis"
            password.length < 6 -> "Minimum 6 caractères"
            !password.any { it.isDigit() } -> "Doit contenir au moins un chiffre"
            !password.any { it.isLetter() } -> "Doit contenir au moins une lettre"
            else -> null
        }

        if (passwordError != null) {
            _uiState.value = _uiState.value.copy(passwordError = passwordError)
            isValid = false
        }

        // Valider confirmation
        val confirmError = when {
            confirmPassword.isEmpty() -> "Confirmation requise"
            password != confirmPassword -> "Les mots de passe ne correspondent pas"
            else -> null
        }

        if (confirmError != null) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = confirmError)
            isValid = false
        }

        return isValid
    }

    fun resetPassword() {
        if (!validateNewPassword()) {
            return
        }



        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Resetting password")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val result = repository.resetPassword(
                    email = _uiState.value.email,
                    code = _uiState.value.otp,
                    newPassword = _uiState.value.newPassword
                )

                result.onSuccess { response ->
                    Log.d(TAG, "✅ Password reset successful")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentStep = ResetPasswordStep.SUCCESS,
                        successMessage = response.message
                    )

                }.onFailure { error ->
                    Log.e(TAG, "❌ Password reset failed: ${error.message}", error)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Échec de réinitialisation"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur: ${e.message}"
                )
            }
        }
    }

    // NAVIGATION

    fun goBackToEmailStep() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentStep = ResetPasswordStep.ENTER_EMAIL,
            otp = "",
            resetToken = null,
            errorMessage = null
        )
    }

    // UTILITAIRES

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun resetState() {
        countdownJob?.cancel()
        _uiState.value = ForgotPasswordUiState()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }}