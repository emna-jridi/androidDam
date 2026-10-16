package tn.esprit.dam.features.scan.presentation.history

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
import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryItemDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryList(
    scans: List<ScanHistoryItemDto>,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onScanClick: (ScanHistoryItemDto) -> Unit
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
    val riskLevel = scan.globalRisk ?: deriveRiskLevel(scan.averageScore)
    val riskColor = riskLevelColor(riskLevel)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(scan) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
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
                            color = Color(0xFF94A3B8)
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
                            text = "${scan.overallScore ?: scan.averageScore ?: 0}",
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
                        label = "Apps"
                    )
                    scan.duration?.let {
                        MiniStatItem(
                            icon = Icons.Default.Timer,
                            value = formatDuration(it),
                            label = "Duration"
                        )
                    }
                    MiniStatItem(
                        icon = Icons.Default.Warning,
                        value = "${scan.highRiskApps ?: 0}",
                        label = "High Risk",
                        color = Color(0xFFEF4444)
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
    color: Color = Color(0xFF6366F1)
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailSheet(scan: ScanHistoryItemDto, onDismiss: () -> Unit) {
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
                    icon = Icons.Default.Apps,
                    modifier = Modifier.weight(1f)
                )
                DetailStatCard(
                    title = "Score",
                    value = scan.averageScore?.let { "$it/100" } ?: "N/A",
                    icon = Icons.Default.Score,
                    color = riskScoreColor(scan.averageScore ?: 0),
                    modifier = Modifier.weight(1f)
                )
            }

            scan.duration?.let {
                DetailStatCard(
                    title = "Duration",
                    value = formatDuration(it),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(
                "Risk distribution",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HistoryRiskBadge("High", scan.highRiskApps ?: 0, Color(0xFFEF4444))
                HistoryRiskBadge("Medium", scan.mediumRiskApps ?: 0, Color(0xFFFBBF24))
                HistoryRiskBadge("Low", scan.lowRiskApps ?: 0, Color(0xFF10B981))
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Scan type",
                color = Color(0xFFCBD5E1),
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
    color: Color = Color(0xFF6366F1),
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(title, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
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
        "low" -> Color(0xFF10B981)
        "medium" -> Color(0xFFFBBF24)
        "high" -> Color(0xFFFF6B6B)
        "critical" -> Color(0xFFEF4444)
        else -> Color(0xFFCBD5E1)
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
        score < 40 -> Color(0xFF10B981)
        score < 70 -> Color(0xFFFBBF24)
        score < 85 -> Color(0xFFFF6B6B)
        else -> Color(0xFFEF4444)
    }
}
