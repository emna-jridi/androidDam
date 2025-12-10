package tn.esprit.dam.screens.appdetails


import tn.esprit.dam.data.model.AppDetails

sealed class AppDetailsUiState {
    object Loading : AppDetailsUiState()
    data class Success(val app: AppDetails) : AppDetailsUiState()
    data class Error(val message: String) : AppDetailsUiState()
}
