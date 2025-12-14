package com.shadowguard.dam.data.remote.api

import com.shadowguard.dam.data.model.*
import android.util.Log
import tn.esprit.dam.data.Config
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for vault operations
 * Implements Zero-Knowledge architecture:
 * - Encryption/decryption happens CLIENT-SIDE ONLY
 * - Server never sees plaintext passwords or encryption keys
 */
class VaultApi(private val client: HttpClient) {
    
    companion object {
        // Backend vault routes are at /vault, not /api/vault
        private val BASE_URL: String get() = Config.BASE_URL
        private val VAULT_URL: String get() = "$BASE_URL/vault"
        private val PASSWORDS_URL: String get() = "$BASE_URL/vault/passwords"
    }

    // ========== Vault Operations ==========
    
    suspend fun createVault(request: CreateVaultRequest): Result<CreateVaultResponse> {
        return try {
            val response = client.post("$VAULT_URL") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlockVault(request: UnlockVaultRequest): Result<VaultUnlockResponse> {
        return try {
            val response = client.post("$VAULT_URL/unlock") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVault(): Result<Vault> {
        return try {
            val response = client.get("$VAULT_URL")
            Result.success(response.body())
        } catch (e: Exception) {
            Log.e("VaultApi", "getVault failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** Lightweight status check: true if vault exists, false if 404 */
    suspend fun getVaultStatus(): Result<Boolean> {
        return try {
            Log.d("VaultApi", "getVaultStatus: GET $VAULT_URL")
            val response = client.get("$VAULT_URL")
            Log.d("VaultApi", "getVaultStatus response: ${response.status}")
            Result.success(response.status == HttpStatusCode.OK)
        } catch (e: Exception) {
            // If server returns 404, ktor throws ClientRequestException; detect status
            if (e is io.ktor.client.plugins.ClientRequestException) {
                val status = e.response.status
                if (status == HttpStatusCode.NotFound) {
                    Log.w("VaultApi", "getVaultStatus: 404 NotFound")
                    return Result.success(false)
                }
            }
            Log.e("VaultApi", "getVaultStatus failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateVaultSettings(request: UpdateVaultSettingsRequest): Result<Vault> {
        return try {
            val response = client.put("$VAULT_URL/settings") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkAutoLock(): Result<VaultCheckLockResponse> {
        return try {
            val response = client.get("$VAULT_URL/check-lock")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== Password Entry Operations ==========

    suspend fun createPasswordEntry(request: CreatePasswordEntryRequest): Result<CreatePasswordEntryResponse> {
        return try {
            val response = client.post("$PASSWORDS_URL") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPasswordEntries(category: String? = null): Result<PasswordEntriesResponse> {
        return try {
            val response = client.get("$PASSWORDS_URL") {
                category?.let { parameter("category", it) }
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchPasswordEntries(query: String): Result<PasswordEntriesResponse> {
        return try {
            val response = client.get("$PASSWORDS_URL/search") {
                parameter("q", query)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPasswordEntry(id: String): Result<PasswordEntryResponse> {
        return try {
            val response = client.get("$PASSWORDS_URL/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePasswordEntry(id: String, request: UpdatePasswordEntryRequest): Result<CreatePasswordEntryResponse> {
        return try {
            val response = client.put("$PASSWORDS_URL/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePasswordEntry(id: String): Result<Unit> {
        return try {
            client.delete("$PASSWORDS_URL/$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(id: String): Result<CreatePasswordEntryResponse> {
        return try {
            val response = client.post("$PASSWORDS_URL/$id/favorite")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzePassword(request: AnalyzePasswordRequest): Result<PasswordStrengthResponse> {
        return try {
            val response = client.post("$PASSWORDS_URL/analyze") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
