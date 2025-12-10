package tn.esprit.dam.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.esprit.dam.data.repository.SearchRepository
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()


    fun updateQuery(value: String) {
        _query.value = value
    }

    fun clearSearch() {
        _query.value = ""
        _uiState.value = SearchUiState.Idle
    }


    fun search() {
        val text = query.value.trim()
        if (text.isEmpty()) return

        _uiState.value = SearchUiState.Loading

        viewModelScope.launch {

            val result = repository.searchApps(text, limit = 20)
            result.fold(
                onSuccess = { items ->
                    if (items.isEmpty()) {
                        _uiState.value = SearchUiState.Error("Aucune application trouvée")
                    } else {
                        _uiState.value = SearchUiState.Success(items)
                    }
                },
                onFailure = { error ->
                    _uiState.value = SearchUiState.Error(error.message ?: "Erreur inconnue")
                }
            )

        }
    }
}
