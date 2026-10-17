package tn.esprit.dam.features.darkweb.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.darkweb.data.model.Breach

/** Passwords and payment data are the leaks that need action first. */
internal fun String.isSensitiveData(): Boolean =
    contains("password", ignoreCase = true) || contains("credit", ignoreCase = true) ||
        contains("bank", ignoreCase = true) || contains("card", ignoreCase = true)

internal fun breachColor(resolved: Boolean): Color = if (resolved) AppColors.riskLow else AppColors.riskHigh

/** First letter of the source; the backend does not send logos. */
@Composable
internal fun BreachAvatar(source: String, resolved: Boolean, size: Dp) {
    val color = breachColor(resolved)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = source.take(1).uppercase(), style = AppTypography.titleMedium, color = color)
    }
}

/** Status as dot + word, never color alone. */
@Composable
internal fun BreachStatusPill(resolved: Boolean) {
    val color = breachColor(resolved)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(AppSpacing.xs))
        Text(
            text = stringResource(if (resolved) R.string.darkweb_status_resolved else R.string.darkweb_status_open),
            style = AppTypography.labelMedium,
            color = color
        )
    }
}

@Composable
internal fun DataClassChips(dataClasses: List<String>, max: Int = Int.MAX_VALUE) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        dataClasses.take(max).forEach { item ->
            val sensitive = item.isSensitiveData()
            Text(
                text = item,
                style = AppTypography.labelMedium,
                color = if (sensitive) AppColors.riskHigh else AppColors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(AppCorners.small))
                    .background(if (sensitive) AppColors.riskHigh.copy(alpha = 0.14f) else AppColors.surfaceVariant)
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
            )
        }
        if (dataClasses.size > max) {
            Text(
                text = stringResource(R.string.darkweb_more, dataClasses.size - max),
                style = AppTypography.labelMedium,
                color = AppColors.primary,
                modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs)
            )
        }
    }
}

/** One stored breach in the list: avatar, source, date, status, the first exposed data types. */
@Composable
fun BreachCard(breach: Breach, onClick: () -> Unit) {
    val date = formatBreachDate(breach.breachDate, "d MMM yyyy") ?: stringResource(R.string.darkweb_unknown_date)
    // Sensitive data first, so passwords show even when there are many types.
    val dataClasses = breach.dataClasses.sortedByDescending { it.isSensitiveData() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .clickable(onClick = onClick)
            .padding(AppSpacing.md)
            .alpha(if (breach.isResolved) 0.7f else 1f),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BreachAvatar(source = breach.source, resolved = breach.isResolved, size = 40.dp)
            Spacer(modifier = Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = breach.source,
                    style = AppTypography.bodyMedium,
                    color = AppColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = date, style = AppTypography.labelMedium, color = AppColors.textTertiary)
            }
            BreachStatusPill(resolved = breach.isResolved)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.textMuted)
        }
        if (dataClasses.isNotEmpty()) DataClassChips(dataClasses, max = 3)
    }
}
