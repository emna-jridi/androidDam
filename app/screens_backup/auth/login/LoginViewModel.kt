package tn.esprit.dam.screens.auth.login

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    // États de chargement/succès/erreur
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ✅ VERSION SANS FACTORY - Utilise ViewModel au lieu de AndroidViewModel
 */
class LoginViewModel : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    // ✅ Repository sera initialisé depuis le Composable
    private lateinit var repository: AuthRepository

    // État de l'UI exposé aux Composables
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * ✅ Initialiser le repository avec le context
     * Appelé une seule fois depuis le Composable
     */
    fun initialize(context: Context) {
        if (!::repository.isInitialized) {
            repository = AuthRepository(context)
            Log.d(TAG, "✅ Repository initialized")
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
            password.length < 6 -> "Minimum 6 caractères"
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
        Log.d(TAG, "🔵 login() called")

        // Vérifier que le repository est initialisé
        if (!::repository.isInitialized) {
            Log.e(TAG, "❌ Repository not initialized!")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Erreur d'initialisation"
            )
            return
        }

        // Valider d'abord
        if (!validateForm()) {
            Log.d(TAG, "❌ Validation failed")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "🔐 Attempting login for: ${_uiState.value.email}")

                // Mettre en état de chargement
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
                    Log.d(TAG, "✅ Login successful for user: ${response.user.name}")

                    // Succès
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }.onFailure { error ->
                    Log.e(TAG, "❌ Login failed: ${error.message}", error)

                    // Échec
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