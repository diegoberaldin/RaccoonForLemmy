package com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui

import androidx.compose.runtime.Composable
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

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
    BlockActionType.Community -> stringResource(MR.strings.block_action_community)
    BlockActionType.Instance -> stringResource(MR.strings.community_detail_block_instance)
    BlockActionType.User -> stringResource(MR.strings.block_action_user)
}