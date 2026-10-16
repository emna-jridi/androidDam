package tn.esprit.dam.features.auth.presentation.forgotpassword

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.auth.presentation.components.AuthFieldLabel
import tn.esprit.dam.features.auth.presentation.components.AuthHeroHeader
import tn.esprit.dam.features.auth.presentation.components.AuthPasswordField
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthScreenLayout
import tn.esprit.dam.features.auth.presentation.components.AuthStatusMessage
import tn.esprit.dam.features.auth.presentation.components.AuthTopBar
import tn.esprit.dam.features.auth.presentation.components.PasswordStrengthMeter

@Composable
fun NewPasswordScreen(
    onPasswordResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep == ResetPasswordStep.SUCCESS) {
            onPasswordResetSuccess()
        }
    }

    AuthScreenLayout {
        AuthTopBar(onBack = onNavigateBack, step = 3)
        AuthHeroHeader(
            icon = Icons.Filled.LockOpen,
            title = stringResource(R.string.new_password_title),
            subtitle = stringResource(R.string.new_password_subtitle)
        )

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        AuthFieldLabel(stringResource(R.string.new_password_label))
        AuthPasswordField(
            value = uiState.newPassword,
            onValueChange = { viewModel.onNewPasswordChange(it) },
            visible = uiState.passwordVisible,
            onToggleVisibility = { viewModel.togglePasswordVisibility() },
            enabled = !uiState.isLoading,
            error = uiState.passwordError
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        PasswordStrengthMeter(password = uiState.newPassword)

        Spacer(modifier = Modifier.height(AppSpacing.md))

        AuthFieldLabel(stringResource(R.string.confirm_password_label))
        AuthPasswordField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            visible = uiState.confirmPasswordVisible,
            onToggleVisibility = { viewModel.toggleConfirmPasswordVisibility() },
            enabled = !uiState.isLoading,
            error = uiState.confirmPasswordError
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

        Text(
            text = stringResource(R.string.votre_mot_de_passe_doit_contenir),
            style = AppTypography.labelMedium,
            color = AppColors.textSecondary
        )
        PasswordRequirement(
            text = stringResource(R.string.au_moins_6_caracteres),
            met = uiState.newPassword.length >= 6
        )
        PasswordRequirement(
            text = stringResource(R.string.au_moins_un_chiffre),
            met = uiState.newPassword.any { it.isDigit() }
        )
        PasswordRequirement(
            text = stringResource(R.string.au_moins_une_lettre),
            met = uiState.newPassword.any { it.isLetter() }
        )

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        AuthPrimaryButton(
            text = stringResource(R.string.reinitialiser_le_mot_de_passe),
            onClick = { viewModel.resetPassword() },
            enabled = !uiState.isLoading,
            isLoading = uiState.isLoading
        )
        uiState.errorMessage?.let { AuthStatusMessage(it.asString(), isError = true) }
    }
}

@Composable
fun PasswordRequirement(text: String, met: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = AppSpacing.sm)
    ) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (met) AppColors.success else AppColors.surfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(
            text = text,
            color = if (met) AppColors.success else AppColors.textTertiary,
            style = AppTypography.labelMedium
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewPasswordScreenPreview() {
    NewPasswordScreen(
        onPasswordResetSuccess = {},
        onNavigateBack = {}
    )
}
