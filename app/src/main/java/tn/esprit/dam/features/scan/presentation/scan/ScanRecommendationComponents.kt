package tn.esprit.dam.features.scan.presentation.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography

/** Violet "AI advice" card: first tip always visible, the rest and the risk factors on tap. */
@Composable
fun MLRecommendationsCard(
    recommendations: List<String>,
    riskFactors: List<String>,
    note: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val hasMore = recommendations.size > 1 || riskFactors.isNotEmpty()
    val shape = RoundedCornerShape(AppCorners.large)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.accentViolet.copy(alpha = 0.10f))
            .border(1.dp, AppColors.accentViolet.copy(alpha = 0.35f), shape)
            .clickable(enabled = hasMore) { expanded = !expanded }
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AppColors.accentViolet, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(
                text = stringResource(R.string.scan_ai_advice),
                style = AppTypography.labelLarge,
                color = AppColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (hasMore) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppColors.textTertiary
                )
            }
        }

        note?.let { Text(text = it, style = AppTypography.bodyMedium, color = AppColors.textSecondary) }

        recommendations.firstOrNull()?.let {
            RecommendationRow(text = it, icon = Icons.Default.CheckCircle, color = AppColors.success)
        }

        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                recommendations.drop(1).forEach {
                    RecommendationRow(text = it, icon = Icons.Default.CheckCircle, color = AppColors.success)
                }
                if (riskFactors.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.risk_factors),
                        style = AppTypography.labelMedium,
                        color = AppColors.riskHigh,
                        modifier = Modifier.padding(top = AppSpacing.xs)
                    )
                    riskFactors.forEach {
                        RecommendationRow(text = it, icon = Icons.Default.Warning, color = AppColors.riskHigh)
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationRow(
    text: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(text = text, style = AppTypography.bodyMedium, color = AppColors.textSecondary)
    }
}
