package tn.esprit.dam.features.scan.presentation

import tn.esprit.dam.features.scan.domain.RiskLevel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.features.scan.data.LocalAppInfo
import tn.esprit.dam.features.scan.data.ScanState
import tn.esprit.dam.data.api.models.ScanLevel
import tn.esprit.dam.features.scan.presentation.ScanTheme.Surface
import tn.esprit.dam.ui.theme.Surface
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppCorners
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppTypography
import tn.esprit.dam.ui.components.AppLoadingState

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
    
    // Show initial loading screen ONLY while apps are being fetched (very first load)
    if ((scanState.status == "LOADING" || (scanState.status == "IDLE" && availableApps.isEmpty())) && 
        !scanState.scanCompleted) {
        InitialLoadingScreen()
        return
    }
    
    // Scaffold removed - AppNavGraph already provides topBar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
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
                scanLevel = scanState.scanLevel,
                onToggleApp = { viewModel.toggleAppSelection(it) },
                onSelectAll = { viewModel.selectAllApps() },
                onDeselectAll = { viewModel.deselectAllApps() },
                onClearError = { viewModel.clearError() },
                onLevelChange = { viewModel.setScanLevel(it) },
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
                        tint = ScanTheme.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text(
                        "Select at least one application",
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
                            if (canScan) "Analyze Applications"
                            else "Select apps",
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
            bottom = 140.dp // leave room for the fixed bottom CTA
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

/* -------------------------------------------------------------------------- */
/*  SMALL COMPONENTS (UNCHANGED LOGIC)                                        */
/* -------------------------------------------------------------------------- */

@Composable
private fun HeaderCard(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    scanLevel: ScanLevel,
    onLevelChange: (ScanLevel) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        // Selection status card
        Card(
            colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
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
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Select the apps you want to analyze",
                        color = ScanTheme.TextSecondary,
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
        
        // Scan level toggle
        Text(
            "Scan Type",
            color = ScanTheme.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        ScanLevelToggle(scanLevel = scanLevel, onLevelChange = onLevelChange)
    }
}

@Composable
private fun ScanLevelToggle(
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
private fun LevelChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) Color(0xFF6366F1) else Color(0xFF1E293B).copy(alpha = 0.5f)
    val border = if (selected) Color(0xFF6366F1) else Color(0xFF64748B).copy(alpha = 0.3f)
    val textColor = if (selected) Color.White else ScanTheme.TextSecondary

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
                color = if (selected) Color.White.copy(alpha = 0.8f) else ScanTheme.TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}
@Composable
private fun AppRow(app: LocalAppInfo, onToggle: () -> Unit) {
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
                    else ScanTheme.Border.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon - Real icon from system or fallback
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
                    // Fallback: Show first letter of app name
                    Text(
                        text = app.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (app.isSelected) selectedBorderColor else ScanTheme.TextSecondary
                    )
                }
            }
            
            // App Name and Package
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp), 
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.displayName,
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    color = ScanTheme.TextSecondary.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Selection indicator - cleaner design
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

/* -------------------------------------------------------------------------- */
/*  INITIAL LOADING SCREEN - Full screen loading before app list              */
/* -------------------------------------------------------------------------- */

@Composable
private fun InitialLoadingScreen() {
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
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val dotAlpha1 by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dotAlpha2 by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 200, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dotAlpha3 by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = 400, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScanTheme.DarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Shield Icon with glow
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulse),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
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
                
                // Rotating arc
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
                
                // Inner circle background
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(ScanTheme.CardBg, CircleShape),
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
            
            // Title
            Text(
                "Preparing Security Scan",
                style = MaterialTheme.typography.headlineSmall,
                color = ScanTheme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Animated loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Loading installed apps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ScanTheme.TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(dotAlpha1)
                        .background(Color(0xFF6366F1), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(dotAlpha2)
                        .background(Color(0xFF6366F1), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(dotAlpha3)
                        .background(Color(0xFF6366F1), CircleShape)
                )
            }
            
            Spacer(Modifier.height(48.dp))
            
            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f),
                colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "Scanning your device",
                            style = MaterialTheme.typography.labelLarge,
                            color = ScanTheme.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "We're detecting all installed applications to analyze their security",
                            style = MaterialTheme.typography.bodySmall,
                            color = ScanTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*  ENHANCED ANIMATED LOADING STATE - During scan analysis                    */
/* -------------------------------------------------------------------------- */

@Composable
private fun AnimatedLoadingState(
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
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val scanLine by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing)
        ),
        label = "scanLine"
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
            .background(ScanTheme.DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Main animated icon with circular progress
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer rotating ring
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
            
            // Progress arc background
            Canvas(modifier = Modifier.size(150.dp)) {
                drawArc(
                    color = Color(0xFF334155),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // Progress arc
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
            
            // Inner circle with icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulse)
                    .background(ScanTheme.CardBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Scanning line effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .offset(y = (120.dp * scanLine) - 60.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF6366F1).copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color(0xFF6366F1)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        
        // Percentage display
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
            color = ScanTheme.TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            "$scannedApps of $totalApps applications analyzed",
            style = MaterialTheme.typography.bodyMedium,
            color = ScanTheme.TextSecondary
        )

        Spacer(Modifier.height(32.dp))
        
        // Progress bar with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF818CF8),
                                progressColor
                            )
                        )
                    )
            )
        }

        Spacer(Modifier.height(32.dp))

        // Analysis stages card
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnalysisStageRow(
                    icon = Icons.Default.Android,
                    label = "Detecting permissions",
                    isActive = percent < 40,
                    isComplete = percent >= 40
                )
                AnalysisStageRow(
                    icon = Icons.Default.Shield,
                    label = "Scanning for trackers",
                    isActive = percent in 40..69,
                    isComplete = percent >= 70
                )
                AnalysisStageRow(
                    icon = Icons.Default.Psychology,
                    label = "AI threat analysis",
                    isActive = percent >= 70,
                    isComplete = percent >= 100
                )
            }
        }

        if (!analysisNote.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        analysisNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = ScanTheme.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisStageRow(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    isComplete: Boolean
) {
    val infinite = rememberInfiniteTransition(label = "stageAnim")
    val alpha by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "stageAlpha"
    )
    
    val iconColor = when {
        isComplete -> Color(0xFF10B981)
        isActive -> Color(0xFF6366F1)
        else -> ScanTheme.TextSecondary.copy(alpha = 0.5f)
    }
    
    val textColor = when {
        isComplete -> ScanTheme.TextPrimary
        isActive -> ScanTheme.TextPrimary
        else -> ScanTheme.TextSecondary.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isActive) alpha else 1f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    iconColor.copy(alpha = if (isComplete || isActive) 0.15f else 0.05f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isComplete) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isActive || isComplete) FontWeight.Medium else FontWeight.Normal
        )
        Spacer(Modifier.weight(1f))
        if (isComplete) {
            Text(
                "Done",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF10B981)
            )
        } else if (isActive) {
            Text(
                "In progress...",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6366F1)
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
            .background(ScanTheme.DarkBg),
        contentPadding = PaddingValues(horizontal = ScanTheme.Spacing20, vertical = ScanTheme.Spacing16),
        verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing16)
    ) {
        // Privacy Score Gauge
        item {
            PrivacyScoreGauge(
                score = scanState.averageScore.toInt(),
                high = scanState.highRiskCount,
                medium = scanState.mediumRiskCount,
                low = scanState.lowRiskCount,
                onNewScan = onNewScan
            )
        }

        // Confidence Score Badge
        scanState.confidenceScore?.let { confidence ->
            item {
                ConfidenceScoreBadge(
                    score = confidence,
                    recommendDeepAnalysis = scanState.recommendDeepAnalysis && scanState.scanLevel == ScanLevel.SMART
                )
            }
        }

        // ML Analysis Source Badge
        scanState.analysisSource?.let { source ->
            item {
                MLSourceBadge(source = source)
            }
        }

        // ML Explanation Card
        scanState.mlExplanation?.let { explanation ->
            item {
                MLExplanationCard(explanation = explanation)
            }
        }

        // ML Recommendations Section
        if (scanState.mlRecommendations.isNotEmpty()) {
            item {
                MLRecommendationsCard(
                    recommendations = scanState.mlRecommendations,
                    riskFactors = scanState.mlRiskFactors,
                    safetyTips = scanState.mlSafetyTips
                )
            }
        }

        if (!analysisNote.isNullOrBlank()) {
            item {
                AnalysisNoteCard(text = analysisNote)
            }
        }

        // Filter Tabs
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

        // Analyzed Apps Header
        item {
            Text(
                "Analyzed Apps (${filteredApps.size})",
                style = MaterialTheme.typography.titleMedium,
                color = ScanTheme.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // App List
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
        
        // Bottom spacing
        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PrivacyScoreGauge(
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
    
    val scoreLabel = when (clampedScore) {
        in 0..40 -> "Poor"
        in 41..60 -> "Fair"
        in 61..80 -> "Good"
        else -> "Excellent"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Privacy Score Title
            Text(
                "PRIVACY SCORE",
                style = MaterialTheme.typography.labelMedium,
                color = ScanTheme.TextSecondary,
                letterSpacing = 2.sp
            )
            
            // Circular Score Gauge
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background track
                Canvas(modifier = Modifier.size(160.dp)) {
                    drawArc(
                        color = Color(0xFF334155),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                // Progress arc with gradient effect
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
                
                // Score Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        animatedValue.toInt().toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = ScanTheme.TextPrimary
                    )
                    Text(
                        scoreLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scoreColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Risk Count Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RiskCountBadge(count = high, label = "High", color = Color(0xFFEF4444))
                RiskCountBadge(count = medium, label = "Med", color = Color(0xFFFB923C))
                RiskCountBadge(count = low, label = "Low", color = Color(0xFF10B981))
            }
            
            // Start New Scan Button
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
private fun RiskCountBadge(count: Int, label: String, color: Color) {
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
            color = ScanTheme.TextSecondary
        )
    }
}

@Composable
private fun FilterTabs(
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
                color = if (isSelected) Color(0xFF6366F1) else ScanTheme.SurfaceVariant,
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
                        color = if (isSelected) Color.White else ScanTheme.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (count > 0) {
                        Text(
                            "($count)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else ScanTheme.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppResultCard(
    app: LocalAppInfo,
    appRisk: AppRisk,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
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
                    // App Icon placeholder
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ScanTheme.SurfaceVariant, RoundedCornerShape(10.dp)),
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
                            color = ScanTheme.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${app.permissions.size} permissions • ${app.trackers.size} trackers",
                            color = ScanTheme.TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                // Score Badge
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
                HorizontalDivider(color = ScanTheme.SurfaceVariant, thickness = 1.dp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (app.permissions.isNotEmpty()) {
                        Text("Sensitive Permissions", style = MaterialTheme.typography.bodySmall, color = ScanTheme.TextPrimary, fontWeight = FontWeight.SemiBold)
                        app.permissions.take(4).forEach {
                            Text("• $it", style = MaterialTheme.typography.labelSmall, color = ScanTheme.TextSecondary)
                        }
                    }
                    if (app.trackers.isNotEmpty()) {
                        Text("Detected Trackers", style = MaterialTheme.typography.bodySmall, color = ScanTheme.TextPrimary, fontWeight = FontWeight.SemiBold)
                        app.trackers.take(4).forEach { tracker ->
                            Text("• ${tracker.name}", style = MaterialTheme.typography.labelSmall, color = ScanTheme.TextSecondary)
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

// Keep the old ScoreGauge for backward compatibility but it won't be used
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
            Text("Analysis Complete", style = MaterialTheme.typography.titleLarge, color = ScanTheme.TextPrimary, fontWeight = FontWeight.Bold)
            Text(
                text = when (clampedScore) {
                    in 0..40 -> "Multiple threats detected"
                    in 41..70 -> "Moderate security, monitor alerts"
                    else -> "Device well secured"
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
                    StatItem("High", high, Color(0xFFEF4444))
                    StatItem("Medium", medium, Color(0xFFFB923C))
                    StatItem("Low", low, Color(0xFF10B981))
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
    return AppRisk("Unknown", Color.Gray, 0)
}

@Composable
private fun ConfidenceScoreBadge(
    score: Int,
    recommendDeepAnalysis: Boolean = false
) {
    val scoreColor = when {
        score >= 80 -> Color(0xFF10B981) // Green - High confidence
        score >= 60 -> Color(0xFFFB923C) // Amber - Medium confidence
        else -> Color(0xFFEF4444) // Red - Low confidence
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ScanTheme.Spacing8),
        shape = RoundedCornerShape(ScanTheme.CornerMedium),
        colors = CardDefaults.cardColors(
            containerColor = scoreColor.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanTheme.Spacing12),
            verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing4)) {
                    Text(
                        "Analysis Confidence",
                        style = MaterialTheme.typography.labelSmall,
                        color = ScanTheme.TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(scoreColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$score%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            when {
                                score >= 80 -> "Very High"
                                score >= 60 -> "Medium"
                                else -> "Low"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = ScanTheme.TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (recommendDeepAnalysis) {
                HorizontalDivider(color = scoreColor.copy(alpha = 0.2f), thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = scoreColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Run a DEEP scan for a more complete analysis",
                        style = MaterialTheme.typography.bodySmall,
                        color = ScanTheme.TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * ML Source Badge - Shows analysis source (TensorFlow, Gemini, or Hybrid)
 */
@Composable
private fun MLSourceBadge(source: String) {
    val (icon, label, color) = when (source.lowercase()) {
        "gemini" -> Triple("\uD83E\uDD16", "Gemini AI", Color(0xFF4285F4))
        "tensorflow" -> Triple("\uD83E\uDDE0", "TensorFlow", Color(0xFFFF6F00))
        "hybrid" -> Triple("\u26A1", "Hybrid (TF + Gemini)", Color(0xFF10B981))
        else -> Triple("\uD83D\uDD0D", "Analysis", Color(0xFF6366F1))
    }

    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = ScanTheme.Spacing12, vertical = ScanTheme.Spacing8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
    ) {
        Text(icon, style = MaterialTheme.typography.bodySmall)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * ML Explanation Card - Shows AI analysis summary
 */
@Composable
private fun MLExplanationCard(explanation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(ScanTheme.CornerMedium),
        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanTheme.Spacing16),
            verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "AI Analysis",
                    style = MaterialTheme.typography.titleSmall,
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                explanation,
                style = MaterialTheme.typography.bodySmall,
                color = ScanTheme.TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * ML Recommendations Card - Shows recommendations, risk factors and safety tips
 */
@Composable
private fun MLRecommendationsCard(
    recommendations: List<String>,
    riskFactors: List<String>,
    safetyTips: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
        shape = RoundedCornerShape(ScanTheme.CornerMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanTheme.Spacing16),
            verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
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
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ScanTheme.TextSecondary
                )
            }

            // Show first recommendation always
            if (recommendations.isNotEmpty()) {
                RecommendationItem(
                    text = recommendations.first(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981)
                )
            }

            // Expandable content
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(ScanTheme.Spacing12)) {
                    // Rest of recommendations
                    recommendations.drop(1).forEach { rec ->
                        RecommendationItem(
                            text = rec,
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF10B981)
                        )
                    }

                    // Risk Factors
                    if (riskFactors.isNotEmpty()) {
                        HorizontalDivider(color = ScanTheme.SurfaceVariant, thickness = 1.dp)
                        Text(
                            "Risk Factors",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.SemiBold
                        )
                        riskFactors.forEach { factor ->
                            RecommendationItem(
                                text = factor,
                                icon = Icons.Default.Warning,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    // Safety Tips
                    if (safetyTips.isNotEmpty()) {
                        HorizontalDivider(color = ScanTheme.SurfaceVariant, thickness = 1.dp)
                        Text(
                            "Safety Tips",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF6366F1),
                            fontWeight = FontWeight.SemiBold
                        )
                        safetyTips.forEach { tip ->
                            RecommendationItem(
                                text = tip,
                                icon = Icons.Default.Shield,
                                color = Color(0xFF6366F1)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    text: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(ScanTheme.Spacing8)
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
            color = ScanTheme.TextSecondary,
            lineHeight = 18.sp
        )
    }
}

