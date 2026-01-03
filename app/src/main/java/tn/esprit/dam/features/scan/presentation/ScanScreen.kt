package tn.esprit.dam.features.scan.presentation

import tn.esprit.dam.features.scan.domain.RiskLevel

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.features.scan.data.LocalAppInfo
import tn.esprit.dam.features.scan.data.ScanState
import tn.esprit.dam.features.scan.presentation.ScanTheme.Surface
import tn.esprit.dam.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    userId: String,
    deviceId: String,
    onNavigateToHome: () -> Unit,
    onNavigateToAppDetails: (String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val scanState by viewModel.scanState.collectAsState()
    val availableApps by viewModel.availableApps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize(userId, deviceId)
    }

        // Scaffold removed - AppNavGraph already provides topBar
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScanTheme.DarkBg)
        ) {

        when (scanState.status) {
            "ANALYZING" -> AnimatedLoadingState(
                totalApps = scanState.totalApps.coerceAtLeast(scanState.selectedApps.size.coerceAtLeast(1)),
                scannedApps = scanState.scannedApps,
                analysisNote = scanState.analysisNote,
                modifier = Modifier.weight(1f)
            )

            "COMPLETED" -> ResultsContent(
                scanState = scanState,
                analysisNote = scanState.analysisNote,
                onViewDetails = onNavigateToAppDetails,
                onNewScan = { viewModel.resetScan() },
                onImportApk = { /* APK import disabled on Scan screen */ },
                modifier = Modifier.weight(1f)
            )

            else -> SelectionContent(
                availableApps = availableApps,
                selectedCount = scanState.selectedApps.size,
                error = scanState.error,
                onToggleApp = { viewModel.toggleAppSelection(it) },
                onSelectAll = { viewModel.selectAllApps() },
                onDeselectAll = { viewModel.deselectAllApps() },
                onClearError = { viewModel.clearError() },
                modifier = Modifier.weight(1f)
            )
        }

            // Bottom CTA bar
            if (scanState.status == "IDLE") {
                ScanBottomBar(
                    selectedCount = scanState.selectedApps.size,
                    onStartScan = { viewModel.startScan() }
                )
            }
    }
}

/* -------------------------------------------------------------------------- */
/*  BOTTOM BAR (NEW â€“ FIXED CTA)                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun ScanBottomBar(
    selectedCount: Int,
    onStartScan: () -> Unit
) {
    val canScan = selectedCount > 0

    Surface(
        color = ScanTheme.DarkBg,
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            if (!canScan) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = ScanTheme.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Sélectionnez au moins une application",
                        color = ScanTheme.TextSecondary
                    )
                }
            }

            Button(
                onClick = onStartScan,
                enabled = canScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canScan) Color(0xFF6B7FBD) else Color(0xFF475569),
                    disabledContainerColor = Color(0xFF475569)
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (canScan) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Column {
                        Text(
                            if (canScan) "Analyser les applications"
                            else "Sélectionnez des apps",
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

/* -------------------------------------------------------------------------- */
/*  SELECTION CONTENT                                                         */
/* -------------------------------------------------------------------------- */

@Composable
private fun SelectionContent(
    availableApps: List<LocalAppInfo>,
    selectedCount: Int,
    error: String?,
    onToggleApp: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userApps = availableApps.filterNot { it.isSystemApp }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 140.dp // leave room for the fixed bottom CTA
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            HeaderCard(
                selectedCount = selectedCount,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll
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
private fun ErrorCard(
    message: String,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ScanTheme.CardBg
        ),
        border = BorderStroke(
            1.dp,
            Color(0xFFEF4444).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = Color(0xFFEF4444),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onClear) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Clear error",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*  SMALL COMPONENTS (UNCHANGED LOGIC)                                        */
/* -------------------------------------------------------------------------- */

@Composable
private fun HeaderCard(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
        shape = RoundedCornerShape(16.dp)
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
                    "$selectedCount application(s)",
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sélectionnez les apps à analyser",
                    color = ScanTheme.TextSecondary,
                    fontSize = 12.sp
                )
            }

            TextButton(
                onClick = if (selectedCount > 0) onDeselectAll else onSelectAll
            ) {
                Text(
                    if (selectedCount > 0) "Tout désélectionner" else "Tout",
                    color = Color(0xFF6B7FBD)
                )
            }
        }
    }
}
@Composable
private fun AppRow(app: LocalAppInfo, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ScanTheme.CornerLarge))
            .clickable(role = Role.Checkbox) { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (app.isSelected) Color(0xFF6B7FBD).copy(alpha = 0.14f) else ScanTheme.CardBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (app.isSelected) 6.dp else 3.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (app.isSelected) 2.dp else 1.dp,
            color = if (app.isSelected) Color(0xFF6B7FBD) else ScanTheme.Border.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanTheme.Spacing16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (app.isSelected) Color(0xFF6B7FBD).copy(alpha = 0.2f) else ScanTheme.SurfaceVariant,
                            shape = RoundedCornerShape(ScanTheme.CornerMedium)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Android,
                        contentDescription = null,
                        tint = if (app.isSelected) Color(0xFF6B7FBD) else ScanTheme.TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing4), modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.displayName,
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = app.packageName,
                        color = ScanTheme.TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CheckboxIcon(selected = app.isSelected)
        }
    }
}


@Composable
private fun StatPill(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .background(ScanTheme.SurfaceVariant, RoundedCornerShape(50))
            .padding(horizontal = ScanTheme.Spacing8, vertical = ScanTheme.Spacing4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing4)
    ) {
        Icon(icon, contentDescription = null, tint = ScanTheme.TextSecondary, modifier = Modifier.size(14.dp))
        Text(text, color = ScanTheme.TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CheckboxIcon(selected: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "checkboxScale"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF6B7FBD) else ScanTheme.SurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ScanTheme.Border.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun AnimatedLoadingState(
    totalApps: Int,
    scannedApps: Int,
    analysisNote: String?,
    modifier: Modifier = Modifier
) {
    val progress = if (totalApps > 0) scannedApps.toFloat() / totalApps.toFloat() else 0f
    val clampedProgress = progress.coerceIn(0f, 1f)
    val infinite = rememberInfiniteTransition(label = "loading")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1600, easing = LinearEasing)),
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScanTheme.DarkBg)
            .padding(ScanTheme.Spacing32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(116.dp)
                .background(ScanTheme.CardBg, shape = CircleShape)
                .padding(12.dp)
                .rotate(rotation),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF6366F1).copy(alpha = 0.35f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(72.dp), tint = Color(0xFF6366F1))
        }

        Spacer(Modifier.height(ScanTheme.Spacing32))
        Text("Analyse de sécurité en cours", style = MaterialTheme.typography.headlineMedium, color = ScanTheme.TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(ScanTheme.Spacing12))
        Text("$scannedApps/$totalApps applications analysées", style = MaterialTheme.typography.bodyMedium, color = ScanTheme.TextSecondary)

        Spacer(Modifier.height(ScanTheme.Spacing24))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(ScanTheme.CornerSmall))
                .background(ScanTheme.Surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clampedProgress)
                    .background(ScanTheme.GradientPrimary)
            )
        }

        Spacer(Modifier.height(ScanTheme.Spacing12))
        val percent = (clampedProgress * 100).toInt()
        Text("$percent%", style = MaterialTheme.typography.labelMedium, color = ScanTheme.TextSecondary)

        if (!analysisNote.isNullOrBlank()) {
            Spacer(Modifier.height(ScanTheme.Spacing16))
            Text(
                analysisNote,
                style = MaterialTheme.typography.bodySmall,
                color = ScanTheme.TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultsContent(
    scanState: ScanState,
    analysisNote: String?,
    onViewDetails: (String) -> Unit,
    onNewScan: () -> Unit,
    onImportApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScanTheme.DarkBg)
            .padding(horizontal = ScanTheme.Spacing20, vertical = ScanTheme.Spacing16),
        verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing16)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Résultats", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ScanTheme.TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onImportApk) { Text("Analyser un APK", color = Color(0xFF6366F1)) }
                TextButton(onClick = onNewScan) { Text("Nouveau scan", color = Color(0xFF6366F1)) }
            }
        }

        ScoreGauge(
            score = scanState.averageScore.toInt(),
            high = scanState.highRiskCount,
            medium = scanState.mediumRiskCount,
            low = scanState.lowRiskCount
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
        ) {
            StatChip(label = "Élevé", value = scanState.highRiskCount, color = Color(0xFFEF4444))
            StatChip(label = "Moyen", value = scanState.mediumRiskCount, color = Color(0xFFFB923C))
            StatChip(label = "Faible", value = scanState.lowRiskCount, color = Color(0xFF10B981))
        }

        if (!analysisNote.isNullOrBlank()) {
            AnalysisNoteCard(text = analysisNote)
        }

        Text("Applications analysées", style = MaterialTheme.typography.titleMedium, color = ScanTheme.TextPrimary, fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12)) {
            items(scanState.selectedApps) { app ->
                val expanded = expandedMap[app.packageName] ?: false
                val appRisk = deriveAppRisk(app)
                Card(
                    colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
                    shape = RoundedCornerShape(ScanTheme.CornerLarge),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(ScanTheme.CornerLarge))
                        .combinedClickable(
                            onClick = { onViewDetails(app.packageName) },
                            onLongClick = { expandedMap[app.packageName] = !expanded }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ScanTheme.Spacing16),
                        verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing4), modifier = Modifier.weight(1f)) {
                                Text(app.displayName, color = ScanTheme.TextPrimary, fontWeight = FontWeight.Bold)
                                Text(app.packageName, color = ScanTheme.TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            RiskBadgeChip(label = appRisk.label, score = appRisk.score, color = appRisk.color)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)) {
                            StatPill("Permissions: ${app.permissions.size}", Icons.Default.Warning)
                            StatPill("Trackers: ${app.trackers.size}", Icons.Default.CheckCircle)
                        }

                        if (expanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)) {
                                Text("Détails", style = MaterialTheme.typography.labelLarge, color = ScanTheme.TextSecondary)
                                if (app.permissions.isNotEmpty()) {
                                    Text("Permissions sensibles", style = MaterialTheme.typography.bodySmall, color = ScanTheme.TextPrimary, fontWeight = FontWeight.SemiBold)
                                    app.permissions.take(6).forEach {
                                        Text("\u2022 $it", style = MaterialTheme.typography.labelSmall, color = ScanTheme.TextSecondary)
                                    }
                                }
                                if (app.trackers.isNotEmpty()) {
                                    Text("Trackers détectés", style = MaterialTheme.typography.bodySmall, color = ScanTheme.TextPrimary, fontWeight = FontWeight.SemiBold)
                                    app.trackers.take(6).forEach { tracker ->
                                        Text("\u2022 ${tracker.name}", style = MaterialTheme.typography.labelSmall, color = ScanTheme.TextSecondary)
                                    }
                                }
                                if (app.permissions.isEmpty() && app.trackers.isEmpty()) {
                                    Text("Aucune information détaillée disponible", style = MaterialTheme.typography.labelSmall, color = ScanTheme.TextSecondary)
                                }

                                Spacer(Modifier.height(ScanTheme.Spacing8))
                                Text("Recommandations de sécurité", style = MaterialTheme.typography.bodySmall, color = ScanTheme.TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Aucune recommandation fournie pour cette application.", style = MaterialTheme.typography.labelSmall, color = ScanTheme.TextSecondary)
                                TextButton(onClick = { onViewDetails(app.packageName) }) {
                                    Text("Voir plus de détails", color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreGauge(score: Int, high: Int, medium: Int, low: Int) {
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

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(ScanTheme.CornerLarge),
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanTheme.Spacing24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing16)
        ) {
            Text("Analyse terminée", style = MaterialTheme.typography.titleLarge, color = ScanTheme.TextPrimary, fontWeight = FontWeight.Bold)
            Text(
                text = when (clampedScore) {
                    in 0..40 -> "Plusieurs menaces détectées"
                    in 41..70 -> "Sécurité correcte, surveillez les alertes"
                    else -> "Appareil bien sécurisé"
                },
                style = MaterialTheme.typography.bodySmall,
                color = ScanTheme.TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing24)
            ) {
                Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = animatedScore,
                        strokeWidth = 10.dp,
                        color = Color(0xFF6366F1),
                        trackColor = ScanTheme.SurfaceVariant,
                        modifier = Modifier.size(110.dp)
                    )
                    Text(animatedValue.toInt().toString(), style = MaterialTheme.typography.displaySmall, color = ScanTheme.TextPrimary, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12)) {
                    StatItem("Élevé", high, Color(0xFFEF4444))
                    StatItem("Moyen", medium, Color(0xFFFB923C))
                    StatItem("Faible", low, Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text("$count $label", style = MaterialTheme.typography.bodySmall, color = ScanTheme.TextSecondary)
    }
}

@Composable
private fun StatChip(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = ScanTheme.Spacing12, vertical = ScanTheme.Spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text("$value $label", color = ScanTheme.TextPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AnalysisNoteCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ScanTheme.SurfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF6366F1))
            Text(text, color = ScanTheme.TextPrimary, style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
private fun RiskBadgeChip(label: String, score: Int, color: Color) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = ScanTheme.Spacing12, vertical = ScanTheme.Spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Text("$score/100", color = ScanTheme.TextPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        Text(label, color = ScanTheme.TextSecondary, style = MaterialTheme.typography.labelSmall)
    }
}

private data class AppRisk(val label: String, val color: Color, val score: Int)

private fun deriveAppRisk(app: LocalAppInfo): AppRisk {
    // Use pre-calculated risk result if available
    val risk = app.riskResult
    
    if (risk != null) {
        val color = when (risk.riskLevel) {
            RiskLevel.CRITICAL, RiskLevel.HIGH -> Color(0xFFEF4444)
            RiskLevel.MEDIUM -> Color(0xFFFB923C)
            RiskLevel.LOW -> Color(0xFF10B981)
            RiskLevel.SAFE -> Color(0xFF10B981)
        }
        return AppRisk(label = risk.riskLevel.label, color = color, score = risk.score)
    }

    // Fallback if risk result is missing (should not happen with new logic)
    return AppRisk("Inconnu", Color.Gray, 0)
}
