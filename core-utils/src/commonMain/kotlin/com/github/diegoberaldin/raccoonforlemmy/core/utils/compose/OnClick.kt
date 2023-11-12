package com.github.diegoberaldin.raccoonforlemmy.core.utils.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.DateTime

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.onClick(
    debounceInterval: Long = 300,
    onClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(0L) }
    combinedClickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = {
            val currentTime = DateTime.epochMillis()
            if ((currentTime - lastClickTime) < debounceInterval) return@combinedClickable
            lastClickTime = currentTime
            onClick()
        },
        onDoubleClick = {
            val currentTime = DateTime.epochMillis()
            if ((currentTime - lastClickTime) < debounceInterval) return@combinedClickable
            lastClickTime = currentTime
            onDoubleClick()
        },
    )
}