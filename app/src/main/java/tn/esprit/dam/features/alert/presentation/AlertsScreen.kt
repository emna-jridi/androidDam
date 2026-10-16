package tn.esprit.dam.features.alert.presentation
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

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
import tn.esprit.dam.features.alert.data.model.Alert
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.core.ui.components.AppLoadingState
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.util.animateListItem
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

    var displayedCount by remember { mutableStateOf(10) }
    val displayedAlerts = alerts.take(displayedCount)
    val groupedAlerts = displayedAlerts.groupBy { getAlertDateGroup(it.timestamp) }.toSortedMap(compareBy { it })

    val criticalCount = alerts.count { it.severity.lowercase() == "critical" }
    val highCount = alerts.count { it.severity.lowercase() == "high" }
    val mediumCount = alerts.count { it.severity.lowercase() == "medium" }
    val infoCount = alerts.count { it.severity.lowercase() == "info" || it.severity.isBlank() }
    val unreadCount = alerts.count { !it.read }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.lg, bottom = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.background(AppColors.textPrimary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AppColors.textPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.security_logs),
                        color = AppColors.textPrimary,
                        style = AppTypography.titleMedium
                    )
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
                                color = AppColors.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.Center)
                            )
                        }
                    }
                }

                Row {
                    if (unreadCount > 0) {
                        IconButton(
                            onClick = { viewModel.markAllAsRead() },
                            modifier = Modifier.background(AppColors.primary.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = stringResource(R.string.mark_all_as_read), tint = AppColors.primary)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.loadAlerts() },
                        modifier = Modifier.background(AppColors.textPrimary.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh), tint = AppColors.primary)
                    }
                }
            }

            if (isLoading) {
                AppSkeletonList(itemCount = 5)
            } else if (alerts.isEmpty()) {
                AppEmptyState(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.no_threats_detected),
                    message = stringResource(R.string.all_your_applications_are_secure)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    SeveritySummaryBadge(
                        critical = criticalCount,
                        high = highCount,
                        medium = mediumCount,
                        info = infoCount
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    if (criticalCount > 0 || highCount > 0) {
                        AlertRecommendationsCard(criticalCount = criticalCount, highCount = highCount)
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                    }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color)
                }

                Spacer(modifier = Modifier.width(AppSpacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cleanPackageName(alert.packageName),
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
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

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(80.dp)) {
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

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = AppColors.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = AppColors.surfaceVariant, modifier = Modifier.padding(horizontal = AppSpacing.md))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    AlertDetailRow("Date", formatDetailedTimestamp(alert.timestamp))

                    AlertDetailRow("Package", alert.packageName)

                    AlertDetailRow("Data", getEventExplanation(alert.event))

                    AlertDetailRow("Severity", alert.severity.uppercase(), severityColor)

                    AlertDetailRow("Notified", if (alert.notified) "Yes ✓" else "No")
                }
            }
        }
    }
}

@Composable
private fun AlertDetailRow(label: String, value: String, accentColor: Color = AppColors.primary) {
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
            SeverityBadgeItem(
                label = stringResource(R.string.critical),
                count = critical,
                color = AppColors.riskCritical,
                modifier = Modifier.weight(1f)
            )

            SeverityBadgeItem(
                label = stringResource(R.string.high),
                count = high,
                color = AppColors.riskHigh,
                modifier = Modifier.weight(1f)
            )

            SeverityBadgeItem(
                label = stringResource(R.string.medium),
                count = medium,
                color = AppColors.riskMedium,
                modifier = Modifier.weight(1f)
            )

            SeverityBadgeItem(
                label = stringResource(R.string.info),
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
                    text = stringResource(R.string.security_recommendations),
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
        } catch (_: Exception) {
            // Not this format; try the next pattern.
        }
    }
    s.toLongOrNull()?.let { num ->
        val millis = when (s.length) {
            10 -> num * 1000
            13 -> num
            else -> num
        }
        return Date(millis)
    }
    return null
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
