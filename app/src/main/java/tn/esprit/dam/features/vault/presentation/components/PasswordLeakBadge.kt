package tn.esprit.dam.features.vault.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.vault.presentation.passwords.PasswordLeakState

/** "Seen in leaks?" result line. Shows nothing while idle; an error is never shown as "not found". */
@Composable
fun PasswordLeakBadge(state: PasswordLeakState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (state) {
        PasswordLeakState.Idle -> Unit
        PasswordLeakState.Checking -> LeakRow(
            color = AppColors.textTertiary,
            text = stringResource(R.string.vault_leak_checking),
            modifier = modifier,
            leading = {
                CircularProgressIndicator(color = AppColors.textTertiary, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            }
        )
        is PasswordLeakState.Checked -> if (state.count == 0) {
            LeakRow(AppColors.riskLow, stringResource(R.string.vault_leak_safe), modifier, icon = Icons.Default.VerifiedUser)
        } else {
            LeakRow(
                AppColors.riskCritical,
                stringResource(R.string.vault_leak_found, NumberFormat.getIntegerInstance().format(state.count)),
                modifier,
                icon = Icons.Default.GppBad
            )
        }
        PasswordLeakState.Failed -> LeakRow(
            AppColors.riskMedium,
            stringResource(R.string.vault_leak_failed),
            modifier,
            icon = Icons.Default.CloudOff,
            action = {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.vault_leak_retry), style = AppTypography.labelMedium, color = AppColors.primary)
                }
            }
        )
    }
}

@Composable
private fun LeakRow(
    color: Color,
    text: String,
    modifier: Modifier,
    icon: ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke() ?: icon?.let { Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(18.dp)) }
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(text = text, style = AppTypography.labelMedium, color = color, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}
