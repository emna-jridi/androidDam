package tn.esprit.dam.features.auth.domain.usecase

import tn.esprit.dam.features.auth.data.model.LoginResponse
import tn.esprit.dam.features.auth.data.repository.AuthRepository
import tn.esprit.dam.features.auth.domain.validation.AuthValidator
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    fun validateEmail(email: String): String? = AuthValidator.validateEmail(email)

    fun validatePassword(password: String): String? = AuthValidator.validateLoginPassword(password)

    suspend fun executeLogin(email: String, password: String): LoginResponse {
        val error = validateEmail(email) ?: validatePassword(password)
        if (error != null) throw IllegalArgumentException(error)

        return authRepository.login(email, password).getOrThrow()
    }
}
