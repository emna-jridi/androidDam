package tn.esprit.dam.features.scan.presentation.detail

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.features.scan.data.remote.dto.AppInfoDto
import tn.esprit.dam.features.scan.data.remote.dto.AppScanHistoryDto
import tn.esprit.dam.features.scan.data.remote.dto.SimpleTrackerInfo
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.ScanRiskResult
import tn.esprit.dam.features.scan.domain.SecurityUtils
import tn.esprit.dam.core.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun DetailHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            text = "APP ANALYSIS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {}) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
        }
    }
}

@Composable
fun AppHeaderWithRiskCard(
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
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
fun StatBadge(
    icon: ImageVector,
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
fun WhyFlaggedSection(
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
fun RiskIndicatorChip(
    icon: ImageVector,
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
fun PermissionsAnalysisSection(app: AppInfoDto) {
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
fun EnhancedPermissionChip(permission: String) {
    val permissionData = remember(permission) {
        SecurityUtils.analyzePermission(permission)
    }

    val icon: ImageVector = when {
        permission.contains("CAMERA", ignoreCase = true) -> Icons.Default.CameraAlt
        permission.contains("LOCATION", ignoreCase = true) -> Icons.Default.LocationOn
        permission.contains("MICROPHONE", ignoreCase = true) || permission.contains("RECORD_AUDIO", ignoreCase = true) -> Icons.Default.Mic
        permission.contains("CONTACT", ignoreCase = true) -> Icons.Default.Contacts
        permission.contains("STORAGE", ignoreCase = true) || permission.contains("READ_EXTERNAL", ignoreCase = true) -> Icons.Default.Folder
        permission.contains("SMS", ignoreCase = true) -> Icons.Default.Message
        permission.contains("CALL", ignoreCase = true) || permission.contains("PHONE", ignoreCase = true) -> Icons.Default.Call
        permission.contains("INTERNET", ignoreCase = true) -> Icons.Default.Wifi
        permission.contains("BLUETOOTH", ignoreCase = true) -> Icons.Default.Bluetooth
        else -> Icons.Default.Security
    }

    val color: Color = when {
        permission.contains("CAMERA", ignoreCase = true) -> Color(0xFFEF4444)
        permission.contains("LOCATION", ignoreCase = true) -> Color(0xFFF97316)
        permission.contains("MICROPHONE", ignoreCase = true) || permission.contains("RECORD_AUDIO", ignoreCase = true) -> Color(0xFFDC2626)
        permission.contains("CONTACT", ignoreCase = true) -> Color(0xFF8B5CF6)
        permission.contains("STORAGE", ignoreCase = true) || permission.contains("READ_EXTERNAL", ignoreCase = true) -> Color(0xFF3B82F6)
        permission.contains("SMS", ignoreCase = true) -> Color(0xFFEC4899)
        permission.contains("CALL", ignoreCase = true) || permission.contains("PHONE", ignoreCase = true) -> Color(0xFF10B981)
        permission.contains("INTERNET", ignoreCase = true) -> Color(0xFF6366F1)
        permission.contains("BLUETOOTH", ignoreCase = true) -> Color(0xFF0EA5E9)
        else -> Color(0xFF94A3B8)
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
fun DetectedTrackersSection(app: AppInfoDto) {
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
fun EnhancedTrackerCard(tracker: SimpleTrackerInfo) {
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
fun RecommendationsSection(
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
fun EnhancedCollapsibleRecommendationCard(
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

@Composable
fun AppHistorySection(history: List<AppScanHistoryDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Scan History (${history.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        history.forEach { entry ->
            HistoryEntryCard(entry)
        }
    }
}

@Composable
fun HistoryEntryCard(entry: AppScanHistoryDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
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
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "Score: ${entry.score.toInt()}/100",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            RiskLevelBadge(riskLevel = entry.riskLevel)
        }
    }
}

@Composable
fun RiskLevelBadge(riskLevel: String) {
    val color = when (riskLevel.lowercase()) {
        "low" -> Color(0xFF4CAF50)
        "medium" -> Color(0xFFFF9800)
        "high" -> Color(0xFFF44336)
        "critical" -> Color(0xFF9C27B0)
        else -> Color(0xFF6366F1)
    }
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

fun formatHistoryDate(dateString: String): String {
    return runCatching {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(dateString)
        val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH)
        date?.let { output.format(it) } ?: dateString
    }.getOrElse { dateString }
}

data class RecommendationItem(
    val title: String,
    val description: String,
    val icon: ImageVector = Icons.Default.Security
)
