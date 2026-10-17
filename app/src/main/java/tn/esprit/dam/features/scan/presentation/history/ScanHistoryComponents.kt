package tn.esprit.dam.features.scan.presentation.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.presentation.detail.scoreColor
import tn.esprit.dam.features.scan.presentation.home.RiskCountsRow
import tn.esprit.dam.features.scan.presentation.home.RiskPill

/** Score shown for a scan. Higher = safer, like everywhere else in the app. */
val ScanHistoryItemUi.score: Int? get() = (overallScore ?: averageScore)?.roundToInt()

private val ScanHistoryItemUi.isApk: Boolean get() = totalApps <= 1

/** Scans are only compared with the previous scan of the same kind (all apps, or the same APK). */
private val ScanHistoryItemUi.kindKey: String get() = if (isApk) "apk:${packageName ?: appName}" else "apps"

private data class HistoryRow(val scan: ScanHistoryItemUi, val delta: Int?)

private sealed interface DayLabel {
    data object Today : DayLabel
    data object Yesterday : DayLabel
    data class OnDate(val text: String) : DayLabel
}

private data class DayGroup(val label: DayLabel, val rows: List<HistoryRow>)

@Composable
fun HistoryList(
    scans: List<ScanHistoryItemUi>,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onScanClick: (ScanHistoryItemUi) -> Unit
) {
    val listState = rememberLazyListState()
    val groups = remember(scans) { groupByDay(scans) }
    val trend = remember(scans) {
        scans.filter { !it.isApk && it.score != null }
            .sortedBy { parseIsoDate(it.createdAt)?.time ?: 0L }
            .takeLast(10)
            .mapNotNull { it.score }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (trend.isNotEmpty()) {
            item { TrendCard(scores = trend) }
        }
        groups.forEach { group ->
            item(key = "day-${group.label}") {
                Text(
                    text = when (val label = group.label) {
                        DayLabel.Today -> stringResource(R.string.history_today)
                        DayLabel.Yesterday -> stringResource(R.string.history_yesterday)
                        is DayLabel.OnDate -> label.text
                    },
                    style = AppTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.textTertiary,
                    modifier = Modifier.padding(top = AppSpacing.md, bottom = AppSpacing.xs)
                )
            }
            items(group.rows, key = { it.scan.scanId }) { row ->
                ScanHistoryCard(row.scan, row.delta, onClick = onScanClick)
            }
        }
        if (hasMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = AppColors.primary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    } else {
                        TextButton(onClick = onLoadMore) {
                            Text(stringResource(R.string.charger_plus), color = AppColors.primary)
                        }
                    }
                }
            }
        }
    }

    // Read the latest values each time, not the ones captured on first composition.
    val currentHasMore by rememberUpdatedState(hasMore)
    val currentIsLoading by rememberUpdatedState(isLoading)
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val reachedEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            last >= listState.layoutInfo.totalItemsCount - 1
        }
    }
    LaunchedEffect(reachedEnd) {
        if (reachedEnd && currentHasMore && !currentIsLoading) currentOnLoadMore()
    }
}

@Composable
private fun TrendCard(scores: List<Int>) {
    val last = scores.last()
    val color = scoreColor(last)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.xlarge))
            .background(AppColors.surface)
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.history_trend_title), style = AppTypography.titleMedium, color = AppColors.textPrimary)
                Text(stringResource(R.string.history_last_score), style = AppTypography.labelMedium, color = AppColors.textTertiary)
            }
            Text(
                text = last.toString(),
                style = AppTypography.displayLarge.copy(fontWeight = FontWeight.Black),
                color = color
            )
        }
        if (scores.size >= 2) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                val stepX = size.width / (scores.size - 1)
                fun y(score: Int) = size.height * (1f - score.coerceIn(0, 100) / 100f)
                val points = scores.mapIndexed { i, s -> Offset(i * stepX, y(s)) }
                drawLine(
                    AppColors.surfaceVariant,
                    Offset(0f, y(50)),
                    Offset(size.width, y(50)),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
                )
                val line = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                val area = Path().apply {
                    addPath(line)
                    lineTo(points.last().x, size.height)
                    lineTo(points.first().x, size.height)
                    close()
                }
                drawPath(area, color.copy(alpha = 0.15f))
                drawPath(line, color, style = Stroke(width = 2.dp.toPx()))
                drawCircle(color, radius = 4.dp.toPx(), center = points.last())
            }
        }
    }
}

@Composable
fun ScanHistoryCard(scan: ScanHistoryItemUi, delta: Int?, onClick: (ScanHistoryItemUi) -> Unit) {
    val score = scan.score
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .clickable { onClick(scan) }
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(AppCorners.medium))
                .background(AppColors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (scan.isApk) Icons.Default.Android else Icons.Default.Apps,
                contentDescription = stringResource(if (scan.isApk) R.string.history_type_apk else R.string.history_type_apps),
                tint = AppColors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (scan.isApk) {
                    scan.appName ?: scan.packageName ?: stringResource(R.string.history_apk_scan)
                } else {
                    pluralStringResource(R.plurals.history_apps_scanned, scan.scannedApps, scan.scannedApps)
                },
                style = AppTypography.bodyMedium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val high = scan.highRiskApps ?: 0
            val subtitle = listOfNotNull(
                formatTime(scan.createdAt),
                statusLabel(scan.status),
                if (!scan.isApk) {
                    if (high > 0) pluralStringResource(R.plurals.history_high_risk_n, high, high)
                    else stringResource(R.string.history_no_high_risk)
                } else null
            ).joinToString(" · ")
            Text(text = subtitle, style = AppTypography.labelMedium, color = AppColors.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            if (score != null) RiskPill(score = score.toFloat())
            delta?.let { DeltaText(it) }
        }
    }
}

@Composable
private fun DeltaText(delta: Int) {
    val (text, color) = when {
        delta > 0 -> stringResource(R.string.history_delta_up, delta) to AppColors.riskLow
        delta < 0 -> stringResource(R.string.history_delta_down, -delta) to AppColors.riskHigh
        else -> stringResource(R.string.history_delta_same) to AppColors.textTertiary
    }
    Text(text = text, style = AppTypography.labelMedium.copy(fontWeight = FontWeight.Bold), color = color)
}

@Composable
private fun statusLabel(status: String): String? = when (status.lowercase()) {
    "failed" -> stringResource(R.string.history_status_failed)
    "completed", "partial" -> null
    else -> stringResource(R.string.history_status_running)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailSheet(scan: ScanHistoryItemUi, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppColors.background) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = AppSpacing.md, end = AppSpacing.md, bottom = AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.details_du_scan), style = AppTypography.titleLarge, color = AppColors.textPrimary)
                    Text(formatDate(scan.createdAt), style = AppTypography.labelMedium, color = AppColors.textTertiary)
                }
                scan.score?.let { RiskPill(score = it.toFloat()) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                SheetStat(
                    title = stringResource(R.string.history_sheet_apps),
                    value = stringResource(R.string.history_apps_value, scan.scannedApps, scan.totalApps),
                    modifier = Modifier.weight(1f)
                )
                SheetStat(
                    title = stringResource(R.string.score),
                    value = scan.score?.let { stringResource(R.string.history_score_value, it) } ?: stringResource(R.string.history_no_score),
                    valueColor = scan.score?.let { scoreColor(it) } ?: AppColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                scan.duration?.let {
                    SheetStat(
                        title = stringResource(R.string.history_sheet_duration),
                        value = formatDuration(it),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (!scan.isApk) {
                Text(stringResource(R.string.history_sheet_distribution), style = AppTypography.titleMedium, color = AppColors.textPrimary)
                RiskCountsRow(
                    high = scan.highRiskApps ?: 0,
                    medium = scan.mediumRiskApps ?: 0,
                    safe = scan.lowRiskApps ?: 0
                )
            }
        }
    }
}

@Composable
private fun SheetStat(title: String, value: String, modifier: Modifier, valueColor: Color = AppColors.textPrimary) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .padding(AppSpacing.md)
    ) {
        Text(text = value, style = AppTypography.titleMedium, color = valueColor)
        Text(text = title, style = AppTypography.labelMedium, color = AppColors.textTertiary)
    }
}

// --- Dates (SimpleDateFormat/Calendar: minSdk 24 without desugaring) ---

private fun parseIsoDate(value: String): Date? {
    listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'").forEach { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.parse(value)
        }.getOrNull()?.let { return it }
    }
    return null
}

private fun groupByDay(scans: List<ScanHistoryItemUi>): List<DayGroup> {
    val sorted = scans.sortedByDescending { parseIsoDate(it.createdAt)?.time ?: 0L }
    // Delta = this scan's score minus the previous (older) scan of the same kind.
    val rows = sorted.mapIndexed { index, scan ->
        val previous = sorted.drop(index + 1).firstOrNull { it.kindKey == scan.kindKey && it.score != null }
        val delta = scan.score?.let { s -> previous?.score?.let { s - it } }
        HistoryRow(scan, delta)
    }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val dayFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH)

    return rows.groupBy { row ->
        val date = parseIsoDate(row.scan.createdAt)
        if (date == null) {
            DayLabel.OnDate(row.scan.createdAt.substringBefore('T'))
        } else {
            val cal = Calendar.getInstance().apply { time = date }
            when {
                cal.sameDayAs(today) -> DayLabel.Today
                cal.sameDayAs(yesterday) -> DayLabel.Yesterday
                else -> DayLabel.OnDate(dayFormat.format(date).replaceFirstChar { it.uppercase() })
            }
        }
    }.map { (label, items) -> DayGroup(label, items) }
}

private fun Calendar.sameDayAs(other: Calendar) =
    get(Calendar.YEAR) == other.get(Calendar.YEAR) && get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

private fun formatTime(dateString: String): String? =
    parseIsoDate(dateString)?.let { SimpleDateFormat("HH:mm", Locale.FRENCH).format(it) }

fun formatDate(dateString: String): String =
    parseIsoDate(dateString)?.let { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.FRENCH).format(it) } ?: dateString

@Composable
fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000).toInt()
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> stringResource(R.string.history_duration_h, hours, minutes % 60)
        minutes > 0 -> stringResource(R.string.history_duration_m, minutes, seconds % 60)
        else -> stringResource(R.string.history_duration_s, seconds)
    }
}
