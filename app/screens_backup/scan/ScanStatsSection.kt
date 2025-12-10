package tn.esprit.dam.screens.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.ui.theme.*
import kotlin.Int


@Composable
fun ScanStatsCard(stats: ScanStatsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScanTheme.CardBg
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Résumé du Scan",
                style = MaterialTheme.typography.titleMedium.copy(
                    color =Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    count = stats.totalApps,
                    label = "Apps",
                    icon = Icons.Default.Apps,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    count = stats.avgScore,
                    label = "Score",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = DarkCardBg)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (stats.criticalApps + stats.highRiskApps > 0) {
                    RiskRow(
                        count = stats.criticalApps + stats.highRiskApps,
                        label = "Critique",
                        color = CriticalText,
                        bgColor = CriticalBg
                    )
                }
                if (stats.mediumRiskApps > 0) {
                    RiskRow(
                        count = stats.mediumRiskApps,
                        label = "Modéré",
                        color = MediumText,
                        bgColor = MediumBg
                    )
                }
                RiskRow(
                    count = stats.lowRiskApps,
                    label = "Faible",
                    color = LowText,
                    bgColor = LowBg
                )
            }
        }
    }
}

@Composable
fun StatChip(
    count: Int,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = DarkCardBg
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = DarkTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanTheme.TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RiskRow(
    count: Int,
    label: String,
    color: Color,
    bgColor: Color
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
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ScanTheme.TextSecondary
                )
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bgColor
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

