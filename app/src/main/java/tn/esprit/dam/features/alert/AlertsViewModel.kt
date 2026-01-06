package tn.esprit.dam.features.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.model.Alert
import tn.esprit.dam.data.repository.AlertRepository
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val repository: AlertRepository
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _isLoading.value = true
            _alerts.value = repository.getAlertHistory()
            _isLoading.value = false
        }
    }
    
    fun markAsRead(alertId: String) {
        viewModelScope.launch {
            if (repository.markAsRead(alertId)) {
                // Update local state to reflect read status
                _alerts.value = _alerts.value.map { alert ->
                    if (alert.id == alertId) alert.copy(read = true) else alert
                }
            }
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            if (repository.markAllAsRead()) {
                // Update local state to reflect all as read
                _alerts.value = _alerts.value.map { it.copy(read = true) }
            }
        }
    }
}
