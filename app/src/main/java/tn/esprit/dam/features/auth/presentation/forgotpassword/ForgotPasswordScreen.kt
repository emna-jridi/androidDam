package tn.esprit.dam.features.auth.presentation.forgotpassword
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.features.auth.presentation.components.AuthBackButton
import tn.esprit.dam.features.auth.presentation.components.AuthBackground
import tn.esprit.dam.features.auth.presentation.components.AuthCard
import tn.esprit.dam.features.auth.presentation.components.AuthCardTitle
import tn.esprit.dam.features.auth.presentation.components.AuthFieldLabel
import tn.esprit.dam.features.auth.presentation.components.AuthIconBadge
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthStatusMessage
import tn.esprit.dam.features.auth.presentation.components.AuthSubtitle
import tn.esprit.dam.features.auth.presentation.components.AuthTextField
import tn.esprit.dam.features.auth.presentation.components.AuthTitle

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

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthIconBadge(Icons.Filled.LockReset)
            Spacer(modifier = Modifier.height(24.dp))
            AuthTitle("Mot de passe oublié ?")
            Spacer(modifier = Modifier.height(16.dp))
            AuthSubtitle("Pas de souci, nous vous enverrons un code de réinitialisation")
            Spacer(modifier = Modifier.height(48.dp))

            AuthCard {
                AuthCardTitle("Réinitialisation")
                Spacer(modifier = Modifier.height(24.dp))
                AuthFieldLabel("Email")
                AuthTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    placeholder = stringResource(R.string.votre_email_com),
                    leadingIcon = Icons.Filled.Email,
                    enabled = !uiState.isLoading,
                    error = uiState.emailError,
                    styleDisabledState = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                AuthPrimaryButton(
                    text = stringResource(R.string.envoyer_le_code),
                    onClick = { viewModel.requestPasswordReset() },
                    enabled = !uiState.isLoading,
                    isLoading = uiState.isLoading
                )
                uiState.errorMessage?.let { AuthStatusMessage(it.asString(), isError = true) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            AuthBackButton("Retour à la connexion", onNavigateBack)
        }
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
