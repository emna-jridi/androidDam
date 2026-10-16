package tn.esprit.dam.features.darkweb.presentation

import tn.esprit.dam.core.ui.toUiTextOr
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import tn.esprit.dam.features.darkweb.data.model.Breach
import tn.esprit.dam.features.darkweb.data.model.ManualBreachResult
import tn.esprit.dam.features.darkweb.data.repository.DarkWebRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DarkWebUiState(
    val isLoading: Boolean = false,
    val breaches: List<Breach> = emptyList(),
    val error: UiText? = null,
    val manualEmailResult: List<ManualBreachResult>? = null,
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
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.getBreaches()
            if (result.isSuccess) {
                _uiState.update { it.copy(
                    isLoading = false,
                    breaches = result.getOrNull() ?: emptyList(),
                    error = null
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message.toUiTextOr(R.string.error_unknown_en)
                ) }
            }
        }
    }

    fun checkNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.checkNow()
            if (result.isSuccess) {
                loadBreaches()
            } else {
                 _uiState.update { it.copy(
                    isLoading = false,
                    error = UiText.Res(R.string.darkweb_error_check_failed, result.exceptionOrNull()?.message.orEmpty())
                ) }
            }
        }
    }

    fun manualCheckEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isLoading = true,
                manualEmailResult = null,
                error = null
            ) }
            val result = repository.checkEmail(email)
            if (result.isSuccess) {
                 _uiState.update { it.copy(
                    isLoading = false,
                    manualEmailResult = result.getOrNull()
                ) }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message?.let { UiText.Raw(it) }
                ) }
            }
        }
    }

    fun manualCheckPassword(password: String) {
        if (password.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, manualPasswordCount = null, passwordCheckPerformed = false) }
            val sha1 = java.security.MessageDigest.getInstance("SHA-1")
            val bytes = sha1.digest(password.toByteArray())
            val hex = bytes.joinToString("") { "%02x".format(it) }.uppercase()
            val prefix = hex.take(5)

            val result = repository.checkPassword(prefix)
             if (result.isSuccess) {
                 _uiState.update { it.copy(
                    isLoading = false,
                    manualPasswordCount = result.getOrNull() ?: 0,
                    passwordCheckPerformed = true
                ) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message?.let { UiText.Raw(it) }) }
            }
        }
    }

    fun resetManualState() {
        _uiState.update { it.copy(manualEmailResult = null, manualPasswordCount = null, passwordCheckPerformed = false) }
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
