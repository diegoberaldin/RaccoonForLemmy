package com.github.diegoberaldin.raccoonforlemmy.core.utils.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.onClick(
    key: Any = Unit,
    onClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
): Modifier = combinedClickable(
    indication = null,
    interactionSource = remember { MutableInteractionSource() },
    onClick = rememberCallback(key) {
        onClick()
    },
    onDoubleClick = rememberCallback(key) {
        onDoubleClick()
    },
    onLongClick = rememberCallback(key) {
        onLongClick()
    }
)