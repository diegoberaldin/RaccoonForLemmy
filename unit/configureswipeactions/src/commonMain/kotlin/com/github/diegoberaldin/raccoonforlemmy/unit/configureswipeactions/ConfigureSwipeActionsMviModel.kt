package com.github.diegoberaldin.raccoonforlemmy.unit.configureswipeactions

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipeDirection

@Stable
interface ConfigureSwipeActionsMviModel :
    MviModel<ConfigureSwipeActionsMviModel.Intent, ConfigureSwipeActionsMviModel.UiState, ConfigureSwipeActionsMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object ResetActionsPosts : Intent
        data class DeleteActionPosts(
            val value: ActionOnSwipe,
            val direction: ActionOnSwipeDirection,
        ) :
            Intent

        data object ResetActionsComments : Intent

        data class DeleteActionComments(
            val value: ActionOnSwipe,
            val direction: ActionOnSwipeDirection,
        ) :
            Intent

        data object ResetActionsInbox : Intent
        data class DeleteActionInbox(
            val value: ActionOnSwipe,
            val direction: ActionOnSwipeDirection,
        ) :
            Intent
    }

    data class UiState(
        val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = emptyList(),
        val availableOptionsPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToStartComments: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndComments: List<ActionOnSwipe> = emptyList(),
        val availableOptionsComments: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToStartInbox: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndInbox: List<ActionOnSwipe> = emptyList(),
        val availableOptionsInbox: List<ActionOnSwipe> = emptyList(),
    )

    sealed interface Effect
}
