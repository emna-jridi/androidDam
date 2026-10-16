package tn.esprit.dam.features.vault.data.repository

import tn.esprit.dam.core.model.*
import tn.esprit.dam.core.network.toResult
import tn.esprit.dam.features.alert.data.model.*
import tn.esprit.dam.features.auth.data.model.*
import tn.esprit.dam.features.darkweb.data.model.*
import tn.esprit.dam.features.profile.data.model.*
import tn.esprit.dam.features.report.data.model.*
import tn.esprit.dam.features.vault.data.model.*
import tn.esprit.dam.features.vault.data.remote.VaultApi
import tn.esprit.dam.features.vault.data.crypto.VaultCrypto
import javax.crypto.SecretKey
import tn.esprit.dam.features.vault.data.ai.OllamaAdvice
import tn.esprit.dam.features.vault.domain.password.PasswordAnalysisMetrics

class VaultRepository(private val api: VaultApi) {
    private var encryptionKey: SecretKey? = null
    private var vaultSalt: String? = null

    suspend fun createVault(masterPassword: String): Result<CreateVaultResponse> {
        return api.createVault(CreateVaultRequest(masterPassword)).toResult()
            .onSuccess { response ->
                vaultSalt = response.salt
                encryptionKey = VaultCrypto.deriveKeyFromPassword(masterPassword, response.salt)
            }
    }

    suspend fun unlockVault(masterPassword: String): Result<VaultUnlockResponse> {
        return api.unlockVault(UnlockVaultRequest(masterPassword)).toResult()
            .onSuccess { response ->
                if (response.success && response.salt != null) {
                    vaultSalt = response.salt
                    encryptionKey = VaultCrypto.deriveKeyFromPassword(masterPassword, response.salt)
                }
            }
    }

    suspend fun getVault(): Result<Vault> {
        return api.getVault().toResult()
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
        ).toResult()
    }

    suspend fun checkAutoLock(): Result<VaultCheckLockResponse> {
        return api.checkAutoLock().toResult()
    }

    fun lockVault() {
        encryptionKey?.let { VaultCrypto.clearKey(it) }
        encryptionKey = null
        vaultSalt = null
    }

    fun isUnlocked(): Boolean = encryptionKey != null

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
        ).toResult().map { it.entry }
    }

    suspend fun getPasswordEntries(category: String? = null): Result<List<PasswordEntry>> {
        return api.getPasswordEntries(category).toResult().map { it.entries }
    }

    suspend fun searchPasswordEntries(query: String): Result<List<PasswordEntry>> {
        return api.searchPasswordEntries(query).toResult().map { it.entries }
    }

    suspend fun getPasswordEntry(id: String): Result<PasswordEntry> {
        return api.getPasswordEntry(id).toResult().map { it.entry }
    }

    fun decryptPassword(entry: PasswordEntry): Result<String> {
        val key = encryptionKey ?: return Result.failure(Exception("Vault is locked"))
        return try {
            val decrypted = VaultCrypto.decrypt(entry.encryptedPassword, key)
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        ).toResult().map { it.entry }
    }

    suspend fun deletePasswordEntry(id: String): Result<Unit> {
        return api.deletePasswordEntry(id).toResult()
    }

    suspend fun toggleFavorite(id: String): Result<PasswordEntry> {
        return api.toggleFavorite(id).toResult().map { it.entry }
    }

    suspend fun analyzePassword(password: String): Result<PasswordStrengthResponse> {
        return api.analyzePassword(AnalyzePasswordRequest(password)).toResult()
    }

    suspend fun analyzeWithAi(metrics: PasswordAnalysisMetrics): Result<OllamaAdvice> {
        return api.analyzePasswordWithAi(metrics).toResult()
    }

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

    fun generatePassphrase(wordCount: Int = 4): String {
        return VaultCrypto.generatePassphrase(wordCount)
    }
}
