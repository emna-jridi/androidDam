package tn.esprit.dam.screens.history

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.esprit.dam.data.model.ScanHistoryItem
import tn.esprit.dam.data.repository.ScanRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


// ============================================================
// SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanHistoryScreen(
    viewModel: ScanHistoryViewModel = hiltViewModel(),
    token: String,
    userHash: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadScans(token, userHash)
    }

    Scaffold(
        topBar = {
            HistoryTopBar(onNavigateBack = onNavigateBack)
        },
        containerColor = HistoryTheme.DarkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.scans.isEmpty() -> LoadingState()

                uiState.error != null && uiState.scans.isEmpty() -> ErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.refresh(token, userHash) }
                )

                uiState.scans.isEmpty() -> EmptyState()

                else -> HistoryList(
                    scans = uiState.scans,
                    isLoading = uiState.isLoading,
                    hasMore = uiState.hasMore,
                    onLoadMore = { viewModel.loadMore(token, userHash) },
                    onScanClick = onNavigateToDetail
                )
            }
        }
    }
}

// ============================================================
// THEME
// ============================================================

object HistoryTheme {
    val DarkBg = Color(0xFF0F172A)
    val CardBg = Color(0xFF1E293B)
    val CardHover = Color(0xFF334155)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF94A3B8)
    val BorderColor = Color(0xFF334155)
    val CriticalText = Color(0xFFEF4444)
    val MediumText = Color(0xFFFBBF24)
    val LowText = Color(0xFF10B981)
}

// ============================================================
// TOP BAR
// ============================================================

@Composable
fun HistoryTopBar(onNavigateBack: () -> Unit) {
    Surface(
        color = HistoryTheme.DarkBg,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HistoryTheme.CardBg)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = HistoryTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Historique des Scans",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = HistoryTheme.TextPrimary
                )
            )
        }
    }
}

// ============================================================
// STATES
// ============================================================

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF6366F1))
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, "", tint = Color.Red, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(error, color = HistoryTheme.TextSecondary)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Réessayer") }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.History, "", tint = HistoryTheme.TextSecondary, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("Aucun Scan", color = HistoryTheme.TextPrimary, fontWeight = FontWeight.Bold)
        Text("Vos scans apparaîtront ici", color = HistoryTheme.TextSecondary)
    }
}

// ============================================================
// LIST
// ============================================================

@Composable
fun HistoryList(
    scans: List<ScanHistoryItem>,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onScanClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(scans) { scan ->
            ScanHistoryCard(scan = scan, onClick = { onScanClick(scan._id) })
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (!isLoading && hasMore) {
            item {
                LaunchedEffect(Unit) { onLoadMore() }
            }
        }
    }
}

// ============================================================
// CARD
// ============================================================

@Composable
fun ScanHistoryCard(
    scan: ScanHistoryItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HistoryTheme.CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = scan.createdAt?.toSimpleDate() ?: "Date inconnue",
                        color = HistoryTheme.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Apps,
                            "",
                            tint = HistoryTheme.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${scan.totalApps} apps scannées",
                            color = HistoryTheme.TextSecondary
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, "", tint = HistoryTheme.TextSecondary)
            }

            scan.summary?.let { s ->
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = HistoryTheme.BorderColor)
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (s.highRiskApps > 0) RiskChip(s.highRiskApps, "Élevé", HistoryTheme.CriticalText)
                    if (s.mediumRiskApps > 0) RiskChip(s.mediumRiskApps, "Moyen", HistoryTheme.MediumText)
                    if (s.lowRiskApps > 0) RiskChip(s.lowRiskApps, "Faible", HistoryTheme.LowText)
                }
            }
        }
    }
}

// ============================================================
// RISK CHIP
// ============================================================

@Composable
fun RiskChip(count: Int, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                "$count $label",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================================
// DATE FORMATTER
// ============================================================

fun String.toSimpleDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this) ?: return this

        SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.FRENCH).format(date)
    } catch (_: Exception) {
        this
    }
}
