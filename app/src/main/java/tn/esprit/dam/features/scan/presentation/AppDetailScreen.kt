package tn.esprit.dam.features.scan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import tn.esprit.dam.data.api.models.AppInfoDto
import tn.esprit.dam.data.api.models.StoreDataDto
import tn.esprit.dam.data.api.models.AnalysisResultDto

@Composable
fun AppDetailScreen(
    packageName: String,
    viewModel: AppDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    when {
        uiState.isLoading -> {
            LoadingScreen()
        }
        uiState.error != null -> {
            ErrorScreen(
                error = uiState.error!!,
                onRetry = { viewModel.refresh(packageName) }
            )
        }
        uiState.app != null -> {
            AppDetailContent(
                app = uiState.app!!,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Chargement des détails...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Réessayer")
            }
        }
    }
}

@Composable
private fun AppDetailContent(
    app: AppInfoDto,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App Header
        item {
            AppHeaderCard(app = app)
        }

        // Store Data
        if (app.storeData != null) {
            item {
                StoreDataCard(storeData = app.storeData!!)
            }
        }

        // Final Score Breakdown
        if (app.finalScore > 0) {
            item {
                ScoreBreakdownCard(
                    finalScore = app.finalScore.toInt(),
                    riskLevel = app.scanResults?.aiRiskLevel ?: "low"
                )
            }
        }

        // AI Analysis Results
        if (app.scanResults != null) {
            item {
                AIAnalysisCard(scanResults = app.scanResults!!)
            }
        }

        // Permissions
        if (app.permissions.isNotEmpty()) {
            item {
                SectionTitle(title = "Permissions détectées (${app.permissions.size})")
            }
            items(app.permissions.take(10)) { permission ->
                PermissionCard(permission = permission)
            }
            if (app.permissions.size > 10) {
                item {
                    Text(
                        text = "+ ${app.permissions.size - 10} autres permissions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Trackers
        if (app.trackers.isNotEmpty()) {
            item {
                SectionTitle(title = "Trackers identifiés (${app.trackers.size})")
            }
            items(app.trackers) { tracker ->
                TrackerCard(tracker = tracker)
            }
        }
    }
}

@Composable
private fun AppHeaderCard(app: AppInfoDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Icon
            AsyncImage(
                model = app.storeData?.icon ?: "",
                contentDescription = app.displayName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )

            // App Name
            Text(
                text = app.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Developer
            if (app.storeData?.developer != null) {
                Text(
                    text = app.storeData!!.developer!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            // Package Name
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StoreDataCard(storeData: StoreDataDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Rating
            if (storeData.rating != null) {
                StoreDataItem(
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFFB300),
                    value = String.format("%.1f", storeData.rating),
                    label = "Note"
                )
            }

            // Downloads
            if (storeData.downloads != null) {
                StoreDataItem(
                    icon = Icons.Default.PlayArrow,
                    iconColor = MaterialTheme.colorScheme.primary,
                    value = storeData.downloads!!,
                    label = "Téléchargements"
                )
            }

            // Version
            if (storeData.version != null) {
                StoreDataItem(
                    icon = Icons.Default.Info,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    value = storeData.version!!,
                    label = "Version"
                )
            }
        }
    }
}

@Composable
private fun StoreDataItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ScoreBreakdownCard(
    finalScore: Int,
    riskLevel: String
) {
    val scoreColor = when (riskLevel.lowercase()) {
        "low" -> Color(0xFF4CAF50)
        "medium" -> Color(0xFFFF9800)
        "high" -> Color(0xFFF44336)
        "critical" -> Color(0xFF9C27B0)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Score de Risque",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(scoreColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$finalScore",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = riskLevel.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = "Niveau de risque",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { finalScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = scoreColor,
                trackColor = scoreColor.copy(alpha = 0.2f)
            )

            // Breakdown explanation
            Text(
                text = "Score calculé: Ollama (70%) + Play Store (30%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun AIAnalysisCard(scanResults: AnalysisResultDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Analyse de Sécurité",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Summary
            Text(
                text = scanResults.aiSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            // Recommendations
            if (scanResults.aiRecommendations.isNotEmpty()) {
                Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                
                Text(
                    text = "Recommandations",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                scanResults.aiRecommendations.forEachIndexed { index, recommendation ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun PermissionCard(permission: String) {
    val isDangerous = permission.contains("CAMERA") ||
            permission.contains("LOCATION") ||
            permission.contains("CONTACTS") ||
            permission.contains("MICROPHONE") ||
            permission.contains("RECORD_AUDIO") ||
            permission.contains("SMS") ||
            permission.contains("CALL") ||
            permission.contains("STORAGE")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDangerous) 
                Color(0xFFF44336).copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDangerous) Icons.Default.Warning else Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isDangerous) Color(0xFFF44336) else Color(0xFF4CAF50)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permission.split(".").lastOrNull() ?: permission,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = permission,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (isDangerous) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF44336).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "DANGER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackerCard(tracker: tn.esprit.dam.data.api.models.SimpleTrackerInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color(0xFFFF9800)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tracker.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (tracker.riskLevel != null) {
                    Text(
                        text = "Risque: ${tracker.riskLevel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
