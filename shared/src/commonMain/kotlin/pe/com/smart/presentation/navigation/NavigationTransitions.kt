package pe.com.smart.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val SLIDE_DURATION = 240

fun slideInFromRight(): EnterTransition {

    return slideInHorizontally(
        initialOffsetX = { fullWidth ->
            fullWidth
        },
        animationSpec = tween(
            durationMillis = SLIDE_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}

fun slideOutToRight(): ExitTransition {

    return slideOutHorizontally(
        targetOffsetX = { fullWidth ->
            fullWidth
        },
        animationSpec = tween(
            durationMillis = SLIDE_DURATION,
            easing = FastOutSlowInEasing
        )
    )
}

fun noEnterTransition(): EnterTransition =
    EnterTransition.None

fun noExitTransition(): ExitTransition =
    ExitTransition.None