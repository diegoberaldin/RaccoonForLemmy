package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

sealed interface PostLayout {
    data object Card : PostLayout
    data object Compact : PostLayout
    data object Full : PostLayout
}

@Composable
fun PostLayout.toReadableName(): String = when (this) {
    PostLayout.Full -> stringResource(MR.strings.settings_post_layout_full)
    PostLayout.Compact -> stringResource(MR.strings.settings_post_layout_compact)
    else -> stringResource(MR.strings.settings_post_layout_card)
}

fun Int.toPostLayout(): PostLayout = when (this) {
    1 -> PostLayout.Compact
    2 -> PostLayout.Full
    else -> PostLayout.Card
}

fun PostLayout.toInt(): Int = when (this) {
    PostLayout.Full -> 2
    PostLayout.Compact -> 1
    else -> 0
}