package com.github.diegoberaldin.raccoonforlemmy.core.commonui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val FIRST_ACTION_THRESHOLD = 0.14f
private const val SECOND_ACTION_THRESHOLD = 0.38f

data class SwipeAction(
    val swipeContent: @Composable () -> Unit,
    val backgroundColor: Color,
    val onTriggered: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeActionCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
    onGestureBegin: (() -> Unit)? = null,
    swipeToStartActions: List<SwipeAction> = emptyList(),
    swipeToEndActions: List<SwipeAction> = emptyList(),
) {
    if (enabled) {
        var notified by remember { mutableStateOf(false) }
        var secondNotified by remember { mutableStateOf(false) }
        val gestureBeginCallback by rememberUpdatedState(onGestureBegin)
        var lastProgress by remember { mutableStateOf(0.0f) }
        val dismissState = rememberNoFlingSwipeToDismissBoxState(
            confirmValueChange = rememberCallbackArgs { value ->
                when (value) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        val enableSecondAction = swipeToEndActions.size > 1
                        if (lastProgress >= SECOND_ACTION_THRESHOLD && enableSecondAction && secondNotified) {
                            swipeToEndActions.getOrNull(1)?.onTriggered?.invoke()
                        } else {
                            swipeToEndActions.firstOrNull()?.onTriggered?.invoke()

                        }
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        val enableSecondAction = swipeToStartActions.size > 1
                        if (lastProgress >= SECOND_ACTION_THRESHOLD && enableSecondAction && secondNotified) {
                            swipeToStartActions.getOrNull(1)?.onTriggered?.invoke()
                        } else {
                            swipeToStartActions.firstOrNull()?.onTriggered?.invoke()
                        }
                    }

                    else -> Unit
                }
                notified = false
                secondNotified = false
                // return false to stay dismissed
                false
            },
        )
        LaunchedEffect(dismissState, swipeToEndActions, swipeToEndActions) {
            snapshotFlow { dismissState.progress }.onEach { progress ->
                val enableSecondAction = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> false
                    SwipeToDismissBoxValue.StartToEnd -> swipeToEndActions.size > 1
                    SwipeToDismissBoxValue.EndToStart -> swipeToStartActions.size > 1
                }

                if (!enableSecondAction) {
                    when {
                        progress in FIRST_ACTION_THRESHOLD..<1.0f && !notified -> {
                            notified = true
                            gestureBeginCallback?.invoke()
                        }
                    }
                } else {
                    when {
                        progress in FIRST_ACTION_THRESHOLD..<SECOND_ACTION_THRESHOLD && !notified -> {
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

        SwipeToDismissBox(
            modifier = modifier,
            state = dismissState,
            enableDismissFromStartToEnd = swipeToEndActions.isNotEmpty(),
            enableDismissFromEndToStart = swipeToStartActions.isNotEmpty(),
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val actions = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> listOf()
                    SwipeToDismissBoxValue.StartToEnd -> swipeToEndActions
                    SwipeToDismissBoxValue.EndToStart -> swipeToStartActions
                }
                val enableSecondAction = actions.size > 1
                val bgColor by animateColorAsState(
                    targetValue = if (
                        dismissState.progress < SECOND_ACTION_THRESHOLD
                        || dismissState.targetValue == SwipeToDismissBoxValue.Settled
                        || !enableSecondAction
                    ) {
                        actions.firstOrNull()?.backgroundColor ?: Color.Transparent
                    } else {
                        actions.getOrNull(1)?.backgroundColor ?: Color.Transparent
                    },
                )
                val alignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
                Box(
                    Modifier.fillMaxSize()
                        .background(bgColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment,
                ) {
                    if (
                        dismissState.progress < SECOND_ACTION_THRESHOLD || !enableSecondAction
                    ) {
                        actions.firstOrNull()?.swipeContent?.invoke()
                    } else {
                        actions.getOrNull(1)?.swipeContent?.invoke()
                    }
                }
            },
            content = {
                content()
            },
        )
    } else {
        content()
    }
}

/**
 * CREDITS:
 * https://issuetracker.google.com/issues/252334353#comment16
 */
@Composable
@ExperimentalMaterial3Api
private fun rememberNoFlingSwipeToDismissBoxState(
    initialValue: SwipeToDismissBoxValue = SwipeToDismissBoxValue.Settled,
    confirmValueChange: (SwipeToDismissBoxValue) -> Boolean = { true },
    positionalThreshold: (totalDistance: Float) -> Float =
        SwipeToDismissBoxDefaults.positionalThreshold,
): SwipeToDismissBoxState {
    // instead of LocalDensity.current we use a value that makes velocityThreshold to skyrocket
    val density = Density(Float.POSITIVE_INFINITY)
    return rememberSaveable(
        saver = SwipeToDismissBoxState.Saver(
            confirmValueChange = confirmValueChange,
            density = density,
            positionalThreshold = positionalThreshold
        )
    ) {
        SwipeToDismissBoxState(
            initialValue = initialValue,
            density = density,
            confirmValueChange = confirmValueChange,
            positionalThreshold = positionalThreshold
        )
    }
}
