package tn.esprit.dam.features.auth.register
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
 * État de l'UI pour l'écran d'inscription
 */
data class RegisterUiState(
    // Champs de formulaire
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,

    // Erreurs de validation
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,

    // Ã‰tats de chargement/succÃ¨s/erreur
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val registeredEmail: String? = null, // Email pour passer à l'écran de vérification
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel pour l'écran d'inscription
 * Gère toute la logique métier et l'état de l'UI
 */
class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RegisterViewModel"
    }

    private val repository = AuthRepository(application.applicationContext)

    // État de l'UI exposé aux Composables
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // ========================================
    // GESTION DES CHANGEMENTS DE CHAMPS
    // ========================================

    /**
     * Appelé quand l'utilisateur tape son nom
     */
    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            // Effacer l'erreur si l'utilisateur corrige
            nameError = if (_uiState.value.nameError != null) null else _uiState.value.nameError
        )
    }

    /**
     * Appelé quand l'utilisateur tape son email
     */
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = if (_uiState.value.emailError != null) null else _uiState.value.emailError
        )
    }

    /**
     * Appelé quand l'utilisateur tape son mot de passe
     */
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
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
     * Valider le nom
     * @return true si valide
     */
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

    /**
     * Valider l'email
     * @return true si valide
     */
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

    /**
     * Valider le mot de passe
     * @return true si valide
     */
    private fun validatePassword(): Boolean {
        val password = _uiState.value.password

        val error = when {
            password.isEmpty() -> "Mot de passe requis"
            password.length < 6 -> "Minimum 6 caractÃ¨res"
            !password.any { it.isDigit() } -> "Doit contenir au moins un chiffre"
            !password.any { it.isLetter() } -> "Doit contenir au moins une lettre"
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
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isPasswordValid = validatePassword()

        return isNameValid && isEmailValid && isPasswordValid
    }

    // ========================================
    // INSCRIPTION
    // ========================================

    /**
     * Tenter l'inscription
     * Appelé quand l'utilisateur clique sur "S'inscrire"
     */
    fun register() {
        // Valider d'abord
        if (!validateForm()) {
            Log.d(TAG, "âŒ Validation failed")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "ðŸ“ Attempting registration for: ${_uiState.value.email}")

                // Mettre en état de chargement
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )

                // Appeler le repository
                val result = repository.register(
                    email = _uiState.value.email.trim(),
                    password = _uiState.value.password,
                    name = _uiState.value.name.trim()
                )

                result.onSuccess { response ->
                    Log.d(TAG, "âœ… Registration successful")
                    Log.d(TAG, "Message: ")

                    // SuccÃ¨s
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        registeredEmail = response.user.email,
                        successMessage = "Inscription réussie! Vérifiez votre email.",
                        errorMessage = null
                    )
                }.onFailure { error ->
                    Log.e(TAG, "âŒ Registration failed: ${error.message}", error)

                    // Ã‰chec
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = error.message ?: "Inscription échouée",
                        successMessage = null
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "âŒ Unexpected error: ${e.message}", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = "Erreur inattendue: ${e.message}",
                    successMessage = null
                )
            }
        }
    }

    // ========================================
    // UTILITAIRES
    // ========================================

    /**
     * Effacer les messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * Réinitialiser l'état
     */
    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}
