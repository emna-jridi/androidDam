package tn.esprit.dam.features.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.components.AppErrorAlert
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.auth.presentation.components.AuthFieldLabel
import tn.esprit.dam.features.auth.presentation.components.AuthHeroHeader
import tn.esprit.dam.features.auth.presentation.components.AuthPasswordField
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthScreenLayout
import tn.esprit.dam.features.auth.presentation.components.AuthTextField
import tn.esprit.dam.features.auth.presentation.components.AuthTrustBadge

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AuthEvent.LoginSuccess -> onLoginSuccess()
            }
        }
    }

    AuthScreenLayout {
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        AuthHeroHeader(
            icon = Icons.Filled.Shield,
            title = stringResource(R.string.login_welcome_back),
            subtitle = stringResource(R.string.login_subtitle)
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxl))

        AuthFieldLabel(stringResource(R.string.email))
        AuthTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            placeholder = stringResource(R.string.votre_email_com),
            leadingIcon = Icons.Filled.Email,
            enabled = !uiState.isLoading,
            error = uiState.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

        AuthFieldLabel(stringResource(R.string.mot_de_passe))
        AuthPasswordField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            visible = uiState.passwordVisible,
            onToggleVisibility = { viewModel.togglePasswordVisibility() },
            enabled = !uiState.isLoading,
            error = uiState.passwordError
        )

        TextButton(
            onClick = onNavigateToForgotPassword,
            modifier = Modifier.align(Alignment.End),
            enabled = !uiState.isLoading
        ) {
            Text(
                stringResource(R.string.mot_de_passe_oublie),
                style = AppTypography.labelMedium,
                color = AppColors.primary
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))

        AuthPrimaryButton(
            text = stringResource(R.string.se_connecter),
            onClick = { viewModel.login() },
            enabled = !uiState.isLoading,
            isLoading = uiState.isLoading
        )

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(AppSpacing.md))
            AppErrorAlert(
                message = error.asString(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.pas_encore_de_compte),
                color = AppColors.textSecondary,
                style = AppTypography.bodyMedium
            )
            TextButton(
                onClick = onNavigateToRegister,
                enabled = !uiState.isLoading
            ) {
                Text(
                    stringResource(R.string.creer_un_compte),
                    color = AppColors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        AuthTrustBadge(
            text = stringResource(R.string.connexion_securisee_et_chiffree),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginSuccess = {},
        onNavigateToRegister = {},
        onNavigateToForgotPassword = {}
    )
}
