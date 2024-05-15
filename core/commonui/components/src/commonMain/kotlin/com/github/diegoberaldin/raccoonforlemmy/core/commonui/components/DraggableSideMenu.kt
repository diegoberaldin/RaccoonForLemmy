package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

private sealed interface SlideAnchorPosition {
    data object Opened : SlideAnchorPosition
    data object Closed : SlideAnchorPosition
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableSideMenu(
    opened: Boolean,
    modifier: Modifier = Modifier,
    availableWidth: Dp = Dp.Unspecified,
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    val density = LocalDensity.current
    val maxWidth = if (availableWidth.isSpecified) availableWidth * 0.85f else 500.dp
    val draggableState = remember(availableWidth) {
        AnchoredDraggableState(
            initialValue = SlideAnchorPosition.Closed,
            anchors = DraggableAnchors<SlideAnchorPosition> {
                SlideAnchorPosition.Closed at with(density) { availableWidth.toPx() }
                SlideAnchorPosition.Opened at with(density) { (availableWidth - maxWidth).toPx() }
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(),
        )
    }

    LaunchedEffect(opened) {
        val target = if (opened) {
            SlideAnchorPosition.Opened
        } else {
            SlideAnchorPosition.Closed
        }
        draggableState.animateTo(target)
    }

    LaunchedEffect(draggableState) {
        snapshotFlow { draggableState.currentValue }.onEach { value: SlideAnchorPosition ->
            if (value == SlideAnchorPosition.Closed) {
                onDismiss?.invoke()
            }
        }.launchIn(this)
    }

    Box(
        modifier = modifier
            .width(maxWidth)
            .fillMaxHeight()
            .offset {
                IntOffset(
                    x = draggableState.requireOffset().roundToInt(),
                    y = 0,
                )
            }
            .anchoredDraggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .padding(
                top = Spacing.xxl,
                bottom = Spacing.m,
                end = Spacing.s,
                start = Spacing.s,
            ),
    ) {
        content()
    }
}
