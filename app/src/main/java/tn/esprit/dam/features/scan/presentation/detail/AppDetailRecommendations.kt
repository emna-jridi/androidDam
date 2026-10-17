package tn.esprit.dam.features.scan.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.presentation.scan.MLRecommendationsCard

/** Summary tab: AI summary, why the app is flagged, and what to do about it. */
@Composable
fun SummaryTab(
    analysis: AnalysisResultUi?,
    permissions: List<PermissionUi>,
    trackerCount: Int,
    onOpenSettings: () -> Unit
) {
    val sensitive = permissions
        .filter { it.isSensitive }
        .sortedBy { it.group.ordinal }
        .distinctBy { it.label ?: it.raw }

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        val summary = analysis?.aiSummary?.takeIf { it.isNotBlank() }
        val tips = analysis?.aiRecommendations.orEmpty()
        if (summary != null || tips.isNotEmpty()) {
            MLRecommendationsCard(recommendations = tips, riskFactors = emptyList(), note = summary)
        }

        SectionTitle(stringResource(R.string.detail_why_title))
        if (sensitive.isEmpty() && trackerCount == 0) {
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
                Text(stringResource(R.string.detail_why_clean), style = AppTypography.bodyMedium, color = AppColors.textSecondary)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                sensitive.take(3).forEach { WhyLine("${it.labelText()} : ${stringResource(it.description).lowercase()}") }
                if (trackerCount > 0) {
                    WhyLine(pluralStringResource(R.plurals.detail_why_trackers, trackerCount, trackerCount))
                }
            }
        }

        if (sensitive.isNotEmpty()) {
            SectionTitle(stringResource(R.string.detail_todo_title))
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                sensitive.take(3).forEach { permission ->
                    TodoRow(permission = permission, onClick = onOpenSettings)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = AppTypography.titleMedium, color = AppColors.textPrimary)
}

@Composable
private fun WhyLine(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = AppSpacing.sm)
                .size(6.dp)
                .background(AppColors.riskHigh, CircleShape)
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(text = text, style = AppTypography.bodyMedium, color = AppColors.textSecondary)
    }
}

/** One concrete action per sensitive permission; all lead to the app's Android settings. */
@Composable
private fun TodoRow(permission: PermissionUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(AppColors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(AppCorners.medium))
                .background(AppColors.riskHigh.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(permission.icon, contentDescription = null, tint = AppColors.riskHigh, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.detail_todo_remove, permission.labelText()),
                style = AppTypography.bodyMedium,
                color = AppColors.textPrimary
            )
            Text(text = stringResource(R.string.detail_todo_path), style = AppTypography.labelMedium, color = AppColors.textTertiary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.textMuted)
    }
}
