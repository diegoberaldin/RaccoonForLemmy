package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.list

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel

interface InboxMessagesMviModel :
    MviModel<InboxMessagesMviModel.Intent, InboxMessagesMviModel.UiState, InboxMessagesMviModel.SideEffect> {
    sealed interface Intent {
        object Refresh : Intent
        object LoadNextPage : Intent
    }

    data class UiState(
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val unreadOnly: Boolean = true,
        val currentUserId: Int = 0,
        val chats: List<PrivateMessageModel> = emptyList(),
    )

    sealed interface SideEffect
}