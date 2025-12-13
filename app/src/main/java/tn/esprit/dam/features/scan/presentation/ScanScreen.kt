package tn.esprit.dam.features.scan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.features.scan.data.LocalAppInfo
import tn.esprit.dam.features.scan.data.ScanState

@Composable
fun ScanScreen(
    userId: String,
    deviceId: String,
    onNavigateToHome: () -> Unit,
    onNavigateToAppDetails: (packageName: String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val scanState by viewModel.scanState.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize(userId, deviceId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF1A1F3A)
                    )
                )
            )
    ) {
        when {
            scanState.status == "COMPLETED" -> {
                ScanResultsContent(
                    scanState = scanState,
                    selectedApps = scanState.selectedApps,
                    onViewDetails = onNavigateToAppDetails,
                    onNewScan = { viewModel.resetScan() }
                )
            }

            scanState.status == "ANALYZING" -> {
                ScanProgressContent(
                    scanState = scanState,
                    totalApps = scanState.selectedApps.size
                )
            }

            else -> {
                ScanSelectionContent(
                    availableApps = availableApps,
                    selectedCount = scanState.selectedApps.size,
                    onToggleApp = { viewModel.toggleAppSelection(it) },
                    onSelectAll = { viewModel.selectAllApps() },
                    onDeselectAll = { viewModel.deselectAllApps() },
                    onStartScan = {
                        viewModel.startScan()
                    },
                    error = scanState.error,
                    onErrorDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
private fun ScanSelectionContent(
    availableApps: List<LocalAppInfo>,
    selectedCount: Int,
    onToggleApp: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onStartScan: () -> Unit,
    error: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.nav_scan),
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Sélectionnez les applications à analyser",
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Selection Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount applications sélectionnées",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSelectAll,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED)
                    )
                ) {
                    Text("Tout sélectionner", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                }
                Button(
                    onClick = onDeselectAll,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4B5563)
                    )
                ) {
                    Text("Effacer", fontSize = MaterialTheme.typography.labelSmall.fontSize)
                }
            }
        }

        Divider(color = Color(0xFF2D3748), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

        // Apps List - Only user-installed apps
        if (availableApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune application disponible",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                items(availableApps) { app ->
                    AppListItem(
                        app = app,
                        isSelected = app.isSelected,
                        onToggle = { onToggleApp(app.packageName) }
                    )
                }
            }
        }

        // Error message
        if (error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626).copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFFCA5A5),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onErrorDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626))
                    }
                }
            }
        }

        // Start Scan Button
        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C3AED)
            ),
            enabled = selectedCount > 0
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Commencer l'analyse",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.labelLarge.fontSize
            )
        }
    }
}

@Composable
private fun AppListItem(
    app: LocalAppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.2f)
            else Color(0xFF2D3748)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 0.5.dp,
            color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF4B5563)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.displayName,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (app.category != null) {
                        Text(
                            text = app.category!!,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    if (app.isSystemApp) {
                        Text(
                            text = "Système",
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF7C3AED)
                )
            )
        }
    }
}

@Composable
private fun ScanProgressContent(
    scanState: ScanState,
    totalApps: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(120.dp),
            color = Color(0xFF7C3AED),
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Analyse en cours...",
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${scanState.scannedApps}/$totalApps applications analysées",
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        LinearProgressIndicator(
            progress = if (totalApps > 0) (scanState.scannedApps.toFloat() / totalApps) else 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF2D3748), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun ScanResultsContent(
    scanState: ScanState,
    selectedApps: List<LocalAppInfo>,
    onViewDetails: (packageName: String) -> Unit,
    onNewScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                        )
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.scan_results_title),
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(id = R.string.scan_apps_analyzed, scanState.scannedApps),
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Risk Summary Cards
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RiskSummaryCard(
                        title = stringResource(id = R.string.level_high),
                        count = scanState.highRiskCount,
                        color = Color(0xDCDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    RiskSummaryCard(
                        title = stringResource(id = R.string.level_medium),
                        count = scanState.mediumRiskCount,
                        color = Color(0xFFA16207),
                        modifier = Modifier.weight(1f)
                    )
                    RiskSummaryCard(
                        title = stringResource(id = R.string.level_low),
                        count = scanState.lowRiskCount,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Overall Score
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Score moyen",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                            Text(
                                text = "${scanState.averageScore.toInt()}/100",
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                fontWeight = FontWeight.Bold,
                                color = getRiskColor(scanState.averageScore)
                            )
                        }

                        CircularProgressIndicator(
                            progress = scanState.averageScore / 100f,
                            modifier = Modifier.size(80.dp),
                            color = getRiskColor(scanState.averageScore),
                            strokeWidth = 6.dp
                        )
                    }
                }

                // Scanned Apps List Header
                Text(
                    text = "Applications analysées",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                )
            }
        }

        // Apps List Items
        items(selectedApps) { app ->
            AppResultItem(
                appName = app.displayName,
                packageName = app.packageName,
                onClick = { onViewDetails(app.packageName) }
            )
        }

        // New Scan Button
        item {
            Button(
                onClick = onNewScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nouveau scan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RiskSummaryCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AppResultItem(
    appName: String,
    packageName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = packageName,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

fun getRiskColor(score: Float): Color {
    return when {
        score >= 80 -> Color(0xFF16A34A) // Low risk (good)
        score >= 50 -> Color(0xFFA16207) // Medium risk
        else -> Color(0xFFDC2626) // High risk (bad)
    }
}
