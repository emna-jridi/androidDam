package tn.esprit.dam.features.darkweb.domain

import java.security.MessageDigest
import javax.inject.Inject
import tn.esprit.dam.features.darkweb.data.repository.DarkWebRepository

/**
 * How many times a password appears in known leaks (0 = not found), via k-anonymity:
 * the password is hashed on the device and only the first 5 hex characters of the hash are sent.
 */
class CheckPasswordLeakUseCase @Inject constructor(
    private val repository: DarkWebRepository
) {
    suspend operator fun invoke(password: String): Result<Int> {
        val hash = sha1Hex(password)
        return repository.checkPassword(prefix = hash.take(5), suffix = hash.drop(5))
    }

    companion object {
        fun sha1Hex(value: String): String =
            MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
                .joinToString("") { "%02x".format(it) }
                .uppercase()
    }
}
