package tn.esprit.dam.features.scan.presentation.detail
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.features.scan.domain.ScanRiskResult

@Composable
fun WhyFlaggedSection(
    app: AppDetailUi,
    riskResult: ScanRiskResult
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = stringResource(R.string.pulse_alpha)
    )

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
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = AppColors.error,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.why_was_this_flagged),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.errorContainer.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, AppColors.error.copy(alpha = pulseAlpha))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = when {
                        app.trackers.isNotEmpty() && app.permissions.isNotEmpty() ->
                            "⚠️ ${app.displayName ?: "This app"} connects to the internet and transfers data to analytics and advertising networks without explicit user consent."
                        app.trackers.isNotEmpty() ->
                            "⚠️ ${app.displayName ?: "This app"} contains ${app.trackers.size} trackers that collect your usage data."
                        app.permissions.isNotEmpty() ->
                            "⚠️ ${app.displayName ?: "This app"} requests access to sensitive device features and data."
                        else -> "⚠️ This app has potential security concerns."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.errorLight,
                    lineHeight = 22.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (app.trackers.isNotEmpty()) {
                        RiskIndicatorChip(
                            icon = Icons.Default.Visibility,
                            text = "${app.trackers.size} Trackers",
                            backgroundColor = AppColors.errorDark
                        )
                    }
                    if (app.permissions.size > 10) {
                        RiskIndicatorChip(
                            icon = Icons.Default.Lock,
                            text = "${app.permissions.size} Perms",
                            backgroundColor = AppColors.orange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiskIndicatorChip(
    icon: ImageVector,
    text: String,
    backgroundColor: Color
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = backgroundColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
        }
    }
}
