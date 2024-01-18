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
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
    SwipeActionCard(
        modifier = modifier,
        enabled = enabled,
        content = content,
        onGestureBegin = onGestureBegin,
        swipeToStartActions = buildList {
            if (directions.contains(DismissDirection.EndToStart)) {
                this += SwipeAction(
                    swipeContent = { swipeContent(DismissDirection.EndToStart) },
                    backgroundColor = backgroundColor(DismissValue.DismissedToStart),
                    onTriggered = rememberCallback { onDismissToStart?.invoke() },
                )
                if (enableSecondAction(DismissValue.DismissedToStart)) {
                    this += SwipeAction(
                        swipeContent = { secondSwipeContent?.invoke(DismissDirection.EndToStart) },
                        backgroundColor = secondBackgroundColor?.invoke(DismissValue.DismissedToStart)
                            ?: backgroundColor(DismissValue.DismissedToStart),
                        onTriggered = rememberCallback { onSecondDismissToStart?.invoke() },
                    )
                }
            }
        },
        swipeToEndActions = buildList {
            if (directions.contains(DismissDirection.StartToEnd)) {
                this += SwipeAction(
                    swipeContent = { swipeContent(DismissDirection.StartToEnd) },
                    backgroundColor = backgroundColor(DismissValue.DismissedToEnd),
                    onTriggered = rememberCallback { onDismissToEnd?.invoke() },
                )
                if (enableSecondAction(DismissValue.DismissedToEnd)) {
                    this += SwipeAction(
                        swipeContent = { secondSwipeContent?.invoke(DismissDirection.StartToEnd) },
                        backgroundColor = secondBackgroundColor?.invoke(DismissValue.DismissedToEnd)
                            ?: backgroundColor(DismissValue.DismissedToEnd),
                        onTriggered = rememberCallback { onSecondDismissToEnd?.invoke() },
                    )
                }
            }
        },
    )
}

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
        val dismissState = rememberDismissState(
            confirmValueChange = rememberCallbackArgs { value ->
                when (value) {
                    DismissValue.DismissedToEnd -> {
                        val enableSecondAction = swipeToEndActions.size > 1
                        if (lastProgress >= SECOND_ACTION_THRESHOLD && enableSecondAction && secondNotified) {
                            swipeToEndActions.getOrNull(1)?.onTriggered?.invoke()
                        } else {
                            swipeToEndActions.firstOrNull()?.onTriggered?.invoke()

                        }
                    }

                    DismissValue.DismissedToStart -> {
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
            positionalThreshold = { _ -> 56.dp.toPx() }
        )
        LaunchedEffect(dismissState, swipeToEndActions, swipeToEndActions) {
            snapshotFlow { dismissState.progress }.onEach { progress ->
                val enableSecondAction = when (dismissState.targetValue) {
                    DismissValue.Default -> false
                    DismissValue.DismissedToEnd -> swipeToEndActions.size > 1
                    DismissValue.DismissedToStart -> swipeToStartActions.size > 1
                }

                if (!enableSecondAction) {
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
            directions = buildSet {
                if (swipeToEndActions.isNotEmpty()) {
                    this += DismissDirection.StartToEnd
                }
                if (swipeToStartActions.isNotEmpty()) {
                    this += DismissDirection.EndToStart
                }
            },
            background = {
                val direction = dismissState.dismissDirection ?: DismissDirection.StartToEnd
                val actions = when (dismissState.targetValue) {
                    DismissValue.Default -> listOf()
                    DismissValue.DismissedToEnd -> swipeToEndActions
                    DismissValue.DismissedToStart -> swipeToStartActions
                }
                val enableSecondAction = actions.size > 1
                val bgColor by animateColorAsState(
                    targetValue = if (
                        dismissState.progress < SECOND_ACTION_THRESHOLD
                        || dismissState.targetValue == DismissValue.Default
                        || !enableSecondAction
                    ) {
                        actions.firstOrNull()?.backgroundColor ?: Color.Transparent
                    } else {
                        actions.getOrNull(1)?.backgroundColor ?: Color.Transparent
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
                        dismissState.progress < SECOND_ACTION_THRESHOLD || !enableSecondAction
                    ) {
                        actions.firstOrNull()?.swipeContent?.invoke()
                    } else {
                        actions.getOrNull(1)?.swipeContent?.invoke()
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