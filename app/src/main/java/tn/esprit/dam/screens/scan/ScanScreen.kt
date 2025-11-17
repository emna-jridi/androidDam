package tn.esprit.dam.screens.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.data.model.AppAnalysisResult
import tn.esprit.dam.data.model.ScanItem
import java.text.SimpleDateFormat
import java.util.*

// ============================================================
// MAIN SCREEN
// ============================================================

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
    userHash: String,
    onNavigateToAppDetail: (String) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats by viewModel.stats.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadLastScan(userHash)
    }

    Scaffold(
        topBar = { ScanTopBar(onNavigateToHistory = onNavigateToHistory) },
        floatingActionButton = {
            if (uiState !is ScanUiState.Scanning) {
                ScanFAB(
                    isFromCache = uiState is ScanUiState.Success && (uiState as ScanUiState.Success).isFromCache,
                    onClick = { viewModel.startScan(userHash) }
                )
            }
        },
        containerColor = ScanTheme.DarkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ScanUiState.Initial -> {
                    EmptyState(onStartScan = { viewModel.startScan(userHash) })
                }

                is ScanUiState.LoadingLastScan -> {
                    LoadingLastScanState()
                }

                is ScanUiState.Scanning -> {
                    ScanningState(state.progress, state.totalApps)
                }

                is ScanUiState.Success -> {
                    ScanResultsList(
                        scan = state.scan,
                        stats = stats,
                        isFromCache = state.isFromCache,
                        onAppClick = onNavigateToAppDetail
                    )
                }

                is ScanUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.startScan(userHash) },
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

// ============================================================
// THEME
// ============================================================

object ScanTheme {
    val DarkBg = Color(0xFF0F172A)
    val CardBg = Color(0xFF1E293B)
    val CardHover = Color(0xFF334155)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF94A3B8)
    val BorderColor = Color(0xFF334155)

    val CriticalBg = Color(0x1AEF4444)
    val CriticalText = Color(0xFFEF4444)
    val HighBg = Color(0x1AFB923C)
    val HighText = Color(0xFFFB923C)
    val MediumBg = Color(0x1AFBBF24)
    val MediumText = Color(0xFFFBBF24)
    val LowBg = Color(0x1A10B981)
    val LowText = Color(0xFF10B981)

    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
    )
}

// ============================================================
// TOP BAR
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTopBar(onNavigateToHistory: () -> Unit) {
    Surface(
        color = ScanTheme.DarkBg,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ShadowGuard",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ScanTheme.TextPrimary
                    )
                )
                Text(
                    text = "Protection des données",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ScanTheme.TextSecondary,
                        fontSize = 13.sp
                    )
                )
            }

            IconButton(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ScanTheme.CardBg)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "Historique",
                    tint = ScanTheme.TextPrimary
                )
            }
        }
    }
}

// ============================================================
// EMPTY STATE
// ============================================================

@Composable
fun EmptyState(onStartScan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(ScanTheme.PrimaryGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Aucun Scan Disponible",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ScanTheme.TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Lancez votre premier scan pour\nanalyser vos applications",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = ScanTheme.TextSecondary
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScanTheme.PrimaryGradient),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Démarrer le Scan", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ============================================================
// LOADING LAST SCAN STATE
// ============================================================

@Composable
fun LoadingLastScanState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF6366F1),
                strokeWidth = 4.dp
            )
            Text(
                text = "Chargement du dernier scan...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = ScanTheme.TextSecondary
                )
            )
        }
    }
}

// ============================================================
// SCANNING STATE
// ============================================================

@Composable
fun ScanningState(progress: String, totalApps: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .rotate(rotation)
                    .clip(RoundedCornerShape(32.dp))
                    .background(ScanTheme.PrimaryGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }

            Text(
                text = progress,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ScanTheme.TextPrimary
                )
            )

            if (totalApps > 0) {
                Text(
                    text = "$totalApps applications détectées",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = ScanTheme.TextSecondary
                    )
                )
            }

            LinearProgressIndicator(
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF6366F1),
                trackColor = ScanTheme.CardBg
            )
        }
    }
}

// ============================================================
// ERROR STATE
// ============================================================

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = ScanTheme.CardBg
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ScanTheme.CriticalBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = ScanTheme.CriticalText,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Erreur de Scan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ScanTheme.TextSecondary
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ScanTheme.TextSecondary
                        )
                    ) {
                        Text("Annuler")
                    }

                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ScanTheme.PrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Réessayer")
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// RESULTS LIST
// ============================================================

@Composable
fun ScanResultsList(
    scan: ScanItem,  // ✅ Type correct
    stats: ScanStatsData,
    isFromCache: Boolean,
    onAppClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isFromCache) {
            item {
                ScanCacheBadge(scan = scan)
            }
        }

        item {
            ScanStatsCard(stats = stats)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(scan.report.results) { result ->
            ScanResultCard(
                result = result,
                onClick = { onAppClick(result.packageName) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ============================================================
// CACHE BADGE
// ============================================================

@Composable
fun ScanCacheBadge(scan: ScanItem) {  // ✅ Type correct
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanTheme.LowBg
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = ScanTheme.LowText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dernier scan chargé",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ScanTheme.LowText,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Scanné le ${scan.createdAt?.toSimpleDate() ?: "Date inconnue"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanTheme.LowText.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

// ============================================================
// STATS CARD
// ============================================================

@Composable
fun ScanStatsCard(stats: ScanStatsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanTheme.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Résumé du Scan",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    count = stats.totalApps,
                    label = "Apps",
                    icon = Icons.Default.Apps,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    count = stats.avgScore,
                    label = "Score",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = ScanTheme.BorderColor)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (stats.criticalApps + stats.highRiskApps > 0) {
                    RiskRow(
                        count = stats.criticalApps + stats.highRiskApps,
                        label = "Critique",
                        color = ScanTheme.CriticalText,
                        bgColor = ScanTheme.CriticalBg
                    )
                }
                if (stats.mediumRiskApps > 0) {
                    RiskRow(
                        count = stats.mediumRiskApps,
                        label = "Modéré",
                        color = ScanTheme.MediumText,
                        bgColor = ScanTheme.MediumBg
                    )
                }
                RiskRow(
                    count = stats.lowRiskApps,
                    label = "Faible",
                    color = ScanTheme.LowText,
                    bgColor = ScanTheme.LowBg
                )
            }
        }
    }
}

@Composable
fun StatChip(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ScanTheme.CardHover
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ScanTheme.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanTheme.TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RiskRow(
    count: Int,
    label: String,
    color: Color,
    bgColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ScanTheme.TextSecondary
                )
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bgColor
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

// ============================================================
// RESULT CARD
// ============================================================

@Composable
fun ScanResultCard(
    result: AppAnalysisResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanTheme.CardBg
        )
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
                    text = result.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.packageName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanTheme.TextSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                ScoreBadge(score = result.score)
                Spacer(modifier = Modifier.height(4.dp))
                RiskBadge(riskLevel = result.riskLevel)
            }
        }
    }
}

@Composable
fun ScoreBadge(score: Int) {
    val (color, bgColor) = when {
        score >= 70 -> ScanTheme.LowText to ScanTheme.LowBg
        score >= 40 -> ScanTheme.MediumText to ScanTheme.MediumBg
        else -> ScanTheme.CriticalText to ScanTheme.CriticalBg
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = "$score/100",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun RiskBadge(riskLevel: String) {
    val (color, bgColor, label) = when (riskLevel.lowercase()) {
        "low" -> Triple(ScanTheme.LowText, ScanTheme.LowBg, "Faible")
        "medium" -> Triple(ScanTheme.MediumText, ScanTheme.MediumBg, "Moyen")
        "high", "critical" -> Triple(ScanTheme.CriticalText, ScanTheme.CriticalBg, "Élevé")
        else -> Triple(ScanTheme.TextSecondary, ScanTheme.CardHover, "Inconnu")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        )
    }
}

// ============================================================
// FAB
// ============================================================

@Composable
fun ScanFAB(
    isFromCache: Boolean,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF6366F1),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (isFromCache) Icons.Default.Refresh else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Text(
                text = if (isFromCache) "Nouveau Scan" else "Lancer Scan",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================
// DATE FORMATTER
// ============================================================

fun String.toSimpleDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this) ?: return this

        val outputFormat = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.FRENCH)
        outputFormat.format(date)
    } catch (e: Exception) {
        this
    }
}