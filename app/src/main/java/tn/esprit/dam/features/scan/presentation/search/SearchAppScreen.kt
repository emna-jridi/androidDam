package tn.esprit.dam.features.scan.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing

@Composable
fun SearchAppScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onAppClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Open the keyboard straight away when nothing has been searched yet.
    LaunchedEffect(Unit) {
        if (uiState.query.isEmpty()) focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        AppSearchBar(
            query = uiState.query,
            onQueryChange = { viewModel.updateQuery(it) },
            onSearch = {
                focusManager.clearFocus()
                viewModel.search()
            },
            onClear = { viewModel.clearSearch() },
            focusRequester = focusRequester
        )

        val error = uiState.error
        when {
            uiState.isLoading -> AppSkeletonList(itemCount = 5, modifier = Modifier.padding(horizontal = AppSpacing.md))
            error != null -> AppErrorState(
                message = error.asString(),
                onRetry = { viewModel.search() },
                retryText = stringResource(R.string.search_retry)
            )
            uiState.results.isEmpty() && uiState.hasSearched -> AppEmptyState(
                icon = Icons.Default.SearchOff,
                title = stringResource(R.string.search_no_result_title),
                message = stringResource(R.string.search_no_result_desc, uiState.query)
            )
            uiState.results.isEmpty() -> SearchPlaceholder(
                onPick = { name ->
                    focusManager.clearFocus()
                    viewModel.updateQuery(name)
                    viewModel.search()
                }
            )
            else -> SearchResults(results = uiState.results, onAppClick = onAppClick)
        }
    }
}
