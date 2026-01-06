package tn.esprit.dam.features.vault.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.features.vault.viewmodel.VaultViewModel
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouveau mot de passe", style = AppTypography.titleLarge, color = AppColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                // Hero header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppCorners.xlarge),
                    tonalElevation = 2.dp,
                    color = AppColors.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(AppColors.surface, AppColors.surfaceVariant)
                                )
                            )
                            .padding(AppSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Ajouter un mot de passe", style = AppTypography.displayLarge, color = AppColors.textPrimary)
                        Text("Sécurise-le, ajoute des notes et une catégorie pour le retrouver rapidement.",
                            style = AppTypography.bodyMedium, color = AppColors.textSecondary)
                    }
                }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { password = generateStrongPassword() }) {
                            Text("Générer", style = AppTypography.labelMedium, color = AppColors.primary)
                        }
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (showPassword) "Masquer" else "Afficher",
                                tint = AppColors.primary
                            )
                        }
                    }
                },
                singleLine = true
            )

                // Strength meter
                PasswordStrengthBar(password = password)

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
}

@Composable
private fun PasswordStrengthBar(password: String) {
    val score = remember(password) { passwordStrengthScore(password) }
    val colors = listOf(
        AppColors.error,
        AppColors.primaryLight,
        AppColors.primary,
        Color(0xFF2ECC71)
    )
    val labels = listOf("Très faible", "Moyen", "Bon", "Excellent")
    val index = (score.coerceIn(0, 100) / 25).coerceAtMost(3)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = score / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = colors[index]
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(labels[index], style = AppTypography.labelMedium, color = AppColors.textSecondary)
            Text("${score}%", style = AppTypography.labelMedium, color = AppColors.textSecondary)
        }
    }
}

private fun passwordStrengthScore(pwd: String): Int {
    if (pwd.isBlank()) return 0
    var score = 0
    if (pwd.length >= 12) score += 40 else score += (pwd.length * 3).coerceAtMost(30)
    if (pwd.any { it.isLowerCase() }) score += 10
    if (pwd.any { it.isUpperCase() }) score += 10
    if (pwd.any { it.isDigit() }) score += 15
    if (pwd.any { !it.isLetterOrDigit() }) score += 25
    return score.coerceAtMost(100)
}

private fun generateStrongPassword(length: Int = 16): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789@#&!*+-_=.?"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}
