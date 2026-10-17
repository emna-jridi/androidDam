package tn.esprit.dam.features.scan.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppPrimaryButton
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.util.openAppSettings
import tn.esprit.dam.features.scan.domain.SecurityUtils

@Composable
fun AppDetailScreen(
    packageName: String,
    viewModel: AppDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    val error = uiState.error
    val app = uiState.app
    when {
        uiState.isLoading -> AppSkeletonList(
            itemCount = 4,
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .padding(AppSpacing.md)
        )
        error != null -> AppErrorState(
            message = error.asString(),
            onRetry = { viewModel.refresh(packageName) },
            retryText = stringResource(R.string.retry)
        )
        app != null -> AppDetailContent(app = app, history = uiState.history)
    }
}

@Composable
private fun AppDetailContent(
    app: AppDetailUi,
    history: List<AppScanHistoryUi>
) {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(DetailTab.Summary) }

    val permissions = remember(app.permissions) { app.permissions.map { it.toPermissionUi() } }
    // Backend score first, so this page matches the scan results; local estimate only as a fallback.
    val score = remember(app) {
        val backend = app.scanResults?.aiRiskScore?.takeIf { it > 0f } ?: app.finalScore.takeIf { it > 0f }
        backend?.toInt() ?: SecurityUtils.calculateAppRisk(
            permissions = app.permissions,
            trackers = app.trackers.map { it.name },
            isSystemApp = false
        ).score
    }
    val notInstalledMessage = stringResource(R.string.detail_app_not_installed)
    val openSettings = { openAppSettings(context, app.packageName, notInstalledMessage) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentPadding = PaddingValues(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        item { DetailAppHeader(app = app, score = score) }
        item {
            DetailStatsRow(
                permissions = permissions.size,
                sensitive = permissions.count { it.isSensitive },
                trackers = app.trackers.size
            )
        }
        item { DetailTabs(selected = tab, onSelect = { tab = it }) }
        item {
            when (tab) {
                DetailTab.Summary -> SummaryTab(
                    analysis = app.scanResults,
                    permissions = permissions,
                    trackerCount = app.trackers.size,
                    onOpenSettings = openSettings
                )
                DetailTab.Permissions -> PermissionsTab(permissions)
                DetailTab.Trackers -> TrackersTab(app.trackers)
                DetailTab.History -> HistoryTab(history)
            }
        }
        item {
            AppPrimaryButton(text = stringResource(R.string.detail_open_settings), onClick = openSettings)
        }
    }
}
