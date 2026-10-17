package tn.esprit.dam.features.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import tn.esprit.dam.R
import tn.esprit.dam.core.model.User
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography

/** Initials avatar in a cyan ring, name, email, verification badge and the edit action. */
@Composable
fun ProfileHeader(
    user: User,
    onEdit: () -> Unit
) {
    val initials = user.name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { user.email.take(1).uppercase() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.xlarge))
            .background(AppColors.surface)
            .background(Brush.radialGradient(listOf(AppColors.primary.copy(alpha = 0.14f), Color.Transparent), radius = 600f))
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .border(2.dp, AppColors.primary.copy(alpha = 0.6f), CircleShape)
                .padding(AppSpacing.xs)
                .clip(CircleShape)
                .background(AppColors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initials, style = AppTypography.displayLarge, color = AppColors.primary)
        }
        Spacer(modifier = Modifier.size(AppSpacing.sm))
        Text(
            text = user.name,
            style = AppTypography.titleLarge,
            color = AppColors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = user.email,
            style = AppTypography.bodyMedium,
            color = AppColors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        VerifiedBadge(verified = user.isEmailVerified)
        Spacer(modifier = Modifier.size(AppSpacing.sm))
        OutlinedButton(
            onClick = onEdit,
            shape = RoundedCornerShape(AppCorners.medium),
            border = BorderStroke(1.dp, AppColors.surfaceVariant),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.textPrimary)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = AppColors.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(stringResource(R.string.profile_edit), style = AppTypography.labelLarge)
        }
    }
}

@Composable
private fun VerifiedBadge(verified: Boolean) {
    val color = if (verified) AppColors.riskLow else AppColors.riskMedium
    Row(
        modifier = Modifier
            .padding(top = AppSpacing.xs)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (verified) Icons.Default.Verified else Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(AppSpacing.xs))
        Text(
            text = stringResource(if (verified) R.string.profile_email_verified else R.string.profile_email_not_verified),
            style = AppTypography.labelMedium,
            color = color
        )
    }
}
