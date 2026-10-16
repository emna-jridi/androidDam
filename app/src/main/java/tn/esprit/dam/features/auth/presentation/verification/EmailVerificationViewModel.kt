package tn.esprit.dam.features.auth.presentation.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.features.auth.data.repository.AuthRepository
import tn.esprit.dam.features.auth.presentation.components.ResendCountdown

data class EmailVerificationUiState(
    val otp: String = "",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val errorMessage: String? = null,

    val canResend: Boolean = false,
    val resendCountdown: Int = 60,
    val resendMessage: String? = null
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    companion object {
        private const val OTP_LENGTH = 6
        private const val COUNTDOWN_SECONDS = 60
    }

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    private val countdown = ResendCountdown(viewModelScope, COUNTDOWN_SECONDS) { remaining, canResend ->
        _uiState.value = _uiState.value.copy(resendCountdown = remaining, canResend = canResend)
    }

    init {
        countdown.start()
    }

    fun onOtpChange(otp: String) {
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

    fun verifyEmail(email: String) {
        val otp = _uiState.value.otp

        if (otp.length != OTP_LENGTH) {
            _uiState.value = _uiState.value.copy(
                otpError = "Code à 6 chiffres requis"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    otpError = null
                )

                val result = repository.verifyEmail(email, otp)

                result.onSuccess { response ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerified = true
                    )

                    countdown.cancel()
                }.onFailure { error ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Code invalide",
                        otp = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur: ${e.message}",
                    otp = ""
                )
            }
        }
    }

    fun resendOTP(email: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    resendMessage = null
                )

                val result = repository.resendVerificationOTP(email)

                result.onSuccess { response ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otp = "",
                        resendMessage = "Code renvoyé avec succès",
                        canResend = false,
                        resendCountdown = COUNTDOWN_SECONDS
                    )

                    countdown.start()
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

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            resendMessage = null,
            otpError = null
        )
    }

    fun resetState() {
        countdown.cancel()
        _uiState.value = EmailVerificationUiState()
    }

    override fun onCleared() {
        super.onCleared()
        countdown.cancel()
    }
}
