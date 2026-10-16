package tn.esprit.dam.features.vault.presentation.access

import tn.esprit.dam.core.ui.toUiTextOr
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import tn.esprit.dam.features.vault.data.model.Vault
import tn.esprit.dam.features.vault.data.repository.VaultRepository
import tn.esprit.dam.features.vault.data.repository.BiometricAuthRepository
import tn.esprit.dam.features.vault.data.model.BiometricDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VaultUiState {
    object Initial : VaultUiState()
    object Loading : VaultUiState()
    object NoVault : VaultUiState()
    object Locked : VaultUiState()
    data class Unlocked(val vault: Vault) : VaultUiState()
    data class Error(val message: UiText) : VaultUiState()
}

sealed class BiometricOperationState {
    object Idle : BiometricOperationState()
    object Loading : BiometricOperationState()
    data class Success(val message: UiText) : BiometricOperationState()
    data class Error(val message: UiText) : BiometricOperationState()
}

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: VaultRepository,
    private val biometricAuthRepository: BiometricAuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<VaultUiState>(VaultUiState.Initial)
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val _createVaultState = MutableStateFlow<Result<String>?>(null)
    val createVaultState: StateFlow<Result<String>?> = _createVaultState.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _biometricRegistered = MutableStateFlow(false)
    val biometricRegistered: StateFlow<Boolean> = _biometricRegistered.asStateFlow()

    private val _biometricOperationState = MutableStateFlow<BiometricOperationState>(BiometricOperationState.Idle)
    val biometricOperationState: StateFlow<BiometricOperationState> = _biometricOperationState.asStateFlow()

    private val _registeredDevices = MutableStateFlow<List<BiometricDeviceInfo>>(emptyList())
    val registeredDevices: StateFlow<List<BiometricDeviceInfo>> = _registeredDevices.asStateFlow()
    private var cachedMasterPassword: String? = null

    init {
        checkVaultStatus()
        checkBiometricRegistration()
    }

    private fun checkBiometricRegistration() {
        val localRegistered = biometricAuthRepository.isDeviceRegistered()
        _biometricRegistered.value = localRegistered

        viewModelScope.launch {
            biometricAuthRepository.getBiometricStatus()
                .onSuccess { status ->
                    if (status.biometricEnabled && localRegistered) {
                        _biometricRegistered.value = true
                    } else if (!localRegistered) {
                        _biometricRegistered.value = false
                    }
                    _registeredDevices.value = status.devices ?: emptyList()
                }
                .onFailure {}
        }
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
                    val errorMsg = error.message.orEmpty()

                    when {
                        errorMsg.contains("not found", ignoreCase = true) -> {
                            _uiState.value = VaultUiState.NoVault
                        }
                        errorMsg.contains("401", ignoreCase = true) ||
                        errorMsg.contains("Unauthorized", ignoreCase = true) ||
                        errorMsg.contains("token", ignoreCase = true) -> {
                            _uiState.value = VaultUiState.Locked
                        }
                        else -> {
                            _uiState.value = VaultUiState.Error(errorMsg.toUiTextOr(R.string.error_unknown_en))
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
                    unlockVault(masterPassword)
                }
                .onFailure { error ->
                    _createVaultState.value = Result.failure(error)
                    _uiState.value = VaultUiState.Error(error.message.toUiTextOr(R.string.vault_error_create_failed))
                }
        }
    }

    fun unlockVault(masterPassword: String) {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading

            repository.unlockVault(masterPassword)
                .onSuccess { response ->
                    if (response.success) {
                        cachedMasterPassword = masterPassword
                        _biometricEnabled.value = true
                        checkVaultStatus()
                    } else {
                        _uiState.value = VaultUiState.Error(response.message.toUiTextOr(R.string.vault_error_unlock_failed))
                    }
                }
                .onFailure { error ->
                    _uiState.value = VaultUiState.Error(error.message.toUiTextOr(R.string.vault_error_unlock_failed))
                }
        }
    }

    fun unlockVaultWithBiometric() {
        cachedMasterPassword?.let { password ->
            unlockVault(password)
        } ?: run {
            _uiState.value = VaultUiState.Error(UiText.Res(R.string.vault_error_unlock_first))
        }
    }

    fun registerBiometricDevice(activity: FragmentActivity, deviceName: String? = null) {
        viewModelScope.launch {
            _biometricOperationState.value = BiometricOperationState.Loading

            val result = biometricAuthRepository.registerDevice(
                activity = activity,
                deviceName = deviceName ?: biometricAuthRepository.getDeviceName()
            )

            when (result) {
                is BiometricAuthRepository.RegistrationResult.Success -> {
                    _biometricOperationState.value = BiometricOperationState.Success(UiText.Raw(result.message))
                    _biometricRegistered.value = true
                    checkBiometricRegistration()
                }
                is BiometricAuthRepository.RegistrationResult.Error -> {
                    _biometricOperationState.value = BiometricOperationState.Error(UiText.Raw(result.message))
                }
                BiometricAuthRepository.RegistrationResult.BiometricNotAvailable -> {
                    _biometricOperationState.value = BiometricOperationState.Error(
                        UiText.Res(R.string.biometric_not_available_device)
                    )
                }
                BiometricAuthRepository.RegistrationResult.Cancelled -> {
                    _biometricOperationState.value = BiometricOperationState.Error(
                        UiText.Res(R.string.biometric_setup_cancelled)
                    )
                }
            }
        }
    }

    fun authenticateWithCryptoBiometric(activity: FragmentActivity) {
        viewModelScope.launch {
            _uiState.value = VaultUiState.Loading
            _biometricOperationState.value = BiometricOperationState.Loading

            val result = biometricAuthRepository.authenticateWithBiometric(activity)

            when (result) {
                is BiometricAuthRepository.BiometricAuthResult.Success -> {
                    _biometricOperationState.value = BiometricOperationState.Success(
                        UiText.Res(R.string.biometric_authenticated_as, result.userEmail.orEmpty())
                    )
                    checkVaultStatus()
                }
                is BiometricAuthRepository.BiometricAuthResult.Error -> {
                    _biometricOperationState.value = BiometricOperationState.Error(UiText.Raw(result.message))
                    _uiState.value = VaultUiState.Locked
                }
                BiometricAuthRepository.BiometricAuthResult.Cancelled -> {
                    _biometricOperationState.value = BiometricOperationState.Idle
                    _uiState.value = VaultUiState.Locked
                }
                BiometricAuthRepository.BiometricAuthResult.NotRegistered -> {
                    _biometricOperationState.value = BiometricOperationState.Error(
                        UiText.Res(R.string.biometric_not_set_up)
                    )
                    _uiState.value = VaultUiState.Locked
                }
                BiometricAuthRepository.BiometricAuthResult.BiometricNotAvailable -> {
                    _biometricOperationState.value = BiometricOperationState.Error(
                        UiText.Res(R.string.biometric_not_available)
                    )
                    _uiState.value = VaultUiState.Locked
                }
            }
        }
    }

    fun removeBiometricDevice() {
        viewModelScope.launch {
            _biometricOperationState.value = BiometricOperationState.Loading

            val success = biometricAuthRepository.removeDevice(removeFromBackend = true)

            if (success) {
                _biometricOperationState.value = BiometricOperationState.Success(
                    UiText.Res(R.string.biometric_disabled)
                )
                _biometricRegistered.value = false
                _registeredDevices.value = emptyList()
            } else {
                _biometricOperationState.value = BiometricOperationState.Error(
                    UiText.Res(R.string.biometric_remove_failed)
                )
            }
        }
    }

    fun isBiometricAvailable(): Boolean {
        return biometricAuthRepository.isBiometricAvailable()
    }

    fun needsBiometricEnrollment(): Boolean {
        return biometricAuthRepository.needsEnrollment()
    }

    fun clearBiometricOperationState() {
        _biometricOperationState.value = BiometricOperationState.Idle
    }

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
                    checkVaultStatus()
                }
                .onFailure { error ->
                    _uiState.value = VaultUiState.Error(error.message.toUiTextOr(R.string.vault_error_update_settings))
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
