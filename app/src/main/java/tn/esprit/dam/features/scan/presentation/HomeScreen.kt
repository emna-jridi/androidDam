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
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
            .background(ScanTheme.DarkBg)
            .padding(horizontal = ScanTheme.Spacing20, vertical = ScanTheme.Spacing20),
        verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing20)
    ) {
            item {
                if (homeState.loading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
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
                    style = MaterialTheme.typography.titleMedium,
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
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
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
                        shape = RoundedCornerShape(ScanTheme.CornerLarge),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(ScanTheme.Spacing16),
                            verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
                        ) {
                            Text(
                                text = "Aucun scan effectué",
                                style = MaterialTheme.typography.titleMedium,
                                color = ScanTheme.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Lancez votre premier scan pour sécuriser vos applications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ScanTheme.TextSecondary
                            )
                            TextButton(onClick = onNavigateToScan) {
                                Text("Commencer un scan", color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
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
            .padding(horizontal = ScanTheme.Spacing20, vertical = ScanTheme.Spacing16),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing4)) {
            Text(
                text = "ShadowGuard",
                style = MaterialTheme.typography.headlineMedium,
                color = ScanTheme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Protection et confidentialité",
                style = MaterialTheme.typography.bodySmall,
                color = ScanTheme.TextSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12)) {
            IconButton(
                onClick = onProfile,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(ScanTheme.CornerMedium))
                    .background(ScanTheme.Surface)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = ScanTheme.TextPrimary)
            }
            IconButton(
                onClick = onMenu,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(ScanTheme.CornerMedium))
                    .background(ScanTheme.Surface)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = ScanTheme.TextPrimary)
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
        containerColor = ScanTheme.CardBg,
        tonalElevation = 8.dp,
        contentColor = ScanTheme.TextPrimary
    ) {
        NavigationBarItem(
            selected = true,
            onClick = onHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Accueil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6366F1),
                selectedTextColor = ScanTheme.TextPrimary,
                indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onScan,
            icon = { Icon(Icons.Default.Security, contentDescription = null) },
            label = { Text("Sécurité") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6366F1),
                selectedTextColor = ScanTheme.TextPrimary,
                indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onHistory,
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("Historique") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6366F1),
                selectedTextColor = ScanTheme.TextPrimary,
                indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfile,
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6366F1),
                selectedTextColor = ScanTheme.TextPrimary,
                indicatorColor = Color(0xFF6366F1).copy(alpha = 0.1f)
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
        shape = RoundedCornerShape(ScanTheme.CornerXLarge),
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanTheme.Spacing24),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing32)
        ) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFF6366F1).copy(alpha = 0.3f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                CircularProgressIndicator(
                    progress = progress,
                    strokeWidth = 10.dp,
                    color = Color(0xFF6366F1),
                    trackColor = ScanTheme.SurfaceVariant,
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = animatedScore.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 42.sp),
                    color = ScanTheme.TextPrimary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12)
            ) {
                Text(
                    text = "Score de sécurité",
                    style = MaterialTheme.typography.labelMedium,
                    color = ScanTheme.TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = when (clampedScore) {
                        0 -> "Aucun scan effectuÃ©"
                        in 1..30 -> "âš ï¸ Score faible â€“ recommandations disponibles"
                        in 31..70 -> "ðŸŸ  Score moyen"
                        else -> "🟢 Très bon score"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (clampedScore) {
                        0 -> "Aucun scan effectuÃ©"
                        in 1..30 -> "Plusieurs risques importants"
                        in 31..70 -> "Quelques risques Ã  surveiller"
                        else -> "Appareil globalement sécurisé"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ScanTheme.TextSecondary
                )
                if (clampedScore > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)) {
                        ScoreTag("Permissions", Color(0xFFEF4444).copy(alpha = 0.18f), Color(0xFFEF4444))
                        ScoreTag("Trackers", Color(0xFFFB923C).copy(alpha = 0.18f), Color(0xFFFB923C))
                    }
                }
                Text(
                    text = "Le score est basé sur les permissions sensibles et les trackers détectés",
                    style = MaterialTheme.typography.labelSmall,
                    color = ScanTheme.TextSecondary
                )
                TextButton(onClick = onImproveClick) {
                    Text("Voir comment améliorer", color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ScoreTag(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(ScanTheme.CornerSmall))
            .padding(horizontal = ScanTheme.Spacing8, vertical = ScanTheme.Spacing4)
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeatureGrid(
    onScan: () -> Unit,
    onSearch: () -> Unit,
    onHistory: () -> Unit,
    onScanApk: () -> Unit,
    onShadowGuard: () -> Unit,
    onAlerts: () -> Unit
) {
    val items = listOf(
        FeatureItem("Nouveau scan", Icons.Default.Security, "Analyser maintenant", onScan, listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))),
        FeatureItem("Historique", Icons.Default.History, "Derniers résultats", onHistory, listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))),
        FeatureItem("Scanner un APK", Icons.Default.Android, "Fichier externe", onScanApk, listOf(Color(0xFF10B981), Color(0xFF059669))),
        FeatureItem("Rechercher une application", Icons.Default.Search, "Vérifier un app", onSearch, listOf(Color(0xFF14B8A6), Color(0xFF0EA5E9))),
        FeatureItem("Alertes de sécurité", Icons.Default.Error, "Journaux d'accès", onAlerts, listOf(Color(0xFFEF4444), Color(0xFFF97316))),
        FeatureItem("ShadowVault", Icons.Default.Lock, "Gestion des mots de passe", onShadowGuard, listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing16),
        verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing16),
        modifier = Modifier
            .fillMaxWidth()
            .height((140.dp * 3) + (ScanTheme.Spacing16 * 2))
    ) {
        items(items) { item ->
            FeatureCard(item)
        }
    }
}

private data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit,
    val gradient: List<Color>
)

@Composable
private fun FeatureCard(item: FeatureItem) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "featurePress"
    )

    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 140.dp)
            .scale(scale)
            .clip(RoundedCornerShape(ScanTheme.CornerLarge))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        val success = try { tryAwaitRelease(); true } catch (e: Exception) { false }
                        pressed = false
                        if (success) item.onClick()
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = ScanTheme.SurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ScanTheme.Border.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ScanTheme.Spacing12),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(brush = Brush.linearGradient(item.gradient), shape = RoundedCornerShape(ScanTheme.CornerMedium)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = item.title, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(ScanTheme.Spacing8))
            Text(item.title, style = MaterialTheme.typography.labelLarge, color = ScanTheme.TextPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(item.description, style = MaterialTheme.typography.labelSmall, color = ScanTheme.TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RecentActivitySection(
    riskyApps: List<AppResult>,
    onNavigateToAppDetails: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12)) {
        Text(
            text = "Activité récente",
            style = MaterialTheme.typography.titleMedium,
            color = ScanTheme.TextPrimary,
            fontWeight = FontWeight.Bold
        )

        if (riskyApps.isEmpty()) {
            Text(
                text = "Aucune analyse récente",
                style = MaterialTheme.typography.bodySmall,
                color = ScanTheme.TextSecondary
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
                    colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
                    shape = RoundedCornerShape(ScanTheme.CornerLarge),
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
                            .padding(ScanTheme.Spacing16),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing4), modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = ScanTheme.TextPrimary)
                            Text("Score ${score.toInt()}/100", style = MaterialTheme.typography.labelSmall, color = ScanTheme.TextSecondary)
                            RiskBadge(score = score)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ScanTheme.TextSecondary)
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
        colors = CardDefaults.cardColors(containerColor = ScanTheme.SurfaceVariant),
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
                    text = appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pkgName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
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
        get() = totalApps > 0
}

data class RecentScan(
    val date: String,
    val appsScanned: Int,
    val issuesFound: Int
)
