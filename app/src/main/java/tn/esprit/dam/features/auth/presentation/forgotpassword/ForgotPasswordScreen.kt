package tn.esprit.dam.features.auth.presentation.forgotpassword

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.features.auth.presentation.components.AuthFieldLabel
import tn.esprit.dam.features.auth.presentation.components.AuthHeroHeader
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthScreenLayout
import tn.esprit.dam.features.auth.presentation.components.AuthStatusMessage
import tn.esprit.dam.features.auth.presentation.components.AuthTextField
import tn.esprit.dam.features.auth.presentation.components.AuthTopBar

@Composable
fun ForgotPasswordScreen(
    onNavigateToOTP: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep == ResetPasswordStep.VERIFY_OTP) {
            onNavigateToOTP()
        }
    }

    AuthScreenLayout {
        AuthTopBar(onBack = onNavigateBack, step = 1)
        AuthHeroHeader(
            icon = Icons.Filled.LockReset,
            title = stringResource(R.string.forgot_title),
            subtitle = stringResource(R.string.forgot_subtitle)
        )

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        AuthFieldLabel(stringResource(R.string.email))
        AuthTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            placeholder = stringResource(R.string.votre_email_com),
            leadingIcon = Icons.Filled.Email,
            enabled = !uiState.isLoading,
            error = uiState.emailError,
            styleDisabledState = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        AuthPrimaryButton(
            text = stringResource(R.string.envoyer_le_code),
            onClick = { viewModel.requestPasswordReset() },
            enabled = !uiState.isLoading,
            isLoading = uiState.isLoading
        )
        uiState.errorMessage?.let { AuthStatusMessage(it.asString(), isError = true) }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(
        onNavigateToOTP = {},
        onNavigateBack = {}
    )
}
