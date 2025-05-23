package com.flexfitnessapp.ui.util

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val DEFAULT_SWIPE_THRESHOLD = 1000f
private const val DEFAULT_SENSITIVITY = 8f
private const val DEFAULT_MAX_SWIPE_OFFSET = 2000f
private const val DEFAULT_ROTATION_DIVIDER = 64f
private const val DEFAULT_DISMISS_DELAY = 400L

/**
 * A composable layout that enables swipe-to-dismiss behavior with left and right directions.
 * Supports both user-driven swipes and programmatically triggered dismissals.
 *
 * @param onDismissed Callback invoked after the swipe is completed and the layout is dismissed.
 * Receives the direction of dismissal ([DismissDirection.LEFT] or [DismissDirection.RIGHT]).
 *
 * @param triggeredDismissDirection Optional direction to trigger dismissal programmatically.
 * If set, the layout animates and dismisses without any swipe gesture.
 *
 * @param dismissDelay Delay in milliseconds before invoking [onDismissed] after a layout dismissal.
 * Useful for giving time to animate the swipe out.
 *
 * @param swipeThreshold The distance threshold after which a swipe is considered a dismissal.
 * Measured in pixels and compared against the swipe offset.
 *
 * @param sensitivityFactor Factor used to scale the drag amount;
 * higher values make the swipe more sensitive.
 *
 * @param maxSwipeOffset The maximum offset value applied when dismissing.
 * Controls how far the layout moves off-screen.
 *
 * @param rotationFactor Divider to control card rotation during swipe.
 * Lower values result in more rotation.
 *
 * @param content The content composable that should be displayed inside the swipeable layout.
 */
@Composable
fun SwipeableLayout(
    onDismissed: (DismissDirection) -> Unit,
    triggeredDismissDirection: DismissDirection? = null,
    dismissDelay: Long = DEFAULT_DISMISS_DELAY,
    swipeThreshold: Float = DEFAULT_SWIPE_THRESHOLD,
    sensitivityFactor: Float = DEFAULT_SENSITIVITY,
    maxSwipeOffset: Float = DEFAULT_MAX_SWIPE_OFFSET,
    rotationFactor: Float = DEFAULT_ROTATION_DIVIDER,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current.density
    var offset by remember { mutableFloatStateOf(0f) }

    var dismissDirection by remember(triggeredDismissDirection) {
        mutableStateOf(triggeredDismissDirection)
    }

    LaunchedEffect(dismissDirection) {
        offset = when (dismissDirection) {
            DismissDirection.LEFT -> -maxSwipeOffset
            DismissDirection.RIGHT -> maxSwipeOffset
            null -> return@LaunchedEffect
        }
        delay(dismissDelay)
        dismissDirection?.let { onDismissed(it) }
        dismissDirection = null
    }

    val animatedOffset = animateFloatAsState(
        offset,
        animationSpec = if (triggeredDismissDirection != null) {
            /*
            Helps to get card swiped smoothly preventing user to see
            sudden change in offset value made by triggered dismiss
            */
            spring(stiffness = Spring.StiffnessVeryLow)
        } else {
            spring()
        },
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(animatedOffset.value.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        offset = when (dismissDirection) {
                            DismissDirection.LEFT -> -maxSwipeOffset
                            DismissDirection.RIGHT -> maxSwipeOffset
                            null -> 0f
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        offset += (dragAmount / density) * sensitivityFactor
                        when {
                            offset > swipeThreshold -> {
                                dismissDirection = DismissDirection.RIGHT
                            }

                            offset < -swipeThreshold -> {
                                dismissDirection = DismissDirection.LEFT
                            }
                        }
                        if (change.positionChange() != Offset.Zero) change.consume()
                    },
                )
            }
            .graphicsLayer(
                rotationZ = animateFloatAsState(animatedOffset.value / rotationFactor).value,
            ),
    ) {
        content()
    }
}

enum class DismissDirection { LEFT, RIGHT }
