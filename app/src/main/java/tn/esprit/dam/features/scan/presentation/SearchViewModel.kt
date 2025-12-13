package tn.esprit.dam.features.scan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.api.models.SearchResultDto
import tn.esprit.dam.data.repository.ScanRepository

data class SearchUIState(
    val query: String = "",
    val results: List<SearchResultDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUIState())
    val uiState: StateFlow<SearchUIState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /**
     * Update search query with debouncing
     */
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        
        // Cancel previous search job
        searchJob?.cancel()
        
        // Don't search if query is too short
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(
                results = emptyList(),
                hasSearched = false,
                error = null
            )
            return
        }
        
        // Debounce search (wait 500ms after typing stops)
        searchJob = viewModelScope.launch {
            delay(500)
            performSearch(query)
        }
    }

    /**
     * Perform search immediately
     */
    fun search() {
        val query = _uiState.value.query
        if (query.length >= 2) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                performSearch(query)
            }
        }
    }

    /**
     * Perform the actual search API call
     */
    private suspend fun performSearch(query: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        when (val result = repository.searchApps(query, "android", 15)) {
            is ApiResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    results = result.data.results,
                    isLoading = false,
                    hasSearched = true,
                    error = null
                )
            }
            is ApiResult.ApiError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasSearched = true,
                    error = result.message
                )
            }
            is ApiResult.NetworkError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasSearched = true,
                    error = "Erreur réseau: vérifiez votre connexion"
                )
            }
            is ApiResult.SerializationError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasSearched = true,
                    error = "Erreur de données"
                )
            }
        }
    }

    /**
     * Clear search results
     */
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = SearchUIState()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
