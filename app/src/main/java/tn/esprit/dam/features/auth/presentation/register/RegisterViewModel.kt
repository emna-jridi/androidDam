package tn.esprit.dam.features.auth.presentation.register
import android.util.Patterns
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
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            passwordVisible = !_uiState.value.passwordVisible
        )
    }

    private fun validateName(): Boolean {
        val name = _uiState.value.name.trim()

        val error = when {
            name.isEmpty() -> "Nom requis"
            name.length < 2 -> "Nom trop court"
            else -> null
        }

        _uiState.value = _uiState.value.copy(nameError = error)
        return error == null
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

    private fun validatePassword(): Boolean {
        val password = _uiState.value.password

        val error = when {
            password.isEmpty() -> "Mot de passe requis"
            password.length < 6 -> "Minimum 6 caractères"
            !password.any { it.isDigit() } -> "Doit contenir au moins un chiffre"
            !password.any { it.isLetter() } -> "Doit contenir au moins une lettre"
            else -> null
        }

        _uiState.value = _uiState.value.copy(passwordError = error)
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
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )

                val result = repository.register(
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password,
                    name = _uiState.value.name.trim()
                )

                result.onSuccess { response ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredEmail = response.user.email,
                        successMessage = "Inscription réussie! Vérifiez votre email.",
                        errorMessage = null
                    )
                }.onFailure { error ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = error.message ?: "Inscription échouée",
                        successMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Erreur inattendue: ${e.message}",
                    successMessage = null
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}
