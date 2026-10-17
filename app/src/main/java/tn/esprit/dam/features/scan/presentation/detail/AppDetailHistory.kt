package tn.esprit.dam.features.scan.presentation.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.presentation.home.RiskPill

/** History tab: score curve (oldest to newest) and the list of past scans (newest first). */
@Composable
fun HistoryTab(history: List<AppScanHistoryUi>) {
    if (history.isEmpty()) {
        Text(
            text = stringResource(R.string.detail_history_empty),
            style = AppTypography.bodyMedium,
            color = AppColors.textTertiary
        )
        return
    }
    val chronological = remember(history) { history.sortedBy { it.scanDate } }

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (chronological.size >= 2) ScoreTrendCard(chronological)
        history.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(AppColors.surface)
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatHistoryDate(entry.scanDate),
                    style = AppTypography.bodyMedium,
                    color = AppColors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(text = entry.score.toInt().toString(), style = AppTypography.titleMedium, color = AppColors.textPrimary)
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                RiskPill(score = entry.score)
            }
        }
    }
}

@Composable
private fun ScoreTrendCard(points: List<AppScanHistoryUi>) {
    val delta = (points.last().score - points.first().score).toInt()
    val lineColor = scoreColor(points.last().score.toInt())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.detail_history_title),
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = (if (delta >= 0) "▲ " else "▼ ") + stringResource(R.string.detail_history_delta, delta),
                style = AppTypography.labelMedium,
                color = if (delta >= 0) AppColors.riskLow else AppColors.riskHigh
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val stepX = size.width / (points.size - 1)
            fun y(score: Float) = size.height * (1f - score.coerceIn(0f, 100f) / 100f)
            val offsets = points.mapIndexed { i, p -> Offset(i * stepX, y(p.score)) }

            // Mid line at score 50 as a reference.
            drawLine(
                AppColors.surfaceVariant,
                Offset(0f, y(50f)),
                Offset(size.width, y(50f)),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
            )
            val line = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                offsets.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val area = Path().apply {
                addPath(line)
                lineTo(offsets.last().x, size.height)
                lineTo(offsets.first().x, size.height)
                close()
            }
            drawPath(area, lineColor.copy(alpha = 0.15f))
            drawPath(line, lineColor, style = Stroke(width = 2.dp.toPx()))
            drawCircle(lineColor, radius = 4.dp.toPx(), center = offsets.last())
        }
    }
}

// SimpleDateFormat rather than java.time: minSdk is 24 and desugaring is off.
fun formatHistoryDate(dateString: String): String = runCatching {
    val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    val date = input.parse(dateString.substringBefore('.').removeSuffix("Z"))!!
    SimpleDateFormat("d MMM yyyy", Locale.FRENCH).format(date)
}.getOrElse { dateString.substringBefore('T') }
