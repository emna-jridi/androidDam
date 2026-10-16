package tn.esprit.dam.features.vault.data.remote

import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.Config
import tn.esprit.dam.core.network.KtorHttpClient
import tn.esprit.dam.core.network.map
import tn.esprit.dam.features.vault.data.ai.OllamaAdvice
import tn.esprit.dam.features.vault.data.model.AnalyzePasswordRequest
import tn.esprit.dam.features.vault.data.model.CreatePasswordEntryRequest
import tn.esprit.dam.features.vault.data.model.CreatePasswordEntryResponse
import tn.esprit.dam.features.vault.data.model.CreateVaultRequest
import tn.esprit.dam.features.vault.data.model.CreateVaultResponse
import tn.esprit.dam.features.vault.data.model.PasswordEntriesResponse
import tn.esprit.dam.features.vault.data.model.PasswordEntryResponse
import tn.esprit.dam.features.vault.data.model.PasswordStrengthResponse
import tn.esprit.dam.features.vault.data.model.UnlockVaultRequest
import tn.esprit.dam.features.vault.data.model.UpdatePasswordEntryRequest
import tn.esprit.dam.features.vault.data.model.UpdateVaultSettingsRequest
import tn.esprit.dam.features.vault.data.model.Vault
import tn.esprit.dam.features.vault.data.model.VaultCheckLockResponse
import tn.esprit.dam.features.vault.data.model.VaultUnlockResponse
import tn.esprit.dam.features.vault.domain.password.PasswordAnalysisMetrics
import javax.inject.Inject

class VaultApi @Inject constructor(private val client: KtorHttpClient) {
    companion object {
        private val VAULT_URL: String get() = "${Config.API_URL}/vault"
        private val PASSWORDS_URL: String get() = "${Config.API_URL}/vault/passwords"
    }

    suspend fun createVault(request: CreateVaultRequest): ApiResult<CreateVaultResponse> {
        return client.post(VAULT_URL, request)
    }

    suspend fun unlockVault(request: UnlockVaultRequest): ApiResult<VaultUnlockResponse> {
        return client.post("$VAULT_URL/unlock", request)
    }

    suspend fun getVault(): ApiResult<Vault> {
        return client.get(VAULT_URL)
    }

    /** true if the user has a vault, false on 404. */
    suspend fun getVaultStatus(): ApiResult<Boolean> {
        val result = client.get<Vault>(VAULT_URL)
        return if (result is ApiResult.ApiError && result.code == 404) {
            ApiResult.Success(false)
        } else {
            result.map { true }
        }
    }

    suspend fun updateVaultSettings(request: UpdateVaultSettingsRequest): ApiResult<Vault> {
        return client.put("$VAULT_URL/settings", request)
    }

    suspend fun checkAutoLock(): ApiResult<VaultCheckLockResponse> {
        return client.get("$VAULT_URL/check-lock")
    }

    suspend fun createPasswordEntry(request: CreatePasswordEntryRequest): ApiResult<CreatePasswordEntryResponse> {
        return client.post(PASSWORDS_URL, request)
    }

    suspend fun getPasswordEntries(category: String? = null): ApiResult<PasswordEntriesResponse> {
        val url = if (category != null) "$PASSWORDS_URL?category=$category" else PASSWORDS_URL
        return client.get(url)
    }

    suspend fun searchPasswordEntries(query: String): ApiResult<PasswordEntriesResponse> {
        return client.get("$PASSWORDS_URL/search?q=$query")
    }

    suspend fun getPasswordEntry(id: String): ApiResult<PasswordEntryResponse> {
        return client.get("$PASSWORDS_URL/$id")
    }

    suspend fun updatePasswordEntry(id: String, request: UpdatePasswordEntryRequest): ApiResult<CreatePasswordEntryResponse> {
        return client.put("$PASSWORDS_URL/$id", request)
    }

    suspend fun deletePasswordEntry(id: String): ApiResult<Unit> {
        return client.delete("$PASSWORDS_URL/$id")
    }

    suspend fun toggleFavorite(id: String): ApiResult<CreatePasswordEntryResponse> {
        return client.post("$PASSWORDS_URL/$id/favorite", emptyMap<String, String>())
    }

    suspend fun analyzePassword(request: AnalyzePasswordRequest): ApiResult<PasswordStrengthResponse> {
        return client.post("$PASSWORDS_URL/analyze", request)
    }

    suspend fun analyzePasswordWithAi(metrics: PasswordAnalysisMetrics): ApiResult<OllamaAdvice> {
        return client.post("$VAULT_URL/ai-analyze", metrics)
    }
}
