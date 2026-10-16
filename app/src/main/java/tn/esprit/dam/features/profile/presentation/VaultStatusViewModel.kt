package tn.esprit.dam.features.profile.presentation

import tn.esprit.dam.core.network.toResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tn.esprit.dam.features.vault.data.remote.VaultApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed class VaultStatusUiState {
    object Loading : VaultStatusUiState()
    object NoVault : VaultStatusUiState()
    object VaultExists : VaultStatusUiState()
    data class Error(val message: String) : VaultStatusUiState()
}

@HiltViewModel
class VaultStatusViewModel @Inject constructor(
    private val api: VaultApi
) : ViewModel() {
    private val _status = MutableStateFlow<VaultStatusUiState>(VaultStatusUiState.Loading)
    val status: StateFlow<VaultStatusUiState> = _status

    fun checkStatus() {
        _status.value = VaultStatusUiState.Loading
        viewModelScope.launch {
            val result = withTimeoutOrNull(5000) {
                api.getVaultStatus().toResult()
            }
            if (result == null) {
                _status.value = VaultStatusUiState.Error("Timeout while checking vault status")
                return@launch
            }
            result
                .onSuccess { exists ->
                    _status.value = if (exists) VaultStatusUiState.VaultExists else VaultStatusUiState.NoVault
                }
                .onFailure { e ->
                    _status.value = VaultStatusUiState.Error(e.message ?: "Failed to check vault status")
                }
        }
    }
}
