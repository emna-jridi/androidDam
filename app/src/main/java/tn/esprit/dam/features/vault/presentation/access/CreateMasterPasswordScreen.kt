package tn.esprit.dam.features.vault.presentation.access

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.auth.presentation.components.AuthFieldLabel
import tn.esprit.dam.features.auth.presentation.components.AuthHeroHeader
import tn.esprit.dam.features.auth.presentation.components.AuthPasswordField
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthScreenLayout
import tn.esprit.dam.features.auth.presentation.components.AuthStatusMessage
import tn.esprit.dam.features.auth.presentation.components.PasswordStrengthMeter

@Composable
fun CreateMasterPasswordScreen(
    onCreateVault: (String, String) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val passwordsMatch = masterPassword == confirmPassword && masterPassword.isNotBlank()
    val passwordLongEnough = masterPassword.length >= 8

    AuthScreenLayout(applySystemBarsPadding = false) {
        AuthHeroHeader(
            icon = Icons.Filled.Shield,
            title = stringResource(R.string.secure_your_vault),
            subtitle = stringResource(R.string.set_a_master_password_to_encrypt_your_pe)
        )

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        AuthFieldLabel(stringResource(R.string.master_password))
        AuthPasswordField(
            value = masterPassword,
            onValueChange = { masterPassword = it },
            visible = showPassword,
            onToggleVisibility = { showPassword = !showPassword },
            enabled = !isLoading,
            error = if (masterPassword.isNotBlank() && !passwordLongEnough) {
                stringResource(R.string.must_be_at_least_8_characters)
            } else null
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        PasswordStrengthMeter(password = masterPassword)

        Spacer(modifier = Modifier.height(AppSpacing.md))

        AuthFieldLabel(stringResource(R.string.confirm_password))
        AuthPasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            visible = showConfirm,
            onToggleVisibility = { showConfirm = !showConfirm },
            enabled = !isLoading,
            error = if (confirmPassword.isNotBlank() && !passwordsMatch) {
                stringResource(R.string.passwords_do_not_match)
            } else null
        )

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        NoRecoveryWarning()

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        AuthPrimaryButton(
            text = stringResource(R.string.encrypt_create_vault),
            onClick = { onCreateVault(masterPassword, confirmPassword) },
            enabled = passwordsMatch && passwordLongEnough && !isLoading,
            isLoading = isLoading
        )
        error?.let { AuthStatusMessage(it, isError = true) }
    }
}

/** The one thing users must understand before creating the vault: a lost master password cannot be recovered. */
@Composable
private fun NoRecoveryWarning() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.warning.copy(alpha = 0.10f))
            .border(1.dp, AppColors.warning.copy(alpha = 0.35f), RoundedCornerShape(AppCorners.large))
            .padding(AppSpacing.md)
    ) {
        Icon(
            Icons.Filled.WarningAmber,
            contentDescription = null,
            tint = AppColors.warning,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column {
            Text(
                text = stringResource(R.string.vault_no_recovery_title),
                style = AppTypography.labelLarge,
                color = AppColors.warning
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                text = stringResource(R.string.we_do_not_store_your_master_password_if),
                style = AppTypography.labelMedium,
                color = AppColors.textSecondary
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateMasterPasswordScreenPreview() {
    CreateMasterPasswordScreen(onCreateVault = { _, _ -> }, isLoading = false, error = null)
}
