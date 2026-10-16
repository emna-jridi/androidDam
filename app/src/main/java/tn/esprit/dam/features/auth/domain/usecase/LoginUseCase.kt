package tn.esprit.dam.features.auth.domain.usecase

import android.util.Patterns
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.auth.data.repository.AuthRepository
import tn.esprit.dam.features.auth.data.model.LoginResponse
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email est requis"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Format email invalide"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Mot de passe est requis"
            password.length < 6 -> "Minimum 6 caractères"
            else -> null
        }
    }

    suspend fun executeLogin(email: String, password: String): LoginResponse {
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (emailError != null || passwordError != null) {
            throw IllegalArgumentException(emailError ?: passwordError)
        }

        val result = authRepository.login(email, password)
        return result.getOrThrow()
    }
}
