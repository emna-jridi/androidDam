package tn.esprit.dam.features.scan.presentation.home
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource
import timber.log.Timber

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography

@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAppSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAppDetails: (String) -> Unit,
    onNavigateToVault: () -> Unit,
    onLogout: () -> Unit = {},
    onNavigateToAlerts: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val context = LocalContext.current

    val apkPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Timber.w(e, "HomeScreen.HomeScreen failed")
                }
                viewModel.scanApk(it)
            }
        }
    )

    val pickApk: () -> Unit = {
        apkPicker.launch(
            arrayOf(
                "application/vnd.android.package-archive",
                "application/octet-stream"
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        item {
            if (homeState.loading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.primary)
                }
            } else if (homeState.hasScan) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(350)) + slideInVertically(initialOffsetY = { 24 })
                ) {
                    ScoreSection(
                        score = homeState.overallScore.toInt(),
                        onImproveClick = onNavigateToScan
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.actions_rapides),
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary
            )
        }

        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(350)) + slideInVertically(initialOffsetY = { 32 })
            ) {
                FeatureGrid(
                    onScan = onNavigateToScan,
                    onSearch = onNavigateToAppSearch,
                    onHistory = onNavigateToHistory,
                    onScanApk = pickApk,
                    onShadowGuard = onNavigateToVault,
                    onAlerts = onNavigateToAlerts
                )
            }
        }

        if (homeState.hasScan) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(350)) + slideInVertically(initialOffsetY = { 24 })
                ) {
                    RecentActivitySection(
                        riskyApps = homeState.riskyApps,
                        onNavigateToAppDetails = onNavigateToAppDetails
                    )
                }
            }
        }

        if (!homeState.hasScan && !homeState.loading) {
            item {
                AppEmptyState(
                    icon = Icons.Default.Search,
                    title = stringResource(R.string.no_scan_performed),
                    message = stringResource(R.string.launch_your_first_scan_to_secure_your_ap),
                    onAction = onNavigateToScan,
                    actionText = stringResource(R.string.start_a_scan)
                )
            }
        }
    }
}

data class HomeState(
    val lastScanDate: String? = null,
    val overallScore: Float = 0f,
    val highRiskCount: Int = 0,
    val mediumRiskCount: Int = 0,
    val lowRiskCount: Int = 0,
    val totalApps: Int = 0,
    val riskyApps: List<RiskyAppUi> = emptyList(),
    val recentScans: List<RecentScan> = emptyList(),
    val loading: Boolean = false,
    val error: UiText? = null
) {
    val hasScan: Boolean
        get() = totalApps > 0
}

data class RecentScan(
    val date: String,
    val appsScanned: Int,
    val issuesFound: Int
)
