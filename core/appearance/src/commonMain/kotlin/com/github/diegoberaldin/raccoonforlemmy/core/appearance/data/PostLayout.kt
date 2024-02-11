package com.github.diegoberaldin.raccoonforlemmy.core.appearance.data

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

sealed interface PostLayout {
    data object Card : PostLayout
    data object Compact : PostLayout
    data object Full : PostLayout
}

@Composable
fun PostLayout.toReadableName(): String = when (this) {
    PostLayout.Full -> LocalXmlStrings.current.settingsPostLayoutFull
    PostLayout.Compact -> LocalXmlStrings.current.settingsPostLayoutCompact
    else -> LocalXmlStrings.current.settingsPostLayoutCard
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