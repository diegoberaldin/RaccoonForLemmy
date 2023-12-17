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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCard(
    modifier: Modifier = Modifier,
    directions: Set<DismissDirection> = setOf(
        DismissDirection.StartToEnd,
        DismissDirection.EndToStart,
    ),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
    swipeContent: @Composable (DismissDirection) -> Unit,
    backgroundColor: (DismissValue) -> Color,
    onGestureBegin: (() -> Unit)? = null,
    onDismissToEnd: (() -> Unit)? = null,
    onDismissToStart: (() -> Unit)? = null,
) {
    if (enabled) {
        val dismissToEndCallback by rememberUpdatedState(onDismissToEnd)
        val dismissToStartCallback by rememberUpdatedState(onDismissToStart)
        val gestureBeginCallback by rememberUpdatedState(onGestureBegin)
        val dismissState = rememberDismissState(
            confirmValueChange = rememberCallbackArgs { direction ->
                when (direction) {
                    DismissValue.DismissedToEnd -> {
                        dismissToEndCallback?.invoke()
                    }

                    DismissValue.DismissedToStart -> {
                        dismissToStartCallback?.invoke()
                    }

                    else -> Unit
                }
                false
            },
            positionalThreshold = { _ -> 56.dp.toPx() }
        )

        var notified by remember { mutableStateOf(false) }
        LaunchedEffect(dismissState) {
            snapshotFlow { dismissState.progress }.stateIn(this).onEach { progress ->
                if (progress in 0.0..<1.0 && !notified) {
                    notified = true
                    gestureBeginCallback?.invoke()
                } else if (progress >= 1) {
                    notified = false
                }
            }.launchIn(this)
        }

        SwipeToDismiss(
            modifier = modifier,
            state = dismissState,
            directions = directions,
            background = {
                val direction = dismissState.dismissDirection ?: DismissDirection.StartToEnd
                val bgColor by animateColorAsState(
                    backgroundColor(dismissState.targetValue),
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
                    swipeContent(direction)
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