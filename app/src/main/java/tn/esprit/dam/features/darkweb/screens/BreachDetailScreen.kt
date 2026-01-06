package tn.esprit.dam.features.darkweb.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.LightbulbCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tn.esprit.dam.features.darkweb.DarkWebViewModel
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography

/**
 * Format ISO date string to human-readable format
 */
private fun formatBreachDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "Unknown date"
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
        val cleanDate = isoDate.replace("Z", "").substringBefore(".")
        val date = inputFormat.parse(cleanDate)
        date?.let { outputFormat.format(it) } ?: isoDate.substringBefore("T")
    } catch (e: Exception) {
        isoDate.substringBefore("T")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreachDetailScreen(
    navController: NavController,
    breachId: String,
    viewModel: DarkWebViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val breach = uiState.breaches.find { it._id == breachId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        // Custom Header (no Scaffold/TopAppBar to avoid double app bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.surface)
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text(
                text = breach?.source ?: "Breach Details",
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        if (breach == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Breach not found",
                    style = AppTypography.bodyMedium,
                    color = AppColors.textSecondary
                )
            }
        } else {
            val resolved = breach.isResolved
            val accentColor = if (resolved) AppColors.success else AppColors.warning
            val formattedDate = formatBreachDate(breach.breachDate)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
                // Status Card
                Card(
                    shape = RoundedCornerShape(AppCorners.large),
                    colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(AppCorners.medium)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (resolved) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(AppSpacing.md))
                        Column {
                            Text(
                                text = if (resolved) "RESOLVED" else "ACTION REQUIRED",
                                style = AppTypography.labelLarge,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            Text(
                                text = "Breach Date: $formattedDate",
                                style = AppTypography.bodyMedium,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }

                // Exposed Data Section
                Card(
                    shape = RoundedCornerShape(AppCorners.large),
                    colors = CardDefaults.cardColors(containerColor = AppColors.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = AppColors.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.sm))
                            Text(
                                "Exposed Data (${breach.dataClasses.size} types)",
                                style = AppTypography.titleMedium,
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // FlowRow for chips - no overflow
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            breach.dataClasses.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            AppColors.error.copy(alpha = 0.1f),
                                            RoundedCornerShape(AppCorners.small)
                                        )
                                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                                ) {
                                    Text(
                                        text = item,
                                        style = AppTypography.labelMedium,
                                        color = AppColors.error
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Security Insight Card
                Card(
                    shape = RoundedCornerShape(AppCorners.large),
                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AppColors.primary.copy(alpha = 0.15f), RoundedCornerShape(AppCorners.small)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LightbulbCircle,
                                    contentDescription = null,
                                    tint = AppColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(AppSpacing.sm))
                            Text(
                                "AI Security Insight",
                                style = AppTypography.titleMedium,
                                color = AppColors.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            buildSecurityInsight(breach.source, breach.dataClasses),
                            style = AppTypography.bodyMedium,
                            color = AppColors.textSecondary,
                            lineHeight = 22.sp
                        )

                        // Recommendations
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            Text(
                                "Recommended Actions:",
                                style = AppTypography.labelMedium,
                                color = AppColors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            getRecommendations(breach.dataClasses).forEach { rec ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("•", color = AppColors.primary, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                                    Text(
                                        rec,
                                        style = AppTypography.bodyMedium,
                                        color = AppColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Mark as Resolved Button
                if (!resolved) {
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Button(
                        onClick = { viewModel.resolveBreach(breach._id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(AppCorners.medium),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.textPrimary)
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Text(
                            "Mark as Resolved",
                            style = AppTypography.labelLarge,
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Build contextual security insight based on exposed data types
 */
private fun buildSecurityInsight(source: String, dataClasses: List<String>): String {
    val hasPasswords = dataClasses.any { it.contains("password", ignoreCase = true) }
    val hasEmails = dataClasses.any { it.contains("email", ignoreCase = true) }
    val hasPhone = dataClasses.any { it.contains("phone", ignoreCase = true) }
    val hasFinancial = dataClasses.any { it.contains("credit", ignoreCase = true) || it.contains("bank", ignoreCase = true) }
    
    return buildString {
        append("This breach from $source exposed ${dataClasses.size} types of personal data. ")
        when {
            hasPasswords -> append("Your login credentials were compromised, which puts you at high risk of credential stuffing attacks. ")
            hasFinancial -> append("Financial information was exposed, which could lead to fraud or identity theft. ")
            hasEmails && hasPhone -> append("Contact information was leaked, increasing risk of phishing and social engineering. ")
            else -> append("This data could be used for identity verification bypasses or targeted attacks. ")
        }
        append("Take immediate action to protect your accounts.")
    }
}

/**
 * Get contextual recommendations based on exposed data
 */
private fun getRecommendations(dataClasses: List<String>): List<String> {
    val recommendations = mutableListOf<String>()
    
    if (dataClasses.any { it.contains("password", ignoreCase = true) }) {
        recommendations.add("Change your password immediately and use a unique, strong password")
    }
    if (dataClasses.any { it.contains("email", ignoreCase = true) }) {
        recommendations.add("Be vigilant for phishing emails impersonating this service")
    }
    if (dataClasses.any { it.contains("phone", ignoreCase = true) }) {
        recommendations.add("Watch for suspicious calls or SMS messages")
    }
    recommendations.add("Enable two-factor authentication (2FA) if available")
    recommendations.add("Monitor your accounts for unauthorized activity")
    
    return recommendations.take(4)
}
