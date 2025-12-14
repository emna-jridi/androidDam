package com.shadowguard.dam.ui.vault.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shadowguard.dam.ui.vault.viewmodel.PasswordDetailData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    data: PasswordDetailData,
    onBack: () -> Unit,
    onEdit: () -> Unit = {}, // Future placeholder
    onDelete: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // --- Header ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(data.entry.site, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Credentials Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LabelValueRow(
                    label = "Username / Email",
                    value = data.entry.username,
                    onCopy = { clipboardManager.setText(AnnotatedString(data.entry.username)) }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Password Row
                Column {
                    Text("Password", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = data.decryptedPassword,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle")
                                    }
                                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(data.decryptedPassword)) }) {
                                        Icon(Icons.Default.ContentCopy, "Copy")
                                    }
                                }
                            }
                        )
                    }
                }
                
                if (data.entry.url != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    LabelValueRow(label = "URL", value = data.entry.url)
                }
                
                if (data.decryptedNotes != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    LabelValueRow(label = "Notes", value = data.decryptedNotes)
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                LabelValueRow(label = "Category", value = data.entry.category.capitalize())
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- Security Analysis Card ---
        // Only show if we have metrics
        if (data.entry.strengthScore != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                 Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Security, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Score: ${data.entry.strengthScore}/100", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))
                        data.entry.estimatedCrackTime?.let {
                            Text("Crack Time: ~$it")
                        }
                    }
                    
                    // Issues
                    data.entry.strengthIssues?.let { issues ->
                        if (issues.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            issues.forEach { issue ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(issue, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    
                    // AI Recommendations (if stored, but currently model doesn't strictly persist AI text, 
                    // only metrics for AI to re-analyze or just issues list. 
                    // We added aiRecommendations list to PasswordEntry in Step 378? YES.)
                    data.entry.aiRecommendations?.let { recommendations ->
                         if (recommendations.isNotEmpty()) {
                             Spacer(modifier = Modifier.height(8.dp))
                             HorizontalDivider()
                             Spacer(modifier = Modifier.height(8.dp))
                             Text("Suggestions:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                             Spacer(modifier = Modifier.height(4.dp))
                             recommendations.forEach { rec ->
                                 Text("• $rec", style = MaterialTheme.typography.bodySmall)
                             }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // --- Actions ---
        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Entry")
        }
    }
}

@Composable
fun LabelValueRow(label: String, value: String, onCopy: (() -> Unit)? = null) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value, 
                style = MaterialTheme.typography.bodyLarge, 
                modifier = Modifier.weight(1f)
            )
            if (onCopy != null) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

private fun String.capitalize() = replaceFirstChar { it.uppercase() }
