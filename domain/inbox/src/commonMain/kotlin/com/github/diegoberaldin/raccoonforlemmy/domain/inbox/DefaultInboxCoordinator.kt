package com.github.diegoberaldin.raccoonforlemmy.domain.inbox

import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.usecase.GetUnreadItemsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class DefaultInboxCoordinator(
    private val identityRepository: IdentityRepository,
    private val getUnreadItemsUseCase: GetUnreadItemsUseCase,
) : InboxCoordinator {
    private val scope = CoroutineScope(SupervisorJob())

    override val events = MutableSharedFlow<InboxCoordinator.Event>()
    override val unreadOnly = MutableStateFlow(true)
    override val unreadReplies = MutableStateFlow(0)
    override val unreadMentions = MutableStateFlow(0)
    override val unreadMessages = MutableStateFlow(0)
    override val totalUnread =
        combine(
            unreadMentions,
            unreadMessages,
            unreadReplies,
        ) { res1, res2, res3 ->
            res1 + res2 + res3
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0,
        )

    init {
        scope.launch {
            identityRepository.isLogged.onEach {
                updateCounters()
            }.launchIn(this)
        }
    }

    override fun setUnreadOnly(value: Boolean) {
        unreadOnly.value = value
    }

    override suspend fun updateUnreadCount(): Int {
        updateCounters()
        return totalUnread.value
    }

    private suspend fun updateCounters() {
        if (!identityRepository.authToken.value.isNullOrEmpty()) {
            unreadMentions.value = getUnreadItemsUseCase.getUnreadMentions()
            unreadReplies.value = getUnreadItemsUseCase.getUnreadReplies()
            unreadMessages.value = getUnreadItemsUseCase.getUnreadMessages()
        } else {
            unreadReplies.value = 0
            unreadMentions.value = 0
            unreadMessages.value = 0
        }
    }

    override suspend fun sendEvent(event: InboxCoordinator.Event) {
        events.emit(event)
    }
}
