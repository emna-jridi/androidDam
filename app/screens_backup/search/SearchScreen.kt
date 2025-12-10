package tn.esprit.dam.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onAppDetails: (String) -> Unit
) {
    val viewModel: SearchViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SearchTheme.DarkBg)
    ) {

        Column {

            //--------------------------------------------------
            // 🔙 HEADER
            //--------------------------------------------------
            ModernSearchHeader(onBack)

            //--------------------------------------------------
            // 🔍 Search Bar + Search Button
            //--------------------------------------------------
            Column(Modifier.padding(20.dp)) {

                ModernSearchBar(
                    query = query,
                    onQueryChange = { viewModel.updateQuery(it) },
                    onClear = { viewModel.clearSearch() },
                    onSearch = { viewModel.search() },
                    enabled = uiState !is SearchUiState.Loading
                )

                Spacer(Modifier.height(12.dp))

                ModernSearchButton(
                    onClick = { viewModel.search() },
                    enabled = query.isNotEmpty() && uiState !is SearchUiState.Loading,
                    isLoading = uiState is SearchUiState.Loading
                )

                Spacer(Modifier.height(12.dp))
            }

            //--------------------------------------------------
            // 📄 Results (Idle, Loading, Success, Error)
            //--------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState) {

                    // 🟡 IDLE
                    SearchUiState.Idle -> {
                        ModernEmptyState()
                    }

                    // 🔄 LOADING
                    SearchUiState.Loading -> {
                        ModernLoadingState()
                    }

                    // ❌ ERROR
                    is SearchUiState.Error -> {
                        ModernErrorState(
                            error = (uiState as SearchUiState.Error).message,
                            onRetry = { viewModel.search() }
                        )
                    }

                    // ✅ SUCCESS
                    is SearchUiState.Success -> {
                        val results = (uiState as SearchUiState.Success).results

                        if (results.isEmpty()) {
                            ModernNoResultsState(query)
                        } else {
                            ModernResultsList(
                                results = results,   // List<SearchItem>
                                onAppClick = { pkg ->
                                    onAppDetails(pkg) // navigate with packageName
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
