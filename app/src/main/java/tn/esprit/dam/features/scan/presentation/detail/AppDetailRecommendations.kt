package tn.esprit.dam.features.scan.presentation.detail
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.features.scan.domain.ScanRiskResult

@Composable
fun RecommendationsSection(
    app: AppDetailUi,
    riskResult: ScanRiskResult
) {
    val recommendations = remember(app, riskResult) {
        buildList {
            if (app.permissions.any { it.contains("LOCATION", ignoreCase = true) }) {
                add(RecommendationItem(
                    title = "Restrict Location Access",
                    description = "Limit app access to your precise location data to protect your privacy.",
                    icon = Icons.Default.LocationOff
                ))
            }
            if (app.permissions.any { it.contains("CAMERA", ignoreCase = true) }) {
                add(RecommendationItem(
                    title = "Disable Camera Access",
                    description = "Revoke camera permissions if not needed for core functionality.",
                    icon = Icons.Default.NoPhotography
                ))
            }
            if (app.permissions.any { it.contains("MICROPHONE", ignoreCase = true) || it.contains("RECORD_AUDIO", ignoreCase = true) }) {
                add(RecommendationItem(
                    title = "Disable Microphone Access",
                    description = "Prevent unauthorized audio recording by revoking microphone permissions.",
                    icon = Icons.Default.MicOff
                ))
            }
            if (app.trackers.isNotEmpty()) {
                add(RecommendationItem(
                    title = "Block Tracker Network Requests",
                    description = "Use a privacy-focused DNS or firewall to block tracker connections.",
                    icon = Icons.Default.Block
                ))
            }
            add(RecommendationItem(
                title = "Review App Regularly",
                description = "Periodically review app permissions and behavior for changes.",
                icon = Icons.Default.Refresh
            ))
        }
    }

    var selectedRecs by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = AppColors.warning,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.recommendations),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
        }

        recommendations.forEachIndexed { index, rec ->
            EnhancedCollapsibleRecommendationCard(
                recommendation = rec,
                isExpanded = index in selectedRecs,
                onToggle = {
                    selectedRecs = if (index in selectedRecs) {
                        selectedRecs - index
                    } else {
                        selectedRecs + index
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppColors.textMuted),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.textPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.mark_safe), fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.protect), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EnhancedCollapsibleRecommendationCard(
    recommendation: RecommendationItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = AppColors.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = recommendation.icon,
                                contentDescription = null,
                                tint = AppColors.primaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = recommendation.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AppColors.textTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = recommendation.description,
                    fontSize = 13.sp,
                    color = AppColors.textTertiary,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }
    }
}

data class RecommendationItem(
    val title: String,
    val description: String,
    val icon: ImageVector = Icons.Default.Security
)
