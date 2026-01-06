package tn.esprit.dam.features.darkweb.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tn.esprit.dam.data.model.Breach
import tn.esprit.dam.data.remote.darkweb.ManualBreachResult
import tn.esprit.dam.features.darkweb.DarkWebUiState
import tn.esprit.dam.features.darkweb.DarkWebViewModel
import tn.esprit.dam.ui.components.AppEmptyState
import tn.esprit.dam.ui.components.AppLoadingState
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkWebMonitoringScreen(
    navController: NavController,
    viewModel: DarkWebViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("My Breaches", "Manual Check")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        // Header with title and refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.surface)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Dark Web Monitoring",
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.checkNow() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Scan Now", tint = AppColors.primary)
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AppColors.surface,
            contentColor = AppColors.primary,
            indicator = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedTab == index
                Tab(
                    selected = selected,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            style = AppTypography.bodyMedium,
                            color = if (selected) AppColors.primary else AppColors.textSecondary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
        ) {
            if (selectedTab == 0) {
                when {
                    uiState.isLoading -> AppLoadingState()
                    uiState.breaches.isEmpty() -> AppEmptyState(
                        icon = Icons.Default.CheckCircle,
                        title = "No breaches detected",
                        message = "Your monitored accounts are safe"
                    )
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(AppSpacing.lg),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                        ) {
                            items(uiState.breaches) { breach ->
                                BreachCard(breach = breach, onClick = {
                                    navController.navigate("breach_detail/${breach._id}")
                                })
                            }
                        }
                    }
                }
            } else {
                // Reset manual state when switching to this tab
                LaunchedEffect(selectedTab) {
                    if (selectedTab == 1) {
                        viewModel.resetManualState()
                    }
                }
               ManualCheckContent(viewModel, uiState)
            }
        }
    }
}

@Composable
fun ManualCheckContent(viewModel: DarkWebViewModel, uiState: DarkWebUiState) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var checkedEmail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .padding(AppSpacing.lg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
    ) {
        // Email Check Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppCorners.large),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text("Check Email Address", style = AppTypography.titleMedium, color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                Text(
                    "Check if your email has been exposed in known data breaches.",
                    style = AppTypography.bodyMedium,
                    color = AppColors.textSecondary
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter Email") },
                    placeholder = { Text("example@email.com", color = AppColors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
                Button(
                    onClick = {
                        checkedEmail = email
                        viewModel.manualCheckEmail(email)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = email.isNotBlank() && !uiState.isLoading,
                    shape = RoundedCornerShape(AppCorners.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    if (uiState.isLoading && checkedEmail.isNotBlank()) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AppColors.textPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                    }
                    Text("Check Email", color = AppColors.textPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Email Results Section
        if (uiState.manualEmailResult != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppCorners.large),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.manualEmailResult!!.isEmpty()) 
                        AppColors.success.copy(alpha = 0.1f) 
                    else AppColors.error.copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (uiState.manualEmailResult!!.isEmpty()) AppColors.success.copy(alpha = 0.3f) 
                    else AppColors.error.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (uiState.manualEmailResult!!.isEmpty()) AppColors.success.copy(alpha = 0.2f) 
                                    else AppColors.error.copy(alpha = 0.2f),
                                    RoundedCornerShape(AppCorners.small)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (uiState.manualEmailResult!!.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (uiState.manualEmailResult!!.isEmpty()) AppColors.success else AppColors.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column {
                            Text(
                                if (uiState.manualEmailResult!!.isEmpty()) "Email is Safe!" else "Breaches Found!",
                                style = AppTypography.titleMedium,
                                color = if (uiState.manualEmailResult!!.isEmpty()) AppColors.success else AppColors.error,
                                fontWeight = FontWeight.Bold
                            )
                            if (checkedEmail.isNotBlank()) {
                                Text(
                                    checkedEmail,
                                    style = AppTypography.labelMedium,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                    }
                    
                    if (uiState.manualEmailResult!!.isEmpty()) {
                        Text(
                            "Good news! This email address was not found in any known data breaches.",
                            style = AppTypography.bodyMedium,
                            color = AppColors.textSecondary
                        )
                    } else {
                        Text(
                            "Found in ${uiState.manualEmailResult!!.size} data breaches:",
                            style = AppTypography.labelMedium,
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        // Show breaches as cards
                        uiState.manualEmailResult!!.forEach { breach ->
                            EmailBreachResultCard(breach)
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                        }
                    }
                }
            }
        }

        // Show error if any
        if (uiState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppCorners.medium),
                colors = CardDefaults.cardColors(containerColor = AppColors.error.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AppColors.error)
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Text("Error: ${uiState.error}", color = AppColors.error, style = AppTypography.bodyMedium)
                }
            }
        }

        // Password Check Section (K-Anonymity)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppCorners.large),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text("Check Password Safety", style = AppTypography.titleMedium, color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                Text(
                    "We check securely using k-anonymity. Only the first 5 characters of the SHA-1 hash are sent - your password never leaves your device.",
                    style = AppTypography.bodyMedium,
                    color = AppColors.textSecondary
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter Password") },
                    placeholder = { Text("Enter password to check", color = AppColors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
                Button(
                    onClick = { viewModel.manualCheckPassword(password) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = password.isNotBlank() && !uiState.isLoading,
                    shape = RoundedCornerShape(AppCorners.medium),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                ) {
                    Text("Check Password", color = AppColors.textPrimary, fontWeight = FontWeight.SemiBold)
                }

                // Password Result Card
                if (uiState.passwordCheckPerformed) {
                    val count = uiState.manualPasswordCount ?: 0
                    val isSafe = count == 0
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppCorners.medium),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSafe) AppColors.success.copy(alpha = 0.1f) else AppColors.error.copy(alpha = 0.1f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSafe) AppColors.success.copy(alpha = 0.3f) else AppColors.error.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(AppSpacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isSafe) AppColors.success.copy(alpha = 0.2f) else AppColors.error.copy(alpha = 0.2f),
                                        RoundedCornerShape(AppCorners.small)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isSafe) AppColors.success else AppColors.error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(AppSpacing.md))
                            Column {
                                Text(
                                    if (isSafe) "Password is Safe!" else "Password Compromised!",
                                    style = AppTypography.labelLarge,
                                    color = if (isSafe) AppColors.success else AppColors.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (isSafe) "Not found in any known data breaches." 
                                    else "Found $count times in data breaches. Change it immediately!",
                                    style = AppTypography.labelMedium,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailBreachResultCard(breach: ManualBreachResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCorners.medium),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    breach.name.ifBlank { "Unknown" },
                    style = AppTypography.labelLarge,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (breach.breachDate.isNotBlank()) {
                    Text(
                        breach.breachDate.substringBefore("T"),
                        style = AppTypography.labelMedium,
                        color = AppColors.textTertiary
                    )
                }
            }
            if (breach.domain.isNotBlank()) {
                Text(
                    breach.domain,
                    style = AppTypography.labelMedium,
                    color = AppColors.textSecondary
                )
            }
            if (breach.description.isNotBlank()) {
                Text(
                    breach.description,
                    style = AppTypography.labelMedium,
                    color = AppColors.textSecondary
                )
            }
            if (breach.dataClasses.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    breach.dataClasses.take(5).forEach { dataClass ->
                        Box(
                            modifier = Modifier
                                .background(AppColors.error.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = AppSpacing.sm, vertical = 2.dp)
                        ) {
                            Text(
                                dataClass,
                                style = AppTypography.labelMedium,
                                color = AppColors.error
                            )
                        }
                    }
                    if (breach.dataClasses.size > 5) {
                        Text(
                            "+${breach.dataClasses.size - 5} more",
                            style = AppTypography.labelMedium,
                            color = AppColors.textTertiary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Format ISO date string to human-readable format
 */
private fun formatBreachDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "Unknown date"
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        val cleanDate = isoDate.replace("Z", "").substringBefore(".")
        val date = inputFormat.parse(cleanDate)
        date?.let { outputFormat.format(it) } ?: isoDate.substringBefore("T")
    } catch (e: Exception) {
        isoDate.substringBefore("T")
    }
}

@Composable
fun BreachCard(breach: Breach, onClick: () -> Unit) {
    val resolved = breach.isResolved
    val accentColor = if (resolved) AppColors.success else AppColors.warning
    val description = breach.description?.take(180) ?: "No description available"
    val formattedDate = formatBreachDate(breach.breachDate)
    val exposedDataCount = breach.dataClasses.size

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (resolved) AppColors.success.copy(alpha = 0.3f) else AppColors.warning.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            // Header: Status badge + Source name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(AppCorners.small)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (resolved) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = breach.source,
                        style = AppTypography.titleMedium,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedDate,
                        style = AppTypography.labelMedium,
                        color = AppColors.textTertiary
                    )
                }
                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (resolved) AppColors.success.copy(alpha = 0.15f) else AppColors.warning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(AppCorners.small)
                        )
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                ) {
                    Text(
                        text = if (resolved) "Resolved" else "Action needed",
                        style = AppTypography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Description
            Text(
                text = if (breach.description?.length ?: 0 > 180) "$description..." else description,
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary,
                lineHeight = 20.sp
            )

            // Exposed data section
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    text = "Exposed data ($exposedDataCount types)",
                    style = AppTypography.labelMedium,
                    color = AppColors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
                // Data class chips in a flow layout (max 2 rows)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    maxItemsInEachRow = 3
                ) {
                    breach.dataClasses.take(6).forEach { dataClass ->
                        Box(
                            modifier = Modifier
                                .background(
                                    AppColors.surfaceVariant,
                                    RoundedCornerShape(AppCorners.small)
                                )
                                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                        ) {
                            Text(
                                text = dataClass,
                                style = AppTypography.labelMedium,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                    if (breach.dataClasses.size > 6) {
                        Box(
                            modifier = Modifier
                                .background(
                                    AppColors.primary.copy(alpha = 0.15f),
                                    RoundedCornerShape(AppCorners.small)
                                )
                                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                        ) {
                            Text(
                                text = "+${breach.dataClasses.size - 6} more",
                                style = AppTypography.labelMedium,
                                color = AppColors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
