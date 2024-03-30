package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings

sealed interface BlockActionType {
    data object User : BlockActionType
    data object Community : BlockActionType
    data object Instance : BlockActionType
}

fun Int.toBlockActionType(): BlockActionType = when (this) {
    2 -> BlockActionType.Instance
    1 -> BlockActionType.Community
    else -> BlockActionType.User
}

@Composable
fun BlockActionType.toReadableName(): String = when (this) {
    BlockActionType.Community -> LocalXmlStrings.current.blockActionCommunity
    BlockActionType.Instance -> LocalXmlStrings.current.communityDetailBlockInstance
    BlockActionType.User -> LocalXmlStrings.current.blockActionUser
}
