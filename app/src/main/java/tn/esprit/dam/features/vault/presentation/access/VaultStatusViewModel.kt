package tn.esprit.dam.features.vault.presentation.access

import tn.esprit.dam.core.ui.toUiTextOr
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tn.esprit.dam.features.vault.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

sealed class VaultStatusUiState {
    object Loading : VaultStatusUiState()
    object NoVault : VaultStatusUiState()
    object VaultExists : VaultStatusUiState()
    data class Error(val message: UiText) : VaultStatusUiState()
}

@HiltViewModel
class VaultStatusViewModel @Inject constructor(
    private val repository: VaultRepository
) : ViewModel() {
    private val _status = MutableStateFlow<VaultStatusUiState>(VaultStatusUiState.Loading)
    val status: StateFlow<VaultStatusUiState> = _status

    fun checkStatus() {
        _status.value = VaultStatusUiState.Loading
        viewModelScope.launch {
            val result = withTimeoutOrNull(5000) {
                repository.hasVault()
            }
            if (result == null) {
                _status.value = VaultStatusUiState.Error(UiText.Res(R.string.vault_status_timeout))
                return@launch
            }
            result
                .onSuccess { exists ->
                    _status.value = if (exists) VaultStatusUiState.VaultExists else VaultStatusUiState.NoVault
                }
                .onFailure { e ->
                    _status.value = VaultStatusUiState.Error(e.message.toUiTextOr(R.string.vault_status_failed))
                }
        }
    }
}
