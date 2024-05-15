package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxUnreadOnly
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxViewModel(
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val coordinator: InboxCoordinator,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
) : InboxMviModel,
    DefaultMviModel<InboxMviModel.Intent, InboxMviModel.UiState, InboxMviModel.Effect>(
        initialState = InboxMviModel.UiState(),
    ) {
        init {
            screenModelScope.launch {
                identityRepository.isLogged.onEach { logged ->
                    updateState { it.copy(isLogged = logged) }
                }.launchIn(this)

                coordinator.unreadMentions.onEach { value ->
                    updateState { it.copy(unreadMentions = value) }
                }.launchIn(this)
                coordinator.unreadReplies.onEach { value ->
                    updateState { it.copy(unreadReplies = value) }
                }.launchIn(this)
                coordinator.unreadMessages.onEach { value ->
                    updateState { it.copy(unreadMessages = value) }
                }.launchIn(this)

                notificationCenter.subscribe(NotificationCenterEvent.ChangeInboxType::class)
                    .onEach { evt ->
                        changeUnreadOnly(evt.unreadOnly)
                    }.launchIn(this)
                notificationCenter.subscribe(NotificationCenterEvent.ResetInbox::class).onEach {
                    onFirstLoad()
                }.launchIn(this)
            }
            onFirstLoad()
        }

        private fun onFirstLoad() {
            val settingsUnreadOnly = settingsRepository.currentSettings.value.defaultInboxType.toInboxUnreadOnly()
            if (uiState.value.unreadOnly != settingsUnreadOnly) {
                changeUnreadOnly(settingsUnreadOnly)
            }
        }

        override fun reduce(intent: InboxMviModel.Intent) {
            when (intent) {
                is InboxMviModel.Intent.ChangeSection ->
                    updateState {
                        it.copy(section = intent.value)
                    }

                InboxMviModel.Intent.ReadAll -> markAllRead()
            }
        }

        private fun changeUnreadOnly(value: Boolean) {
            updateState {
                it.copy(unreadOnly = value)
            }
            coordinator.setUnreadOnly(value)
        }

        private fun markAllRead() {
            screenModelScope.launch {
                val auth = identityRepository.authToken.value
                userRepository.readAll(auth)
                emitEffect(InboxMviModel.Effect.Refresh)
                coordinator.sendEvent(InboxCoordinator.Event.Refresh)
            }
        }
    }
