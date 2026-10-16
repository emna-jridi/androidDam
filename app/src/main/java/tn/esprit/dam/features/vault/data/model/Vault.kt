package tn.esprit.dam.features.vault.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Vault(
    val vaultId: String,
    val autoLockTimeout: Long = 300000L,
    val paranoidMode: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val lastUnlockedAt: String,
    val failedUnlockAttempts: Int = 0,
    val isLocked: Boolean? = false
)

@Serializable
data class CreateVaultRequest(
    val masterPassword: String
)

@Serializable
data class CreateVaultResponse(
    val message: String,
    val vaultId: String,
    val salt: String
)

@Serializable
data class UnlockVaultRequest(
    val masterPassword: String
)

@Serializable
data class VaultUnlockResponse(
    val success: Boolean,
    val salt: String? = null,
    val vaultId: String? = null,
    val message: String? = null
)

@Serializable
data class UpdateVaultSettingsRequest(
    val autoLockTimeout: Long? = null,
    val paranoidMode: Boolean? = null,
    val twoFactorEnabled: Boolean? = null
)

/** PUT /vault/settings returns only the updated settings, not the whole vault. */
@Serializable
data class VaultSettingsResponse(
    val message: String? = null,
    val autoLockTimeout: Long = 300000L,
    val paranoidMode: Boolean = false,
    val twoFactorEnabled: Boolean = false
)

@Serializable
data class VaultCheckLockResponse(
    val shouldLock: Boolean,
    val lastUnlockedAt: String,
    val autoLockTimeout: Long
)
