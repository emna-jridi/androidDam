package tn.esprit.dam.features.scan.presentation.scan
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

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
                                AppColors.primary.copy(alpha = 0.3f),
                                AppColors.primary.copy(alpha = 0.1f),
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
                                AppColors.primary,
                                AppColors.primaryLight,
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
                        tint = AppColors.primary
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Text(
                stringResource(R.string.preparing_security_scan),
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(R.string.loading_installed_apps),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary
            )
        }
    }
}

private enum class ConsoleKind { Start, Info, Ai, Done, Note }

private data class ConsoleLine(val text: String, val kind: ConsoleKind)

/**
 * Analysis step as a security console. Progress, the app list and each app's permission/tracker
 * counts are real; the per-app sub-steps are paced locally because the backend only reports a count.
 */
@Composable
fun ScanConsoleState(
    apps: List<LocalAppInfo>,
    totalApps: Int,
    scannedApps: Int,
    analysisNote: String?,
    modifier: Modifier = Modifier
) {
    val progress = if (totalApps > 0) (scannedApps.toFloat() / totalApps).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "consoleProgress"
    )

    // Reveal the current app's lines one by one, then wait for the next count from the server.
    var subStep by remember { mutableIntStateOf(0) }
    LaunchedEffect(scannedApps) {
        subStep = 0
        while (subStep < 3) {
            delay(550)
            subStep++
        }
    }

    val startLabel = stringResource(R.string.scan_console_waiting)
    val permissionsFmt = stringResource(R.string.scan_console_permissions)
    val trackersFmt = stringResource(R.string.scan_console_trackers)
    val aiLabel = stringResource(R.string.scan_console_ai)
    val doneFmt = stringResource(R.string.scan_console_done)

    val lines = remember(apps, scannedApps, subStep, analysisNote) {
        buildList {
            add(ConsoleLine(startLabel, ConsoleKind.Info))
            apps.take(scannedApps.coerceAtMost(apps.size)).forEach { app ->
                add(ConsoleLine(app.packageName, ConsoleKind.Start))
                add(ConsoleLine(String.format(permissionsFmt, app.permissions.size), ConsoleKind.Info))
                add(ConsoleLine(String.format(trackersFmt, app.trackers.size), ConsoleKind.Info))
                add(ConsoleLine(String.format(doneFmt, app.displayName), ConsoleKind.Done))
            }
            apps.getOrNull(scannedApps)?.let { current ->
                add(ConsoleLine(current.packageName, ConsoleKind.Start))
                if (subStep >= 1) add(ConsoleLine(String.format(permissionsFmt, current.permissions.size), ConsoleKind.Info))
                if (subStep >= 2) add(ConsoleLine(String.format(trackersFmt, current.trackers.size), ConsoleKind.Info))
                if (subStep >= 3) add(ConsoleLine(aiLabel, ConsoleKind.Ai))
            }
            analysisNote?.let { add(ConsoleLine(it, ConsoleKind.Note)) }
        }.takeLast(MAX_CONSOLE_LINES)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background)
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            text = stringResource(R.string.scan_console_title),
            style = AppTypography.titleLarge,
            color = AppColors.textPrimary
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.scan_console_progress),
                    style = AppTypography.labelMedium,
                    color = AppColors.textTertiary
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = AppTypography.displayLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium),
                    color = AppColors.textPrimary
                )
            }
            Text(
                text = stringResource(R.string.scan_console_count, scannedApps, totalApps),
                style = AppTypography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = AppColors.textSecondary
            )
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = AppColors.primary,
            trackColor = AppColors.surfaceVariant
        )
        ConsolePanel(lines = lines, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ConsolePanel(lines: List<ConsoleLine>, modifier: Modifier = Modifier) {
    val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 500), RepeatMode.Reverse),
        label = "cursorBlink"
    )
    val mono = AppTypography.labelMedium.copy(fontFamily = FontFamily.Monospace, lineHeight = 20.sp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.console)
            .border(1.dp, AppColors.surfaceVariant, RoundedCornerShape(AppCorners.large))
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.Bottom
    ) {
        lines.forEachIndexed { index, line ->
            val (prefix, color) = when (line.kind) {
                ConsoleKind.Start -> "▶ " to AppColors.primary
                ConsoleKind.Info -> "  " to AppColors.textTertiary
                ConsoleKind.Ai -> "  " to AppColors.accentViolet
                ConsoleKind.Done -> "✓ " to AppColors.success
                ConsoleKind.Note -> "✦ " to AppColors.accentViolet
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prefix + line.text,
                    style = mono,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (index == lines.lastIndex) {
                    Text(text = " ▍", style = mono, color = AppColors.primary, modifier = Modifier.alpha(cursorAlpha))
                }
            }
        }
    }
}

private const val MAX_CONSOLE_LINES = 16
