package tn.esprit.dam.features.scan.presentation.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.core.ui.theme.AppColors

@Composable
fun ScanScreen(
    userId: String,
    deviceId: String,
    onNavigateToHome: () -> Unit,
    onNavigateToAppDetails: (String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val scanState by viewModel.scanState.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize(userId, deviceId)
    }

    if ((scanState.status == "LOADING" || (scanState.status == "IDLE" && availableApps.isEmpty())) &&
        !scanState.scanCompleted) {
        InitialLoadingScreen()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        when (scanState.status) {
            "ANALYZING" -> AnimatedLoadingState(
                totalApps = scanState.totalApps.coerceAtLeast(scanState.selectedApps.size.coerceAtLeast(1)),
                scannedApps = scanState.scannedApps,
                analysisNote = scanState.analysisNote,
                modifier = Modifier.weight(1f)
            )

            "COMPLETED" -> ResultsContent(
                scanState = scanState,
                analysisNote = scanState.analysisNote,
                onViewDetails = onNavigateToAppDetails,
                onNewScan = { viewModel.resetScan() },
                onImportApk = {  },
                modifier = Modifier.weight(1f)
            )

            else -> SelectionContent(
                availableApps = availableApps,
                selectedCount = scanState.selectedApps.size,
                error = scanState.error,
                scanLevel = scanState.scanLevel,
                onToggleApp = { viewModel.toggleAppSelection(it) },
                onSelectAll = { viewModel.selectAllApps() },
                onDeselectAll = { viewModel.deselectAllApps() },
                onClearError = { viewModel.clearError() },
                onLevelChange = { viewModel.setScanLevel(it) },
                modifier = Modifier.weight(1f)
            )
        }

        if (scanState.status == "IDLE") {
            ScanBottomBar(
                selectedCount = scanState.selectedApps.size,
                onStartScan = { viewModel.startScan() }
            )
        }
    }
}
