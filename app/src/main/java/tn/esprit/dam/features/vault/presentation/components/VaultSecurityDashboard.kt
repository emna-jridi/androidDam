package tn.esprit.dam.features.vault.presentation.components
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.esprit.dam.features.vault.data.model.PasswordEntry
import tn.esprit.dam.features.vault.domain.password.PasswordStrengthCalculator

@Composable
fun VaultSecurityDashboard(
    passwords: List<PasswordEntry>,
    modifier: Modifier = Modifier
) {
    val metrics = remember(passwords.size, passwords.hashCode()) {
        calculateDashboardMetrics(passwords)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Security, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.vault_health), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${metrics.avgScore}/100",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = strengthScoreColor(metrics.avgScore)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem(
                    label = stringResource(R.string.weak),
                    value = "${metrics.weakCount}",
                    color = if (metrics.weakCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    icon = if (metrics.weakCount > 0) Icons.Default.Warning else Icons.Outlined.CheckCircle
                )
                MetricItem(
                    label = stringResource(R.string.reused),
                    value = "${metrics.reusedCount}",
                    color = if (metrics.reusedCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    icon = if (metrics.reusedCount > 0) Icons.Default.Warning else Icons.Outlined.CheckCircle
                )
                MetricItem(
                    label = stringResource(R.string.strong),
                    value = "${metrics.strongCount}",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Outlined.CheckCircle
                )
            }

            if (metrics.weakCount > 0 || metrics.reusedCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Recommendation: Update ${metrics.weakCount + metrics.reusedCount} risky passwords.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class DashboardMetrics(
    val avgScore: Int,
    val weakCount: Int,
    val reusedCount: Int,
    val strongCount: Int
)

private fun calculateDashboardMetrics(entries: List<PasswordEntry>): DashboardMetrics {
    if (entries.isEmpty()) return DashboardMetrics(0, 0, 0, 0)

    var totalScore = 0
    var weak = 0
    var strong = 0
    val passwordHashes = mutableSetOf<String>()
    var reused = 0

    entries.forEach { entry ->
        val score = entry.strengthScore ?: 0

        totalScore += score
        if (score < 50) weak++
        if (score >= 80) strong++
    }

    return DashboardMetrics(
        avgScore = totalScore / entries.size,
        weakCount = weak,
        reusedCount = 0,
        strongCount = strong
    )
}

private fun strengthScoreColor(score: Int): Color {
    return when {
        score < 50 -> AppColors.error
        score < 80 -> AppColors.warning
        else -> AppColors.success
    }
}
