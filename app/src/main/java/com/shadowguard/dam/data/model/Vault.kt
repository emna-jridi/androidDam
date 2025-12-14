package com.shadowguard.dam.data.model

import kotlinx.serialization.Serializable

/**
 * Vault data models matching backend API
 */
@Serializable
data class Vault(
    val vaultId: String,
    val autoLockTimeout: Long = 300000L, // 5 minutes default
    val paranoidMode: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val lastUnlockedAt: String,
    val failedUnlockAttempts: Int = 0,
    val isLocked: Boolean = false
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

@Serializable
data class VaultCheckLockResponse(
    val shouldLock: Boolean,
    val lastUnlockedAt: String,
    val autoLockTimeout: Long
)
