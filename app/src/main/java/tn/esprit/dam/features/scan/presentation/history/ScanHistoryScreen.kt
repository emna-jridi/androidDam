package tn.esprit.dam.features.scan.presentation.history

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
import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryItemDto
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppLoadingState
import tn.esprit.dam.core.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    viewModel: ScanHistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onScanClick: (ScanHistoryItemDto) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedScan by remember { mutableStateOf<ScanHistoryItemDto?>(null) }

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
                AppLoadingState(message = "Loading history...")
            }
            uiState.error != null && uiState.scans.isEmpty() -> {
                AppErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.refresh() }
                )
            }
            uiState.scans.isEmpty() -> {
                AppEmptyState(
                    icon = Icons.Default.History,
                    title = "No scans",
                    message = "Start a scan to see history"
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
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}
