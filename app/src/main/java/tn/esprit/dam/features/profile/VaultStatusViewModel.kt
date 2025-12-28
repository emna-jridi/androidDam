package tn.esprit.dam.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowguard.dam.data.remote.api.VaultApi
import com.shadowguard.dam.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.api.KtorClient
import android.content.Context
import android.util.Log

sealed class VaultStatusUiState {
    object Loading : VaultStatusUiState()
    object NoVault : VaultStatusUiState()
    object VaultExists : VaultStatusUiState()
    data class Error(val message: String) : VaultStatusUiState()
}

class VaultStatusViewModel(private val context: Context) : ViewModel() {
    private val _status = MutableStateFlow<VaultStatusUiState>(VaultStatusUiState.Loading)
    val status: StateFlow<VaultStatusUiState> = _status

    private val client by lazy { tn.esprit.dam.data.remote.KtorHttpClient(context) }
    private val api by lazy { VaultApi(client) }

    fun checkStatus() {
        _status.value = VaultStatusUiState.Loading
        viewModelScope.launch {
            val result = withTimeoutOrNull(5000) {
                Log.d("VaultStatusVM", "Checking vault status...")
                api.getVaultStatus()
            }
            if (result == null) {
                Log.e("VaultStatusVM", "Status check timed out")
                _status.value = VaultStatusUiState.Error("Timeout while checking vault status")
                return@launch
            }
            result
                .onSuccess { exists ->
                    Log.d("VaultStatusVM", "Vault exists? $exists")
                    _status.value = if (exists) VaultStatusUiState.VaultExists else VaultStatusUiState.NoVault
                }
                .onFailure { e ->
                    Log.e("VaultStatusVM", "Status check failed: ${e.message}", e)
                    _status.value = VaultStatusUiState.Error(e.message ?: "Failed to check vault status")
                }
        }
    }
}
