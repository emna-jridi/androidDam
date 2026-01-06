package tn.esprit.dam.features.vault.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import tn.esprit.dam.data.model.Vault
import tn.esprit.dam.data.repository.VaultRepository
import tn.esprit.dam.features.vault.biometric.BiometricAuthRepository
import tn.esprit.dam.features.vault.biometric.BiometricDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VaultUiState {
    object Initial : VaultUiState()
    object Loading : VaultUiState()
    object NoVault : VaultUiState() // First-time setup
    object Locked : VaultUiState()
    data class Unlocked(val vault: Vault) : VaultUiState()
    data class Error(val message: String) : VaultUiState()
}

/**
 * State for biometric registration/authentication operations
 */
sealed class BiometricOperationState {
    object Idle : BiometricOperationState()
    object Loading : BiometricOperationState()
    data class Success(val message: String) : BiometricOperationState()
    data class Error(val message: String) : BiometricOperationState()
}

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: VaultRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Initial)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val _createVaultState = MutableStateFlow<Result<String>?>(null)
    val createVaultState: StateFlow<Result<String>?> = _createVaultState.asStateFlow()
    
    // Biometric unlock state
    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()
    
    // Biometric registration state (for cryptographic biometric)
    private val _biometricRegistered = MutableStateFlow(false)
    val biometricRegistered: StateFlow<Boolean> = _biometricRegistered.asStateFlow()
    
    // Biometric operation state
    private val _biometricOperationState = MutableStateFlow<BiometricOperationState>(BiometricOperationState.Idle)
    val biometricOperationState: StateFlow<BiometricOperationState> = _biometricOperationState.asStateFlow()
    
    // List of registered devices
    private val _registeredDevices = MutableStateFlow<List<BiometricDeviceInfo>>(emptyList())
    val registeredDevices: StateFlow<List<BiometricDeviceInfo>> = _registeredDevices.asStateFlow()
    
    // Biometric auth repository
    private val biometricAuthRepository = BiometricAuthRepository(context)
    
    private var cachedMasterPassword: String? = null

    init {
        checkVaultStatus()
        checkBiometricRegistration()
    }
    
    /**
     * Check if this device is registered for cryptographic biometric auth
     */
    private fun checkBiometricRegistration() {
        val localRegistered = biometricAuthRepository.isDeviceRegistered()
        _biometricRegistered.value = localRegistered
        android.util.Log.d("VaultViewModel", "🔐 Local biometric registered: $localRegistered")
        
        // Also fetch status from backend
        viewModelScope.launch {
            biometricAuthRepository.getBiometricStatus()
                .onSuccess { status ->
                    android.util.Log.d("VaultViewModel", "🔐 Backend biometricEnabled: ${status.biometricEnabled}, deviceCount: ${status.deviceCount}")
                    // Only update if backend says we have registered devices
                    // But we need to verify if THIS device is registered
                    if (status.biometricEnabled && localRegistered) {
                        _biometricRegistered.value = true
                    } else if (!localRegistered) {
                        // Backend might say enabled but this device isn't registered locally
                        _biometricRegistered.value = false
                    }
                    _registeredDevices.value = status.devices ?: emptyList()
                }
                .onFailure { error ->
                    android.util.Log.e("VaultViewModel", "🔐 Failed to get biometric status: ${error.message}")
                }
        }
    }

    private fun checkVaultStatus() {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading
            
            repository.getVault()
                .onSuccess { vault ->
                    // Always require unlock on app start for security
                    // The encryptionKey is in-memory only and cleared when app is killed
                    _uiState.value = if (repository.isUnlocked()) {
                        VaultUiState.Unlocked(vault)
                    } else {
                        VaultUiState.Locked
                    }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: "Unknown error"
                    android.util.Log.e("VaultViewModel", "❌ getVault failed: $errorMsg")
                    
                    // Handle different error cases
                    when {
                        errorMsg.contains("not found", ignoreCase = true) -> {
                            _uiState.value = VaultUiState.NoVault
                        }
                        errorMsg.contains("401", ignoreCase = true) || 
                        errorMsg.contains("Unauthorized", ignoreCase = true) ||
                        errorMsg.contains("token", ignoreCase = true) -> {
                            // Token expired/invalid - show locked state so user can re-authenticate
                            _uiState.value = VaultUiState.Locked
                        }
                        else -> {
                            _uiState.value = VaultUiState.Error(errorMsg)
                        }
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
                        // Cache password for biometric unlock
                        cachedMasterPassword = masterPassword
                        _biometricEnabled.value = true
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
    
    /**
     * Unlock vault using cached credentials after biometric authentication
     * Only works if user previously unlocked with master password
     */
    fun unlockVaultWithBiometric() {
        cachedMasterPassword?.let { password ->
            unlockVault(password)
        } ?: run {
            _uiState.value = VaultUiState.Error("Please unlock with master password first")
        }
    }
    
    /**
     * Register this device for cryptographic biometric authentication.
     * This creates a key pair in Android Keystore and registers the public key with the backend.
     * Must be called while user is authenticated (after password unlock).
     * 
     * @param activity FragmentActivity required for BiometricPrompt
     * @param deviceName Optional custom name for this device
     */
    fun registerBiometricDevice(activity: FragmentActivity, deviceName: String? = null) {
        viewModelScope.launch {
            _biometricOperationState.value = BiometricOperationState.Loading
            
            val result = biometricAuthRepository.registerDevice(
                activity = activity,
                deviceName = deviceName ?: biometricAuthRepository.getDeviceName()
            )
            
            when (result) {
                is BiometricAuthRepository.RegistrationResult.Success -> {
                    _biometricOperationState.value = BiometricOperationState.Success(result.message)
                    _biometricRegistered.value = true
                    checkBiometricRegistration() // Refresh device list
                }
                is BiometricAuthRepository.RegistrationResult.Error -> {
                    _biometricOperationState.value = BiometricOperationState.Error(result.message)
                }
                BiometricAuthRepository.RegistrationResult.BiometricNotAvailable -> {
                    _biometricOperationState.value = BiometricOperationState.Error(
                        "Biometric authentication not available on this device"
                    )
                }
                BiometricAuthRepository.RegistrationResult.Cancelled -> {
                    _biometricOperationState.value = BiometricOperationState.Error(
                        "Biometric setup cancelled"
                    )
                }
            }
        }
    }
    
    /**
     * Authenticate using cryptographic biometric authentication.
     * This is a full authentication flow that gets new tokens from the backend.
     * Use this when the user hasn't unlocked with password yet.
     * 
     * @param activity FragmentActivity required for BiometricPrompt
     */
    fun authenticateWithCryptoBiometric(activity: FragmentActivity) {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading
            _biometricOperationState.value = BiometricOperationState.Loading
            
            val result = biometricAuthRepository.authenticateWithBiometric(activity)
            
            when (result) {
                is BiometricAuthRepository.BiometricAuthResult.Success -> {
                    _biometricOperationState.value = BiometricOperationState.Success(
                        "Authenticated as ${result.userEmail}"
                    )
                    // Tokens are already saved by the repository
                    // Now we need to also unlock the vault
                    // The backend should have a way to unlock vault after biometric auth
                    // For now, mark as unlocked since we're authenticated
                    checkVaultStatus()
                }
                is BiometricAuthRepository.BiometricAuthResult.Error -> {
                    _biometricOperationState.value = BiometricOperationState.Error(result.message)
                    _uiState.value = VaultUiState.Locked
                }
                BiometricAuthRepository.BiometricAuthResult.Cancelled -> {
                    _biometricOperationState.value = BiometricOperationState.Idle
                    _uiState.value = VaultUiState.Locked
                }
                BiometricAuthRepository.BiometricAuthResult.NotRegistered -> {
                    _biometricOperationState.value = BiometricOperationState.Error(
                        "Biometric not set up. Please unlock with password first and enable biometric."
                    )
                    _uiState.value = VaultUiState.Locked
                }
                BiometricAuthRepository.BiometricAuthResult.BiometricNotAvailable -> {
                    _biometricOperationState.value = BiometricOperationState.Error(
                        "Biometric authentication not available"
                    )
                    _uiState.value = VaultUiState.Locked
                }
            }
        }
    }
    
    /**
     * Remove biometric registration for this device.
     */
    fun removeBiometricDevice() {
        viewModelScope.launch {
            _biometricOperationState.value = BiometricOperationState.Loading
            
            val success = biometricAuthRepository.removeDevice(removeFromBackend = true)
            
            if (success) {
                _biometricOperationState.value = BiometricOperationState.Success(
                    "Biometric authentication disabled"
                )
                _biometricRegistered.value = false
                _registeredDevices.value = emptyList()
            } else {
                _biometricOperationState.value = BiometricOperationState.Error(
                    "Failed to remove biometric authentication"
                )
            }
        }
    }
    
    /**
     * Check if biometric authentication is available on this device.
     */
    fun isBiometricAvailable(): Boolean {
        return biometricAuthRepository.isBiometricAvailable()
    }
    
    /**
     * Check if biometric enrollment is needed.
     */
    fun needsBiometricEnrollment(): Boolean {
        return biometricAuthRepository.needsEnrollment()
    }
    
    /**
     * Clear the biometric operation state.
     */
    fun clearBiometricOperationState() {
        _biometricOperationState.value = BiometricOperationState.Idle
    }
    
    /**
     * Refresh the list of registered devices from the backend.
     */
    fun refreshDeviceList() {
        viewModelScope.launch {
            biometricAuthRepository.getDevices()
                .onSuccess { response ->
                    _registeredDevices.value = response.devices
                }
        }
    }

    fun lockVault() {
        repository.lockVault()
        // Clear cached password on lock for security
        cachedMasterPassword = null
        _biometricEnabled.value = false
        _uiState.value = VaultUiState.Locked
    }
    
    fun disableBiometric() {
        cachedMasterPassword = null
        _biometricEnabled.value = false
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
