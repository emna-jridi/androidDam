package tn.esprit.dam.features.scan.presentation.history
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Score
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

@Composable
fun HistoryList(
    scans: List<ScanHistoryItemUi>,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onScanClick: (ScanHistoryItemUi) -> Unit
) {
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
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
                                color = AppColors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            TextButton(onClick = onLoadMore) {
                                Text(stringResource(R.string.charger_plus), color = AppColors.primary)
                            }
                        }
                    }
                }
            }
        }
    }

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
fun ScanHistoryCard(scan: ScanHistoryItemUi, onClick: (ScanHistoryItemUi) -> Unit) {
    val scanType = if (scan.totalApps <= 1) "APK" else "Applications"
    val riskLevel = scan.globalRisk ?: deriveRiskLevel(scan.averageScore?.roundToInt())
    val riskColor = riskLevelColor(riskLevel)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(scan) },
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, riskColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = riskColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = scan.appName?.firstOrNull()?.uppercaseChar()?.toString() ?: "A",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = riskColor
                            )
                        }
                    }
                    Column {
                        Text(
                            text = scan.appName ?: scan.packageName ?: "Unknown App",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = formatDate(scan.createdAt),
                            fontSize = 12.sp,
                            color = AppColors.textTertiary
                        )
                    }
                }

                Surface(
                    color = riskColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(scan.overallScore ?: scan.averageScore)?.roundToInt() ?: 0}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                        Text(
                            text = riskLevel.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = scan.status)
                TypeChip(label = scanType)
            }

            if (scan.status.lowercase() == "completed") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniStatItem(
                        icon = Icons.Default.Apps,
                        value = "${scan.scannedApps}/${scan.totalApps}",
                        label = stringResource(R.string.apps)
                    )
                    scan.duration?.let {
                        MiniStatItem(
                            icon = Icons.Default.Timer,
                            value = formatDuration(it),
                            label = stringResource(R.string.duration)
                        )
                    }
                    MiniStatItem(
                        icon = Icons.Default.Warning,
                        value = "${scan.highRiskApps ?: 0}",
                        label = stringResource(R.string.high_risk),
                        color = AppColors.error
                    )
                }
            }
        }
    }
}

@Composable
fun MiniStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color = AppColors.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = 10.sp, color = AppColors.textTertiary)
    }
}

@Composable
fun StatusChip(status: String) {
    val (text, color) = when (status) {
        "completed" -> "Terminé" to AppColors.success
        "analyzing" -> "En cours" to AppColors.warning
        "failed" -> "Échoué" to AppColors.error
        else -> "En attente" to AppColors.textTertiary
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
        color = AppColors.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.textSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailSheet(scan: ScanHistoryItemUi, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.background
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
                        stringResource(R.string.details_du_scan),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formatDate(scan.createdAt),
                        color = AppColors.textTertiary,
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
                    title = stringResource(R.string.apps),
                    value = "${scan.scannedApps}/${scan.totalApps}",
                    icon = Icons.Default.Apps,
                    modifier = Modifier.weight(1f)
                )
                DetailStatCard(
                    title = stringResource(R.string.score),
                    value = scan.averageScore?.let { "${it.roundToInt()}/100" } ?: "N/A",
                    icon = Icons.Default.Score,
                    color = riskScoreColor(scan.averageScore?.roundToInt() ?: 0),
                    modifier = Modifier.weight(1f)
                )
            }

            scan.duration?.let {
                DetailStatCard(
                    title = stringResource(R.string.duration),
                    value = formatDuration(it),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                stringResource(R.string.risk_distribution),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HistoryRiskBadge("High", scan.highRiskApps ?: 0, AppColors.error)
                HistoryRiskBadge("Medium", scan.mediumRiskApps ?: 0, AppColors.warning)
                HistoryRiskBadge("Low", scan.lowRiskApps ?: 0, AppColors.success)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.scan_type),
                color = AppColors.textSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            TypeChip(label = if (scan.totalApps <= 1) "APK" else "Applications")
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DetailStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color = AppColors.primary,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(title, color = AppColors.textTertiary, style = MaterialTheme.typography.bodySmall)
            Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HistoryRiskBadge(label: String, count: Int, color: Color) {
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
                .clip(CircleShape)
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

fun deriveRiskLevel(score: Int?): String {
    return when {
        score == null -> "unknown"
        score < 40 -> "low"
        score < 70 -> "medium"
        score < 85 -> "high"
        else -> "critical"
    }
}

fun riskLevelColor(riskLevel: String): Color {
    return when (riskLevel.lowercase()) {
        "low" -> AppColors.success
        "medium" -> AppColors.warning
        "high" -> AppColors.riskHigh
        "critical" -> AppColors.error
        else -> AppColors.textSecondary
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

fun riskScoreColor(score: Int): Color {
    return when {
        score < 40 -> AppColors.success
        score < 70 -> AppColors.warning
        score < 85 -> AppColors.riskHigh
        else -> AppColors.error
    }
}
