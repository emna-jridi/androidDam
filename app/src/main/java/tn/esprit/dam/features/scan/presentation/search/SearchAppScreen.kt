package tn.esprit.dam.features.scan.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.features.scan.presentation.ScanErrorState
import tn.esprit.dam.features.scan.presentation.ScanLoadingState

@Composable
fun SearchAppScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onAppClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppSearchBar(
            query = uiState.query,
            onQueryChange = { viewModel.updateQuery(it) },
            onSearch = {
                focusManager.clearFocus()
                viewModel.search()
            },
            onClear = { viewModel.clearSearch() }
        )

        when {
            uiState.isLoading -> {
                ScanLoadingState("Searching applications...")
            }
            uiState.error != null -> {
                ScanErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.search() }
                )
            }
            uiState.results.isEmpty() && uiState.hasSearched -> {
                EmptySearchResults(query = uiState.query)
            }
            uiState.results.isEmpty() && !uiState.hasSearched -> {
                SearchPlaceholder()
            }
            else -> {
                SearchResults(
                    results = uiState.results,
                    onAppClick = onAppClick
                )
            }
        }
    }
}
