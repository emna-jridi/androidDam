package tn.esprit.dam.features.darkweb.data.remote

import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.Config
import tn.esprit.dam.features.darkweb.data.model.Breach
import tn.esprit.dam.features.darkweb.data.model.ManualBreachResult
import tn.esprit.dam.features.darkweb.data.model.PasswordCheckResult
import tn.esprit.dam.core.network.KtorHttpClient
import javax.inject.Inject

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
