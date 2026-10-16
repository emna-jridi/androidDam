package tn.esprit.dam.features.vault.data.repository

import tn.esprit.dam.features.vault.data.model.*
import tn.esprit.dam.features.vault.data.ai.OllamaAdvice
import tn.esprit.dam.features.vault.domain.password.PasswordAnalysisMetrics

interface VaultRepository {
    suspend fun createVault(masterPassword: String): Result<CreateVaultResponse>

    suspend fun unlockVault(masterPassword: String): Result<VaultUnlockResponse>

    suspend fun getVault(): Result<Vault>

    /** true if the user already created a vault, false if not. */
    suspend fun hasVault(): Result<Boolean>

    suspend fun updateVaultSettings(
        autoLockTimeout: Long? = null,
        paranoidMode: Boolean? = null,
        twoFactorEnabled: Boolean? = null
    ): Result<VaultSettingsResponse>

    suspend fun checkAutoLock(): Result<VaultCheckLockResponse>

    fun lockVault()

    fun isUnlocked(): Boolean

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
    ): Result<PasswordEntry>

    suspend fun getPasswordEntries(category: String? = null): Result<List<PasswordEntry>>

    suspend fun searchPasswordEntries(query: String): Result<List<PasswordEntry>>

    suspend fun getPasswordEntry(id: String): Result<PasswordEntry>

    fun decryptPassword(entry: PasswordEntry): Result<String>

    fun decryptNotes(entry: PasswordEntry): Result<String?>

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
    ): Result<PasswordEntry>

    suspend fun deletePasswordEntry(id: String): Result<Unit>

    suspend fun toggleFavorite(id: String): Result<PasswordEntry>

    suspend fun analyzePassword(password: String): Result<PasswordStrengthResponse>

    suspend fun analyzeWithAi(metrics: PasswordAnalysisMetrics): Result<OllamaAdvice>
}
