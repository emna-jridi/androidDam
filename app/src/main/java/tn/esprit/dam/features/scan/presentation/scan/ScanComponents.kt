package tn.esprit.dam.features.scan.presentation.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import tn.esprit.dam.features.scan.data.remote.dto.ScanLevel
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import tn.esprit.dam.features.scan.presentation.model.ScanState
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing

@Composable
fun ScanBottomBar(
    selectedCount: Int,
    onStartScan: () -> Unit
) {
    val canScan = selectedCount > 0

    Surface(
        color = AppColors.background,
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg)
        ) {
            if (!canScan) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AppSpacing.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = AppColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text(
                        "Select at least one application",
                        color = AppColors.textSecondary
                    )
                }
            }

            Button(
                onClick = onStartScan,
                enabled = canScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(AppCorners.large),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canScan) AppColors.primaryLight else AppColors.surfaceVariant,
                    disabledContainerColor = AppColors.surfaceVariant
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (canScan) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Column {
                        Text(
                            if (canScan) "Analyze Applications" else "Select apps",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (canScan) {
                            Text(
                                "$selectedCount app${if (selectedCount > 1) "s" else ""}",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionContent(
    availableApps: List<LocalAppInfo>,
    selectedCount: Int,
    error: String?,
    scanLevel: ScanLevel,
    onToggleApp: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onClearError: () -> Unit,
    onLevelChange: (ScanLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val userApps = availableApps.filterNot { it.isSystemApp }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeaderCard(
                selectedCount = selectedCount,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                scanLevel = scanLevel,
                onLevelChange = onLevelChange
            )
        }

        if (error != null) {
            item {
                ErrorCard(error, onClearError)
            }
        }

        items(userApps) { app ->
            AppRow(
                app = app,
                onToggle = { onToggleApp(app.packageName) }
            )
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        border = BorderStroke(
            1.dp,
            AppColors.error.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(AppCorners.large)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = AppColors.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onClear) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Clear error",
                    tint = AppColors.error
                )
            }
        }
    }
}

@Composable
fun HeaderCard(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    scanLevel: ScanLevel,
    onLevelChange: (ScanLevel) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            shape = RoundedCornerShape(AppCorners.large),
            border = BorderStroke(
                1.dp,
                if (selectedCount > 0) AppColors.primary.copy(alpha = 0.3f)
                else AppColors.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "$selectedCount application${if (selectedCount != 1) "s" else ""}",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Select the apps you want to analyze",
                        color = AppColors.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = if (selectedCount > 0) onDeselectAll else onSelectAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary
                    ),
                    shape = RoundedCornerShape(AppCorners.medium),
                    modifier = Modifier
                        .height(40.dp)
                        .padding(start = AppSpacing.md),
                    contentPadding = PaddingValues(horizontal = AppSpacing.md)
                ) {
                    Text(
                        if (selectedCount > 0) "Deselect All" else "Select All",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Text(
            "Scan Type",
            color = AppColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        ScanLevelToggle(scanLevel = scanLevel, onLevelChange = onLevelChange)
    }
}

@Composable
fun ScanLevelToggle(
    scanLevel: ScanLevel,
    onLevelChange: (ScanLevel) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LevelChip(
            label = "SMART",
            description = "Fast scan",
            selected = scanLevel == ScanLevel.SMART,
            onClick = { onLevelChange(ScanLevel.SMART) },
            modifier = Modifier.weight(1f)
        )
        LevelChip(
            label = "DEEP",
            description = "Thorough",
            selected = scanLevel == ScanLevel.DEEP,
            onClick = { onLevelChange(ScanLevel.DEEP) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LevelChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) Color(0xFF6366F1) else Color(0xFF1E293B).copy(alpha = 0.5f)
    val border = if (selected) Color(0xFF6366F1) else Color(0xFF64748B).copy(alpha = 0.3f)
    val textColor = if (selected) Color.White else AppColors.textSecondary

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.5.dp, border),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White.copy(alpha = 0.8f) else AppColors.textSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AppRow(app: LocalAppInfo, onToggle: () -> Unit) {
    val selectedBgColor = Color(0xFF6366F1).copy(alpha = 0.12f)
    val selectedBorderColor = Color(0xFF6366F1)
    val unselectedBg = Color(0xFF1E293B).copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Checkbox) { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (app.isSelected) selectedBgColor else unselectedBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (app.isSelected) 2.dp else 0.dp
        ),
        border = BorderStroke(
            width = if (app.isSelected) 1.5.dp else 0.5.dp,
            color = if (app.isSelected) selectedBorderColor.copy(alpha = 0.4f)
                    else Color(0xFF334155).copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (app.isSelected) selectedBorderColor.copy(alpha = 0.1f)
                        else Color(0xFF475569).copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                        contentDescription = app.displayName,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Text(
                        text = app.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (app.isSelected) selectedBorderColor else AppColors.textSecondary
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.displayName,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    color = AppColors.textSecondary.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (app.isSelected) selectedBorderColor
                        else Color(0xFF334155).copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (app.isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InitialLoadingScreen() {
    val infinite = rememberInfiniteTransition(label = "initialLoading")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 2000, easing = LinearEasing)),
        label = "shieldRotation"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulse),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.3f),
                                Color(0xFF6366F1).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }

                Canvas(modifier = Modifier.size(120.dp).rotate(rotation)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF6366F1),
                                Color(0xFF818CF8),
                                Color.Transparent
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(AppColors.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFF6366F1)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Text(
                "Preparing Security Scan",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Loading installed apps...",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
fun AnimatedLoadingState(
    totalApps: Int,
    scannedApps: Int,
    analysisNote: String?,
    modifier: Modifier = Modifier
) {
    val progress = if (totalApps > 0) scannedApps.toFloat() / totalApps.toFloat() else 0f
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 500),
        label = "progressAnimation"
    )

    val infinite = rememberInfiniteTransition(label = "loading")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 2500, easing = LinearEasing)),
        label = "rotation"
    )
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val percent = (animatedProgress * 100).toInt()
    val progressColor = when {
        percent < 30 -> Color(0xFFEF4444)
        percent < 70 -> Color(0xFFFB923C)
        else -> Color(0xFF10B981)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp).rotate(rotation)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF6366F1).copy(alpha = 0.5f),
                            Color(0xFF818CF8),
                            Color.Transparent
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 300f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Canvas(modifier = Modifier.size(150.dp)) {
                drawArc(
                    color = Color(0xFF334155),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Canvas(modifier = Modifier.size(150.dp)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(progressColor, progressColor.copy(alpha = 0.7f))
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulse)
                    .background(AppColors.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color(0xFF6366F1)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            "$percent%",
            style = MaterialTheme.typography.displaySmall,
            color = progressColor,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Security Analysis in Progress",
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "$scannedApps of $totalApps applications analyzed",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary
        )
    }
}

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
        in 0..40 -> Color(0xFFFF6B6B)
        in 41..60 -> Color(0xFFFFD93D)
        in 61..80 -> Color(0xFF6BCF7F)
        else -> Color(0xFF00D9A3)
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
                "SECURITY SCORE",
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
                        color = Color(0xFF334155),
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
                                Color(0xFFFF6B6B),
                                Color(0xFFFFD93D),
                                Color(0xFF6BCF7F),
                                Color(0xFF00D9A3)
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
                RiskCountBadge(count = high, label = "High", color = Color(0xFFEF4444))
                RiskCountBadge(count = medium, label = "Med", color = Color(0xFFFB923C))
                RiskCountBadge(count = low, label = "Low", color = Color(0xFF10B981))
            }

            Button(
                onClick = onNewScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
            ) {
                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start New Scan", fontWeight = FontWeight.SemiBold)
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
                color = if (isSelected) Color(0xFF6366F1) else AppColors.surfaceVariant,
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
                            tint = Color(0xFF6366F1),
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
                        Text("Sensitive Permissions", style = MaterialTheme.typography.bodySmall, color = AppColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        app.permissions.take(4).forEach {
                            Text("• $it", style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
                        }
                    }
                    if (app.trackers.isNotEmpty()) {
                        Text("Detected Trackers", style = MaterialTheme.typography.bodySmall, color = AppColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        app.trackers.take(4).forEach { tracker ->
                            Text("• ${tracker.name}", style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
                        }
                    }
                    TextButton(onClick = onViewDetails) {
                        Text("View Details", color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun MLRecommendationsCard(
    recommendations: List<String>,
    riskFactors: List<String>,
    safetyTips: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        shape = RoundedCornerShape(16.dp)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Recommendations",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AppColors.textSecondary
                )
            }

            if (recommendations.isNotEmpty()) {
                RecommendationRow(
                    text = recommendations.first(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    recommendations.drop(1).forEach { rec ->
                        RecommendationRow(
                            text = rec,
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF10B981)
                        )
                    }

                    if (riskFactors.isNotEmpty()) {
                        HorizontalDivider(color = AppColors.surfaceVariant, thickness = 1.dp)
                        Text(
                            "Risk Factors",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.SemiBold
                        )
                        riskFactors.forEach { factor ->
                            RecommendationRow(
                                text = factor,
                                icon = Icons.Default.Warning,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationRow(
    text: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textSecondary,
            lineHeight = 18.sp
        )
    }
}

data class AppRiskData(val label: String, val color: Color, val score: Int)

fun deriveAppRisk(app: LocalAppInfo): AppRiskData {
    val risk = app.riskResult
    if (risk != null) {
        val color = when (risk.riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH -> Color(0xFFEF4444)
            RiskLevel.MEDIUM -> Color(0xFFFB923C)
            RiskLevel.LOW, RiskLevel.SAFE -> Color(0xFF10B981)
        }
        return AppRiskData(label = risk.riskLevel.label, color = color, score = risk.score)
    }
    return AppRiskData("Unknown", Color.Gray, 0)
}
