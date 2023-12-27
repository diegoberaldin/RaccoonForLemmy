package com.github.diegoberaldin.raccoonforlemmy.core.utils.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.onClick(
    onClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
): Modifier = composed {
    combinedClickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = rememberCallback {
            onClick()
        },
        onDoubleClick = rememberCallback {
            onDoubleClick()
        },
        onLongClick = rememberCallback {
            onLongClick()
        }
    )
}