package tn.esprit.dam.features.alert

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import tn.esprit.dam.ui.components.AppSkeletonList
import tn.esprit.dam.utils.animateListItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Infinite scroll state
    var displayedCount by remember { mutableStateOf(10) }
    val displayedAlerts = alerts.take(displayedCount)
    val groupedAlerts = displayedAlerts.groupBy { getAlertDateGroup(it.timestamp) }.toSortedMap(compareBy { it })
    
    // Severity summary
    val criticalCount = alerts.count { it.severity.lowercase() == "critical" }
    val highCount = alerts.count { it.severity.lowercase() == "high" }
    val mediumCount = alerts.count { it.severity.lowercase() == "medium" }
    val infoCount = alerts.count { it.severity.lowercase() == "info" || it.severity.isBlank() }
    val unreadCount = alerts.count { !it.read }

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Security Logs",
                        color = AppColors.textPrimary,
                        style = AppTypography.titleMedium
                    )
                    // Real-time badge for unread alerts
                    if (unreadCount > 0) {
                        Surface(
                            color = AppColors.riskCritical,
                            shape = CircleShape,
                            modifier = Modifier
                                .padding(start = AppSpacing.sm)
                                .size(20.dp)
                        ) {
                            Text(
                                text = unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.Center)
                            )
                        }
                    }
                }

                // Refresh Button + Mark All Read
                Row {
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = { viewModel.markAllAsRead() },
                            modifier = Modifier.background(AppColors.primary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read", tint = AppColors.primary)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.loadAlerts() },
                        modifier = Modifier.background(AppColors.textPrimary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AppColors.primary)
                    }
                }
            }

            // 📝 Content Area
            if (isLoading) {
                AppSkeletonList(itemCount = 5)
            } else if (alerts.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.Security,
                    title = "No threats detected",
                    message = "All your applications are secure"
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Severity Summary Badge
                    SeveritySummaryBadge(
                        critical = criticalCount,
                        high = highCount,
                        medium = mediumCount,
                        info = infoCount
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    // Recommendations Card
                    if (criticalCount > 0 || highCount > 0) {
                        AlertRecommendationsCard(criticalCount = criticalCount, highCount = highCount)
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                    }
                    
                    // Infinite scroll list grouped by date
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        contentPadding = PaddingValues(bottom = AppSpacing.lg)
                    ) {
                        groupedAlerts.forEach { (dateGroup, groupAlerts) ->
                            stickyHeader {
                                DateGroupHeader(dateGroup = dateGroup)
                            }
                            items(groupAlerts) { alert ->
                                ExpandableAlertCard(
                                    alert = alert,
                                    onMarkAsRead = { viewModel.markAsRead(alert.id) }
                                )
                            }
                        }
                        
                        // Load more indicator
                        if (displayedCount < alerts.size) {
                            item {
                                Button(
                                    onClick = { displayedCount += 10 },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = AppSpacing.md),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.primary.copy(alpha = 0.8f)
                                    )
                                ) {
                                    Text("Load More (${alerts.size - displayedCount} remaining)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Expandable Alert Card - Shows summary and expands for details
 */
@Composable
private fun ExpandableAlertCard(alert: Alert, onMarkAsRead: () -> Unit = {}) {
    var isExpanded by remember { mutableStateOf(false) }
    val (icon, color) = getEventProperties(alert.event)
    val severityColor = getSeverityColor(alert.severity)

    Card(
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                isExpanded = !isExpanded
                if (isExpanded && !alert.read) {
                    onMarkAsRead()
                }
            }
    ) {
        Column {
            // Summary Row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Icon Circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color)
                }

                Spacer(modifier = Modifier.width(AppSpacing.md))

                // 2. Text Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cleanPackageName(alert.packageName),
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        // Unread indicator
                        if (!alert.read) {
                            Surface(
                                color = AppColors.primary,
                                shape = CircleShape,
                                modifier = Modifier
                                    .padding(start = AppSpacing.sm)
                                    .size(8.dp)
                            ) { }
                        }
                    }
                    Text(
                        text = getEventExplanation(alert.event),
                        color = AppColors.textSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "From: ${cleanPackageName(alert.packageName)}",
                        color = AppColors.textTertiary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Text(
                        text = formatSummaryDate(alert.timestamp),
                        color = AppColors.textTertiary,
                        fontSize = 12.sp
                    )
                }

                // 3. Severity & Time & Expand Icon
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(80.dp)) {
                    // Severity Badge
                    Surface(
                        color = severityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(AppCorners.small)
                    ) {
                        Text(
                            text = alert.severity.uppercase(),
                            color = severityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    // Expand Icon
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = AppColors.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Detailed Content (shown when expanded)
            if (isExpanded) {
                HorizontalDivider(color = AppColors.surfaceVariant, modifier = Modifier.padding(horizontal = AppSpacing.md))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    // Date with detailed format
                    DetailRow("Date", formatDetailedTimestamp(alert.timestamp))
                    
                    // Package Name
                    DetailRow("Package", alert.packageName)
                    
                    // Data (explanation)
                    DetailRow("Data", getEventExplanation(alert.event))
                    
                    // Severity
                    DetailRow("Severity", alert.severity.uppercase(), severityColor)
                    
                    // Notified Status
                    DetailRow("Notified", if (alert.notified) "Yes ✓" else "No")
                }
            }
        }
    }
}

/**
 * Detail Row Helper
 */
@Composable
private fun DetailRow(label: String, value: String, accentColor: Color = AppColors.primary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AppTypography.labelMedium,
            color = AppColors.textSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = AppTypography.bodyMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.6f)
        )
    }
}

/**
 * Alert Card Content - Extracted for reuse with animated wrappers
 */
@Composable
fun AlertCardContent(alert: Alert) {
    val (icon, color) = getEventProperties(alert.event)
    val severityColor = getSeverityColor(alert.severity)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.md),
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

        Spacer(modifier = Modifier.width(AppSpacing.md))

        // 2. Text Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cleanPackageName(alert.packageName),
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = alert.event,
                color = AppColors.textSecondary,
                fontSize = 14.sp
            )
        }

        // 3. Severity & Time
        Column(horizontalAlignment = Alignment.End) {
            // Severity Badge
            Surface(
                color = severityColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(AppCorners.small)
            ) {
                Text(
                    text = alert.severity.uppercase(),
                    color = severityColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Time Ago (e.g. "5 mins ago")
            Text(
                text = parseTimestamp(alert.timestamp),
                color = AppColors.textTertiary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AlertCard(alert: Alert) {
    val (icon, color) = getEventProperties(alert.event)
    val severityColor = getSeverityColor(alert.severity)

    Card(
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AlertCardContent(alert)
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
            tint = AppColors.surfaceVariant,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text("No threats detected", color = AppColors.textSecondary, fontSize = 18.sp)
    }
}

// --- 🛡️ Helper Functions ---

fun getAlertDateGroup(timestamp: String): String {
    val date = parseAnyTimestamp(timestamp) ?: return "Unknown"
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val alertDate = Calendar.getInstance().apply { time = date }
    
    return when {
        isSameDay(alertDate, today) -> "Today"
        isSameDay(alertDate, yesterday) -> "Yesterday"
        alertDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) && 
        alertDate.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR) -> "This Week"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
private fun DateGroupHeader(dateGroup: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.background)
            .padding(vertical = AppSpacing.md, horizontal = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateGroup,
            color = AppColors.textSecondary,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier
            .height(1.dp)
            .weight(1f)
            .background(AppColors.surfaceVariant)
            .padding(horizontal = AppSpacing.md)
        )
    }
}

@Composable
private fun SeveritySummaryBadge(critical: Int, high: Int, medium: Int, info: Int) {
    Card(
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface.copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Critical
            SeverityBadgeItem(
                label = "Critical",
                count = critical,
                color = AppColors.riskCritical,
                modifier = Modifier.weight(1f)
            )
            
            // High
            SeverityBadgeItem(
                label = "High",
                count = high,
                color = AppColors.riskHigh,
                modifier = Modifier.weight(1f)
            )
            
            // Medium
            SeverityBadgeItem(
                label = "Medium",
                count = medium,
                color = AppColors.riskMedium,
                modifier = Modifier.weight(1f)
            )
            
            // Info
            SeverityBadgeItem(
                label = "Info",
                count = info,
                color = AppColors.info,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SeverityBadgeItem(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            color = color,
            style = AppTypography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = AppColors.textSecondary,
            style = AppTypography.labelMedium
        )
    }
}

@Composable
private fun AlertRecommendationsCard(criticalCount: Int, highCount: Int) {
    Card(
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.riskCritical.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.riskCritical.copy(alpha = 0.3f), RoundedCornerShape(AppCorners.large))
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppColors.riskCritical,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text(
                    text = "Security Recommendations",
                    color = AppColors.textPrimary,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (criticalCount > 0) {
                Text(
                    text = "⚠️ You have $criticalCount critical alert(s). Review apps requesting sensitive permissions immediately.",
                    color = AppColors.textSecondary,
                    style = AppTypography.bodyMedium,
                    lineHeight = 16.sp
                )
            }
            
            if (highCount > 0) {
                Text(
                    text = "🔔 $highCount high-risk event(s) detected. Consider restricting permissions for suspicious apps.",
                    color = AppColors.textSecondary,
                    style = AppTypography.bodyMedium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

fun cleanPackageName(packageName: String): String {
    // Converts "com.instagram.android" -> "Instagram"
    return packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
}

fun getSeverityColor(severity: String): Color {
    return when (severity.lowercase()) {
        "critical" -> AppColors.riskCritical
        "high" -> AppColors.riskHigh
        "medium" -> AppColors.riskMedium
        else -> AppColors.riskLow
    }
}

fun getEventProperties(event: String): Pair<ImageVector, Color> {
    val evt = event.lowercase()
    return when {
        evt.contains("camera") -> Pair(Icons.Default.CameraAlt, AppColors.error)
        evt.contains("mic") -> Pair(Icons.Default.Mic, AppColors.warning)
        evt.contains("location") -> Pair(Icons.Default.LocationOn, AppColors.info)
        else -> Pair(Icons.Default.Warning, AppColors.textSecondary)
    }
}

fun getEventExplanation(event: String): String {
    val evt = event.lowercase()
    return when {
        evt.contains("camera") -> "The app accessed the camera"
        evt.contains("mic") || evt.contains("microphone") -> "The app used the microphone"
        evt.contains("location") -> "The app requested your location"
        evt.contains("notification") -> "The app sent a notification"
        evt.contains("network") || evt.contains("internet") -> "The app performed a network operation"
        else -> event.replaceFirstChar { it.uppercase() }
    }
}

fun parseAnyTimestamp(timestamp: String): Date? {
    val s = timestamp.trim()
    // Try ISO8601 variants
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX"
    )
    for (p in patterns) {
        try {
            val f = SimpleDateFormat(p, Locale.US)
            if (p.endsWith("'Z'")) f.timeZone = TimeZone.getTimeZone("UTC")
            val d = f.parse(s)
            if (d != null) return d
        } catch (_: Exception) { }
    }
    // Try epoch seconds/millis
    s.toLongOrNull()?.let { num ->
        val millis = when (s.length) {
            10 -> num * 1000 // seconds
            13 -> num // millis
            else -> num // assume millis
        }
        return Date(millis)
    }
    return null
}

fun parseTimestamp(timestamp: String): String {
    val date = parseAnyTimestamp(timestamp)
    val millis = date?.time ?: return "Just now"
    return DateUtils.getRelativeTimeSpanString(millis).toString()
}

fun formatSummaryDate(timestamp: String): String {
    val date = parseAnyTimestamp(timestamp) ?: return "Just now"
    val outputFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    return outputFormat.format(date)
}

fun formatDetailedTimestamp(timestamp: String): String {
    val date = parseAnyTimestamp(timestamp) ?: return "Just now"
    val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return outputFormat.format(date)
}
