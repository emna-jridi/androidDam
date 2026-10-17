package tn.esprit.dam.features.scan.presentation.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import timber.log.Timber
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing

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

    val error = homeState.error
    when {
        homeState.loading -> AppSkeletonList(
            itemCount = 4,
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .padding(AppSpacing.md)
        )

        error != null -> AppErrorState(
            message = error.asString(),
            onRetry = { viewModel.refreshDashboard() },
            retryText = stringResource(R.string.retry)
        )

        else -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background),
            contentPadding = PaddingValues(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            if (homeState.hasScan) {
                item {
                    HomeShieldHero(
                        score = homeState.overallScore.toInt(),
                        appsToCheck = homeState.highRiskCount,
                        caption = homeState.lastScanDate?.let { stringResource(R.string.home_last_scan, it) },
                        actionText = stringResource(R.string.home_run_scan),
                        onScanClick = onNavigateToScan
                    )
                }
                item {
                    RiskCountsRow(
                        high = homeState.highRiskCount,
                        medium = homeState.mediumRiskCount,
                        safe = homeState.lowRiskCount
                    )
                }
            } else {
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

            item {
                QuickToolsRow(
                    onScanApk = pickApk,
                    onSearch = onNavigateToAppSearch,
                    onAlerts = onNavigateToAlerts,
                    onVault = onNavigateToVault
                )
            }

            if (homeState.hasScan) {
                item {
                    WatchlistSection(
                        riskyApps = homeState.riskyApps,
                        onSeeAll = onNavigateToHistory,
                        onAppClick = onNavigateToAppDetails
                    )
                }
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
