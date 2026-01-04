package tn.esprit.dam.features.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.features.auth.login.AuthEvent
import tn.esprit.dam.features.auth.login.AuthViewModel
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography
import tn.esprit.dam.ui.components.AppPrimaryButton
import tn.esprit.dam.ui.components.AppErrorAlert
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AuthEvent.LoginSuccess -> onLoginSuccess()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo et titre
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(AppCorners.xlarge),
                color = AppColors.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            Text(
                text = "ShadowGuard",
                style = AppTypography.displayLarge,
                color = AppColors.textPrimary
            )

            Text(
                text = "Protégez votre vie privée",
                style = AppTypography.bodyLarge,
                color = AppColors.textSecondary
            )

            Spacer(modifier = Modifier.height(AppSpacing.xxl))

            // Card de connexion
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppCorners.xlarge),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.lg),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Connexion",
                        style = AppTypography.titleLarge,
                        color = AppColors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    // Email
                    Text(
                        text = "Email",
                        style = AppTypography.bodyMedium,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(bottom = AppSpacing.sm)
                    )

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        placeholder = { Text("votre@email.com", color = AppColors.textTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        isError = uiState.emailError != null,
                        supportingText = uiState.emailError?.let {
                            { Text(it, color = AppColors.error) }
                        },
                        shape = RoundedCornerShape(AppCorners.medium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = AppColors.surfaceVariant,
                            focusedContainerColor = AppColors.surfaceVariant,
                            unfocusedContainerColor = AppColors.surfaceVariant,
                            cursorColor = AppColors.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.md))

                    // Mot de passe
                    Text(
                        text = "Mot de passe",
                        style = AppTypography.bodyMedium,
                        color = AppColors.textPrimary,
                        modifier = Modifier.padding(bottom = AppSpacing.sm)
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        placeholder = { Text("●●●●●●●●", color = AppColors.textTertiary) },
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.togglePasswordVisibility() },
                                enabled = !uiState.isLoading
                            ) {
                                Icon(
                                    imageVector = if (uiState.passwordVisible)
                                        Icons.Filled.Visibility
                                    else
                                        Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = AppColors.textSecondary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (uiState.passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        isError = uiState.passwordError != null,
                        supportingText = uiState.passwordError?.let {
                            { Text(it, color = AppColors.error) }
                        },
                        shape = RoundedCornerShape(AppCorners.medium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary,
                            focusedBorderColor = AppColors.primary,
                            unfocusedBorderColor = AppColors.surfaceVariant,
                            focusedContainerColor = AppColors.surfaceVariant,
                            unfocusedContainerColor = AppColors.surfaceVariant,
                            cursorColor = AppColors.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.sm))

                    TextButton(
                        onClick = onNavigateToForgotPassword,
                        modifier = Modifier.align(Alignment.End),
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            "Mot de passe oublié ?",
                            color = AppColors.primary,
                        )
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.lg))

                    AppPrimaryButton(
                        text = "Se connecter",
                        onClick = { viewModel.login() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        loading = uiState.isLoading
                    )

                    // Message d'erreur
                    uiState.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                        AppErrorAlert(
                            message = error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            // Ou continuer avec
            Text(
                text = "ou continuer avec",
                color = AppColors.textSecondary,
                style = AppTypography.labelMedium
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Pas de compte
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pas encore de compte ? ",
                    color = AppColors.textSecondary,
                    style = AppTypography.bodyMedium
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        "Créer un compte",
                        color = AppColors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.md))

            // Footer sécurité
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppColors.textTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Text(
                    text = "Connexion sécurisée et chiffrée",
                    color = AppColors.textTertiary,
                    style = AppTypography.labelMedium
                )
            }
        }
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
