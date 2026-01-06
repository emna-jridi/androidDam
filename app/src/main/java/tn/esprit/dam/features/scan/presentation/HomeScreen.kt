package tn.esprit.dam.features.scan.presentation

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.scale
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import tn.esprit.dam.R
import tn.esprit.dam.data.api.models.AppResult
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography
import tn.esprit.dam.ui.components.AppEmptyState
import tn.esprit.dam.ui.components.AppPrimaryButton

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
                } catch (_: SecurityException) {
                    // Permission is best-effort; continue with transient grant
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

    // Scaffold removed - AppNavGraph already provides topBar and bottomBar
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
                    text = "Actions rapides",
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
                        title = "No scan performed",
                        message = "Launch your first scan to secure your applications.",
                        onAction = onNavigateToScan,
                        actionText = "Start a scan"
                    )
                }
            }
        }
}
@Composable
private fun TopBar(
    onMenu: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                text = "ShadowGuard",
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Protection and privacy",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            IconButton(
                onClick = onProfile,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(AppColors.surface)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = AppColors.textPrimary)
            }
            IconButton(
                onClick = onMenu,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(AppColors.surface)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = AppColors.textPrimary)
            }
        }
    }
}

@Composable
private fun BottomNav(
    onHome: () -> Unit,
    onScan: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit
) {
    NavigationBar(
        containerColor = AppColors.surface,
        tonalElevation = 8.dp,
        contentColor = AppColors.textPrimary
    ) {
        NavigationBarItem(
            selected = true,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.textPrimary,
                indicatorColor = AppColors.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onScan,
            icon = { Icon(Icons.Default.Security, contentDescription = null) },
            label = { Text("Security") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.textPrimary,
                indicatorColor = AppColors.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onHistory,
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("History") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.textPrimary,
                indicatorColor = AppColors.primary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfile,
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AppColors.primary,
                selectedTextColor = AppColors.textPrimary,
                indicatorColor = AppColors.primary.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun ScoreSection(score: Int, onImproveClick: () -> Unit) {
    val clampedScore = score.coerceIn(0, 100)
    val animatedScore by animateFloatAsState(
        targetValue = clampedScore.toFloat(),
        animationSpec = tween(durationMillis = 1400),
        label = "scoreValue"
    )
    val progress by animateFloatAsState(
        targetValue = clampedScore / 100f,
        animationSpec = tween(durationMillis = 1400),
        label = "scoreProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(AppCorners.xlarge),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl)
        ) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(AppColors.primary.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                CircularProgressIndicator(
                    progress = progress,
                    strokeWidth = 10.dp,
                    color = AppColors.primary,
                    trackColor = AppColors.surfaceVariant,
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = animatedScore.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 42.sp),
                    color = AppColors.textPrimary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Text(
                    text = "Security Score",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = when (clampedScore) {
                        0 -> "No scan performed"
                        in 1..30 -> "âš ï¸ Low score - recommendations available"
                        in 31..70 -> "ðŸŸ  Medium score"
                        else -> "🟢 Excellent score"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (clampedScore) {
                        0 -> "No scan performed"
                        in 1..30 -> "Several important risks"
                        in 31..70 -> "Some risks to monitor"
                        else -> "Device globally secured"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                if (clampedScore > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        ScoreTag("Permissions", AppColors.error.copy(alpha = 0.18f), AppColors.error)
                        ScoreTag("Trackers", AppColors.warning.copy(alpha = 0.18f), AppColors.warning)
                    }
                }
                Text(
                    text = "Score based on sensitive permissions and detected trackers",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.textSecondary
                )
                TextButton(onClick = onImproveClick) {
                    Text("See how to improve", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ScoreTag(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(AppCorners.small))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FeatureGrid(
    onScan: () -> Unit,
    onSearch: () -> Unit,
    onHistory: () -> Unit,
    onScanApk: () -> Unit,
    onShadowGuard: () -> Unit,
    onAlerts: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            FeatureCard(
                title = "New Scan",
                icon = Icons.Default.Security,
                description = "Analyze now",
                onClick = onScan,
                gradient = listOf(AppColors.primaryDark, AppColors.primary),
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                title = "History",
                icon = Icons.Default.History,
                description = "Latest results",
                onClick = onHistory,
                gradient = listOf(AppColors.info, AppColors.infoDark),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            FeatureCard(
                title = "Scan an APK",
                icon = Icons.Default.Android,
                description = "External file",
                onClick = onScanApk,
                gradient = listOf(AppColors.success, AppColors.successDark),
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                title = "Search an app",
                icon = Icons.Default.Search,
                description = "Check an app",
                onClick = onSearch,
                gradient = listOf(AppColors.teal, AppColors.info),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            FeatureCard(
                title = "Security Alerts",
                icon = Icons.Default.Error,
                description = "Access logs",
                onClick = onAlerts,
                gradient = listOf(AppColors.error, AppColors.warning),
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                title = "ShadowVault",
                icon = Icons.Default.Lock,
                description = "Password management",
                onClick = onShadowGuard,
                gradient = listOf(AppColors.primary, AppColors.primaryDark),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "featurePress"
    )

    Card(
        modifier = modifier
            .height(112.dp)
            .scale(scale)
            .clip(RoundedCornerShape(AppCorners.large))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        val success = try { tryAwaitRelease(); true } catch (e: Exception) { false }
                        pressed = false
                        if (success) onClick()
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = AppColors.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(brush = Brush.linearGradient(gradient), shape = RoundedCornerShape(AppCorners.medium)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun RecentActivitySection(
    riskyApps: List<AppResult>,
    onNavigateToAppDetails: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = "Recent activity",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        if (riskyApps.isEmpty()) {
            Text(
                text = "No recent analysis",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary
            )
        } else {
            riskyApps.take(3).forEach { app ->
                val score = app.aiRiskScore ?: app.finalScore ?: 0f
                val name = app.appName ?: app.packageName ?: "App"

                var appCardPressed by remember { mutableStateOf(false) }
                val appCardScale by animateFloatAsState(
                    targetValue = if (appCardPressed) 0.98f else 1f,
                    animationSpec = tween(durationMillis = 100),
                    label = "appCardPress"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                    shape = RoundedCornerShape(AppCorners.large),
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(appCardScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    appCardPressed = true
                                    val success = try { tryAwaitRelease(); true } catch (e: Exception) { false }
                                    appCardPressed = false
                                    if (success) {
                                        app.packageName?.let { onNavigateToAppDetails(it) }
                                    }
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = AppColors.textPrimary)
                            Text("Score ${score.toInt()}/100", style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
                            RiskBadge(score = score)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.textSecondary)
                    }
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
        shape = RoundedCornerShape(AppCorners.xlarge)
    ) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
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
                        tint = AppColors.primary
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
        colors = CardDefaults.cardColors(containerColor = AppColors.surfaceVariant),
        shape = RoundedCornerShape(AppCorners.xlarge)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = stringResource(id = R.string.security_score_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
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
                        color = AppColors.textSecondary
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
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        LegendPill(color = AppColors.error, label = stringResource(id = R.string.level_high))
        LegendPill(color = AppColors.warning, label = stringResource(id = R.string.level_medium))
        LegendPill(color = AppColors.success, label = stringResource(id = R.string.level_low))
    }
}

@Composable
private fun LegendPill(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(text = label, color = AppColors.textSecondary, style = MaterialTheme.typography.bodySmall)
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
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        shape = RoundedCornerShape(AppCorners.large)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color = AppColors.primary.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AppColors.primary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
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
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary
        )

        apps.forEach { app ->
            // Fix: Handle nullable packageName
            app.packageName?.let { pkg ->
                RiskyAppCard(app = app, onClick = { onNavigateToAppDetails(pkg) })
            }
        }
    }
}

@Composable
private fun RiskyAppCard(app: AppResult, onClick: () -> Unit) {
    // Fix: Handle nullable fields with defaults
    val score = app.aiRiskScore ?: app.finalScore ?: 0f
    val appName = app.appName ?: app.packageName ?: "Unknown App"
    val pkgName = app.packageName ?: ""

    val scoreColor = riskColorForScore(score)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pkgName,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))
                RiskBadge(score = score)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(id = R.string.score_value, score.toInt()),
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
        score < 25f -> stringResource(id = R.string.level_critical) to AppColors.riskCritical
        score < 40f -> stringResource(id = R.string.level_high) to AppColors.riskHigh
        score < 70f -> stringResource(id = R.string.level_medium) to AppColors.riskMedium
        else -> stringResource(id = R.string.level_low) to AppColors.riskLow
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color.copy(alpha = 0.8f), shape = CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textPrimary
        )
    }
}

@Composable
private fun EmptyRiskState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        shape = RoundedCornerShape(AppCorners.large)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = AppColors.textSecondary
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = stringResource(id = R.string.no_risky_apps_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.textPrimary
                )
                Text(
                    text = stringResource(id = R.string.no_risky_apps_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.error.copy(alpha = 0.1f)),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(AppCorners.large)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = AppColors.error,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(
                        text = stringResource(id = R.string.error_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.error
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                }
            }
        }
    }
}

private fun riskColorForScore(score: Float): Color {
    return when {
        score < 40f -> AppColors.error
        score < 70f -> AppColors.warning
        else -> AppColors.success
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
        get() = totalApps > 0
}

data class RecentScan(
    val date: String,
    val appsScanned: Int,
    val issuesFound: Int
)



