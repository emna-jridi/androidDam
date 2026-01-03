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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import tn.esprit.dam.data.api.models.AppInfoDto
import tn.esprit.dam.data.api.models.AppScanHistoryDto
import tn.esprit.dam.data.api.models.StoreDataDto
import tn.esprit.dam.data.api.models.AnalysisResultDto
import tn.esprit.dam.features.scan.domain.SecurityUtils
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.ScanRiskResult
import tn.esprit.dam.utils.SecurityScoring
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
                history = uiState.history,
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
    history: List<AppScanHistoryDto>,
    onBackClick: () -> Unit
) {
    val riskResult = remember(app) {
        SecurityUtils.calculateAppRisk(
            permissions = app.permissions,
            trackers = app.trackers.map { it.name },
            isSystemApp = false
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            DetailHeader(onBackClick = onBackClick)
        }
        // Helper variable
        val lastScanDate = app.lastScanned

        // App Header
        item {
            AppHeaderCard(
                app = app,
                riskResult = riskResult,
                lastScanDate = lastScanDate,
                historyCount = history.size
            )
        }

        item {
            ScoreBreakdownCard(riskResult = riskResult)
        }

        // Store Data
        if (app.storeData != null) {
            item {
                StoreDataCard(storeData = app.storeData!!)
            }
        }

        // AI Analysis Results
        if (app.scanResults != null) {
            item {
                AIAnalysisCard(
                    scanResults = app.scanResults!!,
                    confidence = riskResult.confidence.label,
                    aiStatus = app.scanResults!!.aiStatus ?: "fallback"
                )
            }
        }

        // --- ENRICHED PERMISSION ANALYSIS ---
        val processedPermissions = app.permissions.map { SecurityUtils.analyzePermission(it) }
        val riskyPermissions = processedPermissions.filter { it.risk == RiskLevel.HIGH || it.risk == RiskLevel.CRITICAL }
        val otherPermissions = processedPermissions.filter { it.risk != RiskLevel.HIGH && it.risk != RiskLevel.CRITICAL }

        if (riskyPermissions.isNotEmpty()) {
            item {
                SectionTitle(title = "âš ï¸ Permissions Critiques (${riskyPermissions.size})", color = Color(0xFFEF4444))
            }
            items(riskyPermissions) { perm ->
                EnrichedPermissionCard(perm)
            }
        }

        if (otherPermissions.isNotEmpty()) {
            item {
                SectionTitle(title = "Permissions Standards (${otherPermissions.size})")
            }
            // Collapse others if too many
            if (otherPermissions.size > 5) {
                 items(otherPermissions.take(5)) { perm ->
                    EnrichedPermissionCard(perm)
                }
                item {
                    Text(
                        text = "+ ${otherPermissions.size - 5} autres permissions sÃ»res",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            } else {
                 items(otherPermissions) { perm ->
                    EnrichedPermissionCard(perm)
                }
            }
        }

        // --- ENRICHED TRACKER ANALYSIS ---
        if (app.trackers.isNotEmpty()) {
            val enrichedTrackers = app.trackers.map { tracker ->
                tracker to SecurityUtils.analyzeTracker(tracker.name)
            }
            
            item {
                SectionTitle(title = "Trackers & Publicité (${app.trackers.size})")
            }
            items(enrichedTrackers) { (tracker, analysis) ->
                EnrichedTrackerCard(tracker, analysis)
            }
        } else {
            // Reassuring message if no trackers
            item {
                SafeStateCard(
                    title = "Aucun tracker détecté",
                    message = "Cette application ne semble pas contenir de trackers publicitaires ou analytiques connus.",
                    icon = Icons.Default.CheckCircle
                )
            }
        }
        
        if (history.isNotEmpty()) {
            item {
                AppHistorySection(history)
            }
        }
    }
}

@Composable
private fun DetailHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Retour")
        }
        Text(
            text = "Détails de l'application",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AppHistorySection(history: List<AppScanHistoryDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Historique des scans (${history.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        history.forEach { entry ->
            HistoryEntryCard(entry)
        }
    }
}

@Composable
private fun HistoryEntryCard(entry: AppScanHistoryDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = formatHistoryDate(entry.scanDate),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Score: ${entry.score.toInt()}/100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            RiskLevelBadge(riskLevel = entry.riskLevel)
        }
    }
}

@Composable
private fun RiskLevelBadge(riskLevel: String) {
    val color = riskColor(riskLevel)
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = riskLevel.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun formatHistoryDate(dateString: String): String {
    return runCatching {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(dateString)
        val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH)
        date?.let { output.format(it) } ?: dateString
    }.getOrElse { dateString }
}

private fun riskColor(riskLevel: String): Color {
    return when (riskLevel.lowercase()) {
        "low" -> Color(0xFF4CAF50)
        "medium" -> Color(0xFFFF9800)
        "high" -> Color(0xFFF44336)
        "critical" -> Color(0xFF9C27B0)
        else -> Color(0xFF6366F1)
    }
}

@Composable
private fun AppHeaderCard(
    app: AppInfoDto,
    riskResult: ScanRiskResult,
    lastScanDate: String?,
    historyCount: Int
) {
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
            RiskLevelBadge(riskLevel = riskResult.riskLevel.toString())

            // App Icon
            AsyncImage(
                model = app.storeData?.icon ?: "",
                contentDescription = app.displayName ?: app.packageName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )

            // App Name
            Text(
                text = app.displayName ?: app.packageName,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                lastScanDate?.let { date ->
                    Text(
                        text = "Dernier scan: ${formatHistoryDate(date)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "Analyses: $historyCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
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
    riskResult: ScanRiskResult
) {
    val scoreColor = when (riskResult.riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH -> Color(0xFFEF4444)
            RiskLevel.MEDIUM -> Color(0xFFFB923C)
            RiskLevel.LOW -> Color(0xFF10B981)
            RiskLevel.SAFE -> Color(0xFF10B981)
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
                text = "Score de Sécurité",
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
                        text = "${riskResult.score}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = riskResult.riskLevel.label.uppercase(),
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
                progress = { riskResult.score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = scoreColor,
                trackColor = scoreColor.copy(alpha = 0.2f)
            )

            // Factors
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Facteurs de risque:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                
                ScoreFactorRow("Permissions", riskResult.permissionScore)
                ScoreFactorRow("Trackers & Pubs", riskResult.trackerScore)
            }
        }
    }
}

@Composable
private fun ScoreFactorRow(label: String, subScore: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "$subScore/100", 
            style = MaterialTheme.typography.bodySmall, 
            fontWeight = FontWeight.Bold,
            color = if(subScore < 70) Color(0xFFEF4444) else if (subScore < 90) Color(0xFFFB923C) else Color(0xFF10B981)
        )
    }
}

@Composable
private fun AIAnalysisCard(
    scanResults: AnalysisResultDto,
    confidence: String,
    aiStatus: String = "fallback"
) {
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
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
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                // Confidence Badge
                 Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Confiance: $confidence",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                // AI Status Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (aiStatus == "ok")
                        Color(0xFF4CAF50).copy(alpha = 0.2f)
                    else
                        Color(0xFFFF9800).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (aiStatus == "ok") "IA" else "Heur.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (aiStatus == "ok")
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFFF9800),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
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
private fun SectionTitle(title: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun EnrichedPermissionCard(analysis: SecurityUtils.PermissionAnalysis) {
    val isRisky = analysis.risk == RiskLevel.HIGH || analysis.risk == RiskLevel.CRITICAL
    val cardColor = if (isRisky) Color(0xFFEF4444).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isRisky) Color(0xFFEF4444).copy(alpha = 0.2f) else MaterialTheme.colorScheme.background,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRisky) Icons.Default.Warning else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isRisky) Color(0xFFEF4444) else Color(0xFF10B981)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = analysis.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = analysis.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                 Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(analysis.category.label)
                    if (isRisky) RiskChip(analysis.risk.label)
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun RiskChip(text: String) {
    Surface(
        color = Color(0xFFEF4444).copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = Color(0xFFEF4444)
        )
    }
}

@Composable
private fun EnrichedTrackerCard(
    tracker: tn.esprit.dam.data.api.models.SimpleTrackerInfo, 
    analysis: SecurityUtils.TrackerAnalysis
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFFFF9800)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = analysis.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = analysis.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                 Text(
                    text = analysis.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SafeStateCard(title: String, message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF10B981))
             Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                Text(message, style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857))
            }
        }
    }
}
