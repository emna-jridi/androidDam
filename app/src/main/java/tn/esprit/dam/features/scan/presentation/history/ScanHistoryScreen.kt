package tn.esprit.dam.features.scan.presentation.history
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    viewModel: ScanHistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onScanClick: (ScanHistoryItemUi) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedScan by remember { mutableStateOf<ScanHistoryItemUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        when {
            uiState.isLoading && uiState.scans.isEmpty() -> {
                AppSkeletonList(itemCount = 5, modifier = Modifier.padding(AppSpacing.md))
            }
            uiState.error != null && uiState.scans.isEmpty() -> {
                AppErrorState(
                    message = uiState.error!!.asString(),
                    onRetry = { viewModel.refresh() },
                    retryText = stringResource(R.string.retry)
                )
            }
            uiState.scans.isEmpty() -> {
                AppEmptyState(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.no_scans),
                    message = stringResource(R.string.start_a_scan_to_see_history)
                )
            }
            else -> {
                HistoryList(
                    scans = uiState.scans,
                    isLoading = uiState.isLoading,
                    hasMore = uiState.hasMore,
                    onLoadMore = { viewModel.loadMore() },
                    onScanClick = { scan ->
                        selectedScan = scan
                        onScanClick(scan)
                    }
                )
            }
        }

        selectedScan?.let { scan ->
            ScanDetailSheet(
                scan = scan,
                onDismiss = { selectedScan = null }
            )
        }

        uiState.error?.takeIf { uiState.scans.isNotEmpty() }?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(AppSpacing.md),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            ) {
                Text(error.asString())
            }
        }
    }
}
