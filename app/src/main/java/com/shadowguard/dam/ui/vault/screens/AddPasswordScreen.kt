package com.shadowguard.dam.ui.vault.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shadowguard.dam.data.model.PasswordCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(
    onSave: (String, String, String, String?, String, String) -> Unit,
    onBack: () -> Unit,
    saveState: Result<Unit>? = null,
    onClearSaveState: () -> Unit = {},
    isSaving: Boolean = false
) {
    var site by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PasswordCategory.OTHER) }
    var showPassword by remember { mutableStateOf(false) }

    val saveError = saveState?.exceptionOrNull()?.message

    val strength = remember(password) { calculatePasswordStrength(password) }
    // Allow FAIR passwords and above (block only WEAK passwords)
    val isValid = site.isNotBlank() && username.isNotBlank() && password.isNotBlank() && strength != PasswordStrength.WEAK

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Add Password",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Show error if save failed
        if (saveError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Save Failed",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = saveError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = onClearSaveState) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Site field
        OutlinedTextField(
            value = site,
            onValueChange = { site = it },
            label = { Text("Site Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username / Email *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Hide" else "Show"
                    )
                }
            },
            enabled = !isSaving
        )

        // Password strength indicator
        if (password.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordStrengthIndicator(strength)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // URL field
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Website URL (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (!isSaving) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = category.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !isSaving
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                PasswordCategory.ALL.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            category = cat
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
            leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Validation hints
        if (!isValid && !isSaving) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚠ Please complete:",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (site.isBlank()) {
                        Text("• Site name is required", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    if (username.isBlank()) {
                        Text("• Username is required", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    if (password.isBlank()) {
                        Text("• Password is required", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    } else if (strength == PasswordStrength.WEAK) {
                        Text("• Password is too weak (min 8 chars, mix letters/numbers)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onSave(
                        site,
                        username,
                        password,
                        notes.takeIf { it.isNotBlank() },
                        url.takeIf { it.isNotBlank() } ?: "",
                        category
                    )
                },
                enabled = isValid && !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSaving) "Saving..." else "Save Password")
            }
        }
    }
}

enum class PasswordStrength {
    WEAK, FAIR, GOOD, STRONG
}

fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.length < 8) return PasswordStrength.WEAK

    var score = 0
    if (password.length >= 12) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score < 3 -> PasswordStrength.WEAK
        score == 3 -> PasswordStrength.FAIR
        score == 4 -> PasswordStrength.GOOD
        else -> PasswordStrength.STRONG
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength) {
    val (color, label) = when (strength) {
        PasswordStrength.WEAK -> Color.Red to "Weak"
        PasswordStrength.FAIR -> Color(0xFFFF9800) to "Fair"
        PasswordStrength.GOOD -> Color(0xFF4CAF50) to "Good"
        PasswordStrength.STRONG -> Color(0xFF2196F3) to "Strong"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        LinearProgressIndicator(
            progress = when (strength) {
                PasswordStrength.WEAK -> 0.25f
                PasswordStrength.FAIR -> 0.5f
                PasswordStrength.GOOD -> 0.75f
                PasswordStrength.STRONG -> 1f
            },
            color = color,
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
