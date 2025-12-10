package tn.esprit.dam.screens.scan

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import tn.esprit.dam.data.preferences.PreferencesManager
import tn.esprit.dam.ui.components.common.AnimatedLoadingState
import tn.esprit.dam.ui.components.common.EmptyState
import tn.esprit.dam.ui.components.common.ErrorState
import tn.esprit.dam.ui.components.common.ErrorType
import tn.esprit.dam.ui.components.common.LoadingState
import tn.esprit.dam.ui.components.scan.ScanResultsList
import tn.esprit.dam.ui.theme.CardBg
import tn.esprit.dam.ui.theme.DarkBackground
import tn.esprit.dam.ui.theme.DarkSurface
import tn.esprit.dam.ui.theme.DarkTextSecondary
import tn.esprit.dam.ui.theme.PrimaryGradient
import tn.esprit.dam.ui.theme.TextOnPrimary


@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
    userHash: String,
    preferencesManager: PreferencesManager,
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
        containerColor = DarkBackground
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
                    LoadingState(message = "Chargement du dernier scan...")
                }

                is ScanUiState.Scanning -> {
                    AnimatedLoadingState(
                        message = "Analyse en cours",
                        subtitle = "Vérification de vos applications",
                        progress = "${state.totalApps} applications détectées",
                        icon = Icons.Default.Shield,
                        gradient = PrimaryGradient
                    )
                }

                is ScanUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                            .padding(horizontal = 16.dp)
                    ) {
                        // Cache badge
                        item {
                            ScanCacheBadge(scan = state.scan)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Score Gauge
                        item {
                            ScoreGauge(
                                score = stats?.avgScore ?: 0  // ✅ CHANGÉ: riskScore → totalScore
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Scan results
                        item {
                            ScanResultsList(
                                scan = state.scan,
                                stats = stats,
                                isFromCache = state.isFromCache,
                                onAppClick = onNavigateToAppDetail
                            )
                        }
                    }
                }

                is ScanUiState.Error -> {
                    ErrorState(
                        title = "Erreur de Scan",
                        message = state.message,
                        onRetry = { viewModel.startScan(userHash) },
                        onDismiss = { viewModel.loadLastScan(userHash) },
                        errorType = ErrorType.GENERAL
                    )
                }
            }
        }
    }
}




// ============================================================
// TOP BAR & FAB (unchanged)
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTopBar(onNavigateToHistory: () -> Unit) {
    Surface(
        color = DarkBackground,
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
                        color = TextOnPrimary
                    )
                )
                Text(
                    text = "Protection des données",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DarkTextSecondary,
                        fontSize = 13.sp
                    )
                )
            }

            IconButton(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "Historique",
                    tint = TextOnPrimary
                )
            }
        }
    }
}

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
