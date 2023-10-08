package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel

interface InboxMessagesMviModel :
    MviModel<InboxMessagesMviModel.Intent, InboxMessagesMviModel.UiState, InboxMessagesMviModel.Effect> {
    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
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
    }
}