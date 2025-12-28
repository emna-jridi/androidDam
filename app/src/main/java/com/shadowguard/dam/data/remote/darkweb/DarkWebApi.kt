package com.shadowguard.dam.data.remote.darkweb

import com.shadowguard.dam.data.model.Breach

import tn.esprit.dam.data.remote.KtorHttpClient
import javax.inject.Inject

// Using KtorHttpClient wrapper pattern consistent with VaultApi
class DarkWebApi @Inject constructor(
    private val client: KtorHttpClient
) {
    private val BASE_URL = "http://10.206.164.76:3000/darkweb" // Adjust based on environment

    suspend fun getBreaches(): Result<List<Breach>> {
        return client.get("$BASE_URL/breaches")
    }

    suspend fun checkNow(): Result<Unit> {
        return client.post("$BASE_URL/check", emptyMap<String, String>())
    }

    suspend fun checkEmail(email: String): Result<List<Map<String, Any>>> {
        // Expecting list of objects. Using Map for ad-hoc manual check result or reuse Breach model if compatible
        // Let's define a DTO or just use generic implementation for now to be safe
        // The endpoint returns a JSON array.
        return client.post("$BASE_URL/check-email", mapOf("email" to email))
    }

    suspend fun checkPassword(prefix: String): Result<Map<String, Int>> {
        // Returns { "count": 123 }
        return client.post("$BASE_URL/check-password", mapOf("prefix" to prefix))
    }

    suspend fun resolveBreach(breachId: String): Result<Unit> {
        return client.post("$BASE_URL/resolve/$breachId", emptyMap<String, String>())
    }
}
