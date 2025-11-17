package tn.esprit.dam.screens.comparison


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import tn.esprit.dam.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    viewModel: ComparisonViewModel,
    token: String,
    userHash: String,
    scanId1: String,
    scanId2: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(scanId1, scanId2) {
        viewModel.compareScans(token, userHash, scanId1, scanId2)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Comparison") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->

        when {
            // ✅ Loading
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Comparing scans...")
                    }
                }
            }

            // ✅ Error
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = {
                                viewModel.compareScans(token, userHash, scanId1, scanId2)
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            // ✅ Success
            uiState.comparison != null -> {
                ComparisonContent(
                    comparison = uiState.comparison!!,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@Composable
fun ComparisonContent(
    comparison: ComparisonData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ✅ Summary Card
        item {
            SummaryCard(comparison.summary)
        }

        // ✅ Scan Info Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScanInfoCard(
                    title = "Scan 1",
                    scanInfo = comparison.scan1,
                    modifier = Modifier.weight(1f)
                )
                ScanInfoCard(
                    title = "Scan 2",
                    scanInfo = comparison.scan2,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ✅ New Apps
        if (comparison.differences.newApps.isNotEmpty()) {
            item {
                DifferenceSection(
                    title = "New Apps (${comparison.differences.newApps.size})",
                    apps = comparison.differences.newApps,
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.Add
                )
            }
        }

        // ✅ Removed Apps
        if (comparison.differences.removedApps.isNotEmpty()) {
            item {
                DifferenceSection(
                    title = "Removed Apps (${comparison.differences.removedApps.size})",
                    apps = comparison.differences.removedApps,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Remove
                )
            }
        }

        // ✅ Score Changes
        if (comparison.differences.scoreChanges.isNotEmpty()) {
            item {
                Text(
                    text = "Score Changes (${comparison.differences.scoreChanges.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(comparison.differences.scoreChanges) { change ->
                ScoreChangeCard(change)
            }
        }

        // ✅ Unchanged Apps Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Text(
                        text = "${comparison.differences.unchangedApps.size} apps unchanged",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(summary: ComparisonSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Total Changes",
                    value = summary.totalChanges.toString(),
                    icon = Icons.Default.Sync
                )
                SummaryItem(
                    label = "Apps Added",
                    value = summary.appsAdded.toString(),
                    icon = Icons.Default.Add
                )
                SummaryItem(
                    label = "Apps Removed",
                    value = summary.appsRemoved.toString(),
                    icon = Icons.Default.Remove
                )
            }

            if (summary.avgScoreChange != 0) {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (summary.avgScoreChange > 0) {
                            Icons.Default.TrendingUp
                        } else {
                            Icons.Default.TrendingDown
                        },
                        contentDescription = null,
                        tint = if (summary.avgScoreChange > 0) {
                            Color.Green
                        } else {
                            Color.Red
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Avg Score Change: ${if (summary.avgScoreChange > 0) "+" else ""}${summary.avgScoreChange}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.avgScoreChange > 0) {
                            Color.Green
                        } else {
                            Color.Red
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ScanInfoCard(
    title: String,
    scanInfo: ScanInfo,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val date = remember(scanInfo.scanDate) {
        try {
            dateFormat.format(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    .parse(scanInfo.scanDate)
            )
        } catch (e: Exception) {
            scanInfo.scanDate
        }
    }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${scanInfo.totalApps} apps",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Score: ${scanInfo.avgScore}/100",
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    scanInfo.avgScore >= 80 -> Color.Green
                    scanInfo.avgScore >= 60 -> MaterialTheme.colorScheme.tertiary
                    else -> Color.Red
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DifferenceSection(
    title: String,
    apps: List<String>,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            apps.take(5).forEach { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(8.dp),
                        tint = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = app,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (apps.size > 5) {
                Text(
                    text = "... and ${apps.size - 5} more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ScoreChangeCard(change: ScoreChange) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (change.change > 0) {
                Color.Green.copy(alpha = 0.1f)
            } else {
                Color.Red.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = change.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = change.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Old Score
                Text(
                    text = "${change.oldScore}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                // New Score
                Text(
                    text = "${change.newScore}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                // Change
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (change.change > 0) Color.Green else Color.Red
                ) {
                    Text(
                        text = if (change.change > 0) "+${change.change}" else "${change.change}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}