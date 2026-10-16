package tn.esprit.dam.features.vault.presentation.access
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource
import tn.esprit.dam.features.vault.domain.password.PasswordStrengthCalculator
import tn.esprit.dam.features.vault.domain.password.PasswordRiskLevel

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMasterPasswordScreen(
    onCreateVault: (String, String) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val passwordsMatch = masterPassword == confirmPassword && masterPassword.isNotBlank()
    val passwordLongEnough = masterPassword.length >= 8
    val canSubmit = passwordsMatch && passwordLongEnough && !isLoading

    val strength = remember(masterPassword) {
        calculatePasswordStrength(masterPassword)
    }

    val strengthProgress by animateFloatAsState(
        targetValue = strength.score / 100f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val iconScale by animateFloatAsState(
        targetValue = if (canSubmit) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .scale(iconScale),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.secure_your_vault),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.set_a_master_password_to_encrypt_your_pe),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = masterPassword,
            onValueChange = { masterPassword = it },
            label = { Text(stringResource(R.string.master_password)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            isError = masterPassword.isNotBlank() && !passwordLongEnough,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            supportingText = {
                if (masterPassword.isNotBlank() && !passwordLongEnough) {
                    Text(stringResource(R.string.must_be_at_least_8_characters), color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(stringResource(R.string.confirm_password)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showConfirm = !showConfirm }) {
                    Icon(
                        imageVector = if (showConfirm) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showConfirm) "Hide password" else "Show password"
                    )
                }
            },
            isError = confirmPassword.isNotBlank() && !passwordsMatch,
            supportingText = {
                if (confirmPassword.isNotBlank() && !passwordsMatch) {
                    Text(stringResource(R.string.passwords_do_not_match), color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onCreateVault(masterPassword, confirmPassword) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = passwordsMatch && passwordLongEnough && !isLoading,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.encrypt_create_vault), style = MaterialTheme.typography.titleMedium)
            }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline ?: Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.no_recovery_option),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.we_do_not_store_your_master_password_if),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class PasswordStrength(
    val score: Int,
    val label: String,
    val color: Color
)

private fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength(0, "None", AppColors.textMuted)

    val metrics = PasswordStrengthCalculator.analyze(password)
    val (label, color) = when (metrics.riskLevel) {
        PasswordRiskLevel.VERY_STRONG -> "Very Strong" to AppColors.success
        PasswordRiskLevel.STRONG -> "Strong" to AppColors.success
        PasswordRiskLevel.MEDIUM -> "Moderate" to AppColors.warning
        PasswordRiskLevel.WEAK -> "Weak" to AppColors.error
    }
    return PasswordStrength(metrics.score, label, color)
}
