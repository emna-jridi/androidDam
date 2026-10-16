package tn.esprit.dam.core.ui.util
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

object AppAnimations {
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
    const val STAGGER_DELAY = 50
}

fun fadeInAnimation(
    duration: Int = AppAnimations.NORMAL
): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        )
    )
}

fun fadeOutAnimation(
    duration: Int = AppAnimations.NORMAL
): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = duration,
            easing = LinearEasing
        )
    )
}

fun slideInFromBottomAnimation(
    duration: Int = AppAnimations.NORMAL
): EnterTransition {
    return slideInVertically(
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        ),
        initialOffsetY = { it / 2 }
    ) + fadeInAnimation(duration)
}

fun slideOutToBottomAnimation(
    duration: Int = AppAnimations.NORMAL
): ExitTransition {
    return slideOutVertically(
        animationSpec = tween(
            durationMillis = duration,
            easing = LinearEasing
        ),
        targetOffsetY = { it / 2 }
    ) + fadeOutAnimation(duration)
}

fun scaleInAnimation(
    duration: Int = AppAnimations.NORMAL
): EnterTransition {
    return scaleIn(
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        ),
        initialScale = 0.8f
    ) + fadeInAnimation(duration)
}

fun scaleOutAnimation(
    duration: Int = AppAnimations.NORMAL
): ExitTransition {
    return scaleOut(
        animationSpec = tween(
            durationMillis = duration,
            easing = LinearEasing
        ),
        targetScale = 0.8f
    ) + fadeOutAnimation(duration)
}

@Composable
fun rememberShimmerAnimation(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha = transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = stringResource(R.string.shimmer_alpha)
    )
    return alpha.value
}

fun Modifier.animateListItem(
    index: Int,
    visible: Boolean = true
) = composed {
    val delayMillis = index * AppAnimations.STAGGER_DELAY

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = AppAnimations.NORMAL,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = stringResource(R.string.list_item_alpha)
    )

    val animatedOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(
            durationMillis = AppAnimations.NORMAL,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = stringResource(R.string.list_item_offset)
    )

    this
        .alpha(animatedAlpha)
        .graphicsLayer {
            translationY = animatedOffset
        }
}

@Composable
fun rememberPulseAnimation(): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha = transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = stringResource(R.string.pulse_alpha)
    )
    return alpha.value
}

@Composable
fun AnimatedContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeInAnimation() + slideInFromBottomAnimation(),
    exit: ExitTransition = fadeOutAnimation() + slideOutToBottomAnimation(),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content
    )
}

