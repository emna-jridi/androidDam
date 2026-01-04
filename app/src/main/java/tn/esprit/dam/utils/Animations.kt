package tn.esprit.dam.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Standard animation durations
 */
object AppAnimations {
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 500
}

/**
 * Fade in animation for entering composables
 */
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

/**
 * Fade out animation for exiting composables
 */
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

/**
 * Slide in from bottom animation
 */
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

/**
 * Slide out to bottom animation
 */
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

/**
 * Scale animation for appearing items
 */
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

/**
 * Scale animation for disappearing items
 */
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

/**
 * Modifier for clickable scale effect
 * Provides tactile feedback by scaling down on press
 */
fun Modifier.clickableScale(
    targetScale: Float = 0.95f
) = composed {
    val scale = remember { Animatable(1f) }
    
    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .then(
            Modifier.scale(scale.value)
        )
}

/**
 * Shimmer effect for loading states
 */
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
        label = "shimmer_alpha"
    )
    return alpha.value
}

/**
 * Bounce effect for success states
 */
@Composable
fun rememberBounceAnimation(): Float {
    val transition = rememberInfiniteTransition(label = "bounce")
    val scale = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_scale"
    )
    return scale.value
}
