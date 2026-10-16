package tn.esprit.dam.features.scan.presentation.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.components.AppPrimaryButton
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography

/** Overall protection level for the score (higher score = safer device). */
private enum class ProtectionLevel(@StringRes val label: Int, val color: Color) {
    Excellent(R.string.home_state_excellent, AppColors.riskLow),
    Good(R.string.home_state_good, AppColors.riskLow),
    Medium(R.string.home_state_medium, AppColors.riskMedium),
    Low(R.string.home_state_low, AppColors.riskCritical);

    companion object {
        fun of(score: Int) = when {
            score >= 85 -> Excellent
            score >= 70 -> Good
            score >= 40 -> Medium
            else -> Low
        }
    }
}

/** Hero card: score ring, protection level, what needs attention, and the main scan action. */
@Composable
fun HomeShieldHero(
    score: Int,
    appsToCheck: Int,
    lastScanDate: String?,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clamped = score.coerceIn(0, 100)
    val level = ProtectionLevel.of(clamped)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.xlarge))
            .background(AppColors.surface)
            .background(
                Brush.radialGradient(
                    colors = listOf(level.color.copy(alpha = 0.16f), Color.Transparent),
                    radius = 600f
                )
            )
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScoreRing(score = clamped, color = level.color)
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = level.color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(AppSpacing.sm))
            Text(
                text = stringResource(level.label),
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = if (appsToCheck > 0) {
                pluralStringResource(R.plurals.home_apps_to_check, appsToCheck, appsToCheck)
            } else {
                stringResource(R.string.home_no_app_to_check)
            },
            style = AppTypography.bodyMedium,
            color = AppColors.textSecondary,
            textAlign = TextAlign.Center
        )
        lastScanDate?.let {
            Text(
                text = stringResource(R.string.home_last_scan, it),
                style = AppTypography.labelMedium,
                color = AppColors.textTertiary,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        AppPrimaryButton(text = stringResource(R.string.home_run_scan), onClick = onScanClick)
    }
}

@Composable
private fun ScoreRing(score: Int, color: Color) {
    val progress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 300),
        label = "scoreRing"
    )
    Box(modifier = Modifier.size(148.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            drawArc(AppColors.surfaceVariant, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
            drawArc(color, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = AppTypography.displayLarge.copy(fontSize = 44.sp, fontWeight = FontWeight.Black),
                color = AppColors.textPrimary
            )
            Text(
                text = stringResource(R.string.home_score_label),
                style = AppTypography.labelMedium.copy(letterSpacing = 2.sp),
                color = AppColors.textTertiary
            )
        }
    }
}

/** Three tiles: how many apps are high risk, medium, and safe. */
@Composable
fun RiskCountsRow(high: Int, medium: Int, safe: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        RiskCountTile(high, R.string.home_count_high, AppColors.riskHigh, Modifier.weight(1f))
        RiskCountTile(medium, R.string.home_count_medium, AppColors.riskMedium, Modifier.weight(1f))
        RiskCountTile(safe, R.string.home_count_safe, AppColors.riskLow, Modifier.weight(1f))
    }
}

@Composable
private fun RiskCountTile(count: Int, @StringRes label: Int, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Text(
            text = count.toString(),
            style = AppTypography.titleLarge,
            color = AppColors.textPrimary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.size(AppSpacing.xs))
            Text(
                text = stringResource(label),
                style = AppTypography.labelMedium,
                color = AppColors.textSecondary
            )
        }
    }
}

/** Four secondary tools as a row of icon buttons. History lives in the bottom bar. */
@Composable
fun QuickToolsRow(
    onScanApk: () -> Unit,
    onSearch: () -> Unit,
    onAlerts: () -> Unit,
    onVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickTool(Icons.Default.Android, R.string.home_tool_apk, onScanApk)
        QuickTool(Icons.Default.Search, R.string.home_tool_search, onSearch)
        QuickTool(Icons.Default.NotificationsActive, R.string.home_tool_alerts, onAlerts)
        QuickTool(Icons.Default.Lock, R.string.home_tool_vault, onVault)
    }
}

@Composable
private fun QuickTool(icon: ImageVector, @StringRes label: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(AppCorners.large))
            .clickable(onClick = onClick)
            .padding(AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(AppCorners.large))
                .background(AppColors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.primary, modifier = Modifier.size(24.dp))
        }
        Text(
            text = stringResource(label),
            style = AppTypography.labelMedium,
            color = AppColors.textSecondary
        )
    }
}

/** High-risk apps (score below 40), most risky first. */
@Composable
fun WatchlistSection(
    riskyApps: List<RiskyAppUi>,
    onSeeAll: () -> Unit,
    onAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val highRisk = riskyApps.filter { it.score < 40f }.take(3)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_watchlist),
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary
            )
            TextButton(onClick = onSeeAll) {
                Text(stringResource(R.string.home_see_all), style = AppTypography.labelMedium, color = AppColors.primary)
            }
        }

        if (highRisk.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppCorners.large))
                    .background(AppColors.surface)
                    .padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AppColors.riskLow)
                Spacer(modifier = Modifier.size(AppSpacing.sm))
                Text(
                    text = stringResource(R.string.home_watchlist_empty),
                    style = AppTypography.bodyMedium,
                    color = AppColors.textSecondary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppCorners.large))
                    .background(AppColors.surface)
            ) {
                highRisk.forEachIndexed { index, app ->
                    if (index > 0) HorizontalDivider(color = AppColors.surfaceVariant)
                    WatchlistItem(app = app, onClick = { app.packageName?.let(onAppClick) })
                }
            }
        }
    }
}

@Composable
private fun WatchlistItem(app: RiskyAppUi, onClick: () -> Unit) {
    val name = app.appName ?: app.packageName ?: "?"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(AppCorners.medium))
                .background(AppColors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary
            )
        }
        Spacer(modifier = Modifier.size(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = AppTypography.bodyMedium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.home_app_score, app.score.toInt()),
                style = AppTypography.labelMedium,
                color = AppColors.textTertiary
            )
        }
        RiskPill(score = app.score)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun HomeShieldHeroPreview() {
    Column(modifier = Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        HomeShieldHero(score = 72, appsToCheck = 3, lastScanDate = "16 octobre 2026 à 14:20", onScanClick = {})
        RiskCountsRow(high = 3, medium = 8, safe = 41)
        QuickToolsRow(onScanApk = {}, onSearch = {}, onAlerts = {}, onVault = {})
        WatchlistSection(
            riskyApps = listOf(RiskyAppUi("com.tiktok", "TikTok", 22f), RiskyAppUi("com.flash", "Flashlight Pro", 31f)),
            onSeeAll = {},
            onAppClick = {}
        )
    }
}

/** Risk label pill: always a word plus a color dot, never color alone. */
@Composable
fun RiskPill(score: Float) {
    val (label, color) = when {
        score < 25f -> stringResource(R.string.level_critical) to AppColors.riskCritical
        score < 40f -> stringResource(R.string.level_high) to AppColors.riskHigh
        score < 70f -> stringResource(R.string.level_medium) to AppColors.riskMedium
        else -> stringResource(R.string.level_low) to AppColors.riskLow
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.size(AppSpacing.xs))
        Text(text = label, style = AppTypography.labelMedium, color = color)
    }
}
