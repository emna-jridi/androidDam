package tn.esprit.dam.features.auth.domain.validation

/**
 * Single source of truth for auth form rules. Each `validate*` returns the error message
 * to show, or null when the input is valid.
 */
object AuthValidator {
    const val MIN_PASSWORD_LENGTH = 6
    const val OTP_LENGTH = 6

    // Same pattern as android.util.Patterns.EMAIL_ADDRESS, kept here so it runs in JVM tests.
    private val EMAIL_REGEX = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )

    fun validateEmail(email: String): String? {
        val value = email.trim()
        return when {
            value.isEmpty() -> "Email requis"
            !EMAIL_REGEX.matches(value) -> "Email invalide"
            else -> null
        }
    }

    /** Rules for signing in: only the minimum length, so older accounts can still log in. */
    fun validateLoginPassword(password: String): String? = when {
        password.isBlank() -> "Mot de passe requis"
        password.length < MIN_PASSWORD_LENGTH -> "Minimum $MIN_PASSWORD_LENGTH caractères"
        else -> null
    }

    /** Rules for choosing a password (register, reset). */
    fun validateNewPassword(password: String): String? = validateLoginPassword(password) ?: when {
        !password.any { it.isDigit() } -> "Doit contenir au moins un chiffre"
        !password.any { it.isLetter() } -> "Doit contenir au moins une lettre"
        else -> null
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): String? = when {
        confirmation.isEmpty() -> "Confirmation requise"
        password != confirmation -> "Les mots de passe ne correspondent pas"
        else -> null
    }

    fun validateName(name: String): String? {
        val value = name.trim()
        return when {
            value.isEmpty() -> "Nom requis"
            value.length < 2 -> "Nom trop court"
            else -> null
        }
    }

    fun validateOtp(otp: String): String? =
        if (otp.length == OTP_LENGTH && otp.all { it.isDigit() }) null else "Code à $OTP_LENGTH chiffres requis"

    /** Whether [otp] is acceptable as partial input while the user is still typing. */
    fun isOtpInput(otp: String): Boolean = otp.length <= OTP_LENGTH && otp.all { it.isDigit() }
}
