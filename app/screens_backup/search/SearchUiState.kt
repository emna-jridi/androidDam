package tn.esprit.dam.screens.search
import tn.esprit.dam.data.model.SearchItem

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Error(val message: String) : SearchUiState()

    data class Success(val results: List<SearchItem>) : SearchUiState()
}
