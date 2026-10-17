package tn.esprit.dam.features.vault.presentation.components

import androidx.annotation.StringRes
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.vault.data.model.PasswordEntry

/** Three buckets used everywhere in the vault list: counters, filter and row badge. */
enum class StrengthBucket(val color: Color) {
    Weak(AppColors.riskCritical),
    Fair(AppColors.riskMedium),
    Strong(AppColors.riskLow)
}

/** Uses the stored score when there is one, otherwise the stored level. Same thresholds as before (50 / 80). */
fun PasswordEntry.strengthBucket(): StrengthBucket? {
    strengthScore?.let {
        return when {
            it < 50 -> StrengthBucket.Weak
            it < 80 -> StrengthBucket.Fair
            else -> StrengthBucket.Strong
        }
    }
    return when (strengthLevel) {
        "VERY_WEAK", "WEAK" -> StrengthBucket.Weak
        "FAIR" -> StrengthBucket.Fair
        "GOOD", "STRONG", "VERY_STRONG" -> StrengthBucket.Strong
        else -> null
    }
}

@StringRes
fun PasswordEntry.strengthLabel(): Int? = when (strengthLevel) {
    "VERY_WEAK" -> R.string.vault_strength_very_weak
    "WEAK" -> R.string.vault_strength_weak
    "FAIR" -> R.string.vault_strength_fair
    "GOOD" -> R.string.vault_strength_good
    "STRONG", "VERY_STRONG" -> R.string.vault_strength_strong
    else -> when (strengthBucket()) {
        StrengthBucket.Weak -> R.string.vault_strength_weak
        StrengthBucket.Fair -> R.string.vault_strength_fair
        StrengthBucket.Strong -> R.string.vault_strength_strong
        null -> null
    }
}

/** Vault health: average score as the big number, how many to fix, and the three counters. */
@Composable
fun VaultSecurityDashboard(
    passwords: List<PasswordEntry>,
    modifier: Modifier = Modifier
) {
    val scored = remember(passwords) { passwords.mapNotNull { it.strengthScore } }
    val avg = if (scored.isEmpty()) null else scored.sum() / scored.size
    val buckets = remember(passwords) { passwords.groupingBy { it.strengthBucket() }.eachCount() }
    val weak = buckets[StrengthBucket.Weak] ?: 0
    val accent = when {
        weak > 0 -> StrengthBucket.Weak.color
        (buckets[StrengthBucket.Fair] ?: 0) > 0 -> StrengthBucket.Fair.color
        else -> StrengthBucket.Strong.color
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.xlarge))
            .background(AppColors.surface)
            .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.14f), Color.Transparent), radius = 600f))
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.vault_health_title),
                    style = AppTypography.labelMedium,
                    color = AppColors.textTertiary
                )
                Text(
                    text = if (weak > 0) {
                        pluralStringResource(R.plurals.vault_health_weak_n, weak, weak)
                    } else {
                        stringResource(R.string.vault_health_all_good)
                    },
                    style = AppTypography.titleMedium,
                    color = AppColors.textPrimary
                )
            }
            if (avg != null) {
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = avg.toString(),
                        style = AppTypography.displayLarge.copy(fontWeight = FontWeight.Black),
                        color = accent
                    )
                    Text(
                        text = "/100",
                        style = AppTypography.labelMedium,
                        color = AppColors.textTertiary,
                        modifier = Modifier.padding(bottom = AppSpacing.sm)
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Counter(buckets[StrengthBucket.Weak] ?: 0, R.string.vault_count_weak, StrengthBucket.Weak.color, Modifier.weight(1f))
            Counter(buckets[StrengthBucket.Fair] ?: 0, R.string.vault_count_fair, StrengthBucket.Fair.color, Modifier.weight(1f))
            Counter(buckets[StrengthBucket.Strong] ?: 0, R.string.vault_count_strong, StrengthBucket.Strong.color, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Counter(value: Int, @StringRes label: Int, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(AppColors.background.copy(alpha = 0.6f))
            .padding(AppSpacing.sm + AppSpacing.xs)
    ) {
        Text(text = value.toString(), style = AppTypography.titleLarge, color = AppColors.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(AppSpacing.xs))
            Text(text = stringResource(label), style = AppTypography.labelMedium, color = AppColors.textSecondary)
        }
    }
}
