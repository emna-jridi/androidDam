package tn.esprit.dam.features.scan.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.R
import tn.esprit.dam.features.scan.data.remote.dto.AppResult
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing

@Composable
fun ScoreSection(
    score: Int,
    onImproveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clampedScore = score.coerceIn(0, 100)
    val animatedScore by animateFloatAsState(
        targetValue = clampedScore.toFloat(),
        animationSpec = tween(durationMillis = 1400),
        label = "scoreValue"
    )
    val progress by animateFloatAsState(
        targetValue = clampedScore / 100f,
        animationSpec = tween(durationMillis = 1400),
        label = "scoreProgress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCorners.xlarge),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xl)
        ) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(AppColors.primary.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )
                CircularProgressIndicator(
                    progress = { progress },
                    strokeWidth = 10.dp,
                    color = AppColors.primary,
                    trackColor = AppColors.surfaceVariant,
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = animatedScore.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 42.sp),
                    color = AppColors.textPrimary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Text(
                    text = "Security Score",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = when (clampedScore) {
                        0 -> "No scan performed"
                        in 1..30 -> "⚠️ Low score - recommendations available"
                        in 31..70 -> "🟠 Medium score"
                        else -> "🟢 Excellent score"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (clampedScore) {
                        0 -> "No scan performed"
                        in 1..30 -> "Several important risks"
                        in 31..70 -> "Some risks to monitor"
                        else -> "Device globally secured"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
                if (clampedScore > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        ScoreTag("Permissions", AppColors.error.copy(alpha = 0.18f), AppColors.error)
                        ScoreTag("Trackers", AppColors.warning.copy(alpha = 0.18f), AppColors.warning)
                    }
                }
                Text(
                    text = "Score based on sensitive permissions and detected trackers",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.textSecondary
                )
                TextButton(onClick = onImproveClick) {
                    Text("See how to improve", color = AppColors.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ScoreTag(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(AppCorners.small))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FeatureGrid(
    onScan: () -> Unit,
    onSearch: () -> Unit,
    onHistory: () -> Unit,
    onScanApk: () -> Unit,
    onShadowGuard: () -> Unit,
    onAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            FeatureCard(
                title = "New Scan",
                icon = Icons.Default.Security,
                description = "Analyze now",
                onClick = onScan,
                gradient = listOf(AppColors.primaryDark, AppColors.primary),
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                title = "History",
                icon = Icons.Default.History,
                description = "Latest results",
                onClick = onHistory,
                gradient = listOf(AppColors.info, AppColors.infoDark),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            FeatureCard(
                title = "Scan an APK",
                icon = Icons.Default.Android,
                description = "External file",
                onClick = onScanApk,
                gradient = listOf(AppColors.success, AppColors.successDark),
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                title = "Search an app",
                icon = Icons.Default.Search,
                description = "Check an app",
                onClick = onSearch,
                gradient = listOf(AppColors.teal, AppColors.info),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            FeatureCard(
                title = "Security Alerts",
                icon = Icons.Default.Error,
                description = "Access logs",
                onClick = onAlerts,
                gradient = listOf(AppColors.error, AppColors.warning),
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                title = "ShadowVault",
                icon = Icons.Default.Lock,
                description = "Password management",
                onClick = onShadowGuard,
                gradient = listOf(AppColors.primary, AppColors.primaryDark),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "featurePress"
    )

    Card(
        modifier = modifier
            .height(112.dp)
            .scale(scale)
            .clip(RoundedCornerShape(AppCorners.large))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        val success = try { tryAwaitRelease(); true } catch (e: Exception) { false }
                        pressed = false
                        if (success) onClick()
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = AppColors.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(brush = Brush.linearGradient(gradient), shape = RoundedCornerShape(AppCorners.medium)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun RecentActivitySection(
    riskyApps: List<AppResult>,
    onNavigateToAppDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Text(
            text = "Recent activity",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        if (riskyApps.isEmpty()) {
            Text(
                text = "No recent analysis",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary
            )
        } else {
            riskyApps.take(3).forEach { app ->
                val score = app.aiRiskScore ?: app.finalScore ?: 0f
                val name = app.appName ?: app.packageName ?: "App"

                var appCardPressed by remember { mutableStateOf(false) }
                val appCardScale by animateFloatAsState(
                    targetValue = if (appCardPressed) 0.98f else 1f,
                    animationSpec = tween(durationMillis = 100),
                    label = "appCardPress"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                    shape = RoundedCornerShape(AppCorners.large),
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(appCardScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    appCardPressed = true
                                    val success = try { tryAwaitRelease(); true } catch (e: Exception) { false }
                                    appCardPressed = false
                                    if (success) {
                                        app.packageName?.let { onNavigateToAppDetails(it) }
                                    }
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = AppColors.textPrimary)
                            Text("Score ${score.toInt()}/100", style = MaterialTheme.typography.labelSmall, color = AppColors.textSecondary)
                            RiskBadge(score = score)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun RiskBadge(score: Float) {
    val (label, color) = when {
        score < 25f -> stringResource(id = R.string.level_critical) to AppColors.riskCritical
        score < 40f -> stringResource(id = R.string.level_high) to AppColors.riskHigh
        score < 70f -> stringResource(id = R.string.level_medium) to AppColors.riskMedium
        else -> stringResource(id = R.string.level_low) to AppColors.riskLow
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color.copy(alpha = 0.8f), shape = CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.textPrimary
        )
    }
}

