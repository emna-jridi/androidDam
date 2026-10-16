package tn.esprit.dam.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tn.esprit.dam.core.ui.theme.*
import tn.esprit.dam.core.ui.util.*
import tn.esprit.dam.core.util.*

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val haptic = rememberHapticFeedback()

    Button(
        onClick = {
            haptic.mediumTap()
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(AppCorners.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.primary,
            disabledContainerColor = AppColors.surfaceVariant
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppColors.textPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text,
                style = AppTypography.labelLarge,
                color = AppColors.textPrimary
            )
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.medium),
        onClick = { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            content = content
        )
    }
}

@Composable
fun AppLoadingState(
    message: String = "Loading..."
) {
    val pulseAlpha = rememberPulseAnimation()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            CircularProgressIndicator(
                color = AppColors.primary,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(48.dp)
                    .alpha(pulseAlpha)
            )
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
fun AppErrorState(
    message: String,
    icon: ImageVector = Icons.Default.Error,
    onRetry: (() -> Unit)? = null,
    retryText: String = "Retry"
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleInAnimation() + fadeInAnimation(),
            exit = scaleOutAnimation() + fadeOutAnimation()
        ) {
            AppCard(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppColors.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = message,
                        style = AppTypography.bodyLarge,
                        color = AppColors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (onRetry != null) {
                        AppPrimaryButton(
                            text = retryText,
                            onClick = onRetry,
                            modifier = Modifier.padding(top = AppSpacing.sm)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            modifier = Modifier
                .padding(AppSpacing.xl)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.textTertiary,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = title,
                style = AppTypography.titleLarge,
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                AppPrimaryButton(
                    text = actionText,
                    onClick = onAction,
                    modifier = Modifier.padding(top = AppSpacing.md)
                )
            }
        }
    }
}

@Composable
fun AppErrorAlert(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AppColors.error.copy(alpha = 0.15f),
        shape = RoundedCornerShape(AppCorners.large),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = AppColors.error,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                color = AppColors.error,
                style = AppTypography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AppSkeletonCard(
    modifier: Modifier = Modifier
) {
    val shimmerAlpha = rememberShimmerAnimation()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        AppColors.surfaceVariant.copy(alpha = shimmerAlpha),
                        RoundedCornerShape(AppCorners.medium)
                    )
            )
            Spacer(Modifier.width(AppSpacing.md))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .background(
                            AppColors.surfaceVariant.copy(alpha = shimmerAlpha),
                            RoundedCornerShape(AppCorners.small)
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .background(
                            AppColors.surfaceVariant.copy(alpha = shimmerAlpha),
                            RoundedCornerShape(AppCorners.small)
                        )
                )
            }
        }
    }
}

@Composable
fun AppSkeletonList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        repeat(itemCount) {
            AppSkeletonCard()
        }
    }
}
