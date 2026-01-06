package tn.esprit.dam.data.remote.darkweb

import tn.esprit.dam.data.model.Breach
import tn.esprit.dam.data.remote.KtorHttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * DTO for manual email breach check result from HaveIBeenPwned/LeakCheck
 */
@Serializable
data class ManualBreachResult(
    @SerialName("Name") val name: String = "",
    @SerialName("BreachDate") val breachDate: String = "",
    @SerialName("Description") val description: String = "",
    @SerialName("DataClasses") val dataClasses: List<String> = emptyList(),
    @SerialName("IsResolved") val isResolved: Boolean = false,
    @SerialName("Domain") val domain: String = ""
)

/**
 * DTO for password check result
 */
@Serializable
data class PasswordCheckResult(
    val count: Int = 0
)

// Using KtorHttpClient wrapper pattern consistent with VaultApi
class DarkWebApi @Inject constructor(
    private val client: KtorHttpClient
) {
    private val BASE_URL = "http://172.18.1.18:3000/darkweb" // Adjust based on environment

    suspend fun getBreaches(): Result<List<Breach>> {
        return client.get("$BASE_URL/breaches")
    }

    suspend fun checkNow(): Result<Unit> {
        return client.post("$BASE_URL/check", emptyMap<String, String>())
    }

    suspend fun checkEmail(email: String): Result<List<ManualBreachResult>> {
        return client.post("$BASE_URL/check-email", mapOf("email" to email))
    }

    suspend fun checkPassword(prefix: String): Result<PasswordCheckResult> {
        return client.post("$BASE_URL/check-password", mapOf("prefix" to prefix))
    }

    suspend fun resolveBreach(breachId: String): Result<Unit> {
        return client.post("$BASE_URL/resolve/$breachId", emptyMap<String, String>())
    }
}
