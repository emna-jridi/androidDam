package tn.esprit.dam.features.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
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
import tn.esprit.dam.features.auth.presentation.components.AuthTextField
import tn.esprit.dam.features.auth.presentation.components.AuthTrustBadge
import tn.esprit.dam.features.auth.presentation.components.PasswordStrengthMeter

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess, uiState.registeredEmail) {
        if (uiState.isSuccess && uiState.registeredEmail != null) {
            delay(2000)
            onRegisterSuccess(uiState.registeredEmail!!)
        }
    }

    AuthScreenLayout {
        AuthHeroHeader(
            icon = Icons.Filled.PersonAdd,
            title = stringResource(R.string.register_title),
            subtitle = stringResource(R.string.register_subtitle)
        )

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        AuthFieldLabel(stringResource(R.string.nom_complet))
        AuthTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            placeholder = stringResource(R.string.votre_nom),
            leadingIcon = Icons.Filled.Person,
            enabled = !uiState.isLoading,
            error = uiState.nameError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )

        Spacer(modifier = Modifier.height(AppSpacing.md))

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
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        PasswordStrengthMeter(password = uiState.password)

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        AuthPrimaryButton(
            text = stringResource(R.string.s_inscrire),
            onClick = { viewModel.register() },
            enabled = !uiState.isLoading,
            isLoading = uiState.isLoading
        )
        uiState.successMessage?.let { AuthStatusMessage(it.asString(), isError = false) }
        uiState.errorMessage?.let { AuthStatusMessage(it.asString(), isError = true) }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.deja_un_compte),
                color = AppColors.textSecondary,
                style = AppTypography.bodyMedium
            )
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !uiState.isLoading
            ) {
                Text(
                    stringResource(R.string.se_connecter),
                    color = AppColors.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        AuthTrustBadge(
            text = stringResource(R.string.vos_donnees_sont_securisees_et_chiffrees),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onRegisterSuccess = {},
        onNavigateToLogin = {}
    )
}
