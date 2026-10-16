package tn.esprit.dam.features.auth.presentation.register
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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,

    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,

    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val registeredEmail: String? = null,
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(
            name = name,
            nameError = null
        ) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(
            email = email,
            emailError = null
        ) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(
            password = password,
            passwordError = null
        ) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(
            passwordVisible = !it.passwordVisible
        ) }
    }

    private fun validateName(): Boolean {
        val name = _uiState.value.name.trim()

        val error = AuthValidator.validateName(name)

        _uiState.update { it.copy(nameError = error) }
        return error == null
    }

    private fun validateEmail(): Boolean {
        val email = _uiState.value.email.trim()

        val error = AuthValidator.validateEmail(email)

        _uiState.update { it.copy(emailError = error) }
        return error == null
    }

    private fun validatePassword(): Boolean {
        val password = _uiState.value.password

        val error = AuthValidator.validateNewPassword(password)

        _uiState.update { it.copy(passwordError = error) }
        return error == null
    }

    private fun validateForm(): Boolean {
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()

        return isNameValid && isEmailValid && isPasswordValid
    }

    fun register() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                ) }

                val result = repository.register(
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password,
                    name = _uiState.value.name.trim()
                )

                result.onSuccess { response ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredEmail = response.user.email,
                        successMessage = UiText.Res(R.string.auth_register_success),
                        errorMessage = null
                    ) }
                }.onFailure { error ->

                    _uiState.update { it.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = error.message.toUiTextOr(R.string.auth_error_register_failed),
                        successMessage = null
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = UiText.Res(R.string.error_unexpected, e.message.orEmpty()),
                    successMessage = null
                ) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(
            errorMessage = null,
            successMessage = null
        ) }
    }

    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}
