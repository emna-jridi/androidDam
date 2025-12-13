package tn.esprit.dam.features.auth.login

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.repository.AuthRepository

/**
 * État de l'UI pour l'écran de connexion
 */
data class LoginUiState(
    // Champs de formulaire
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,

    // Erreurs de validation
    val emailError: String? = null,
    val passwordError: String? = null,

    // Ã‰tats de chargement/succÃ¨s/erreur
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * âœ… VERSION SANS FACTORY - Utilise ViewModel au lieu de AndroidViewModel
 */
sealed class AuthEvent {
    object LoginSuccess : AuthEvent()
}

class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    // âœ… Repository sera initialisÃ© depuis le Composable
    private lateinit var repository: AuthRepository

    // Ã‰tat de l'UI exposÃ© aux Composables
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    /**
     * âœ… Initialiser le repository avec le context
     * AppelÃ© une seule fois depuis le Composable
     */
    fun initialize(context: Context) {
        if (!::repository.isInitialized) {
            repository = AuthRepository(context)
            Log.d(TAG, "âœ… Repository initialized")
        }
    }

    // ========================================
    // GESTION DES CHANGEMENTS DE CHAMPS
    // ========================================

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

    // ========================================
    // VALIDATION
    // ========================================

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
            password.length < 6 -> "Minimum 6 caractÃ¨res"
            else -> null
        }

        _uiState.value = _uiState.value.copy(passwordError = error)
        return error == null
    }

    private fun validateForm(): Boolean {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        return isEmailValid && isPasswordValid
    }

    // ========================================
    // LOGIN
    // ========================================

    fun login() {
        Log.d(TAG, "ðŸ”µ login() called")

        // VÃ©rifier que le repository est initialisÃ©
        if (!::repository.isInitialized) {
            Log.e(TAG, "âŒ Repository not initialized!")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Erreur d'initialisation"
            )
            return
        }

        // Valider d'abord
        if (!validateForm()) {
            Log.d(TAG, "âŒ Validation failed")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "ðŸ” Attempting login for: ${_uiState.value.email}")

                // Mettre en Ã©tat de chargement
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

                // Appeler le repository
                val result = repository.login(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )

                result.onSuccess { response ->
                    Log.d(TAG, "âœ… Login successful for user: ${response.user.name}")

                    // SuccÃ¨s
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                    _events.emit(AuthEvent.LoginSuccess)
                }.onFailure { error ->
                    Log.e(TAG, "âŒ Login failed: ${error.message}", error)

                    // Ã‰chec
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = error.message ?: "Connexion Ã©chouÃ©e"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "âŒ Unexpected error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Erreur inattendue: ${e.message}"
                )
            }
        }
    }

    // ========================================
    // UTILITAIRES
    // ========================================

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = LoginUiState()
    }
}
