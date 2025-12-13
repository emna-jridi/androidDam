package tn.esprit.dam.features.scan.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.data.api.models.AppInfoDto
import tn.esprit.dam.data.api.models.AppResult

@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAppSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAppDetails: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val apkPickSuccessMessage = stringResource(id = R.string.apk_picker_success)
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = apkPickSuccessMessage
                    )
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B1024), Color(0xFF141A36))
                    )
                )
                .padding(padding)
        ) {
            if (homeState.loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C3AED))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HomeHeader(
                        lastScanText = homeState.lastScanDate
                            ?: stringResource(id = R.string.last_scan_unknown),
                        onProfileClick = onNavigateToProfile
                    )
                }

                // Only show score card, charts, and risk lists if there's a scan
                if (homeState.hasScan) {
                    item {
                        SecurityScoreCard(
                            score = homeState.overallScore,
                            totalApps = homeState.totalApps
                        )
                    }
                }

                item {
                    ActionGrid(
                        onScan = onNavigateToScan,
                        onHistory = onNavigateToHistory,
                        onPickApk = { apkPickerLauncher.launch("application/vnd.android.package-archive") },
                        onSearch = onNavigateToAppSearch
                    )
                }

                // Only show risky apps if there's a scan
                if (homeState.hasScan) {
                    item {
                        RiskyAppsSection(
                            apps = homeState.riskyApps.take(3),
                            onNavigateToAppDetails = onNavigateToAppDetails,
                            title = stringResource(id = R.string.risky_apps_title)
                        )
                    }
                }

                // Show error if exists
                if (!homeState.loading && homeState.error != null) {
                    item {
                        ErrorState(error = homeState.error!!, onRetry = { viewModel.loadDashboard() })
                    }
                }
            }

            homeState.error?.let { errorMessage ->
                LaunchedEffect(errorMessage) {
                    snackbarHostState.showSnackbar(errorMessage)
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    lastScanText: String,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.shadowguard_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(id = R.string.shadowguard_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.08f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = stringResource(id = R.string.access_profile),
                        tint = Color(0xFF7C3AED)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.last_scan_label, lastScanText),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SecurityScoreCard(score: Float, totalApps: Int) {
    val displayScore = score.coerceIn(0f, 100f)
    val color = riskColorForScore(displayScore)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E233A)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.security_score_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(id = R.string.security_score_value, displayScore.toInt()),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = stringResource(id = R.string.security_score_helper, totalApps),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                CircularProgressIndicator(
                    progress = { displayScore / 100f },
                    modifier = Modifier.size(96.dp),
                    strokeWidth = 7.dp,
                    color = color
                )
            }

            RiskLegend()
        }
    }
}

@Composable
private fun RiskLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LegendPill(color = Color(0xFFDC2626), label = stringResource(id = R.string.level_high))
        LegendPill(color = Color(0xFFFFA227), label = stringResource(id = R.string.level_medium))
        LegendPill(color = Color(0xFF16A34A), label = stringResource(id = R.string.level_low))
    }
}

@Composable
private fun LegendPill(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(text = label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ActionGrid(
    onScan: () -> Unit,
    onHistory: () -> Unit,
    onPickApk: () -> Unit,
    onSearch: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = stringResource(id = R.string.action_new_scan),
                icon = Icons.Default.Security,
                onClick = onScan
            )
            ActionCard(
                title = stringResource(id = R.string.action_history),
                icon = Icons.Default.History,
                onClick = onHistory
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = stringResource(id = R.string.action_scan_apk),
                icon = Icons.Default.Android,
                onClick = onPickApk
            )
            ActionCard(
                title = stringResource(id = R.string.action_search_app),
                icon = Icons.Default.Search,
                onClick = onSearch
            )
        }
    }
}

@Composable
private fun RowScope.ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E233A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color = Color(0xFF7C3AED).copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF7C3AED)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RiskyAppsSection(
    apps: List<AppResult>,
    onNavigateToAppDetails: (String) -> Unit,
    title: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        apps.forEach { app ->
            RiskyAppCard(app = app, onClick = { onNavigateToAppDetails(app.packageName) })
        }
    }
}

@Composable
private fun RiskyAppCard(app: AppResult, onClick: () -> Unit) {
    val scoreColor = riskColorForScore(app.finalScore)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E233A)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))
                RiskBadge(score = app.finalScore)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(id = R.string.score_value, app.finalScore.toInt()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun RiskBadge(score: Float) {
    val (label, color) = when {
        score < 25f -> stringResource(id = R.string.level_critical) to Color(0xFFB91C1C)
        score < 40f -> stringResource(id = R.string.level_high) to Color(0xFFDC2626)
        score < 70f -> stringResource(id = R.string.level_medium) to Color(0xFFFFA227)
        else -> stringResource(id = R.string.level_low) to Color(0xFF16A34A)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color.copy(alpha = 0.8f), shape = CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

@Composable
private fun EmptyRiskState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E233A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(id = R.string.no_risky_apps_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = stringResource(id = R.string.no_risky_apps_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1F1F)),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(id = R.string.error_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun riskColorForScore(score: Float): Color {
    return when {
        score < 40f -> Color(0xFFDC2626)
        score < 70f -> Color(0xFFFFA227)
        else -> Color(0xFF16A34A)
    }
}

data class HomeState(
    val lastScanDate: String? = null,
    val overallScore: Float = 0f,
    val highRiskCount: Int = 0,
    val mediumRiskCount: Int = 0,
    val lowRiskCount: Int = 0,
    val totalApps: Int = 0,
    val riskyApps: List<AppResult> = emptyList(),
    val recentScans: List<RecentScan> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
) {
    val hasScan: Boolean
        get() = riskyApps.isNotEmpty()
}

data class RecentScan(
    val date: String,
    val appsScanned: Int,
    val issuesFound: Int
)
