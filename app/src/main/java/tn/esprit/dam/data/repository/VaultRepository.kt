package tn.esprit.dam.data.repository

import tn.esprit.dam.data.model.*
import tn.esprit.dam.data.remote.api.VaultApi
import tn.esprit.dam.utils.VaultCrypto
import javax.crypto.SecretKey
import tn.esprit.dam.data.remote.ai.OllamaAdvice
import tn.esprit.dam.features.vault.utils.PasswordAnalysisMetrics

/**
 * Repository for vault operations
 * Handles client-side encryption/decryption before API calls
 */
class VaultRepository(private val api: VaultApi) {

    // In-memory encryption key (cleared on lock)
    private var encryptionKey: SecretKey? = null
    private var vaultSalt: String? = null

    // ========== Vault Operations ==========

    suspend fun createVault(masterPassword: String): Result<CreateVaultResponse> {
        return api.createVault(CreateVaultRequest(masterPassword))
            .onSuccess { response ->
                // Derive and cache encryption key
                vaultSalt = response.salt
                encryptionKey = VaultCrypto.deriveKeyFromPassword(masterPassword, response.salt)
            }
    }

    suspend fun unlockVault(masterPassword: String): Result<VaultUnlockResponse> {
        return api.unlockVault(UnlockVaultRequest(masterPassword))
            .onSuccess { response ->
                if (response.success && response.salt != null) {
                    // Derive and cache encryption key
                    vaultSalt = response.salt
                    encryptionKey = VaultCrypto.deriveKeyFromPassword(masterPassword, response.salt)
                }
            }
    }

    suspend fun getVault(): Result<Vault> {
        return api.getVault()
    }

    suspend fun updateVaultSettings(
        autoLockTimeout: Long? = null,
        paranoidMode: Boolean? = null,
        twoFactorEnabled: Boolean? = null
    ): Result<Vault> {
        return api.updateVaultSettings(
            UpdateVaultSettingsRequest(
                autoLockTimeout = autoLockTimeout,
                paranoidMode = paranoidMode,
                twoFactorEnabled = twoFactorEnabled
            )
        )
    }

    suspend fun checkAutoLock(): Result<VaultCheckLockResponse> {
        return api.checkAutoLock()
    }

    fun lockVault() {
        encryptionKey?.let { VaultCrypto.clearKey(it) }
        encryptionKey = null
        vaultSalt = null
    }

    fun isUnlocked(): Boolean = encryptionKey != null

    // ========== Password Entry Operations ==========

    suspend fun createPasswordEntry(
        site: String,
        username: String,
        password: String,
        notes: String? = null,
        url: String? = null,
        category: String = PasswordCategory.OTHER,
        tags: List<String> = emptyList(),
        isFavorite: Boolean = false,
        strengthScore: Int? = null,
        strengthLevel: String? = null,
        estimatedCrackTime: String? = null,
        strengthIssues: List<String>? = null
    ): Result<PasswordEntry> {
        val key = encryptionKey ?: return Result.failure(Exception("Vault is locked"))

        // Encrypt password and notes client-side
        val encryptedPassword = VaultCrypto.encrypt(password, key)
        val encryptedNotes = notes?.let { VaultCrypto.encrypt(it, key) }

        return api.createPasswordEntry(
            CreatePasswordEntryRequest(
                site = site,
                username = username,
                encryptedPassword = encryptedPassword,
                encryptedNotes = encryptedNotes,
                url = url,
                category = category,
                tags = tags,
                isFavorite = isFavorite,
                strengthScore = strengthScore,
                strengthLevel = strengthLevel,
                estimatedCrackTime = estimatedCrackTime,
                strengthIssues = strengthIssues
            )
        ).map { it.entry }
    }

    suspend fun getPasswordEntries(category: String? = null): Result<List<PasswordEntry>> {
        return api.getPasswordEntries(category).map { it.entries }
    }

    suspend fun searchPasswordEntries(query: String): Result<List<PasswordEntry>> {
        return api.searchPasswordEntries(query).map { it.entries }
    }

    suspend fun getPasswordEntry(id: String): Result<PasswordEntry> {
        return api.getPasswordEntry(id).map { it.entry }
    }

    /**
     * Decrypt password from entry
     * Returns decrypted plaintext password
     */
    fun decryptPassword(entry: PasswordEntry): Result<String> {
        val key = encryptionKey ?: return Result.failure(Exception("Vault is locked"))
        return try {
            val decrypted = VaultCrypto.decrypt(entry.encryptedPassword, key)
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Decrypt notes from entry
     */
    fun decryptNotes(entry: PasswordEntry): Result<String?> {
        val key = encryptionKey ?: return Result.failure(Exception("Vault is locked"))
        return try {
            val decrypted = entry.encryptedNotes?.let { VaultCrypto.decrypt(it, key) }
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePasswordEntry(
        id: String,
        site: String? = null,
        username: String? = null,
        password: String? = null,
        notes: String? = null,
        url: String? = null,
        category: String? = null,
        tags: List<String>? = null,
        isFavorite: Boolean? = null,
        strengthScore: Int? = null,
        strengthLevel: String? = null,
        estimatedCrackTime: String? = null,
        strengthIssues: List<String>? = null
    ): Result<PasswordEntry> {
        val key = encryptionKey ?: return Result.failure(Exception("Vault is locked"))

        // Encrypt password and notes if provided
        val encryptedPassword = password?.let { VaultCrypto.encrypt(it, key) }
        val encryptedNotes = notes?.let { VaultCrypto.encrypt(it, key) }

        return api.updatePasswordEntry(
            id,
            UpdatePasswordEntryRequest(
                site = site,
                username = username,
                encryptedPassword = encryptedPassword,
                encryptedNotes = encryptedNotes,
                url = url,
                category = category,
                tags = tags,
                isFavorite = isFavorite,
                strengthScore = strengthScore,
                strengthLevel = strengthLevel,
                estimatedCrackTime = estimatedCrackTime,
                strengthIssues = strengthIssues
            )
        ).map { it.entry }
    }

    suspend fun deletePasswordEntry(id: String): Result<Unit> {
        return api.deletePasswordEntry(id)
    }

    suspend fun toggleFavorite(id: String): Result<PasswordEntry> {
        return api.toggleFavorite(id).map { it.entry }
    }

    /**
     * Analyze password strength
     * Sends plaintext password to server ONLY for analysis (never stored)
     */
    suspend fun analyzePassword(password: String): Result<PasswordStrengthResponse> {
        return api.analyzePassword(AnalyzePasswordRequest(password))
    }

    suspend fun analyzeWithAi(metrics: PasswordAnalysisMetrics): Result<OllamaAdvice> {
        return api.analyzePasswordWithAi(metrics)
    }

    /**
     * Generate secure random password
     */
    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        return VaultCrypto.generatePassword(
            length,
            includeUppercase,
            includeLowercase,
            includeNumbers,
            includeSymbols
        )
    }

    /**
     * Generate secure passphrase
     */
    fun generatePassphrase(wordCount: Int = 4): String {
        return VaultCrypto.generatePassphrase(wordCount)
    }
}
