package com.shadowguard.dam.ui.vault.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shadowguard.dam.data.model.PasswordCategory
import com.shadowguard.dam.ui.vault.utils.*
import com.shadowguard.dam.ui.vault.viewmodel.PasswordViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest

enum class InputMode { EXISTING, GENERATED, PASSPHRASE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(
    viewModel: PasswordViewModel,
    onBack: () -> Unit
) {
    // ViewModel State
    val isSaving by viewModel.isPasswordSaving.collectAsState()
    val savedState by viewModel.saveState.collectAsState()
    val metrics by viewModel.passwordMetrics.collectAsState()
    val aiAdvice by viewModel.aiAdvice.collectAsState()
    val generatedPassword by viewModel.generatedPassword.collectAsState()
    
    // UI State
    var mode by remember { mutableStateOf(InputMode.EXISTING) }
    
    // Form Fields
    var site by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var finalPassword by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PasswordCategory.OTHER) }
    var showPassword by remember { mutableStateOf(false) } // For Mode 1
    
    // Generator State
    var genLength by remember { mutableStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useNums by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    
    // Passphrase State
    var phraseWords by remember { mutableStateOf(4f) }
    var phraseSep by remember { mutableStateOf("-") }

    val clipboardManager = LocalClipboardManager.current
    
    // Domain Suggestions
    var domainExpanded by remember { mutableStateOf(false) }
    val commonDomains = remember {
        listOf("google.com", "facebook.com", "amazon.com", "netflix.com", "twitter.com", "linkedin.com", "github.com", "outlook.com")
    }
    val filteredDomains = remember(site) {
        if (site.isBlank()) emptyList() 
        else commonDomains.filter { it.contains(site, ignoreCase = true) }.take(3)
    }

    // Effect: Sync generated password to finalPassword
    LaunchedEffect(generatedPassword) {
        if (generatedPassword.isNotEmpty()) {
            finalPassword = generatedPassword
            viewModel.analyzePassword(finalPassword) // Analyze generated too
        }
    }
    
    // Effect: Real-time analysis for manual input
    LaunchedEffect(finalPassword) {
        if (mode == InputMode.EXISTING) {
            viewModel.analyzePassword(finalPassword)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // --- Header ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Secure Entry", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // --- Common Fields ---
        // Site Name
        ExposedDropdownMenuBox(
            expanded = domainExpanded,
            onExpandedChange = { domainExpanded = !domainExpanded }
        ) {
            OutlinedTextField(
                value = site,
                onValueChange = { site = it; domainExpanded = true },
                label = { Text("Website / App") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            if (filteredDomains.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = domainExpanded,
                    onDismissRequest = { domainExpanded = false }
                ) {
                    filteredDomains.forEach { domain ->
                        DropdownMenuItem(
                            text = { Text(domain) },
                            onClick = { site = domain; domainExpanded = false }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username / Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Mode Selector ---
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            InputMode.values().forEachIndexed { index, inputMode ->
                SegmentedButton(
                    selected = mode == inputMode,
                    onClick = { mode = inputMode },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = InputMode.values().size)
                ) {
                    Text(
                        text = when(inputMode) {
                            InputMode.EXISTING -> "Manual"
                            InputMode.GENERATED -> "Generate"
                            InputMode.PASSPHRASE -> "Passphrase"
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Mode Specific UI ---
        AnimatedContent(targetState = mode) { targetMode ->
            Column {
                when(targetMode) {
                    InputMode.EXISTING -> {
                        // Manual Password Input
                        OutlinedTextField(
                            value = finalPassword,
                            onValueChange = { finalPassword = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle")
                                }
                            }
                        )
                    }
                    InputMode.GENERATED -> {
                        // Generator Controls
                        Text("Length: ${genLength.toInt()}", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = genLength,
                            onValueChange = { genLength = it },
                            valueRange = 8f..64f,
                            steps = 56
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = useUpper,
                                onClick = { useUpper = !useUpper },
                                label = { Text("A-Z") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = useNums,
                                onClick = { useNums = !useNums },
                                label = { Text("0-9") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = useSymbols,
                                onClick = { useSymbols = !useSymbols },
                                label = { Text("#$@") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        Button(
                            onClick = { viewModel.generateNewPassword(genLength.toInt(), useUpper, useNums, useSymbols) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Regenerate")
                        }
                    }
                    InputMode.PASSPHRASE -> {
                        // Passphrase Controls
                        Text("Word Count: ${phraseWords.toInt()}", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = phraseWords,
                            onValueChange = { phraseWords = it },
                            valueRange = 3f..8f,
                            steps = 5
                        )
                         Button(
                            onClick = { viewModel.generateNewPassphrase(phraseWords.toInt(), phraseSep) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Regenerate Passphrase")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // --- Generated Result Display (For Modes 2 & 3) ---
        if (mode != InputMode.EXISTING && finalPassword.isNotEmpty()) {
            OutlinedTextField(
                value = finalPassword,
                onValueChange = {},
                readOnly = true,
                label = { Text("Result") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(finalPassword)) }) {
                        Icon(Icons.Default.ContentCopy, "Copy")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Security Insight Card (Common Analysis) ---
        if (finalPassword.isNotEmpty()) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Security, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Local Metrics
                    metrics?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Score: ${it.score}/100", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Crack Time: ~${it.estimatedCrackTime}")
                        }
                        LinearProgressIndicator(
                            progress = { it.score / 100f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            color = when(it.riskLevel) {
                                PasswordRiskLevel.WEAK -> MaterialTheme.colorScheme.error
                                PasswordRiskLevel.MEDIUM -> MaterialTheme.colorScheme.tertiary
                                PasswordRiskLevel.STRONG -> MaterialTheme.colorScheme.primary
                                PasswordRiskLevel.VERY_STRONG -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    // AI Advice (Ollama)
                    aiAdvice?.let { advice ->
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Insight", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(advice.summary, style = MaterialTheme.typography.bodySmall)
                        if (advice.recommendations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            advice.recommendations.forEach { 
                                Text("• $it", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } ?: run {
                        if (metrics != null && finalPassword.length >= 4) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp)) // Loading AI
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Category ---
        var catExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = catExpanded,
            onExpandedChange = { catExpanded = !catExpanded }
        ) {
            OutlinedTextField(
                value = category.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) }
            )
            ExposedDropdownMenu(
                expanded = catExpanded,
                onDismissRequest = { catExpanded = false }
            ) {
                PasswordCategory.ALL.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.replaceFirstChar { it.uppercase() }) }, onClick = { category = cat; catExpanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Save Button ---
        Button(
            onClick = { viewModel.createPassword(site, username, finalPassword, notes, null, category) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = finalPassword.isNotBlank() && site.isNotBlank() && username.isNotBlank() && !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Encrypting...")
            } else {
                Icon(Icons.Default.Save, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save to Vault")
            }
        }
    }
}
