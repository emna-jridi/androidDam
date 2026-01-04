package tn.esprit.dam.features.alert

import android.text.format.DateUtils
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tn.esprit.dam.data.model.Alert
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography
import tn.esprit.dam.ui.components.AppLoadingState
import tn.esprit.dam.ui.components.AppEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 🌙 Cyber-Security Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.md)
        ) {
            // 📋 Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.lg, bottom = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(AppColors.textPrimary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                }

                Text(
                    text = "Security Logs",
                    color = AppColors.textPrimary,
                    style = AppTypography.titleMedium
                )

                // Refresh Button
                IconButton(
                    onClick = { viewModel.loadAlerts() },
                    modifier = Modifier.background(AppColors.textPrimary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AppColors.primary)
                }
            }

            // 📝 Content Area
            if (isLoading) {
                AppLoadingState(message = "Chargement des logs...")
            } else if (alerts.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.Security,
                    title = "No threats detected",
                    message = "All your applications are secure"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    contentPadding = PaddingValues(bottom = AppSpacing.lg)
                ) {
                    items(alerts) { alert ->
                        AlertCard(alert)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: Alert) {
    val (icon, color) = getEventProperties(alert.event)
    val severityColor = getSeverityColor(alert.severity)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Icon Circle (Dynamic based on event)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cleanPackageName(alert.packageName),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = alert.event,
                    color = Color(0xFF94A3B8), // Slate-400
                    fontSize = 14.sp
                )
            }

            // 3. Severity & Time
            Column(horizontalAlignment = Alignment.End) {
                // Severity Badge
                Surface(
                    color = severityColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = alert.severity.uppercase(),
                        color = severityColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Time Ago (e.g. "5 mins ago")
                Text(
                    text = parseTimestamp(alert.timestamp),
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = Color(0xFF334155),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No threats detected", color = Color.Gray, fontSize = 18.sp)
    }
}

// --- 🛡️ Helper Functions ---

fun cleanPackageName(packageName: String): String {
    // Converts "com.instagram.android" -> "Instagram"
    return packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
}

fun getSeverityColor(severity: String): Color {
    return when (severity.lowercase()) {
        "critical" -> Color(0xFFEF4444) // Red
        "high" -> Color(0xFFF97316)     // Orange
        "medium" -> Color(0xFFF59E0B)   // Amber
        else -> Color(0xFF10B981)       // Emerald
    }
}

fun getEventProperties(event: String): Pair<ImageVector, Color> {
    val evt = event.lowercase()
    return when {
        evt.contains("camera") -> Pair(Icons.Default.CameraAlt, Color(0xFFEF4444))
        evt.contains("mic") -> Pair(Icons.Default.Mic, Color(0xFFF59E0B))
        evt.contains("location") -> Pair(Icons.Default.LocationOn, Color(0xFF3B82F6))
        else -> Pair(Icons.Default.Warning, Color(0xFF94A3B8))
    }
}

fun parseTimestamp(timestamp: String): String {
    return try {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = format.parse(timestamp)
        val millis = date?.time ?: System.currentTimeMillis()
        android.text.format.DateUtils.getRelativeTimeSpanString(millis).toString()
    } catch (e: Exception) {
        "Just now"
    }
}
