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
        percent < 30 -> AppColors.error
        percent < 70 -> AppColors.orange
        else -> AppColors.success
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
                            AppColors.primary.copy(alpha = 0.5f),
                            AppColors.primaryLight,
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
                    color = AppColors.surfaceVariant,
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
                    tint = AppColors.primary
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
            stringResource(R.string.security_analysis_in_progress),
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
