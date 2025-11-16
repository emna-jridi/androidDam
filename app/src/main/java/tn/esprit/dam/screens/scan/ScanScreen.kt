package tn.esprit.dam.screens.scan

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.dam.data.model.SavedScan
import tn.esprit.dam.data.model.AppAnalysisResult // ✅ CHANGÉ

@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    onAppDetails: (String) -> Unit,
    userHash: String,
    viewModel: ScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val stats by viewModel.stats.collectAsState()

    // Initialiser le ViewModel
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        // Charger le dernier scan au démarrage
        viewModel.loadLastScan(context, userHash)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScanTheme.DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            ScanHeader(onBack = onNavigateBack)

            // Content selon l'état
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val state = uiState) {
                    is ScanUiState.Initial -> {
                        ScanEmptyState()
                    }

                    is ScanUiState.LoadingLastScan -> {
                        ScanLoadingState(
                            message = "Chargement du dernier scan...",
                            showProgress = false
                        )
                    }

                    is ScanUiState.Scanning -> {
                        ScanLoadingState(
                            message = state.progress,
                            totalApps = state.totalApps,
                            showProgress = true
                        )
                    }

                    is ScanUiState.Success -> {
                        ScanResultsList(
                            scan = state.scan,
                            stats = stats,
                            isFromCache = state.isFromCache,
                            onAppClick = onAppDetails
                        )
                    }

                    is ScanUiState.Error -> {
                        ScanErrorState(
                            error = state.message,
                            onRetry = { viewModel.clearError() }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        ScanFAB(
            isScanning = uiState is ScanUiState.Scanning,
            isDisabled = uiState is ScanUiState.LoadingLastScan,
            onClick = {
                viewModel.startScan(context, userHash)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}

// ==================== THEME ====================

object ScanTheme {
    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
    )
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
}

// ==================== COMPONENTS ====================

@Composable
fun ScanHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ScanTheme.DarkBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ScanTheme.CardBg)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = ScanTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ScanTheme.PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Analyse Complète",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScanTheme.TextPrimary
                        )
                    )
                    Text(
                        text = "Scanner les applications",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ScanTheme.TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ScanEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(ScanTheme.PrimaryGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Aucun Scan Effectué",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Appuyez sur le bouton ci-dessous pour\nanalyser vos applications installées",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ScanTheme.TextSecondary
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScanLoadingState(
    message: String,
    totalApps: Int = 0,
    showProgress: Boolean = true
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ScanTheme.PrimaryGradient),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (totalApps > 0) {
                    "Analyse de $totalApps applications..."
                } else {
                    message
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cela peut prendre quelques minutes...",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ScanTheme.TextSecondary
                ),
                textAlign = TextAlign.Center
            )

            if (showProgress) {
                Spacer(modifier = Modifier.height(24.dp))

                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF6366F1),
                    trackColor = ScanTheme.CardBg
                )
            }
        }
    }
}

@Composable
fun ScanErrorState(
    error: String,
    onRetry: () -> Unit
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
                    text = error,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ScanTheme.TextSecondary
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
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
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Réessayer", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanResultsList(
    scan: SavedScan,
    stats: ScanStatsData,
    isFromCache: Boolean,
    onAppClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Badge "Dernier scan" si chargé depuis le cache
        if (isFromCache) {
            item {
                ScanCacheBadge(scanDate = scan.scanDate)
            }
        }

        // Stats Card
        item {
            ScanStatsCard(stats = stats)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Results
        items(scan.results) { result ->
            ScanResultCard(
                result = result,
                onClick = { onAppClick(result.packageName) }
            )
        }

        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ScanCacheBadge(scanDate: String) {
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
                    text = "Scanné le $scanDate",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanTheme.LowText.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ScanStatsCard(stats: ScanStatsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ScanTheme.LowText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Scan Terminé",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ScanTheme.TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${stats.totalApps} applications analysées",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScanTheme.TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val criticalCount = stats.criticalApps + stats.highRiskApps

                StatChip(
                    count = criticalCount,
                    label = "À risque",
                    color = ScanTheme.CriticalText,
                    bgColor = ScanTheme.CriticalBg,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    count = stats.mediumRiskApps,
                    label = "Modéré",
                    color = ScanTheme.MediumText,
                    bgColor = ScanTheme.MediumBg,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    count = stats.lowRiskApps,
                    label = "Sûres",
                    color = ScanTheme.LowText,
                    bgColor = ScanTheme.LowBg,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatChip(
    count: Int,
    label: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun ScanResultCard(
    result: AppAnalysisResult, // ✅ CHANGÉ de ScanResult à AppAnalysisResult
    onClick: () -> Unit
) {
    // ✅ Calculer des valeurs par défaut pour les champs manquants
    val score = when {
        result.totalTrackers > 10 -> 20
        result.totalTrackers > 5 -> 50
        else -> 80
    }

    val riskLevel = when {
        result.totalTrackers > 10 -> "HIGH"
        result.totalTrackers > 5 -> "MEDIUM"
        else -> "LOW"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ScanTheme.CardHover
                    ) {
                        Text(
                            text = "${result.totalTrackers} trackers", // ✅ CHANGÉ
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ScanTheme.TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ScanTheme.CardHover
                    ) {
                        Text(
                            text = "${result.permissions.size} permissions", // ✅ CHANGÉ
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ScanTheme.TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                PrivacyScoreChip(score = score) // ✅ Utiliser le score calculé
                Spacer(modifier = Modifier.height(8.dp))
                RiskLevelChip(riskLevel = riskLevel) // ✅ Utiliser le riskLevel calculé
            }
        }
    }
}

@Composable
fun PrivacyScoreChip(score: Int) {
    val (color, bgColor) = when {
        score >= 70 -> ScanTheme.LowText to ScanTheme.LowBg
        score >= 40 -> ScanTheme.MediumText to ScanTheme.MediumBg
        else -> ScanTheme.CriticalText to ScanTheme.CriticalBg
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = "$score/100",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun RiskLevelChip(riskLevel: String) {
    val (color, bgColor, label) = when (riskLevel.lowercase()) {
        "low" -> Triple(ScanTheme.LowText, ScanTheme.LowBg, "Faible")
        "medium" -> Triple(ScanTheme.MediumText, ScanTheme.MediumBg, "Moyen")
        "high", "critical" -> Triple(ScanTheme.CriticalText, ScanTheme.CriticalBg, "Élevé")
        else -> Triple(ScanTheme.TextSecondary, ScanTheme.CardHover, "Inconnu")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun ScanFAB(
    isScanning: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(68.dp),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDisabled) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.5f),
                                Color(0xFF9333EA).copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        ScanTheme.PrimaryGradient
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning || isDisabled) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Démarrer le scan",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}