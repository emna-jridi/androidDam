package tn.esprit.dam.screens.AlertScreen

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 🌑 Cyber-Security Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Deep Navy
                        Color(0xFF1E293B)  // Slate
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 🔝 Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "Security Logs",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Refresh Button
                IconButton(
                    onClick = { viewModel.loadAlerts() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF7C3AED))
                }
            }

            // 📋 Content Area
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7C3AED))
                }
            } else if (alerts.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
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
                    text = DateUtils.getRelativeTimeSpanString(alert.timestamp).toString(),
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

// --- 🛠️ Helper Functions ---

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