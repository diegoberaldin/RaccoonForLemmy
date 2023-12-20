package com.github.diegoberaldin.raccoonforlemmy.unit.messages

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel

@Stable
interface InboxMessagesMviModel :
    MviModel<InboxMessagesMviModel.Intent, InboxMessagesMviModel.UiState, InboxMessagesMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class MarkAsRead(val read: Boolean, val id: Int) : Intent
    }

    data class UiState(
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val unreadOnly: Boolean = true,
        val currentUserId: Int = 0,
        val chats: List<PrivateMessageModel> = emptyList(),
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect {
        data class UpdateUnreadItems(val value: Int) : Effect
        data object BackToTop : Effect
    }
}