package com.github.diegoberaldin.raccoonforlemmy.domain.inbox

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface InboxCoordinator {
    sealed interface Event {
        data object Refresh : Event
    }

    val events: SharedFlow<Event>
    val unreadOnly: StateFlow<Boolean>
    val unreadReplies: StateFlow<Int>
    val unreadMentions: StateFlow<Int>
    val unreadMessages: StateFlow<Int>
    val totalUnread: StateFlow<Int>

    fun setUnreadOnly(value: Boolean)

    suspend fun updateUnreadCount(): Int

    suspend fun sendEvent(event: Event)
}
