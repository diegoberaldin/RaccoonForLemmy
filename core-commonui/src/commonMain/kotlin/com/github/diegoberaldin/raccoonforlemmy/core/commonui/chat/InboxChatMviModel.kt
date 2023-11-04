package com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel

@Stable
interface InboxChatMviModel :
    MviModel<InboxChatMviModel.Intent, InboxChatMviModel.UiState, InboxChatMviModel.SideEffect>,
    ScreenModel {
    sealed interface Intent {
        data object LoadNextPage : Intent
        data class SetNewMessageContent(val value: String) : Intent
        data object SubmitNewMessage : Intent
    }

    data class UiState(
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val currentUserId: Int? = null,
        val otherUserName: String = "",
        val otherUserAvatar: String? = null,
        val messages: List<PrivateMessageModel> = emptyList(),
        val newMessageContent: String = "",
        val autoLoadImages: Boolean = true,
    )

    sealed interface SideEffect
}