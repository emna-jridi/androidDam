package tn.esprit.dam.screens.history

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.model.AppAnalysisResult
import tn.esprit.dam.data.model.ScanItem
import tn.esprit.dam.data.repository.ScanRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// ============================================================
// VIEWMODEL
// ============================================================

data class ScanDetailUiState(
    val isLoading: Boolean = false,
    val scan: ScanItem? = null,  // ✅ CHANGÉ: ScanHistoryItem -> ScanItem
    val error: String? = null
)

@HiltViewModel
class ScanDetailViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ScanDetailViewModel"
    }

    private val _uiState = MutableStateFlow(ScanDetailUiState())
    val uiState: StateFlow<ScanDetailUiState> = _uiState.asStateFlow()

    fun loadScan(token: String, scanId: String) {
        viewModelScope.launch {
            Log.d(TAG, "🔍 Loading scan: $scanId")

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            repository.getScanHistoryById(token, scanId)
                .onSuccess { scan ->
                    Log.d(TAG, "✅ Scan loaded: ${scan._id}")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        scan = scan  // ✅ scan est maintenant de type ScanItem
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Load scan failed", error)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load scan"
                    )
                }
        }
    }
}

// ============================================================
// SCREEN
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailScreen(
    viewModel: ScanDetailViewModel = hiltViewModel(),
    token: String,
    scanId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scanId) {
        viewModel.loadScan(token, scanId)
    }

    Scaffold(
        topBar = {
            ScanDetailTopBar(
                onNavigateBack = onNavigateBack,
                scan = uiState.scan
            )
        },
        containerColor = ScanDetailTheme.DarkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }

                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadScan(token, scanId) }
                    )
                }

                uiState.scan != null -> {
                    ScanDetailContent(scan = uiState.scan!!)
                }
            }
        }
    }
}

// ==================== THEME ====================

object ScanDetailTheme {
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

// ==================== TOP BAR ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailTopBar(
    onNavigateBack: () -> Unit,
    scan: ScanItem?  // ✅ Type correct
) {
    Surface(
        color = ScanDetailTheme.DarkBg,
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ScanDetailTheme.CardBg)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,  // ✅ CORRIGÉ
                            contentDescription = "Retour",
                            tint = ScanDetailTheme.TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Détails du Scan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ScanDetailTheme.TextPrimary
                            )
                        )
                        if (scan != null) {
                            Text(
                                text = scan.createdAt?.toSimpleDate() ?: "Date inconnue",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ScanDetailTheme.TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==================== MAIN CONTENT ====================

@Composable
fun ScanDetailContent(scan: ScanItem) {  // ✅ Type correct
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistiques générales
        item {
            ScanStatsCard(scan)
        }

        // Répartition par niveau de risque
        item {
            RiskDistributionCard(scan)
        }

        // Section titre
        item {
            Text(
                text = "Applications Analysées (${scan.report.results.size})",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = ScanDetailTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Liste des applications
        items(scan.report.results) { result ->
            AppDetailCard(result)
        }

        // Espace en bas
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==================== SCAN STATS CARD ====================

@Composable
fun ScanStatsCard(scan: ScanItem) {  // ✅ Type correct
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanDetailTheme.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ScanDetailTheme.PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Scan Terminé",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = ScanDetailTheme.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${scan.totalApps} applications analysées",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ScanDetailTheme.TextSecondary
                        )
                    )
                }
            }

            HorizontalDivider(color = ScanDetailTheme.BorderColor)  // ✅ CORRIGÉ

            // Informations du scan
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(
                    icon = Icons.Default.DateRange,
                    label = "Date du scan",
                    value = scan.createdAt?.toSimpleDate() ?: "Inconnue"
                )
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Type de scan",
                    value = when (scan.type) {
                        "batch_installed" -> "Applications installées"
                        "single_apk" -> "Analyse APK"
                        else -> scan.type
                    }
                )
                InfoRow(
                    icon = Icons.Default.Info,
                    label = "ID du scan",
                    value = scan._id
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = ScanDetailTheme.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ScanDetailTheme.TextSecondary,
                    fontSize = 12.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ScanDetailTheme.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// ==================== RISK DISTRIBUTION ====================

@Composable
fun RiskDistributionCard(scan: ScanItem) {  // ✅ Type correct
    val criticalCount = scan.report.results.count {
        it.riskLevel.equals("critical", ignoreCase = true)
    }
    val highCount = scan.report.results.count {
        it.riskLevel.equals("high", ignoreCase = true)
    }
    val mediumCount = scan.report.results.count {
        it.riskLevel.equals("medium", ignoreCase = true)
    }
    val lowCount = scan.report.results.count {
        it.riskLevel.equals("low", ignoreCase = true)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanDetailTheme.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Répartition par Niveau de Risque",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ScanDetailTheme.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (criticalCount > 0 || highCount > 0) {
                    RiskChip(
                        count = criticalCount + highCount,
                        label = "Critique",
                        color = ScanDetailTheme.CriticalText,
                        bgColor = ScanDetailTheme.CriticalBg,
                        modifier = Modifier.weight(1f)
                    )
                }
                RiskChip(
                    count = mediumCount,
                    label = "Modéré",
                    color = ScanDetailTheme.MediumText,
                    bgColor = ScanDetailTheme.MediumBg,
                    modifier = Modifier.weight(1f)
                )
                RiskChip(
                    count = lowCount,
                    label = "Faible",
                    color = ScanDetailTheme.LowText,
                    bgColor = ScanDetailTheme.LowBg,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RiskChip(
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
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = color.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            )
        }
    }
}

// ==================== APP DETAIL CARD ====================

@Composable
fun AppDetailCard(result: AppAnalysisResult) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanDetailTheme.CardBg
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ScanDetailTheme.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.packageName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ScanDetailTheme.TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    PrivacyScoreBadge(score = result.score)
                    Spacer(modifier = Modifier.height(6.dp))
                    RiskLevelBadge(riskLevel = result.riskLevel)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Statistiques rapides
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickStat(
                    icon = Icons.Default.Shield,
                    value = "${result.trackers.size}",
                    label = "trackers"
                )
                QuickStat(
                    icon = Icons.Default.Lock,
                    value = "${result.permissions.total}",
                    label = "permissions"
                )
                if (result.alerts.isNotEmpty()) {
                    QuickStat(
                        icon = Icons.Default.Warning,
                        value = "${result.alerts.size}",
                        label = "alertes",
                        color = ScanDetailTheme.CriticalText
                    )
                }
            }

            // Contenu expandable
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ScanDetailTheme.BorderColor)  // ✅ CORRIGÉ
                Spacer(modifier = Modifier.height(16.dp))

                // Alertes
                if (result.alerts.isNotEmpty()) {
                    AlertsSection(alerts = result.alerts)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Trackers
                if (result.trackers.isNotEmpty()) {
                    TrackersSection(trackers = result.trackers)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Permissions dangereuses
                if (result.permissions.dangerous.isNotEmpty()) {
                    DangerousPermissionsSection(permissions = result.permissions.dangerous)
                }
            }

            // Indicateur expand
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Réduire" else "Développer",
                    tint = ScanDetailTheme.TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun QuickStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color = ScanDetailTheme.TextSecondary
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ScanDetailTheme.CardHover
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$value $label",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun AlertsSection(alerts: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "🚨 Alertes",
            style = MaterialTheme.typography.titleSmall.copy(
                color = ScanDetailTheme.CriticalText,
                fontWeight = FontWeight.Bold
            )
        )
        alerts.forEach { alert ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ScanDetailTheme.CriticalBg
            ) {
                Text(
                    text = alert,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanDetailTheme.CriticalText
                    )
                )
            }
        }
    }
}

@Composable
fun TrackersSection(trackers: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "📍 Trackers Détectés (${trackers.size})",
            style = MaterialTheme.typography.titleSmall.copy(
                color = ScanDetailTheme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
        trackers.forEach { tracker ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ScanDetailTheme.CardHover
            ) {
                Text(
                    text = tracker,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanDetailTheme.TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
fun DangerousPermissionsSection(permissions: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "⚠️ Permissions Dangereuses (${permissions.size})",
            style = MaterialTheme.typography.titleSmall.copy(
                color = ScanDetailTheme.HighText,
                fontWeight = FontWeight.Bold
            )
        )
        permissions.forEach { permission ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ScanDetailTheme.HighBg
            ) {
                Text(
                    text = permission.removePrefix("android.permission."),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanDetailTheme.HighText,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                )
            }
        }
    }
}

@Composable
fun PrivacyScoreBadge(score: Int) {
    val (color, bgColor) = when {
        score >= 70 -> ScanDetailTheme.LowText to ScanDetailTheme.LowBg
        score >= 40 -> ScanDetailTheme.MediumText to ScanDetailTheme.MediumBg
        else -> ScanDetailTheme.CriticalText to ScanDetailTheme.CriticalBg
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = "$score/100",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun RiskLevelBadge(riskLevel: String) {
    val (color, bgColor, label) = when (riskLevel.lowercase()) {
        "low" -> Triple(ScanDetailTheme.LowText, ScanDetailTheme.LowBg, "Faible")
        "medium" -> Triple(ScanDetailTheme.MediumText, ScanDetailTheme.MediumBg, "Moyen")
        "high", "critical" -> Triple(ScanDetailTheme.CriticalText, ScanDetailTheme.CriticalBg, "Élevé")
        else -> Triple(ScanDetailTheme.TextSecondary, ScanDetailTheme.CardHover, "Inconnu")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        )
    }
}
