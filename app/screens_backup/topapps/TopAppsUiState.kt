package tn.esprit.dam.screens.topapps


import tn.esprit.dam.data.model.AppAnalysisResult

enum class TopAppsTab { SAFE, DANGEROUS }

data class TopAppsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val safeApps: List<AppAnalysisResult> = emptyList(),
    val dangerousApps: List<AppAnalysisResult> = emptyList(),
    val selectedTab: TopAppsTab = TopAppsTab.SAFE
)
