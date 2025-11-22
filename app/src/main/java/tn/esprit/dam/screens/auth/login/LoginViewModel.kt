package tn.esprit.dam.screens.auth.login

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.repository.AuthRepository

/**
 * UI State for Login Screen
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

class LoginViewModel : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private lateinit var repository: AuthRepository

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Initialize repository with context
     */
    fun initialize(context: Context) {
        if (!::repository.isInitialized) {
            repository = AuthRepository(context)
            Log.d(TAG, "✅ Repository initialized")
        }
    }

    // =====================================
    // FORM STATE
    // =====================================
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    // =====================================
    // VALIDATION
    // =====================================
    private fun validateEmail(): Boolean {
        val email = _uiState.value.email
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
            else -> null
        }
        _uiState.value = _uiState.value.copy(passwordError = error)
        return error == null
    }

    private fun validateForm() = validateEmail() && validatePassword()

    // =====================================
    // LOGIN + SAVE TOKENS + SEND FCM
    // =====================================
    fun login() {
        Log.d(TAG, "🔵 login() called")

        if (!::repository.isInitialized) {
            Log.e(TAG, "❌ Repository not initialized!")
            _uiState.value = _uiState.value.copy(errorMessage = "Erreur d'initialisation")
            return
        }

        if (!validateForm()) {
            Log.d(TAG, "❌ Validation failed")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                Log.d(TAG, "🔐 Attempting login for: ${_uiState.value.email}")

                val result = repository.login(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )

                result.onSuccess { response ->
                    Log.d(TAG, "✅ Login successful for user: ${response.user.name}")

                    // Save tokens and user
                    repository.saveTokensAndUser(response)

                    // Get context & FCM token
                    val ctx = repository.getContext()
                    val fcmToken = TokenManager.getCurrentFcmToken()

                    if (!fcmToken.isNullOrEmpty()) {
                        try {
                            Log.d(TAG, "➡️ Sending FCM token to backend: $fcmToken")
                            TokenManager.sendFcmTokenToBackend(ctx, fcmToken)
                            Log.d(TAG, "✅ FCM token sent successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Failed to send FCM token", e)
                        }
                    } else {
                        Log.d(TAG, "⚠️ No FCM token available yet")
                    }

                    // Update UI state
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }

                result.onFailure { error ->
                    Log.e(TAG, "❌ Login failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = error.message ?: "Connexion échouée"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Unexpected error: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Erreur inattendue: ${e.message}"
                )
            }
        }
    }

    // =====================================
    // HELPERS
    // =====================================
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }
}
