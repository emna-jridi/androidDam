package tn.esprit.dam.features.scan.presentation.scan
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import tn.esprit.dam.features.scan.presentation.model.ScanState
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.core.ui.theme.AppColors

@Composable
fun ResultsContent(
    scanState: ScanState,
    analysisNote: String?,
    onViewDetails: (String) -> Unit,
    onNewScan: () -> Unit,
    onImportApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredApps = remember(scanState.selectedApps, selectedFilter) {
        when (selectedFilter) {
            "Risk Found" -> scanState.selectedApps.filter { app ->
                val risk = deriveAppRisk(app)
                risk.label in listOf("HIGH", "CRITICAL", "MEDIUM")
            }
            "Safe" -> scanState.selectedApps.filter { app ->
                val risk = deriveAppRisk(app)
                risk.label in listOf("LOW", "SAFE")
            }
            "System" -> scanState.selectedApps.filter { it.isSystemApp }
            else -> scanState.selectedApps
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PrivacyScoreGauge(
                score = scanState.averageScore.toInt(),
                high = scanState.highRiskCount,
                medium = scanState.mediumRiskCount,
                low = scanState.lowRiskCount,
                onNewScan = onNewScan
            )
        }

        if (scanState.mlRecommendations.isNotEmpty()) {
            item {
                MLRecommendationsCard(
                    recommendations = scanState.mlRecommendations,
                    riskFactors = scanState.mlRiskFactors,
                    safetyTips = scanState.mlSafetyTips
                )
            }
        }

        item {
            FilterTabs(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                counts = mapOf(
                    "All" to scanState.selectedApps.size,
                    "Risk Found" to scanState.selectedApps.count { deriveAppRisk(it).label in listOf("HIGH", "CRITICAL", "MEDIUM") },
                    "Safe" to scanState.selectedApps.count { deriveAppRisk(it).label in listOf("LOW", "SAFE") },
                    "System" to scanState.selectedApps.count { it.isSystemApp }
                )
            )
        }

        item {
            Text(
                "Analyzed Apps (${filteredApps.size})",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        items(filteredApps) { app ->
            val expanded = expandedMap[app.packageName] ?: false
            val appRisk = deriveAppRisk(app)
            AppResultCard(
                app = app,
                appRisk = appRisk,
                expanded = expanded,
                onToggleExpand = { expandedMap[app.packageName] = !expanded },
                onViewDetails = { onViewDetails(app.packageName) }
            )
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun PrivacyScoreGauge(
    score: Int,
    high: Int,
    medium: Int,
    low: Int,
    onNewScan: () -> Unit
) {
    val clampedScore = score.coerceIn(0, 100)
    val animatedScore by animateFloatAsState(
        targetValue = clampedScore / 100f,
        animationSpec = tween(durationMillis = 1400),
        label = "scoreGaugeProgress"
    )
    val animatedValue by animateFloatAsState(
        targetValue = clampedScore.toFloat(),
        animationSpec = tween(durationMillis = 1400),
        label = "scoreGaugeValue"
    )

    val scoreColor = when (clampedScore) {
        in 0..40 -> AppColors.riskHigh
        in 41..60 -> AppColors.warning
        in 61..80 -> AppColors.success
        else -> AppColors.success
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                stringResource(R.string.security_score_2),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.textSecondary,
                letterSpacing = 2.sp
            )

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    drawArc(
                        color = AppColors.surfaceVariant,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Canvas(modifier = Modifier.size(160.dp)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                AppColors.riskHigh,
                                AppColors.warning,
                                AppColors.success,
                                AppColors.success
                            )
                        ),
                        startAngle = 135f,
                        sweepAngle = 270f * animatedScore,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        animatedValue.toInt().toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    Text(
                        when (clampedScore) {
                            in 0..40 -> "Poor"
                            in 41..60 -> "Fair"
                            in 61..80 -> "Good"
                            else -> "Excellent"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = scoreColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RiskCountBadge(count = high, label = stringResource(R.string.high), color = AppColors.error)
                RiskCountBadge(count = medium, label = stringResource(R.string.med), color = AppColors.orange)
                RiskCountBadge(count = low, label = stringResource(R.string.low), color = AppColors.success)
            }

            Button(
                onClick = onNewScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.start_new_scan), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RiskCountBadge(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.textSecondary
        )
    }
}

@Composable
fun FilterTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    counts: Map<String, Int>
) {
    val filters = listOf("All", "Risk Found", "Safe", "System")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selectedFilter
            val count = counts[filter] ?: 0

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onFilterSelected(filter) },
                color = if (isSelected) AppColors.primary else AppColors.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        filter,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else AppColors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (count > 0) {
                        Text(
                            "($count)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else AppColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppResultCard(
    app: LocalAppInfo,
    appRisk: AppRiskData,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onViewDetails,
                onLongClick = onToggleExpand
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(AppColors.surfaceVariant, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Android,
                            contentDescription = null,
                            tint = AppColors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            app.displayName,
                            color = AppColors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${app.permissions.size} permissions • ${app.trackers.size} trackers",
                            color = AppColors.textSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(appRisk.color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${appRisk.score}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = appRisk.color
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = AppColors.surfaceVariant, thickness = 1.dp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (app.permissions.isNotEmpty()) {
                        Text(stringResource(R.string.sensitive_permissions), style = MaterialTheme.typography.bodySmall, color = AppColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        app.permissions.take(4).forEach {
                            Text("• $it", style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
                        }
                    }
                    if (app.trackers.isNotEmpty()) {
                        Text(stringResource(R.string.detected_trackers), style = MaterialTheme.typography.bodySmall, color = AppColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        app.trackers.take(4).forEach { tracker ->
                            Text("• ${tracker.name}", style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
                        }
                    }
                    TextButton(onClick = onViewDetails) {
                        Text(stringResource(R.string.view_details), color = AppColors.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

data class AppRiskData(val label: String, val color: Color, val score: Int)

fun deriveAppRisk(app: LocalAppInfo): AppRiskData {
    val risk = app.riskResult
    if (risk != null) {
        val color = when (risk.riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH -> AppColors.error
            RiskLevel.MEDIUM -> AppColors.orange
            RiskLevel.LOW, RiskLevel.SAFE -> AppColors.success
        }
        return AppRiskData(label = risk.riskLevel.label, color = color, score = risk.score)
    }
    return AppRiskData("Unknown", Color.Gray, 0)
}
