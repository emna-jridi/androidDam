package tn.esprit.dam.features.auth.presentation.login

import tn.esprit.dam.core.ui.toUiTextOr
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.features.auth.data.repository.AuthRepository
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.features.auth.domain.usecase.LoginUseCase
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: UiText? = null
)

sealed class AuthEvent {
    object LoginSuccess : AuthEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

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

    private fun validateForm(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password

        val emailError = loginUseCase.validateEmail(email)
        val passwordError = loginUseCase.validatePassword(password)

        _uiState.update { it.copy(
            emailError = emailError,
            passwordError = passwordError
        ) }

        return emailError == null && passwordError == null
    }

    fun login() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isLoading = true,
                    errorMessage = null
                ) }

                val result = loginUseCase.executeLogin(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )

                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                ) }
                _events.emit(AuthEvent.LoginSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = e.message.toUiTextOr(R.string.auth_error_login_failed)
                ) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }
}
