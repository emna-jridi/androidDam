package com.shadowguard.dam.ui.vault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowguard.dam.data.model.Vault
import com.shadowguard.dam.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VaultUiState {
    object Initial : VaultUiState()
    object Loading : VaultUiState()
    object NoVault : VaultUiState() // First-time setup
    object Locked : VaultUiState()
    data class Unlocked(val vault: Vault) : VaultUiState()
    data class Error(val message: String) : VaultUiState()
}

class VaultViewModel(private val repository: VaultRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Initial)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val _createVaultState = MutableStateFlow<Result<String>?>(null)
    val createVaultState: StateFlow<Result<String>?> = _createVaultState.asStateFlow()

    init {
        checkVaultStatus()
    }

    private fun checkVaultStatus() {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading
            
            repository.getVault()
                .onSuccess { vault ->
                    _uiState.value = if (repository.isUnlocked()) {
                        VaultUiState.Unlocked(vault)
                    } else {
                        VaultUiState.Locked
                    }
                }
                .onFailure { error ->
                    // Vault not found = first-time setup
                    if (error.message?.contains("not found", ignoreCase = true) == true) {
                        _uiState.value = VaultUiState.NoVault
                    } else {
                        _uiState.value = VaultUiState.Error(error.message ?: "Unknown error")
                    }
                }
        }
    }

    fun createVault(masterPassword: String, confirmPassword: String) {
        if (masterPassword != confirmPassword) {
            _createVaultState.value = Result.failure(Exception("Passwords do not match"))
            return
        }

        if (masterPassword.length < 8) {
            _createVaultState.value = Result.failure(Exception("Master password must be at least 8 characters"))
            return
        }

        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading
            
            repository.createVault(masterPassword)
                .onSuccess { response ->
                    _createVaultState.value = Result.success(response.vaultId)
                    // Automatically unlock the vault after creation
                    unlockVault(masterPassword)
                }
                .onFailure { error ->
                    _createVaultState.value = Result.failure(error)
                    _uiState.value = VaultUiState.Error(error.message ?: "Failed to create vault")
                }
        }
    }

    fun unlockVault(masterPassword: String) {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading
            
            repository.unlockVault(masterPassword)
                .onSuccess { response ->
                    if (response.success) {
                        checkVaultStatus() // Will update to Unlocked state
                    } else {
                        _uiState.value = VaultUiState.Error(response.message ?: "Failed to unlock vault")
                    }
                }
                .onFailure { error ->
                    _uiState.value = VaultUiState.Error(error.message ?: "Failed to unlock vault")
                }
        }
    }

    fun lockVault() {
        repository.lockVault()
        _uiState.value = VaultUiState.Locked
    }

    fun updateSettings(
        autoLockTimeout: Long? = null,
        paranoidMode: Boolean? = null,
        twoFactorEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            repository.updateVaultSettings(autoLockTimeout, paranoidMode, twoFactorEnabled)
                .onSuccess {
                    checkVaultStatus() // Refresh state
                }
                .onFailure { error ->
                    _uiState.value = VaultUiState.Error(error.message ?: "Failed to update settings")
                }
        }
    }

    fun checkAutoLock() {
        viewModelScope.launch {
            repository.checkAutoLock()
                .onSuccess { response ->
                    if (response.shouldLock) {
                        lockVault()
                    }
                }
        }
    }

    fun clearCreateVaultState() {
        _createVaultState.value = null
    }
}
