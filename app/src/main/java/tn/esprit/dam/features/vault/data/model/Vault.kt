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
    val isLocked: Boolean? = false,
    /** Key-derivation salt, needed before unlock. */
    val salt: String? = null,
    /** 1 = legacy vault that still needs the one-time master password upgrade, 2 = auth hash only. */
    val authVersion: Int = 1
)

/** The master password never leaves the device: only a one-way hash of the derived key. */
@Serializable
data class CreateVaultRequest(
    val salt: String,
    val authHash: String
)

@Serializable
data class CreateVaultResponse(
    val message: String,
    val vaultId: String,
    val salt: String
)

/** [masterPassword] is only sent once, to upgrade a legacy (authVersion 1) vault. */
@Serializable
data class UnlockVaultRequest(
    val authHash: String,
    val masterPassword: String? = null
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
