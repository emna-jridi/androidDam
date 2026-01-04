package tn.esprit.dam.features.scan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.data.api.models.ScanHistoryItemDto
import java.text.SimpleDateFormat
import java.util.*
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography
import tn.esprit.dam.ui.components.AppLoadingState
import tn.esprit.dam.ui.components.AppErrorState
import tn.esprit.dam.ui.components.AppEmptyState
import tn.esprit.dam.ui.components.AppPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    viewModel: ScanHistoryViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onScanClick: (ScanHistoryItemDto) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedScan by remember { mutableStateOf<ScanHistoryItemDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    // TopAppBar is provided by NavGraph, don't duplicate it here
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
            when {
                uiState.isLoading && uiState.scans.isEmpty() -> {
                    AppLoadingState(message = "Chargement de l'historique...")
                }
                uiState.error != null && uiState.scans.isEmpty() -> {
                    AppErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.scans.isEmpty() -> {
                    AppEmptyState(
                        icon = Icons.Default.History,
                        title = "Aucun scan",
                        message = "Commencez un scan pour voir l'historique"
                    )
                }
                else -> {
                    HistoryList(
                        scans = uiState.scans,
                        isLoading = uiState.isLoading,
                        hasMore = uiState.hasMore,
                        onLoadMore = { viewModel.loadMore() },
                        onScanClick = { scan ->
                            selectedScan = scan
                            onScanClick(scan)
                        }
                    )
                }
            }

            selectedScan?.let { scan ->
                ScanDetailSheet(
                    scan = scan,
                    onDismiss = { selectedScan = null }
                )
            }
            // Error snackbar
            uiState.error?.takeIf { uiState.scans.isNotEmpty() }?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

@Composable
fun HistoryLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF6366F1))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Chargement de l'historique...",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun HistoryErrorState(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Erreur",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                error,
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

@Composable
fun HistoryEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Aucun scan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Commencez un scan pour voir l'historique",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun HistoryList(
    scans: List<ScanHistoryItemDto>,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onScanClick: (ScanHistoryItemDto) -> Unit
) {
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(scans) { scan ->
                ScanHistoryCard(scan, onClick = onScanClick)
            }

            if (hasMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF6366F1),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            TextButton(onClick = onLoadMore) {
                                Text("Charger plus", color = Color(0xFF6366F1))
                            }
                        }
                    }
                }
            }
        }
    }

    // Auto-load more when scrolling to bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex == scans.size - 1 && hasMore && !isLoading) {
                    onLoadMore()
                }
            }
    }
}

@Composable
fun ScanHistoryCard(scan: ScanHistoryItemDto, onClick: (ScanHistoryItemDto) -> Unit) {
            val scanType = if (scan.totalApps <= 1) "APK" else "Applications"
            val riskLevel = deriveRiskLevel(scan.averageScore)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClick(scan) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header: Status and date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusChip(status = scan.status)
                            TypeChip(label = scanType)
                            RiskLevelChip(riskLevel = riskLevel)
                        }
                        Text(
                            formatDate(scan.createdAt),
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Apps",
                            value = "${scan.scannedApps}/${scan.totalApps}",
                            icon = Icons.Default.Apps
                        )

                        if (scan.status == "completed" && scan.averageScore != null) {
                            StatItem(
                                label = "Score global",
                                value = "${scan.averageScore}/100",
                                icon = Icons.Default.Score,
                                color = getScoreColor(scan.averageScore)
                            )
                        }

                        if (scan.duration != null) {
                            StatItem(
                                label = "Durée",
                                value = formatDuration(scan.duration),
                                icon = Icons.Default.Timer
                            )
                        }
                    }

                    // Risk breakdown (if completed)
                    if (scan.status == "completed") {
                        Spacer(modifier = Modifier.height(12.dp))
                        RiskBreakdown(
                            high = scan.highRiskApps ?: 0,
                            medium = scan.mediumRiskApps ?: 0,
                            low = scan.lowRiskApps ?: 0
                        )
                    }
                }
            }
        }

        @Composable
        fun StatusChip(status: String) {
            val (text, color) = when (status) {
                "completed" -> "Terminé" to Color(0xFF10B981)
                "analyzing" -> "En cours" to Color(0xFFFBBF24)
                "failed" -> "Échoué" to Color(0xFFEF4444)
                else -> "En attente" to Color(0xFF94A3B8)
            }

            Surface(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        @Composable
        fun TypeChip(label: String) {
            Surface(
                color = Color(0xFF334155),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        @Composable
        fun RiskLevelChip(riskLevel: String) {
            val color = riskLevelColor(riskLevel)
            Surface(
                color = color.copy(alpha = 0.18f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (riskLevel.lowercase()) {
                        "low" -> "Faible"
                        "medium" -> "Moyen"
                        "high" -> "Élevé"
                        "critical" -> "Critique"
                        else -> "Inconnu"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        private fun ScanDetailSheet(scan: ScanHistoryItemDto, onDismiss: () -> Unit) {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                containerColor = Color(0xFF0F172A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Détails du scan",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatDate(scan.createdAt),
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        StatusChip(status = scan.status)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DetailStatCard(
                            title = "Apps",
                            value = "${scan.scannedApps}/${scan.totalApps}",
                            icon = Icons.Default.Apps
                        )
                        DetailStatCard(
                            title = "Score",
                            value = scan.averageScore?.let { "$it/100" } ?: "N/A",
                            icon = Icons.Default.Score,
                            color = getScoreColor(scan.averageScore ?: 0)
                        )
                    }

                    scan.duration?.let {
                        DetailStatCard(
                            title = "Durée",
                            value = formatDuration(it),
                            icon = Icons.Default.Timer
                        )
                    }

                    Text(
                        "Répartition des risques",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RiskBadge("Élevé", scan.highRiskApps ?: 0, Color(0xFFEF4444))
                        RiskBadge("Moyen", scan.mediumRiskApps ?: 0, Color(0xFFFBBF24))
                        RiskBadge("Faible", scan.lowRiskApps ?: 0, Color(0xFF10B981))
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Type de scan",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodySmall
                    )
                    TypeChip(label = if (scan.totalApps <= 1) "APK" else "Applications")
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        @Composable
        private fun DetailStatCard(
            title: String,
            value: String,
            icon: androidx.compose.ui.graphics.vector.ImageVector,
            color: Color = Color(0xFF6366F1)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        title,
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        value,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        @Composable
        fun StatItem(
            label: String,
            value: String,
            icon: androidx.compose.ui.graphics.vector.ImageVector,
            color: Color = Color(0xFF6366F1)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    label,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        @Composable
        fun RiskBreakdown(high: Int, medium: Int, low: Int) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RiskBadge("Élevé", high, Color(0xFFEF4444))
                RiskBadge("Moyen", medium, Color(0xFFFBBF24))
                RiskBadge("Faible", low, Color(0xFF10B981))
            }
        }

        @Composable
        fun RiskBadge(label: String, count: Int, color: Color) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "$label: $count",
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        private fun deriveRiskLevel(score: Int?): String {
            return when {
                score == null -> "unknown"
                score < 40 -> "low"
                score < 70 -> "medium"
                score < 85 -> "high"
                else -> "critical"
            }
        }

        private fun riskLevelColor(riskLevel: String): Color {
            return when (riskLevel.lowercase()) {
                "low" -> Color(0xFF10B981)
                "medium" -> Color(0xFFFBBF24)
                "high" -> Color(0xFFFF6B6B)
                "critical" -> Color(0xFFEF4444)
                else -> Color(0xFFCBD5E1)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateString)
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }

        private fun formatDuration(durationMs: Long): String {
            val seconds = durationMs / 1000
            val minutes = seconds / 60
            val hours = minutes / 60

            return when {
                hours > 0 -> "${hours}h ${minutes % 60}m"
                minutes > 0 -> "${minutes}m ${seconds % 60}s"
                else -> "${seconds}s"
            }
        }

        private fun getScoreColor(score: Int): Color {
            return when {
                score < 40 -> Color(0xFF10B981)   // Low
                score < 70 -> Color(0xFFFBBF24)   // Medium
                score < 85 -> Color(0xFFFF6B6B)   // High
                else -> Color(0xFFEF4444)         // Critical
            }
        }
