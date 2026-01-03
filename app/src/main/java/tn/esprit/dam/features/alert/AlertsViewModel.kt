package tn.esprit.dam.features.alert

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.ApiClient
import tn.esprit.dam.data.model.Alert
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val apiClient = ApiClient.getInstance(context)

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
            _alerts.value = apiClient.getAlertHistory()
            _isLoading.value = false
        }
    }
}
