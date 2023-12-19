package com.github.diegoberaldin.raccoonforlemmy.feature.inbox

import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface InboxCoordinator {
    val unreadOnly: StateFlow<Boolean>
    val effects: SharedFlow<InboxMviModel.Effect>
    val unreadReplies: StateFlow<Int>
    val unreadMentions: StateFlow<Int>
    val unreadMessages: StateFlow<Int>
    val totalUnread: StateFlow<Int>

    fun setUnreadOnly(value: Boolean)

    suspend fun emitEffect(effect: InboxMviModel.Effect)

    suspend fun updateUnreadCount(): Int
}
