package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.detail

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel

interface InboxChatMviModel :
    MviModel<InboxChatMviModel.Intent, InboxChatMviModel.UiState, InboxChatMviModel.SideEffect> {
    sealed interface Intent {
        object LoadNextPage : Intent
        data class SetNewMessageContent(val value: String) : Intent
        object SubmitNewMessage : Intent
    }

    data class UiState(
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val currentUserId: Int = 0,
        val otherUserName: String = "",
        val otherUserAvatar: String? = null,
        val messages: List<PrivateMessageModel> = emptyList(),
        val newMessageContent: String = "",
    )

    sealed interface SideEffect
}