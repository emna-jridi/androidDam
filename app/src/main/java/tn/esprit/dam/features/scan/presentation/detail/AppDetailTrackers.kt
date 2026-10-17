package tn.esprit.dam.features.scan.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.SecurityUtils
import tn.esprit.dam.features.scan.presentation.model.TrackerUi

@Composable
fun TrackersTab(trackers: List<TrackerUi>) {
    if (trackers.isEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppCorners.large))
                .background(AppColors.surface)
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AppColors.riskLow)
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(stringResource(R.string.detail_no_trackers), style = AppTypography.bodyMedium, color = AppColors.textSecondary)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        trackers.forEach { TrackerRow(it) }
    }
}

@Composable
private fun TrackerRow(tracker: TrackerUi) {
    val analysis = SecurityUtils.analyzeTracker(tracker.name)
    val (icon, category) = when {
        analysis.category.contains("Advert", ignoreCase = true) -> Icons.Default.Campaign to R.string.detail_tracker_ads
        analysis.category.contains("Analytic", ignoreCase = true) -> Icons.Default.Insights to R.string.detail_tracker_analytics
        else -> Icons.Default.TrackChanges to R.string.detail_tracker_other
    }
    val (levelLabel, color) = when (analysis.risk) {
        RiskLevel.CRITICAL, RiskLevel.HIGH -> R.string.level_high to AppColors.riskHigh
        RiskLevel.MEDIUM -> R.string.level_medium to AppColors.riskMedium
        RiskLevel.LOW, RiskLevel.SAFE -> R.string.level_low to AppColors.riskLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(AppColors.surface)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(AppCorners.medium))
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tracker.name,
                style = AppTypography.bodyMedium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = stringResource(category), style = AppTypography.labelMedium, color = AppColors.textTertiary)
        }
        Text(
            text = stringResource(levelLabel),
            style = AppTypography.labelMedium,
            color = color,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.14f))
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
        )
    }
}
