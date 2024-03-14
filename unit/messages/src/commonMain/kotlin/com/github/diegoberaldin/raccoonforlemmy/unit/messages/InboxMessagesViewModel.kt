package com.github.diegoberaldin.raccoonforlemmy.unit.messages

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.otherUser
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxMessagesViewModel(
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val messageRepository: PrivateMessageRepository,
    private val settingsRepository: SettingsRepository,
    private val coordinator: InboxCoordinator,
    private val notificationCenter: NotificationCenter,
) : InboxMessagesMviModel,
    DefaultMviModel<InboxMessagesMviModel.Intent, InboxMessagesMviModel.UiState, InboxMessagesMviModel.Effect>(
        initialState = InboxMessagesMviModel.UiState(),
    ) {

    private var currentPage: Int = 1

    init {
        screenModelScope.launch {
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
                updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                handleLogout()
            }.launchIn(this)

            launch(Dispatchers.IO) {
                val auth = identityRepository.authToken.value.orEmpty()
                val currentUserId = siteRepository.getCurrentUser(auth)?.id ?: 0
                updateState { it.copy(currentUserId = currentUserId) }

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
            InboxMessagesMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                loadNextPage()
            }

            InboxMessagesMviModel.Intent.Refresh -> screenModelScope.launch {
                refresh()
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        updateState {
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
        updateState { it.copy(unreadOnly = value) }
        screenModelScope.launch {
            refresh(initial = true)
            emitEffect(InboxMessagesMviModel.Effect.BackToTop)
        }
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        updateState { it.copy(loading = true) }
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
        updateState {
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
        screenModelScope.launch {
            val unreadCount = coordinator.updateUnreadCount()
            emitEffect(InboxMessagesMviModel.Effect.UpdateUnreadItems(unreadCount))
        }
    }

    private fun handleLogout() {
        updateState { it.copy(chats = emptyList()) }
    }
}