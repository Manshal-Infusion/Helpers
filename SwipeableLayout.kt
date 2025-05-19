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
private const val DEFAULT_SENSITIVITY = 6f
private const val DEFAULT_MAX_SWIPE_OFFSET = 2500f
private const val DEFAULT_ROTATION_DIVIDER = 64f
private const val DEFAULT_DISMISS_DELAY = 300L

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
    var offset by remember { mutableFloatStateOf(Offset.Zero.x) }

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
