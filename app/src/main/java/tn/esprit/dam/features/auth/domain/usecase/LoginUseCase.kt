package tn.esprit.dam.features.auth.domain.usecase

import android.util.Patterns
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.repository.AuthRepository
import tn.esprit.dam.data.model.LoginResponse
import javax.inject.Inject

/**
 * LoginUseCase - Business logic for user authentication
 * Handles validation, API calls, and token storage
 * Replaces login logic scattered in ViewModels
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Validate email format
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email est requis"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Format email invalide"
            else -> null
        }
    }
    
    /**
     * Validate password requirements
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Mot de passe est requis"
            password.length < 6 -> "Minimum 6 caractères"
            else -> null
        }
    }
    
    /**
     * Execute login with email and password
     * Returns success response or throws exception on failure
     */
    suspend fun executeLogin(email: String, password: String): LoginResponse {
        // Validate inputs
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        
        if (emailError != null || passwordError != null) {
            throw IllegalArgumentException(emailError ?: passwordError)
        }
        
        // Call repository
        val result = authRepository.login(email, password)
        return result.getOrThrow()  // Throws exception on failure
    }
}
