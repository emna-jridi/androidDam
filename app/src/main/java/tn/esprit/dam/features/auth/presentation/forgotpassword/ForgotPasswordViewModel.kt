package tn.esprit.dam.features.auth.presentation.forgotpassword
import tn.esprit.dam.core.ui.toUiTextOr
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import tn.esprit.dam.features.auth.domain.validation.AuthValidator
import kotlinx.coroutines.flow.update
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
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null,

    val canResend: Boolean = false,
    val resendCountdown: Int = 60,

    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    companion object {
        private const val COUNTDOWN_SECONDS = 60
    }

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val countdown = ResendCountdown(viewModelScope, COUNTDOWN_SECONDS) { remaining, canResend ->
        _uiState.update { it.copy(resendCountdown = remaining, canResend = canResend) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(
            email = email,
            emailError = null,
            errorMessage = null
        ) }
    }

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email.trim()

        val error = AuthValidator.validateEmail(email)

        _uiState.update { it.copy(emailError = error) }
        return error == null
    }

    fun requestPasswordReset() {
        if (!validateEmail()) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isLoading = true,
                    errorMessage = null
                ) }

                val result = repository.requestPasswordReset(_uiState.value.email.trim())

                result.onSuccess { response ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        currentStep = ResetPasswordStep.VERIFY_OTP,
                        successMessage = UiText.Raw(response.message)
                    ) }

                    countdown.start()
                }.onFailure { error ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        currentStep = ResetPasswordStep.VERIFY_OTP,
                        successMessage = UiText.Res(R.string.auth_reset_code_sent_if_exists)
                    ) }

                    countdown.start()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = UiText.Res(R.string.error_with_message, e.message.orEmpty())
                ) }
            }
        }
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

    fun verifyResetOTP() {
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
                    errorMessage = null
                ) }

                val result = repository.verifyPasswordResetOTP(
                    email = _uiState.value.email,
                    otp = otp
                )

                result.onSuccess { response ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        currentStep = ResetPasswordStep.NEW_PASSWORD,
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

    fun resendResetOTP() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isLoading = true,
                    errorMessage = null
                ) }

                val result = repository.requestPasswordReset(_uiState.value.email)

                result.onSuccess {
                    _uiState.update { it.copy(
                        isLoading = false,
                        otp = "",
                        successMessage = UiText.Res(R.string.auth_code_resent),
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

    fun onNewPasswordChange(password: String) {
        _uiState.update { it.copy(
            newPassword = password,
            passwordError = null,
            errorMessage = null
        ) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(
            confirmPassword = password,
            confirmPasswordError = null,
            errorMessage = null
        ) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(
            passwordVisible = !it.passwordVisible
        ) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(
            confirmPasswordVisible = !it.confirmPasswordVisible
        ) }
    }

    private fun validateNewPassword(): Boolean {
        val password = _uiState.value.newPassword
        val confirmPassword = _uiState.value.confirmPassword

        var isValid = true

        val passwordError = AuthValidator.validateNewPassword(password)

        if (passwordError != null) {
            _uiState.update { it.copy(passwordError = passwordError) }
            isValid = false
        }

        val confirmError = AuthValidator.validatePasswordConfirmation(password, confirmPassword)

        if (confirmError != null) {
            _uiState.update { it.copy(confirmPasswordError = confirmError) }
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
                _uiState.update { it.copy(
                    isLoading = true,
                    errorMessage = null
                ) }

                val result = repository.resetPassword(
                    email = _uiState.value.email,
                    code = _uiState.value.otp,
                    newPassword = _uiState.value.newPassword
                )

                result.onSuccess { response ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        currentStep = ResetPasswordStep.SUCCESS,
                        successMessage = UiText.Raw(response.message)
                    ) }
                }.onFailure { error ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = error.message.toUiTextOr(R.string.auth_error_reset_failed)
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

    fun goBackToEmailStep() {
        countdown.cancel()
        _uiState.update { it.copy(
            currentStep = ResetPasswordStep.ENTER_EMAIL,
            otp = "",
            resetToken = null,
            errorMessage = null
        ) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(
            errorMessage = null,
            successMessage = null
        ) }
    }

    fun resetState() {
        countdown.cancel()
        _uiState.value = ForgotPasswordUiState()
    }

    override fun onCleared() {
        super.onCleared()
        countdown.cancel()
    }}
