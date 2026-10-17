package tn.esprit.dam.features.vault.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tn.esprit.dam.core.network.toResult
import tn.esprit.dam.features.vault.data.model.*
import tn.esprit.dam.features.vault.data.remote.VaultApi
import tn.esprit.dam.features.vault.data.crypto.VaultBiometricStore
import tn.esprit.dam.features.vault.data.crypto.VaultCrypto
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import tn.esprit.dam.features.vault.data.ai.OllamaAdvice
import tn.esprit.dam.features.vault.domain.password.PasswordAnalysisMetrics

class VaultRepositoryImpl @Inject constructor(
    private val api: VaultApi,
    private val biometricStore: VaultBiometricStore
) : VaultRepository {
    private var encryptionKey: SecretKey? = null
        set(value) {
            field = value
            _unlocked.value = value != null
        }
    private var vaultSalt: String? = null

    private val _unlocked = MutableStateFlow(false)
    override val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    override suspend fun createVault(masterPassword: String): Result<CreateVaultResponse> {
        return api.createVault(CreateVaultRequest(masterPassword)).toResult()
            .onSuccess { response ->
                vaultSalt = response.salt
                encryptionKey = VaultCrypto.deriveKeyFromPassword(masterPassword, response.salt)
            }
    }

    override suspend fun unlockVault(masterPassword: String): Result<VaultUnlockResponse> {
        return api.unlockVault(UnlockVaultRequest(masterPassword)).toResult()
            .onSuccess { response ->
                if (response.success && response.salt != null) {
                    vaultSalt = response.salt
                    encryptionKey = VaultCrypto.deriveKeyFromPassword(masterPassword, response.salt)
                }
            }
    }

    override suspend fun getVault(): Result<Vault> {
        return api.getVault().toResult()
    }

    override suspend fun hasVault(): Result<Boolean> {
        return api.getVaultStatus().toResult()
    }

    override suspend fun updateVaultSettings(
        autoLockTimeout: Long?,
        paranoidMode: Boolean?,
        twoFactorEnabled: Boolean?
    ): Result<VaultSettingsResponse> {
        return api.updateVaultSettings(
            UpdateVaultSettingsRequest(
                autoLockTimeout = autoLockTimeout,
                paranoidMode = paranoidMode,
                twoFactorEnabled = twoFactorEnabled
            )
        ).toResult()
    }

    override suspend fun checkAutoLock(): Result<VaultCheckLockResponse> {
        return api.checkAutoLock().toResult()
    }

    override fun lockVault() {
        encryptionKey?.let { VaultCrypto.clearKey(it) }
        encryptionKey = null
        vaultSalt = null
    }

    override fun isUnlocked(): Boolean = encryptionKey != null

    override fun isBiometricUnlockEnabled(): Boolean = biometricStore.isEnabled()

    override fun biometricEnableCipher(): Cipher? =
        if (encryptionKey == null) null else biometricStore.encryptCipher()

    override fun biometricUnlockCipher(): Cipher? = biometricStore.decryptCipher()

    override fun enableBiometricUnlock(cipher: Cipher): Result<Unit> = runCatching {
        val key = checkNotNull(encryptionKey) { "Vault is locked" }
        biometricStore.save(cipher, key.encoded)
    }

    override fun unlockWithBiometric(cipher: Cipher): Result<Unit> = runCatching {
        val keyBytes = checkNotNull(biometricStore.read(cipher)) { "Biometric unlock is off" }
        encryptionKey = SecretKeySpec(keyBytes, "AES")
    }

    override fun disableBiometricUnlock() = biometricStore.clear()

    override suspend fun createPasswordEntry(
        site: String,
        username: String,
        password: String,
        notes: String?,
        url: String?,
        category: String,
        tags: List<String>,
        isFavorite: Boolean,
        strengthScore: Int?,
        strengthLevel: String?,
        estimatedCrackTime: String?,
        strengthIssues: List<String>?
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

    override suspend fun getPasswordEntries(category: String?): Result<List<PasswordEntry>> {
        return api.getPasswordEntries(category).toResult().map { it.entries }
    }

    override suspend fun searchPasswordEntries(query: String): Result<List<PasswordEntry>> {
        return api.searchPasswordEntries(query).toResult().map { it.entries }
    }

    override suspend fun getPasswordEntry(id: String): Result<PasswordEntry> {
        return api.getPasswordEntry(id).toResult().map { it.entry }
    }

    override fun decryptPassword(entry: PasswordEntry): Result<String> {
        val key = encryptionKey ?: return Result.failure(Exception("Vault is locked"))
        return try {
            val decrypted = VaultCrypto.decrypt(entry.encryptedPassword, key)
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun decryptNotes(entry: PasswordEntry): Result<String?> {
        val key = encryptionKey ?: return Result.failure(Exception("Vault is locked"))
        return try {
            val decrypted = entry.encryptedNotes?.let { VaultCrypto.decrypt(it, key) }
            Result.success(decrypted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePasswordEntry(
        id: String,
        site: String?,
        username: String?,
        password: String?,
        notes: String?,
        url: String?,
        category: String?,
        tags: List<String>?,
        isFavorite: Boolean?,
        strengthScore: Int?,
        strengthLevel: String?,
        estimatedCrackTime: String?,
        strengthIssues: List<String>?
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

    override suspend fun deletePasswordEntry(id: String): Result<Unit> {
        return api.deletePasswordEntry(id).toResult()
    }

    override suspend fun toggleFavorite(id: String): Result<PasswordEntry> {
        return api.toggleFavorite(id).toResult().map { it.entry }
    }


    override suspend fun analyzeWithAi(metrics: PasswordAnalysisMetrics): Result<OllamaAdvice> {
        return api.analyzePasswordWithAi(metrics).toResult()
    }

}
