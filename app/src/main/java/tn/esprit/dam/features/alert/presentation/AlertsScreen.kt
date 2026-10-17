package tn.esprit.dam.features.alert.presentation

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.core.ui.util.openAppSettings
import tn.esprit.dam.features.alert.data.model.Alert

/** What the app touched. Drives the icon, the explanation and the type filter. */
private enum class AlertType(@StringRes val filterLabel: Int, val icon: ImageVector, val color: Color) {
    Camera(R.string.alerts_type_camera, Icons.Default.CameraAlt, AppColors.riskCritical),
    Mic(R.string.alerts_type_mic, Icons.Default.Mic, AppColors.riskHigh),
    Location(R.string.alerts_type_location, Icons.Default.LocationOn, AppColors.riskMedium),
    Other(R.string.alerts_type_other, Icons.Default.NotificationsActive, AppColors.textTertiary)
}

private fun Alert.type(): AlertType {
    val evt = event.lowercase()
    return when {
        "camera" in evt -> AlertType.Camera
        "mic" in evt || "audio" in evt -> AlertType.Mic
        "location" in evt -> AlertType.Location
        else -> AlertType.Other
    }
}

@StringRes
private fun Alert.explanation(): Int? {
    val evt = event.lowercase()
    return when {
        "camera" in evt -> R.string.alerts_event_camera
        "mic" in evt || "audio" in evt -> R.string.alerts_event_mic
        "location" in evt -> R.string.alerts_event_location
        "install" in evt -> R.string.alerts_event_install
        "notification" in evt -> R.string.alerts_event_notification
        "network" in evt || "internet" in evt -> R.string.alerts_event_network
        else -> null
    }
}

private fun severityColor(severity: String): Color = when (severity.lowercase()) {
    "critical" -> AppColors.riskCritical
    "high" -> AppColors.riskHigh
    "medium" -> AppColors.riskMedium
    else -> AppColors.info
}

@StringRes
private fun severityLabel(severity: String): Int = when (severity.lowercase()) {
    "critical" -> R.string.critical
    "high" -> R.string.high
    "medium" -> R.string.medium
    else -> R.string.info
}

private sealed interface DayKey {
    data object Today : DayKey
    data object Yesterday : DayKey
    data class OnDate(val text: String) : DayKey
}

@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var typeFilter by rememberSaveable { mutableStateOf<AlertType?>(null) }
    var displayedCount by rememberSaveable { mutableIntStateOf(PAGE) }

    val unread = alerts.count { !it.read }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AppColors.textPrimary)
            }
            Text(
                text = stringResource(R.string.security_logs),
                style = AppTypography.titleLarge,
                color = AppColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (unread > 0) {
                IconButton(onClick = { viewModel.markAllAsRead() }) {
                    Icon(Icons.Default.DoneAll, contentDescription = stringResource(R.string.mark_all_as_read), tint = AppColors.primary)
                }
            }
            IconButton(onClick = { viewModel.loadAlerts() }) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh), tint = AppColors.textSecondary)
            }
        }

        val currentError = error
        when {
            isLoading && alerts.isEmpty() -> AppSkeletonList(itemCount = 5, modifier = Modifier.padding(AppSpacing.md))
            // A failed load must never look like "no threats".
            currentError != null && alerts.isEmpty() -> AppErrorState(
                message = currentError.asString(),
                onRetry = { viewModel.loadAlerts() },
                retryText = stringResource(R.string.retry)
            )
            alerts.isEmpty() -> AppEmptyState(
                icon = Icons.Default.Security,
                title = stringResource(R.string.no_threats_detected),
                message = stringResource(R.string.all_your_applications_are_secure)
            )
            else -> {
                val filtered = remember(alerts, typeFilter) {
                    alerts
                        .filter { typeFilter == null || it.type() == typeFilter }
                        .sortedByDescending { parseAnyTimestamp(it.timestamp)?.time ?: 0L }
                }
                val shown = filtered.take(displayedCount)
                val groups = remember(shown) { shown.groupBy { dayKey(it.timestamp) } }

                LazyColumn(
                    contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.md, bottom = AppSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    item { AlertSummaryCard(alerts = alerts, unread = unread) }
                    item {
                        TypeFilterRow(
                            alerts = alerts,
                            selected = typeFilter,
                            onSelect = {
                                typeFilter = it
                                displayedCount = PAGE
                            }
                        )
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.alerts_empty_filter),
                                style = AppTypography.bodyMedium,
                                color = AppColors.textTertiary,
                                modifier = Modifier.padding(vertical = AppSpacing.md)
                            )
                        }
                    }
                    groups.forEach { (day, dayAlerts) ->
                        item(key = "day-$day") {
                            Text(
                                text = when (day) {
                                    DayKey.Today -> stringResource(R.string.alerts_today)
                                    DayKey.Yesterday -> stringResource(R.string.alerts_yesterday)
                                    is DayKey.OnDate -> day.text
                                },
                                style = AppTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = AppColors.textTertiary,
                                modifier = Modifier.padding(top = AppSpacing.md, bottom = AppSpacing.xs)
                            )
                        }
                        items(dayAlerts, key = { it.id }) { alert ->
                            AlertCard(alert = alert, onMarkAsRead = { viewModel.markAsRead(alert.id) })
                        }
                    }
                    if (filtered.size > displayedCount) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                TextButton(onClick = { displayedCount += PAGE }) {
                                    Text(
                                        text = stringResource(R.string.alerts_load_more, minOf(PAGE, filtered.size - displayedCount)),
                                        color = AppColors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Unread count as the headline, and what was accessed today by type. */
@Composable
private fun AlertSummaryCard(alerts: List<Alert>, unread: Int) {
    val today = remember(alerts) { alerts.filter { dayKey(it.timestamp) == DayKey.Today } }
    val sensitiveToday = today.count { it.type() != AlertType.Other }
    val color = if (unread > 0) AppColors.riskHigh else AppColors.riskLow

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.xlarge))
            .background(AppColors.surface)
            .background(Brush.radialGradient(listOf(color.copy(alpha = 0.14f), Color.Transparent), radius = 600f))
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Column {
            Text(
                text = if (unread > 0) pluralStringResource(R.plurals.alerts_unread_n, unread, unread)
                else stringResource(R.string.alerts_all_read),
                style = AppTypography.titleLarge,
                color = AppColors.textPrimary
            )
            Text(
                text = if (sensitiveToday > 0) pluralStringResource(R.plurals.alerts_today_n, sensitiveToday, sensitiveToday)
                else stringResource(R.string.alerts_none_today),
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            listOf(AlertType.Camera, AlertType.Mic, AlertType.Location).forEach { type ->
                TypeCounter(type = type, count = today.count { it.type() == type }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TypeCounter(type: AlertType, count: Int, modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(AppColors.background.copy(alpha = 0.6f))
            .padding(AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(type.icon, contentDescription = stringResource(type.filterLabel), tint = type.color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(text = count.toString(), style = AppTypography.titleMedium, color = AppColors.textPrimary)
    }
}

@Composable
private fun TypeFilterRow(alerts: List<Alert>, selected: AlertType?, onSelect: (AlertType?) -> Unit) {
    val options: List<AlertType?> = listOf(null) + AlertType.entries
    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        items(options) { option ->
            val isSelected = option == selected
            val count = if (option == null) alerts.size else alerts.count { it.type() == option }
            val shape = RoundedCornerShape(50)
            Text(
                text = "${stringResource(option?.filterLabel ?: R.string.alerts_type_all)} $count",
                style = AppTypography.labelMedium,
                color = if (isSelected) AppColors.onPrimary else AppColors.textSecondary,
                modifier = Modifier
                    .clip(shape)
                    .background(if (isSelected) AppColors.primary else AppColors.surface)
                    .border(1.dp, if (isSelected) AppColors.primary else AppColors.surfaceVariant, shape)
                    .clickable(role = Role.Tab) { onSelect(option) }
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
            )
        }
    }
}

@Composable
private fun AlertCard(alert: Alert, onMarkAsRead: () -> Unit) {
    val context = LocalContext.current
    var expanded by rememberSaveable(alert.id) { mutableStateOf(false) }
    val type = alert.type()
    val sevColor = severityColor(alert.severity)
    val (appName, appIcon) = rememberInstalledApp(context, alert.packageName)
    val notInstalled = stringResource(R.string.alerts_not_installed)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .clickable {
                expanded = !expanded
                if (expanded && !alert.read) onMarkAsRead()
            }
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(AppCorners.medium))
                        .background(type.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (appIcon != null) {
                        Image(bitmap = appIcon, contentDescription = null, modifier = Modifier.size(44.dp))
                    } else {
                        Icon(type.icon, contentDescription = null, tint = type.color)
                    }
                }
                // Small type badge on the app icon, so the action is visible at a glance.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AppColors.surface)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(type.color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(type.icon, contentDescription = null, tint = AppColors.onPrimary, modifier = Modifier.size(12.dp))
                }
            }
            Spacer(modifier = Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appName,
                        style = AppTypography.bodyMedium.copy(fontWeight = if (alert.read) FontWeight.Normal else FontWeight.Bold),
                        color = AppColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!alert.read) {
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Box(modifier = Modifier.size(8.dp).background(AppColors.primary, CircleShape))
                    }
                }
                Text(
                    text = alert.explanation()?.let { stringResource(it) } ?: alert.event,
                    style = AppTypography.labelMedium,
                    color = AppColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = relativeTime(alert.timestamp),
                    style = AppTypography.labelMedium,
                    color = AppColors.textTertiary
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(
                text = stringResource(severityLabel(alert.severity)),
                style = AppTypography.labelMedium,
                color = sevColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(sevColor.copy(alpha = 0.14f))
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
            )
        }

        if (expanded) {
            HorizontalDivider(color = AppColors.surfaceVariant, modifier = Modifier.padding(horizontal = AppSpacing.md))
            Column(
                modifier = Modifier.padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                DetailRow(stringResource(R.string.alerts_detail_time), formatDetailedTimestamp(alert.timestamp))
                DetailRow(stringResource(R.string.alerts_detail_package), alert.packageName)
                DetailRow(
                    stringResource(R.string.alerts_detail_notified),
                    stringResource(if (alert.notified) R.string.alerts_yes else R.string.alerts_no)
                )
                TextButton(onClick = { openAppSettings(context, alert.packageName, notInstalled) }) {
                    Text(stringResource(R.string.alerts_open_settings), style = AppTypography.labelLarge, color = AppColors.primary)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = AppTypography.labelMedium, color = AppColors.textTertiary, modifier = Modifier.weight(0.4f))
        Text(text = value, style = AppTypography.labelMedium, color = AppColors.textPrimary, modifier = Modifier.weight(0.6f))
    }
}

/** Real app name and icon from the device; falls back to the last part of the package name. */
@Composable
private fun rememberInstalledApp(context: Context, packageName: String): Pair<String, ImageBitmap?> =
    remember(packageName) {
        val pm = context.packageManager
        runCatching {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString() to pm.getApplicationIcon(info).toBitmap(96, 96).asImageBitmap()
        }.getOrElse {
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() } to null
        }
    }

// --- Dates (SimpleDateFormat/Calendar: minSdk 24 without desugaring) ---

private const val PAGE = 20

private fun dayKey(timestamp: String): DayKey {
    val date = parseAnyTimestamp(timestamp) ?: return DayKey.OnDate(timestamp.substringBefore('T'))
    val cal = Calendar.getInstance().apply { time = date }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    return when {
        cal.sameDay(today) -> DayKey.Today
        cal.sameDay(yesterday) -> DayKey.Yesterday
        else -> DayKey.OnDate(SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(date).replaceFirstChar { it.uppercase() })
    }
}

private fun Calendar.sameDay(other: Calendar) =
    get(Calendar.YEAR) == other.get(Calendar.YEAR) && get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

/** "5 minutes ago", "2 hours ago"…, in the device language. */
private fun relativeTime(timestamp: String): String {
    val date = parseAnyTimestamp(timestamp) ?: return timestamp
    return DateUtils.getRelativeTimeSpanString(date.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
}

private fun formatDetailedTimestamp(timestamp: String): String =
    parseAnyTimestamp(timestamp)?.let { SimpleDateFormat("d MMM yyyy, HH:mm:ss", Locale.getDefault()).format(it) } ?: timestamp

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
            f.parse(s)?.let { return it }
        } catch (_: Exception) {
            // Not this format; try the next pattern.
        }
    }
    return s.toLongOrNull()?.let { num -> Date(if (s.length == 10) num * 1000 else num) }
}
