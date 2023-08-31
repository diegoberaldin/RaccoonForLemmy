package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel

interface InboxRepliesMviModel :
    MviModel<InboxRepliesMviModel.Intent, InboxRepliesMviModel.UiState, InboxRepliesMviModel.Effect> {

    sealed interface Intent {
        object Refresh : Intent
        object LoadNextPage : Intent
        data class ChangeUnreadOnly(val unread: Boolean) : Intent
        data class MarkMentionAsRead(val read: Boolean, val mentionId: Int) : Intent
        object HapticIndication : Intent
    }

    data class UiState(
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val unreadOnly: Boolean = true,
        val mentions: List<PersonMentionModel> = emptyList(),
    )

    sealed interface Effect
}
