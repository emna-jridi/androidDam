package tn.esprit.dam.features.auth.presentation.forgotpassword
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.features.auth.presentation.components.AuthBackButton
import tn.esprit.dam.features.auth.presentation.components.AuthBackground
import tn.esprit.dam.features.auth.presentation.components.AuthCard
import tn.esprit.dam.features.auth.presentation.components.AuthCardTitle
import tn.esprit.dam.features.auth.presentation.components.AuthFieldLabel
import tn.esprit.dam.features.auth.presentation.components.AuthIconBadge
import tn.esprit.dam.features.auth.presentation.components.AuthPasswordField
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthStatusMessage
import tn.esprit.dam.features.auth.presentation.components.AuthSubtitle
import tn.esprit.dam.features.auth.presentation.components.AuthTitle

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

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthIconBadge(Icons.Filled.LockOpen)
            Spacer(modifier = Modifier.height(24.dp))
            AuthTitle("Nouveau Mot de Passe")
            Spacer(modifier = Modifier.height(16.dp))
            AuthSubtitle("Choisissez un mot de passe sécurisé")
            Spacer(modifier = Modifier.height(48.dp))

            AuthCard {
                AuthCardTitle("Créer un nouveau mot de passe")
                Spacer(modifier = Modifier.height(24.dp))
                AuthFieldLabel("Nouveau mot de passe")
                AuthPasswordField(
                    value = uiState.newPassword,
                    onValueChange = { viewModel.onNewPasswordChange(it) },
                    visible = uiState.passwordVisible,
                    onToggleVisibility = { viewModel.togglePasswordVisibility() },
                    enabled = !uiState.isLoading,
                    error = uiState.passwordError
                )
                Spacer(modifier = Modifier.height(16.dp))
                AuthFieldLabel("Confirmer le mot de passe")
                AuthPasswordField(
                    value = uiState.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChange(it) },
                    visible = uiState.confirmPasswordVisible,
                    onToggleVisibility = { viewModel.toggleConfirmPasswordVisibility() },
                    enabled = !uiState.isLoading,
                    error = uiState.confirmPasswordError
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.votre_mot_de_passe_doit_contenir),
                            color = AppColors.textSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                AuthPrimaryButton(
                    text = stringResource(R.string.reinitialiser_le_mot_de_passe),
                    onClick = { viewModel.resetPassword() },
                    enabled = !uiState.isLoading,
                    isLoading = uiState.isLoading
                )
                uiState.errorMessage?.let { AuthStatusMessage(it.asString(), isError = true) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            AuthBackButton("Retour", onNavigateBack)
        }
    }
}

@Composable
fun PasswordRequirement(text: String, met: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (met) AppColors.success else AppColors.textMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = if (met) AppColors.success else AppColors.textSecondary,
            style = MaterialTheme.typography.bodySmall
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
