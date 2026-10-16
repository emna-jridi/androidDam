package tn.esprit.dam.features.auth.presentation.verification

import tn.esprit.dam.core.ui.toUiTextOr
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import tn.esprit.dam.features.auth.domain.validation.AuthValidator
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update
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
    val errorMessage: UiText? = null,

    val canResend: Boolean = false,
    val resendCountdown: Int = 60,
    val resendMessage: UiText? = null
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    companion object {
        private const val COUNTDOWN_SECONDS = 60
    }

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    private val countdown = ResendCountdown(viewModelScope, COUNTDOWN_SECONDS) { remaining, canResend ->
        _uiState.update { it.copy(resendCountdown = remaining, canResend = canResend) }
    }

    init {
        countdown.start()
    }

    fun onOtpChange(otp: String) {
        if (AuthValidator.isOtpInput(otp)) {
            _uiState.update { it.copy(
                otp = otp,
                otpError = null,
                errorMessage = null
            ) }
        }
    }

    fun onOtpDelete() {
        val currentOtp = _uiState.value.otp
        if (currentOtp.isNotEmpty()) {
            _uiState.update { it.copy(
                otp = currentOtp.dropLast(1)
            ) }
        }
    }

    fun verifyEmail(email: String) {
        val otp = _uiState.value.otp

        val otpError = AuthValidator.validateOtp(otp)
        if (otpError != null) {
            _uiState.update { it.copy(
                otpError = otpError
            ) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isLoading = true,
                    errorMessage = null,
                    otpError = null
                ) }

                val result = repository.verifyEmail(email, otp)

                result.onSuccess { response ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        isVerified = true
                    ) }

                    countdown.cancel()
                }.onFailure { error ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = error.message.toUiTextOr(R.string.auth_error_invalid_code),
                        otp = ""
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = UiText.Res(R.string.error_with_message, e.message.orEmpty()),
                    otp = ""
                ) }
            }
        }
    }

    fun resendOTP(email: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isLoading = true,
                    errorMessage = null,
                    resendMessage = null
                ) }

                val result = repository.resendVerificationOTP(email)

                result.onSuccess { response ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        otp = "",
                        resendMessage = UiText.Res(R.string.auth_code_resent),
                        canResend = false,
                        resendCountdown = COUNTDOWN_SECONDS
                    ) }

                    countdown.start()
                }.onFailure { error ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = error.message.toUiTextOr(R.string.auth_error_resend_failed)
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = UiText.Res(R.string.error_with_message, e.message.orEmpty())
                ) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(
            errorMessage = null,
            resendMessage = null,
            otpError = null
        ) }
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
