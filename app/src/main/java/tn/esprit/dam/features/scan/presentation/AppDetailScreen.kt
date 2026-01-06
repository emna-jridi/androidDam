package tn.esprit.dam.features.scan.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    userId: String,
    viewModel: AppDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName, userId)
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
        uiState.details != null -> {
            // Build an AppInfoDto from the new response format
            val details = uiState.details!!
            val app = details.app ?: AppInfoDto(
                packageName = details.packageName ?: packageName,
                displayName = details.appName ?: packageName,
                permissions = details.permissions,
                trackers = details.trackers?.trackers?.map { 
                    tn.esprit.dam.data.api.models.SimpleTrackerInfo(
                        name = it.name,
                        riskLevel = it.category
                    )
                } ?: emptyList(),
                finalScore = details.overallScore ?: details.securityScore ?: 0f,
                scanResults = tn.esprit.dam.data.api.models.AnalysisResultDto(
                    aiRiskScore = (100f - (details.securityScore ?: 0f)),
                    aiRiskLevel = details.globalRisk ?: "LOW",
                    aiSummary = "Security Score: ${details.securityScore?.toInt() ?: 0}/100, Privacy Score: ${details.privacyScore?.toInt() ?: 0}/100",
                    aiRecommendations = details.recommendations,
                    permissionsScore = details.privacyScore,
                    trackersScore = details.trackers?.privacyScore?.toFloat()
                )
            )
            AppDetailContent(
                app = app,
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
                text = "Loading details...",
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
                Text("Retry")
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
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            DetailHeader(onBackClick = onBackClick)
        }

        // App Header with Risk Score
        item {
            AppHeaderWithRiskCard(app = app, riskResult = riskResult)
        }

        // Why was this flagged section
        if (riskResult.riskLevel != RiskLevel.SAFE && riskResult.riskLevel != RiskLevel.LOW) {
            item {
                WhyFlaggedSection(app = app, riskResult = riskResult)
            }
        }

        // Permissions Analysis Section
        item {
            PermissionsAnalysisSection(app = app)
        }

        // Detected Trackers Section
        item {
            DetectedTrackersSection(app = app)
        }

        // Recommendations Section
        item {
            RecommendationsSection(app = app, riskResult = riskResult)
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
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = "APP ANALYSIS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
        }
    }
}

@Composable
private fun AppHeaderWithRiskCard(
    app: AppInfoDto,
    riskResult: ScanRiskResult
) {
    val riskColor = when (riskResult.riskLevel) {
        RiskLevel.CRITICAL -> Color(0xFFDC2626)
        RiskLevel.HIGH -> Color(0xFFEF4444)
        RiskLevel.MEDIUM -> Color(0xFFFB923C)
        RiskLevel.LOW -> Color(0xFF22C55E)
        RiskLevel.SAFE -> Color(0xFF10B981)
    }

    val gradientColors = when (riskResult.riskLevel) {
        RiskLevel.CRITICAL, RiskLevel.HIGH -> listOf(Color(0xFF991B1B), Color(0xFFDC2626))
        RiskLevel.MEDIUM -> listOf(Color(0xFF9A3412), Color(0xFFF97316))
        RiskLevel.LOW, RiskLevel.SAFE -> listOf(Color(0xFF166534), Color(0xFF22C55E))
    }

    // Animation for score
    val animatedScore by animateFloatAsState(
        targetValue = riskResult.score.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "score_animation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon with gradient border
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            brush = Brush.linearGradient(colors = gradientColors),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(3.dp)
                ) {
                    Surface(
                        color = Color(0xFF334155),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = (app.displayName?.firstOrNull()?.uppercaseChar() ?: 'A').toString(),
                                fontSize = 28.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.displayName ?: app.packageName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Circular Risk Score
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Background circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF334155),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(size.width - 12.dp.toPx(), size.height - 12.dp.toPx()),
                        topLeft = Offset(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                // Progress circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = gradientColors + gradientColors.first()
                        ),
                        startAngle = -90f,
                        sweepAngle = (animatedScore / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(size.width - 12.dp.toPx(), size.height - 12.dp.toPx()),
                        topLeft = Offset(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                // Center content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = animatedScore.toInt().toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "RISK SCORE",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Risk Level Badge
            Surface(
                color = riskColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(riskColor, CircleShape)
                    )
                    Text(
                        text = riskResult.riskLevel.toString().uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge(
                    icon = Icons.Default.Lock,
                    value = "${app.permissions.size}",
                    label = "Permissions",
                    color = if (app.permissions.size > 10) Color(0xFFEF4444) else Color(0xFF6366F1)
                )
                StatBadge(
                    icon = Icons.Default.Visibility,
                    value = "${app.trackers.size}",
                    label = "Trackers",
                    color = if (app.trackers.isNotEmpty()) Color(0xFFF97316) else Color(0xFF22C55E)
                )
                StatBadge(
                    icon = Icons.Default.Shield,
                    value = "${100 - riskResult.score}%",
                    label = "Safe",
                    color = if (riskResult.score < 50) Color(0xFF22C55E) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
    }
}

@Composable
private fun WhyFlaggedSection(
    app: AppInfoDto,
    riskResult: ScanRiskResult
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Why was this flagged?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF7F1D1D).copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = pulseAlpha))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = when {
                        app.trackers.isNotEmpty() && app.permissions.isNotEmpty() ->
                            "⚠️ ${app.displayName ?: "This app"} connects to the internet and transfers data to analytics and advertising networks without explicit user consent."
                        app.trackers.isNotEmpty() ->
                            "⚠️ ${app.displayName ?: "This app"} contains ${app.trackers.size} trackers that collect your usage data."
                        app.permissions.isNotEmpty() ->
                            "⚠️ ${app.displayName ?: "This app"} requests access to sensitive device features and data."
                        else -> "⚠️ This app has potential security concerns."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFCA5A5),
                    lineHeight = 22.sp
                )

                // Risk indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (app.trackers.isNotEmpty()) {
                        RiskIndicatorChip(
                            icon = Icons.Default.Visibility,
                            text = "${app.trackers.size} Trackers",
                            backgroundColor = Color(0xFFDC2626)
                        )
                    }
                    if (app.permissions.size > 10) {
                        RiskIndicatorChip(
                            icon = Icons.Default.Lock,
                            text = "${app.permissions.size} Perms",
                            backgroundColor = Color(0xFFF97316)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskIndicatorChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    backgroundColor: Color
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = backgroundColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
        }
    }
}

@Composable
private fun PermissionsAnalysisSection(app: AppInfoDto) {
    var showAllPermissions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Permissions Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Surface(
                color = Color(0xFF334155),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${app.permissions.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        val permissionsToShow = if (showAllPermissions) app.permissions else app.permissions.take(6)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Permission chips in a flow layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    permissionsToShow.forEach { perm ->
                        EnhancedPermissionChip(permission = perm)
                    }
                }

                if (app.permissions.size > 6) {
                    TextButton(
                        onClick = { showAllPermissions = !showAllPermissions },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = if (showAllPermissions) "Show Less" else "Show All (${app.permissions.size})",
                            color = Color(0xFF818CF8),
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (showAllPermissions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedPermissionChip(permission: String) {
    val permissionData = remember(permission) {
        SecurityUtils.analyzePermission(permission)
    }

    val (icon, color) = remember(permissionData) {
        when {
            permission.contains("CAMERA", ignoreCase = true) -> Icons.Default.CameraAlt to Color(0xFFEF4444)
            permission.contains("LOCATION", ignoreCase = true) -> Icons.Default.LocationOn to Color(0xFFF97316)
            permission.contains("MICROPHONE", ignoreCase = true) || permission.contains("RECORD_AUDIO", ignoreCase = true) -> Icons.Default.Mic to Color(0xFFDC2626)
            permission.contains("CONTACT", ignoreCase = true) -> Icons.Default.Contacts to Color(0xFF8B5CF6)
            permission.contains("STORAGE", ignoreCase = true) || permission.contains("READ_EXTERNAL", ignoreCase = true) -> Icons.Default.Folder to Color(0xFF3B82F6)
            permission.contains("SMS", ignoreCase = true) -> Icons.Default.Message to Color(0xFFEC4899)
            permission.contains("CALL", ignoreCase = true) || permission.contains("PHONE", ignoreCase = true) -> Icons.Default.Call to Color(0xFF10B981)
            permission.contains("INTERNET", ignoreCase = true) -> Icons.Default.Wifi to Color(0xFF6366F1)
            permission.contains("BLUETOOTH", ignoreCase = true) -> Icons.Default.Bluetooth to Color(0xFF0EA5E9)
            else -> Icons.Default.Security to Color(0xFF94A3B8)
        }
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(width = 90.dp, height = 80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = permission,
                modifier = Modifier.size(28.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = permissionData.label.take(10),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                maxLines = 2,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun DetectedTrackersSection(app: AppInfoDto) {
    var showAllTrackers by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color(0xFFF97316),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Detected Trackers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Surface(
                color = if (app.trackers.isEmpty()) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${app.trackers.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (app.trackers.isEmpty()) Color(0xFF22C55E) else Color(0xFFEF4444)
                )
            }
        }

        if (app.trackers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF166534).copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF22C55E).copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color(0xFF22C55E)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "No trackers detected",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E)
                        )
                        Text(
                            text = "This app doesn't contain known trackers",
                            fontSize = 13.sp,
                            color = Color(0xFF86EFAC)
                        )
                    }
                }
            }
        } else {
            val trackersToShow = if (showAllTrackers) app.trackers else app.trackers.take(3)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    trackersToShow.forEach { tracker ->
                        EnhancedTrackerCard(tracker)
                    }

                    if (app.trackers.size > 3) {
                        TextButton(
                            onClick = { showAllTrackers = !showAllTrackers },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = if (showAllTrackers) "Show Less" else "View all ${app.trackers.size} trackers",
                                color = Color(0xFFF97316),
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (showAllTrackers) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedTrackerCard(tracker: tn.esprit.dam.data.api.models.SimpleTrackerInfo) {
    val trackerColor = when (tracker.riskLevel?.lowercase()) {
        "critical", "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF97316)
        "low" -> Color(0xFFFBBF24)
        else -> Color(0xFF94A3B8)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = trackerColor.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, trackerColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = trackerColor.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = trackerColor
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tracker.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = tracker.riskLevel?.uppercase() ?: "UNKNOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = trackerColor
                )
            }
            Surface(
                color = trackerColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "TRACKING",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = trackerColor
                )
            }
        }
    }
}

@Composable
private fun RecommendationsSection(
    app: AppInfoDto,
    riskResult: ScanRiskResult
) {
    val recommendations = remember(app, riskResult) {
        buildList {
            if (app.permissions.any { it.contains("LOCATION", ignoreCase = true) }) {
                add(RecommendationItem(
                    title = "Restrict Location Access",
                    description = "Limit app access to your precise location data to protect your privacy.",
                    icon = Icons.Default.LocationOff
                ))
            }
            if (app.permissions.any { it.contains("CAMERA", ignoreCase = true) }) {
                add(RecommendationItem(
                    title = "Disable Camera Access",
                    description = "Revoke camera permissions if not needed for core functionality.",
                    icon = Icons.Default.NoPhotography
                ))
            }
            if (app.permissions.any { it.contains("MICROPHONE", ignoreCase = true) || it.contains("RECORD_AUDIO", ignoreCase = true) }) {
                add(RecommendationItem(
                    title = "Disable Microphone Access",
                    description = "Prevent unauthorized audio recording by revoking microphone permissions.",
                    icon = Icons.Default.MicOff
                ))
            }
            if (app.trackers.isNotEmpty()) {
                add(RecommendationItem(
                    title = "Block Tracker Network Requests",
                    description = "Use a privacy-focused DNS or firewall to block tracker connections.",
                    icon = Icons.Default.Block
                ))
            }
            add(RecommendationItem(
                title = "Review App Regularly",
                description = "Periodically review app permissions and behavior for changes.",
                icon = Icons.Default.Refresh
            ))
        }
    }

    var selectedRecs by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        recommendations.forEachIndexed { index, rec ->
            EnhancedCollapsibleRecommendationCard(
                recommendation = rec,
                isExpanded = index in selectedRecs,
                onToggle = {
                    selectedRecs = if (index in selectedRecs) {
                        selectedRecs - index
                    } else {
                        selectedRecs + index
                    }
                }
            )
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF64748B)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark Safe", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Protect", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EnhancedCollapsibleRecommendationCard(
    recommendation: RecommendationItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
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
                    Surface(
                        color = Color(0xFF6366F1).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = recommendation.icon,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = recommendation.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = recommendation.description,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }
        }
    }
}

data class RecommendationItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Security
)

@Composable
private fun ChipBadge(
    text: String,
    backgroundColor: Color = Color(0xFFEF4444)
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = backgroundColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
            text = "Scan History (${history.size})",
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
        val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH)
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
                        text = "Last scan: ${formatHistoryDate(date)}",
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
                    label = "Rating"
                )
            }

            // Downloads
            if (storeData.downloads != null) {
                StoreDataItem(
                    icon = Icons.Default.PlayArrow,
                    iconColor = MaterialTheme.colorScheme.primary,
                    value = storeData.downloads!!,
                    label = "Downloads"
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
                text = "Security Score",
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
                        text = "Risk Level",
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
                Text("Risk Factors:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                
                ScoreFactorRow("Permissions", riskResult.permissionScore)
                ScoreFactorRow("Trackers & Ads", riskResult.trackerScore)
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
                    text = "Security Analysis",
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
                        text = "Confidence: $confidence",
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
                        text = if (aiStatus == "ok") "AI" else "Heur.",
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
                HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                
                Text(
                    text = "Recommendations",
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
