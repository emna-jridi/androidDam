package tn.esprit.dam.screens.auth.verification

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.repository.AuthRepository

data class EmailVerificationUiState(
    val otp: String = "",
    val otpError: String? = null,
    // États
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val errorMessage: String? = null,

    // Resend OTP
    val canResend: Boolean = false,
    val resendCountdown: Int = 60,
    val resendMessage: String? = null
)


class EmailVerificationViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "EmailVerificationVM"
        private const val OTP_LENGTH = 6
        private const val COUNTDOWN_SECONDS = 60
    }

    private val repository = AuthRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        // Démarrer le countdown au chargement
        startResendCountdown()
    }

    // GESTION OTP

    fun onOtpChange(otp: String) {
        // Accepter seulement les chiffres et max 6 caractères
        if (otp.length <= OTP_LENGTH && otp.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                otp = otp,
                otpError = null,
                errorMessage = null
            )
        }
    }

    fun onOtpDelete() {
        val currentOtp = _uiState.value.otp
        if (currentOtp.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                otp = currentOtp.dropLast(1)
            )
        }
    }

    // VÉRIFICATION
    fun verifyEmail(email: String) {
        val otp = _uiState.value.otp

        // Validation
        if (otp.length != OTP_LENGTH) {
            _uiState.value = _uiState.value.copy(
                otpError = "Code à 6 chiffres requis"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "✉️ Verifying email: $email with OTP: $otp")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    otpError = null
                )

                val result = repository.verifyEmail(email, otp)

                result.onSuccess { response ->
                    Log.d(TAG, " Email verified successfully")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerified = true
                    )

                    // Arrêter le countdown
                    countdownJob?.cancel()

                }.onFailure { error ->
                    Log.e(TAG, " Verification failed: ${error.message}", error)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Code invalide",
                        otp = "" // Effacer le code incorrect
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, " Unexpected error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur: ${e.message}",
                    otp = ""
                )
            }
        }
    }


    // RENVOYER OTP

    fun resendOTP(email: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔄 Resending OTP to: $email")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    resendMessage = null
                )

                val result = repository.resendVerificationOTP(email)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ OTP resent successfully")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otp = "", // Effacer l'ancien code
                        resendMessage = "Code renvoyé avec succès",
                        canResend = false,
                        resendCountdown = COUNTDOWN_SECONDS
                    )

                    // Redémarrer le countdown
                    startResendCountdown()

                }.onFailure { error ->
                    Log.e(TAG, "❌ Resend failed: ${error.message}", error)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Échec de renvoi"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Resend error: ${e.message}", e)

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

            // Autoriser le renvoi
            _uiState.value = _uiState.value.copy(
                canResend = true,
                resendCountdown = 0
            )
        }
    }

    // UTILITAIRES

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            resendMessage = null,
            otpError = null
        )
    }

    fun resetState() {
        countdownJob?.cancel()
        _uiState.value = EmailVerificationUiState()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}