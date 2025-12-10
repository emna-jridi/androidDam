package tn.esprit.dam.screens.appdetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.repository.AppDetailsRepository
import tn.esprit.dam.data.model.AppDetails
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle

@HiltViewModel
class AppDetailsViewModel @Inject constructor(
    private val repository: AppDetailsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packageName: String =
        savedStateHandle["packageName"] ?: ""

    private val _uiState = MutableStateFlow<AppDetailsUiState>(AppDetailsUiState.Loading)
    val uiState: StateFlow<AppDetailsUiState> = _uiState.asStateFlow()

    init {
        Log.d("AppDetailsVM", "Initialized with package: $packageName")
        loadDetails()
    }

    fun loadDetails() {
        if (packageName.isEmpty()) {
            Log.e("AppDetailsVM", "Package name is empty")
            _uiState.value = AppDetailsUiState.Error("Package name manquant")
            return
        }

        viewModelScope.launch {
            _uiState.value = AppDetailsUiState.Loading

            Log.d("AppDetailsVM", "Loading details for $packageName")

            repository.getAppDetails(packageName).fold(
                onSuccess = { details ->
                    Log.d("AppDetailsVM", "Details loaded: ${details.name}")
                    _uiState.value = AppDetailsUiState.Success(details)
                },
                onFailure = { error ->
                    Log.e("AppDetailsVM", "Failed to load details", error)
                    val message = when (error) {
                        is kotlinx.serialization.SerializationException ->
                            "Erreur de format de données: ${error.message}"
                        is java.net.UnknownHostException ->
                            "Pas de connexion internet"
                        else ->
                            error.message ?: "Erreur inconnue"
                    }
                    _uiState.value = AppDetailsUiState.Error(message)
                }
            )
        }
    }
}