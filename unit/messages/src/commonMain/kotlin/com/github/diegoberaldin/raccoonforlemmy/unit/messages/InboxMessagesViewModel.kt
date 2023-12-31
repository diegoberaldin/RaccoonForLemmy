package com.github.diegoberaldin.raccoonforlemmy.unit.messages

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.otherUser
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxMessagesViewModel(
    private val mvi: DefaultMviModel<InboxMessagesMviModel.Intent, InboxMessagesMviModel.UiState, InboxMessagesMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val messageRepository: PrivateMessageRepository,
    private val settingsRepository: SettingsRepository,
    private val coordinator: InboxCoordinator,
    private val notificationCenter: NotificationCenter,
) : InboxMessagesMviModel,
    MviModel<InboxMessagesMviModel.Intent, InboxMessagesMviModel.UiState, InboxMessagesMviModel.Effect> by mvi {

    private var currentPage: Int = 1

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            coordinator.events.onEach {
                when (it) {
                    InboxCoordinator.Event.Refresh -> refresh()
                }
            }.launchIn(this)
            coordinator.unreadOnly.onEach {
                if (it != uiState.value.unreadOnly) {
                    changeUnreadOnly(it)
                }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                handleLogout()
            }.launchIn(this)

            launch(Dispatchers.IO) {
                val auth = identityRepository.authToken.value.orEmpty()
                val currentUserId = siteRepository.getCurrentUser(auth)?.id ?: 0
                mvi.updateState { it.copy(currentUserId = currentUserId) }

                if (uiState.value.initial) {
                    val value = coordinator.unreadOnly.value
                    changeUnreadOnly(value)
                }
            }

            updateUnreadItems()
        }
    }

    override fun reduce(intent: InboxMessagesMviModel.Intent) {
        when (intent) {
            InboxMessagesMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }

            InboxMessagesMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }

            is InboxMessagesMviModel.Intent.MarkAsRead -> {
                markAsRead(
                    read = intent.read,
                    message = uiState.value.chats.first { it.id == intent.id },
                )
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        mvi.updateState {
            it.copy(
                initial = initial,
                canFetchMore = true,
                refreshing = true
            )
        }
        loadNextPage()
        updateUnreadItems()
    }

    private fun changeUnreadOnly(value: Boolean) {
        if (uiState.value.currentUserId == 0) {
            return
        }
        mvi.updateState { it.copy(unreadOnly = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            refresh(initial = true)
            mvi.emitEffect(InboxMessagesMviModel.Effect.BackToTop)
        }
    }

    private suspend fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val unreadOnly = currentState.unreadOnly
        val itemList = messageRepository.getAll(
            auth = auth,
            page = currentPage,
            unreadOnly = unreadOnly,
        )?.groupBy {
            it.otherUser(currentState.currentUserId)?.id ?: 0
        }?.mapNotNull { entry ->
            val messages = entry.value.sortedBy { m -> m.publishDate }
            messages.lastOrNull()
        }
        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }
        mvi.updateState {
            val newItems = if (refreshing) {
                itemList.orEmpty()
            } else {
                it.chats + itemList.orEmpty().filter { outerChat ->
                    val outerOtherUser = outerChat.otherUser(currentState.currentUserId)
                    currentState.chats.none { chat ->
                        val otherUser = chat.otherUser(currentState.currentUserId)
                        outerOtherUser == otherUser
                    }
                }
            }
            it.copy(
                chats = newItems,
                loading = false,
                canFetchMore = itemList?.isEmpty() != true,
                refreshing = false,
                initial = false,
            )
        }
    }

    private fun updateUnreadItems() {
        mvi.scope?.launch(Dispatchers.IO) {
            val unreadCount = coordinator.updateUnreadCount()
            mvi.emitEffect(InboxMessagesMviModel.Effect.UpdateUnreadItems(unreadCount))
        }
    }

    private fun markAsRead(read: Boolean, message: PrivateMessageModel) {
        val auth = identityRepository.authToken.value
        mvi.scope?.launch(Dispatchers.IO) {
            messageRepository.markAsRead(
                read = read,
                messageId = message.id,
                auth = auth,
            )
            val currentState = uiState.value
            if (read && currentState.unreadOnly) {
                mvi.updateState {
                    it.copy(
                        chats = currentState.chats.filter { c ->
                            c.id != message.id
                        }
                    )
                }
            } else {
                mvi.updateState {
                    it.copy(
                        chats = currentState.chats.map { c ->
                            if (c.id == message.id) {
                                c.copy(read = read)
                            } else {
                                c
                            }
                        }
                    )
                }
            }
            updateUnreadItems()
        }
    }

    private fun handleLogout() {
        mvi.updateState { it.copy(chats = emptyList()) }
    }
}