package tn.esprit.dam.screens.appdetails
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import tn.esprit.dam.data.model.AppDetails
import tn.esprit.dam.data.model.PermissionsInfo
import tn.esprit.dam.data.model.TrackersInfo
import tn.esprit.dam.data.model.FlagsInfo
import tn.esprit.dam.data.model.AppStats
import tn.esprit.dam.data.model.AlternativeApp

import tn.esprit.dam.ui.components.common.ErrorState
import tn.esprit.dam.ui.components.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    packageName: String,
    onBack: () -> Unit
) {
    val viewModel: AppDetailsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de l'application") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when (uiState) {

                is AppDetailsUiState.Loading ->
                    LoadingState()

                is AppDetailsUiState.Error ->
                    ErrorState(
                        message = (uiState as AppDetailsUiState.Error).message,
                        onRetry = { viewModel.loadDetails() }
                    )

                is AppDetailsUiState.Success ->
                    AppDetailsContent(
                        app = (uiState as AppDetailsUiState.Success).app
                    )
            }
        }
    }
}

@Composable
fun AppDetailsContent(app: AppDetails) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item { HeaderCard(app) }

        if (!app.recommendations.isNullOrEmpty()) {
            item {
                AlertsCard(recommendations = app.recommendations)
            }
        }

        item { StatsCard(app) }

        app.permissions?.let { perms ->
            item { PermissionsCard(perms) }
        }

        app.trackers?.let { trackers ->
            item { TrackersCard(trackers) }
        }

        app.flags?.let { flags ->
            item { FlagsCard(flags) }
        }

        if (!app.alternatives.isNullOrEmpty()) {
            item {
                AlternativesCard(
                    currentApp = app.name,
                    alternatives = app.alternatives
                )
            }
        }

        app.stats?.let { stats ->
            item { CommunityStatsCard(stats) }
        }
    }
}
// ===================================================================
// HEADER CARD
// ===================================================================

@Composable
fun HeaderCard(app: AppDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (app.developer.isNotEmpty()) {
                        Text(
                            text = app.developer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (app.category.isNotEmpty()) {
                        Text(
                            text = "📱 ${app.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (app.version.isNotEmpty()) {
                        Text(
                            text = "Version ${app.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PrivacyScoreBadge(
                        score = app.privacyScore,
                        riskLevel = app.riskLevel
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.riskLevel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (app.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = app.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===================================================================
// PRIVACY SCORE BADGE
// ===================================================================

@Composable
fun PrivacyScoreBadge(score: Int, riskLevel: String) {
    val color = when (riskLevel.uppercase()) {
        "LOW" -> Color(0xFF2ECC71)
        "MEDIUM" -> Color(0xFFF39C12)
        "HIGH" -> Color(0xFFE67E22)
        "CRITICAL" -> Color(0xFFE74C3C)
        else -> Color.Gray
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .border(3.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "/100",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

// ===================================================================
// ALERTS CARD
// ===================================================================

@Composable
fun AlertsCard(recommendations: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recommandations (${recommendations.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ===================================================================
// STATS CARD
// ===================================================================

@Composable
fun StatsCard(app: AppDetails) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Vue d'ensemble",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Lock,
                    label = "Permissions",
                    value = "${app.permissions?.total ?: 0}",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Default.Analytics,
                    label = "Trackers",
                    value = "${app.trackers?.total ?: 0}",
                    color = MaterialTheme.colorScheme.error
                )
                StatItem(
                    icon = Icons.Default.Warning,
                    label = "Dangereuses",
                    value = "${app.permissions?.dangerous?.size ?: 0}",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ===================================================================
// PERMISSIONS CARD
// ===================================================================

@Composable
fun PermissionsCard(permissions: PermissionsInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge { Text("${permissions.total}") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (permissions.dangerous.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚠️ Permissions dangereuses (${permissions.dangerous.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        permissions.dangerous.take(5).forEach { perm ->
                            Text(
                                text = "• ${formatPermission(perm)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        if (permissions.dangerous.size > 5) {
                            Text(
                                text = "+ ${permissions.dangerous.size - 5} autres...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Toutes les permissions (${permissions.total})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            permissions.dangerous.take(10).forEach { perm ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (permissions.dangerous.contains(perm)) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatPermission(perm),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (permissions.dangerous.size > 10) {
                Text(
                    text = "+ ${permissions.dangerous.size - 10} autres permissions...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===================================================================
// TRACKERS CARD
// ===================================================================

@Composable
fun TrackersCard(trackers: TrackersInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trackers détectés",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = if (trackers.total > 5)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text("${trackers.total}")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (trackers.total == 0) {

                Text(
                    text = "✅ Aucun tracker détecté",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2ECC71)
                )

            } else {

                trackers.list.forEachIndexed { index, tracker ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tracker,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (index < trackers.list.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

// ===================================================================
// FLAGS CARD
// ===================================================================

@Composable
fun FlagsCard(flags: FlagsInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Flags de sécurité",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlagRow(
                label = "Application debuggable",
                value = flags.isDebuggable,
                trueIsGood = false
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            FlagRow(
                label = "Trackers inconnus détectés",
                value = flags.hasUnknownTrackers,
                trueIsGood = false
            )
        }
    }
}

@Composable
fun FlagRow(
    label: String,
    value: Boolean,
    trueIsGood: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(text = label, style = MaterialTheme.typography.bodyMedium)

        val pair: Pair<ImageVector, Color> =
            if (value) {
                if (trueIsGood)
                    Icons.Default.CheckCircle to Color(0xFF2ECC71)
                else
                    Icons.Default.Cancel  to MaterialTheme.colorScheme.error
            } else {
                if (trueIsGood)
                    Icons.Default.Cancel  to MaterialTheme.colorScheme.error
                else
                    Icons.Default.Close to Color(0xFF2ECC71)
            }

        val icon = pair.first
        val color = pair.second

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (value) "Oui" else "Non",
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ===================================================================
// ALTERNATIVES CARD
// ===================================================================

@Composable
fun AlternativesCard(
    currentApp: String,
    alternatives: List<AlternativeApp>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2ECC71).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color(0xFF2ECC71),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Alternatives plus sûres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            alternatives.forEachIndexed { i, alt ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
                                text = alt.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = alt.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${alt.privacyScore}/100",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF2ECC71),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+${alt.improvement} pts",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2ECC71)
                            )
                        }
                    }
                }

                if (i < alternatives.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// ===================================================================
// COMMUNITY STATS CARD
// ===================================================================

@Composable
fun CommunityStatsCard(stats: AppStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Statistiques communautaires",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        text = "Scans totaux",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.totalScans}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                stats.avgScoreFromCommunity?.let { avg ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Score moyen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$avg/100",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            stats.lastScanned?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dernier scan: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ===================================================================
// UTIL
// ===================================================================

fun formatPermission(permission: String): String {
    return permission
        .replace("android.permission.", "")
        .replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}
