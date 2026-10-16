package tn.esprit.dam.features.scan.presentation.scan
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.core.ui.theme.AppColors

@Composable
fun MLRecommendationsCard(
    recommendations: List<String>,
    riskFactors: List<String>,
    safetyTips: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = AppColors.orange,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        stringResource(R.string.recommendations),
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppColors.textSecondary
                )
            }

            if (recommendations.isNotEmpty()) {
                RecommendationRow(
                    text = recommendations.first(),
                    icon = Icons.Default.CheckCircle,
                    color = AppColors.success
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    recommendations.drop(1).forEach { rec ->
                        RecommendationRow(
                            text = rec,
                            icon = Icons.Default.CheckCircle,
                            color = AppColors.success
                        )
                    }

                    if (riskFactors.isNotEmpty()) {
                        HorizontalDivider(color = AppColors.surfaceVariant, thickness = 1.dp)
                        Text(
                            stringResource(R.string.risk_factors),
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        riskFactors.forEach { factor ->
                            RecommendationRow(
                                text = factor,
                                icon = Icons.Default.Warning,
                                color = AppColors.error
                            )
                        }
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
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textSecondary,
            lineHeight = 18.sp
        )
    }
}
