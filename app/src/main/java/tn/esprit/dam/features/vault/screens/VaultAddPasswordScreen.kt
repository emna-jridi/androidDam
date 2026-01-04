package tn.esprit.dam.features.vault.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import tn.esprit.dam.features.vault.viewmodel.VaultViewModel
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography

@Composable
fun VaultAddPasswordScreen(
    passwordViewModel: Any?,
    vaultViewModel: VaultViewModel,
    onBackClick: () -> Unit,
    onPasswordAdded: () -> Unit
) {
    var serviceName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            // 🔐 Title
            Text(
                text = "Ajouter un mot de passe",
                color = AppColors.textPrimary,
                style = AppTypography.displayLarge,
                modifier = Modifier.padding(bottom = AppSpacing.sm)
            )

            // Service Name
            TextField(
                value = serviceName,
                onValueChange = { serviceName = it },
                label = { Text("Service") },
                placeholder = { Text("ex: Gmail, Twitter") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedLabelColor = AppColors.primary,
                    unfocusedLabelColor = AppColors.textSecondary
                ),
                shape = RoundedCornerShape(AppCorners.large),
                singleLine = true
            )

            // Username/Email
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Identifiant") },
                placeholder = { Text("votre.email@exemple.com") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedLabelColor = AppColors.primary,
                    unfocusedLabelColor = AppColors.textSecondary
                ),
                shape = RoundedCornerShape(AppCorners.large),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Password
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedLabelColor = AppColors.primary,
                    unfocusedLabelColor = AppColors.textSecondary
                ),
                shape = RoundedCornerShape(AppCorners.large),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showPassword) "Masquer" else "Afficher",
                            tint = AppColors.primary
                        )
                    }
                },
                singleLine = true
            )

            // Confirm Password
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmer") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedLabelColor = AppColors.primary,
                    unfocusedLabelColor = AppColors.textSecondary
                ),
                shape = RoundedCornerShape(AppCorners.large),
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showConfirmPassword) "Masquer" else "Afficher",
                            tint = AppColors.primary
                        )
                    }
                },
                singleLine = true
            )

            // Notes (Optional)
            TextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optionnel)") },
                placeholder = { Text("Questions de sécurité, remarques...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSpacing.xl),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.surface,
                    unfocusedContainerColor = AppColors.surface,
                    focusedTextColor = AppColors.textPrimary,
                    unfocusedTextColor = AppColors.textPrimary,
                    focusedLabelColor = AppColors.primary,
                    unfocusedLabelColor = AppColors.textSecondary
                ),
                shape = RoundedCornerShape(AppCorners.large)
            )

            // Error Message
            if (error.isNotEmpty()) {
                Surface(
                    color = AppColors.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(AppCorners.large),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = AppColors.error,
                        modifier = Modifier.padding(AppSpacing.md),
                        style = AppTypography.labelMedium
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    error = ""
                    when {
                        serviceName.isBlank() -> error = "Le service est requis"
                        username.isBlank() -> error = "L'identifiant est requis"
                        password.isBlank() -> error = "Le mot de passe est requis"
                        password != confirmPassword -> error = "Les mots de passe ne correspondent pas"
                        password.length < 6 -> error = "Le mot de passe doit avoir au moins 6 caractères"
                        else -> {
                            isLoading = true
                            onPasswordAdded()
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && serviceName.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary,
                    disabledContainerColor = AppColors.surfaceVariant
                ),
                shape = RoundedCornerShape(AppCorners.large)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.textPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enregistrer", style = AppTypography.labelLarge, color = AppColors.textPrimary)
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))
        }
    }
}
