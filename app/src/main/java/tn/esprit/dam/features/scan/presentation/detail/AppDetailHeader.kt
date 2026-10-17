package tn.esprit.dam.features.scan.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.presentation.home.RiskPill

enum class DetailTab(@StringRes val label: Int) {
    Summary(R.string.detail_tab_summary),
    Permissions(R.string.detail_tab_permissions),
    Trackers(R.string.detail_tab_trackers),
    History(R.string.detail_tab_history)
}

/** Same thresholds as the rest of the app: higher score = safer app. */
fun scoreColor(score: Int): Color = when {
    score < 25 -> AppColors.riskCritical
    score < 40 -> AppColors.riskHigh
    score < 70 -> AppColors.riskMedium
    else -> AppColors.riskLow
}

/** App icon, name, publisher line, risk pill, and the score ring on the right. */
@Composable
fun DetailAppHeader(app: AppDetailUi, score: Int) {
    val name = app.displayName ?: app.packageName
    val meta = listOfNotNull(
        app.storeData?.developer,
        app.storeData?.version?.let { "v$it" },
        app.category
    ).joinToString(" · ")

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        DetailAppIcon(name = name, iconUrl = app.storeData?.icon)
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                text = name,
                style = AppTypography.titleLarge,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = AppTypography.labelMedium,
                    color = AppColors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            RiskPill(score = score.toFloat())
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        DetailScoreRing(score = score)
    }
}

@Composable
private fun DetailAppIcon(name: String, iconUrl: String?) {
    val shape = RoundedCornerShape(AppCorners.large)
    val letter: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = name.take(1).uppercase(), style = AppTypography.titleLarge, color = AppColors.textPrimary)
        }
    }
    Box(modifier = Modifier.size(56.dp).clip(shape)) {
        if (iconUrl.isNullOrBlank()) {
            letter()
        } else {
            SubcomposeAsyncImage(
                model = iconUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { letter() },
                error = { letter() }
            )
        }
    }
}

@Composable
private fun DetailScoreRing(score: Int) {
    val progress by animateFloatAsState(
        targetValue = score.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 300),
        label = "detailScore"
    )
    val color = scoreColor(score)
    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            drawArc(AppColors.surfaceVariant, 0f, 360f, useCenter = false, style = stroke)
            drawArc(color, -90f, 360f * progress, useCenter = false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = AppTypography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = AppColors.textPrimary
            )
            Text(
                text = stringResource(R.string.home_score_label),
                style = AppTypography.labelMedium.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                color = AppColors.textTertiary
            )
        }
    }
}

/** Three key numbers under the header. */
@Composable
fun DetailStatsRow(permissions: Int, sensitive: Int, trackers: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        StatTile(permissions, R.string.detail_stat_permissions, AppColors.textPrimary, Modifier.weight(1f))
        StatTile(sensitive, R.string.detail_stat_sensitive, if (sensitive > 0) AppColors.riskHigh else AppColors.textPrimary, Modifier.weight(1f))
        StatTile(trackers, R.string.detail_stat_trackers, AppColors.textPrimary, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(value: Int, @StringRes label: Int, valueColor: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .padding(AppSpacing.md)
    ) {
        Text(text = value.toString(), style = AppTypography.titleLarge, color = valueColor)
        Text(text = stringResource(label), style = AppTypography.labelMedium, color = AppColors.textTertiary)
    }
}

/** Equal-width text tabs with a cyan underline on the selected one. */
@Composable
fun DetailTabs(selected: DetailTab, onSelect: (DetailTab) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailTab.entries.forEach { tab ->
                val isSelected = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(tab.label),
                        style = AppTypography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) AppColors.primary else AppColors.textTertiary,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = AppSpacing.md)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) AppColors.primary else Color.Transparent)
                    )
                }
            }
        }
        HorizontalDivider(color = AppColors.surfaceVariant)
    }
}
