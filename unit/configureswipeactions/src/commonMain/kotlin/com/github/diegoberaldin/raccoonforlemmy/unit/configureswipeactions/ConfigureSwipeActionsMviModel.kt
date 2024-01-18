package com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe

@Stable
interface ConfigureSwipeActionsMviModel :
    MviModel<ConfigureSwipeActionsMviModel.Intent, ConfigureSwipeActionsMviModel.UiState, ConfigureSwipeActionsMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object ResetActionsPosts : Intent
        data object AddActionPosts : Intent
        data class DeleteActionPost(val value: SwipeAction) : Intent
        data object ResetActionsComments : Intent
        data object AddActionComments : Intent
        data class DeleteActionComments(val value: SwipeAction) : Intent
        data object ResetActionsInbox : Intent
        data object AddActionInbox : Intent
        data class DeleteActionInbox(val value: SwipeAction) : Intent
    }

    data class UiState(
        val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToStartComments: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndComments: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToStartInbox: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndInbox: List<ActionOnSwipe> = emptyList(),
    )

    sealed interface Effect
}
