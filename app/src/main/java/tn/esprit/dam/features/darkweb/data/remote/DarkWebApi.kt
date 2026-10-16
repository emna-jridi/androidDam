package tn.esprit.dam.features.darkweb.data.remote

import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.Config
import tn.esprit.dam.features.darkweb.data.model.Breach
import tn.esprit.dam.core.network.KtorHttpClient
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class ManualBreachResult(
    @SerialName("Name") val name: String = "",
    @SerialName("BreachDate") val breachDate: String = "",
    @SerialName("Description") val description: String = "",
    @SerialName("DataClasses") val dataClasses: List<String> = emptyList(),
    @SerialName("IsResolved") val isResolved: Boolean = false,
    @SerialName("Domain") val domain: String = ""
)

@Serializable
data class PasswordCheckResult(
    val count: Int = 0
)

class DarkWebApi @Inject constructor(
    private val client: KtorHttpClient
) {
    private val baseUrl: String get() = Config.Endpoints.DARKWEB

    suspend fun getBreaches(): ApiResult<List<Breach>> {
        return client.get("$baseUrl/breaches")
    }

    suspend fun checkNow(): ApiResult<Unit> {
        return client.post("$baseUrl/check", emptyMap<String, String>())
    }

    suspend fun checkEmail(email: String): ApiResult<List<ManualBreachResult>> {
        return client.post("$baseUrl/check-email", mapOf("email" to email))
    }

    suspend fun checkPassword(prefix: String): ApiResult<PasswordCheckResult> {
        return client.post("$baseUrl/check-password", mapOf("prefix" to prefix))
    }

    suspend fun resolveBreach(breachId: String): ApiResult<Unit> {
        return client.post("$baseUrl/resolve/$breachId", emptyMap<String, String>())
    }
}
