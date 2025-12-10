package tn.esprit.dam.screens.topapps


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.screens.scan.ScanViewModel
import tn.esprit.dam.data.model.AppAnalysisResult
import javax.inject.Inject

@HiltViewModel
class TopAppsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TopAppsUiState())
    val uiState: StateFlow<TopAppsUiState> = _uiState

    private var scanViewModel: ScanViewModel? = null

    fun attachScanViewModel(vm: ScanViewModel) {
        scanViewModel = vm
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
/*
            val scan = scanViewModel?.getCurrentScan()

            if (scan == null) {
                _uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = "Aucun scan disponible"
                )
                return@launch
            }

            val apps = scan.report.results

            val safe = apps.filter { it.riskLevel.equals("low", true) }
                .sortedByDescending { it.score }

            val dangerous = apps.filter {
                it.riskLevel.equals("high", true) ||
                        it.riskLevel.equals("critical", true)
            }.sortedByDescending { it.score }

            _uiState.value = TopAppsUiState(
                isLoading = false,
                safeApps = safe,
                dangerousApps = dangerous
            )
        }*/
    }}

    fun selectTab(tab: TopAppsTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
}



