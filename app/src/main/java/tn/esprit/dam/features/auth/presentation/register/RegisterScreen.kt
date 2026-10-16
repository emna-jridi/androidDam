package tn.esprit.dam.features.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.delay
import tn.esprit.dam.features.auth.presentation.components.AuthBackground
import tn.esprit.dam.features.auth.presentation.components.AuthCard
import tn.esprit.dam.features.auth.presentation.components.AuthCardTitle
import tn.esprit.dam.features.auth.presentation.components.AuthColors
import tn.esprit.dam.features.auth.presentation.components.AuthFieldLabel
import tn.esprit.dam.features.auth.presentation.components.AuthIconBadge
import tn.esprit.dam.features.auth.presentation.components.AuthPasswordField
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthStatusMessage
import tn.esprit.dam.features.auth.presentation.components.AuthTextField
import tn.esprit.dam.features.auth.presentation.components.AuthTitle

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

    AuthBackground(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthIconBadge(Icons.Filled.PersonAdd)
            Spacer(modifier = Modifier.height(24.dp))
            AuthTitle("ShadowGuard")
            Text(
                text = "Créez votre compte sécurisé",
                style = MaterialTheme.typography.bodyLarge,
                color = AuthColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))

            AuthCard {
                AuthCardTitle("Inscription")
                Spacer(modifier = Modifier.height(24.dp))
                AuthFieldLabel("Nom complet")
                AuthTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    placeholder = "Votre nom",
                    leadingIcon = Icons.Filled.Person,
                    enabled = !uiState.isLoading,
                    error = uiState.nameError
                )
                Spacer(modifier = Modifier.height(16.dp))
                AuthFieldLabel("Email")
                AuthTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    placeholder = "votre@email.com",
                    leadingIcon = Icons.Filled.Email,
                    enabled = !uiState.isLoading,
                    error = uiState.emailError
                )
                Spacer(modifier = Modifier.height(16.dp))
                AuthFieldLabel("Mot de passe")
                AuthPasswordField(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    visible = uiState.passwordVisible,
                    onToggleVisibility = { viewModel.togglePasswordVisibility() },
                    enabled = !uiState.isLoading,
                    error = uiState.passwordError
                )
                Spacer(modifier = Modifier.height(24.dp))
                AuthPrimaryButton(
                    text = "S'inscrire",
                    onClick = { viewModel.register() },
                    enabled = !uiState.isLoading,
                    isLoading = uiState.isLoading
                )
                uiState.successMessage?.let { AuthStatusMessage(it, isError = false) }
                uiState.errorMessage?.let { AuthStatusMessage(it, isError = true) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Déjà un compte ? ",
                    color = AuthColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        "Se connecter",
                        color = AuthColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AuthColors.Placeholder,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Vos données sont sécurisées et chiffrées",
                    color = AuthColors.Placeholder,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
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
