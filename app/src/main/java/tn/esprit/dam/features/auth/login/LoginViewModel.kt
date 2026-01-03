package tn.esprit.dam.features.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.repository.AuthRepository
import tn.esprit.dam.data.security.TokenRepository
import tn.esprit.dam.features.auth.domain.usecase.LoginUseCase
import javax.inject.Inject

/**
 * State for login UI
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Authentication events
 */
sealed class AuthEvent {
    object LoginSuccess : AuthEvent()
}

/**
 * LoginViewModel - Hilt injectable ViewModel
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }
    
    fun initialize(context: android.content.Context) {
        // Initialization logic if needed
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    // Field change handlers
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = if (_uiState.value.emailError != null) null else _uiState.value.emailError
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = if (_uiState.value.passwordError != null) null else _uiState.value.passwordError
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            passwordVisible = !_uiState.value.passwordVisible
        )
    }

    // Validation
    private fun validateForm(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password

        val emailError = loginUseCase.validateEmail(email)
        val passwordError = loginUseCase.validatePassword(password)

        _uiState.value = _uiState.value.copy(
            emailError = emailError,
            passwordError = passwordError
        )

        return emailError == null && passwordError == null
    }

    // Login
    fun login() {
        Log.d(TAG, "Login() called")

        if (!validateForm()) {
            Log.d(TAG, "Validation failed")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting login for: ${_uiState.value.email}")

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                val result = loginUseCase.executeLogin(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )

                Log.d(TAG, "Login successful for user: ${result.user.name}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
                _events.emit(AuthEvent.LoginSuccess)

            } catch (e: Exception) {
                Log.e(TAG, "Login failed: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = e.message ?: "Login failed"
                )
            }
        }
    }

    // Utilities
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }
}
