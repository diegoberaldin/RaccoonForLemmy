package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

private const val SECOND_ACTION_THRESHOLD = 0.38f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCard(
    modifier: Modifier = Modifier,
    directions: Set<DismissDirection> = setOf(
        DismissDirection.StartToEnd,
        DismissDirection.EndToStart,
    ),
    enabled: Boolean = true,
    enableSecondAction: (DismissValue) -> Boolean = { false },
    content: @Composable () -> Unit,
    swipeContent: @Composable (DismissDirection) -> Unit,
    secondSwipeContent: @Composable ((DismissDirection) -> Unit)? = null,
    backgroundColor: (DismissValue) -> Color,
    secondBackgroundColor: ((DismissValue) -> Color)? = null,
    onGestureBegin: (() -> Unit)? = null,
    onDismissToEnd: (() -> Unit)? = null,
    onSecondDismissToEnd: (() -> Unit)? = null,
    onDismissToStart: (() -> Unit)? = null,
    onSecondDismissToStart: (() -> Unit)? = null,
) {
    if (enabled) {
        var notified by remember { mutableStateOf(false) }
        var secondNotified by remember { mutableStateOf(false) }
        val dismissToEndCallback by rememberUpdatedState(onDismissToEnd)
        val dismissToStartCallback by rememberUpdatedState(onDismissToStart)
        val secondDismissToEndCallback by rememberUpdatedState(onSecondDismissToEnd)
        val secondDismissToStartCallback by rememberUpdatedState(onSecondDismissToStart)
        val gestureBeginCallback by rememberUpdatedState(onGestureBegin)
        var lastProgress by remember { mutableStateOf(0.0f) }
        val dismissState = rememberDismissState(
            confirmValueChange = rememberCallbackArgs { value ->
                when (value) {
                    DismissValue.DismissedToEnd -> {
                        if (lastProgress >= SECOND_ACTION_THRESHOLD && enableSecondAction(value) && secondNotified) {
                            secondDismissToEndCallback?.invoke()
                        } else {
                            dismissToEndCallback?.invoke()
                        }
                    }

                    DismissValue.DismissedToStart -> {
                        if (lastProgress >= SECOND_ACTION_THRESHOLD && enableSecondAction(value) && secondNotified) {
                            secondDismissToStartCallback?.invoke()
                        } else {
                            dismissToStartCallback?.invoke()
                        }
                    }

                    else -> Unit
                }
                notified = false
                secondNotified = false
                // return false to stay dismissed
                false
            },
            positionalThreshold = { _ -> 56.dp.toPx() }
        )
        LaunchedEffect(dismissState) {
            snapshotFlow { dismissState.progress }.stateIn(this).onEach { progress ->
                if (!enableSecondAction(dismissState.targetValue)) {
                    when {
                        progress in 0.0f..<1.0f && !notified -> {
                            notified = true
                            gestureBeginCallback?.invoke()
                        }
                    }
                } else {
                    when {
                        progress in 0.0f..<SECOND_ACTION_THRESHOLD && !notified -> {
                            notified = true
                            gestureBeginCallback?.invoke()
                        }

                        progress in SECOND_ACTION_THRESHOLD..<1.0f && !secondNotified -> {
                            secondNotified = true
                            gestureBeginCallback?.invoke()
                        }
                    }
                }
                lastProgress = progress
            }.launchIn(this)
        }

        SwipeToDismiss(
            modifier = modifier,
            state = dismissState,
            directions = directions,
            background = {
                val direction = dismissState.dismissDirection ?: DismissDirection.StartToEnd
                val bgColor by animateColorAsState(
                    targetValue = if (
                        dismissState.progress < SECOND_ACTION_THRESHOLD
                        || dismissState.targetValue == DismissValue.Default
                        || !enableSecondAction(dismissState.targetValue)
                    ) {
                        backgroundColor(dismissState.targetValue)
                    } else {
                        secondBackgroundColor?.invoke(dismissState.targetValue)
                            ?: backgroundColor(dismissState.targetValue)
                    },
                )
                val alignment = when (direction) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                }
                Box(
                    Modifier.fillMaxSize()
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment,
                ) {
                    if (
                        dismissState.progress < SECOND_ACTION_THRESHOLD
                        || !enableSecondAction(dismissState.targetValue)
                    ) {
                        swipeContent(direction)
                    } else {
                        secondSwipeContent?.invoke(direction)
                    }
                }
            },
            dismissContent = {
                content()
            },
        )
    } else {
        content()
    }
}