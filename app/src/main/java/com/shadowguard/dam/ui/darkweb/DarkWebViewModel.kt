package com.shadowguard.dam.ui.darkweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shadowguard.dam.data.model.Breach
import com.shadowguard.dam.data.repository.DarkWebRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DarkWebUiState(
    val isLoading: Boolean = false,
    val breaches: List<Breach> = emptyList(),
    val error: String? = null,
    val manualEmailResult: List<Map<String, Any>>? = null,
    val manualPasswordCount: Int? = null,
    val passwordCheckPerformed: Boolean = false
)

@HiltViewModel
class DarkWebViewModel @Inject constructor(
    private val repository: DarkWebRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DarkWebUiState())
    val uiState: StateFlow<DarkWebUiState> = _uiState.asStateFlow()

    init {
        loadBreaches()
    }

    fun loadBreaches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.getBreaches()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    breaches = result.getOrNull() ?: emptyList(),
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    fun checkNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.checkNow()
            if (result.isSuccess) {
                loadBreaches()
            } else {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Check failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
    
    fun manualCheckEmail(email: String) {
        viewModelScope.launch {
            // Reset previous results first
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                manualEmailResult = null,
                error = null
            )
            val result = repository.checkEmail(email)
            if (result.isSuccess) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    manualEmailResult = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
    
    fun manualCheckPassword(password: String) {
        if (password.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, manualPasswordCount = null, passwordCheckPerformed = false)
            // SHA-1 Hashing
            val sha1 = java.security.MessageDigest.getInstance("SHA-1")
            val bytes = sha1.digest(password.toByteArray())
            val hex = bytes.joinToString("") { "%02x".format(it) }.uppercase()
            val prefix = hex.take(5)
            
            val result = repository.checkPassword(prefix)
             if (result.isSuccess) {
                 _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    manualPasswordCount = result.getOrNull() ?: 0,
                    passwordCheckPerformed = true
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
    
    fun resetManualState() {
        _uiState.value = _uiState.value.copy(manualEmailResult = null, manualPasswordCount = null, passwordCheckPerformed = false)
    }

    fun resolveBreach(breachId: String) {
        viewModelScope.launch {
            val result = repository.resolveBreach(breachId)
            if (result.isSuccess) {
                loadBreaches()
            }
        }
    }
}
