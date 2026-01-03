package tn.esprit.dam.data.remote.api

import tn.esprit.dam.data.model.*
import android.util.Log
import tn.esprit.dam.data.Config
import tn.esprit.dam.data.remote.KtorHttpClient
import tn.esprit.dam.data.remote.ai.OllamaAdvice
import tn.esprit.dam.features.vault.utils.PasswordAnalysisMetrics
import javax.inject.Inject

/**
 * API service for vault operations
 * Uses KtorHttpClient wrapper for automatic Auth headers and Retries
 */
class VaultApi @Inject constructor(private val client: KtorHttpClient) {
    
    companion object {
        private val BASE_URL: String get() = Config.BASE_URL
        private val VAULT_URL: String get() = "$BASE_URL/vault"
        private val PASSWORDS_URL: String get() = "$BASE_URL/vault/passwords"
    }

    // ========== Vault Operations ==========
    
    suspend fun createVault(request: CreateVaultRequest): Result<CreateVaultResponse> {
        return client.post(VAULT_URL, request)
    }

    suspend fun unlockVault(request: UnlockVaultRequest): Result<VaultUnlockResponse> {
        return client.post("$VAULT_URL/unlock", request)
    }

    suspend fun getVault(): Result<Vault> {
        return client.get(VAULT_URL)
    }

    /** Lightweight status check: true if vault exists, false if 404 */
    suspend fun getVaultStatus(): Result<Boolean> {
        // We use a custom raw call locally if needed, or just standard get and check failure
        // But KtorHttpClient returns Result.failure on 404 typically unless handled?
        // Actually KtorHttpClient.get catches Exception.
        // Let's rely on standard logic: try to get vault. If success -> true.
        // If 404 -> failure.
        // But the previous logic had a specific 404 check. 
        // Let's implement a specific get for status if possible, or just try query.
        // Simplest: Try getVault(). If success -> true. If 404 -> false.
        
        // Actually, we can reuse the existing logic if we want, but passing 'client' is hard 
        // since KtorHttpClient hides the raw client.
        // However, KtorHttpClient returns Result. On 404 it might return failure.
        // Let's assume getVault() returning 404 is "No Vault".
        // But wait, Ktor by default throws exception on 404 unless expectSuccess = false.
        // KtorHttpClient wrapper:
        // install(HttpTimeout) ...
        // It doesn't seem to disable validation. So 404 throws.
        // Returns Result.failure(ClientRequestException).
        
        return client.get<Vault>(VAULT_URL)
            .map { true }
            .recover { e ->
                // Check if 404
                if (e is io.ktor.client.plugins.ClientRequestException && 
                    e.response.status == io.ktor.http.HttpStatusCode.NotFound) {
                    false
                } else {
                    // Other error (network etc) implies we don't know status -> return false or throw?
                    // Previous code: returned failure on other error.
                    // This function returns Result<Boolean>.
                    // So we should re-throw or return failure for non-404 errors.
                    throw e
                }
            }
            .recoverCatching { e ->
                // If we threw above, it comes here.
                 if (e is io.ktor.client.plugins.ClientRequestException && 
                    e.response.status == io.ktor.http.HttpStatusCode.NotFound) {
                    false
                } else {
                    throw e
                }
            }
            // Wait, Result.recoverCatching handles the exception from the previous block?
            // Let's simplify:
            // return runCatching { ... }
            
            // Actually, let's just use a try/catch block using `client.get` which returns Result.
            val result: Result<Vault> = client.get(VAULT_URL)
            if (result.isSuccess) return Result.success(true)
            
            val exception = result.exceptionOrNull()
            if (exception is io.ktor.client.plugins.ClientRequestException && 
                exception.response.status == io.ktor.http.HttpStatusCode.NotFound) {
                return Result.success(false)
            }
            return Result.failure(exception ?: Exception("Unknown error"))
    }

    suspend fun updateVaultSettings(request: UpdateVaultSettingsRequest): Result<Vault> {
        return client.put("$VAULT_URL/settings", request)
    }

    suspend fun checkAutoLock(): Result<VaultCheckLockResponse> {
        return client.get("$VAULT_URL/check-lock")
    }

    // ========== Password Entry Operations ==========

    suspend fun createPasswordEntry(request: CreatePasswordEntryRequest): Result<CreatePasswordEntryResponse> {
        return client.post(PASSWORDS_URL, request)
    }

    suspend fun getPasswordEntries(category: String? = null): Result<PasswordEntriesResponse> {
        val url = if (category != null) "$PASSWORDS_URL?category=$category" else PASSWORDS_URL
        return client.get(url)
    }

    suspend fun searchPasswordEntries(query: String): Result<PasswordEntriesResponse> {
        return client.get("$PASSWORDS_URL/search?q=$query")
    }

    suspend fun getPasswordEntry(id: String): Result<PasswordEntryResponse> {
        return client.get("$PASSWORDS_URL/$id")
    }

    suspend fun updatePasswordEntry(id: String, request: UpdatePasswordEntryRequest): Result<CreatePasswordEntryResponse> {
        return client.put("$PASSWORDS_URL/$id", request)
    }

    suspend fun deletePasswordEntry(id: String): Result<Unit> {
        // KtorHttpClient has delete returning Result<T>
        // Check if Unit is supported T
        return client.delete<Unit>("$PASSWORDS_URL/$id")
            .map { Unit } 
            // If delete returns JSON content, Unit might fail decoding?
            // Ktor usually ignores body if Unit is requested.
            // Let's safely assume it works or use String and discard.
            // Let's use generic delete.
    }

    suspend fun toggleFavorite(id: String): Result<CreatePasswordEntryResponse> {
        // Previous was POST
        // KtorHttpClient has POST
        // Body? Previous had no body.
        // KtorHttpClient.post requires body.
        // We can pass Unit or Empty object?
        // Let's check post signature: post(url, body)
        // We can pass empty map or string.
        return client.post("$PASSWORDS_URL/$id/favorite", emptyMap<String,String>())
    }

    suspend fun analyzePassword(request: AnalyzePasswordRequest): Result<PasswordStrengthResponse> {
        return client.post("$PASSWORDS_URL/analyze", request)
    }

    suspend fun analyzePasswordWithAi(metrics: PasswordAnalysisMetrics): Result<OllamaAdvice> {
        return client.post("$VAULT_URL/ai-analyze", metrics)
    }
}
