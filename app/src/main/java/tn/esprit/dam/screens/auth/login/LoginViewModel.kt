package tn.esprit.dam.screens.auth.login

import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
 * ViewModel pour l'écran de connexion
 * Gère toute la logique métier et l'état de l'UI
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val repository = AuthRepository(application.applicationContext)

    // État de l'UI exposé aux Composables
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // ========================================
    // GESTION DES CHANGEMENTS DE CHAMPS
    // ========================================

    /**
     * Appelé quand l'utilisateur tape son email
     */
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            // Effacer l'erreur si l'utilisateur corrige
            emailError = if (_uiState.value.emailError != null) null else _uiState.value.emailError
        )
    }

    /**
     * Appelé quand l'utilisateur tape son mot de passe
     */
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            // Effacer l'erreur si l'utilisateur corrige
            passwordError = if (_uiState.value.passwordError != null) null else _uiState.value.passwordError
        )
    }

    /**
     * Toggle visibilité du mot de passe
     */
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            passwordVisible = !_uiState.value.passwordVisible
        )
    }

    // ========================================
    // VALIDATION
    // ========================================

    /**
     * Valider l'email
     * @return true si valide
     */
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

    /**
     * Valider le mot de passe
     * @return true si valide
     */
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

    /**
     * Valider tout le formulaire
     * @return true si tout est valide
     */
    private fun validateForm(): Boolean {
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()
        return isEmailValid && isPasswordValid
    }

    // ========================================
    // LOGIN
    // ========================================

    /**
     * Tenter la connexion
     * Appelé quand l'utilisateur clique sur "Se connecter"
     */
    fun login() {
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

    /**
     * Effacer le message d'erreur
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Réinitialiser l'état (pour retour depuis un autre écran)
     */
    fun resetState() {
        _uiState.value = LoginUiState()
    }
}