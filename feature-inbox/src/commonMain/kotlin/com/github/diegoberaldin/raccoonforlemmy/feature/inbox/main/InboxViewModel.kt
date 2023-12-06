package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxUnreadOnly
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.InboxCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxViewModel(
    private val mvi: DefaultMviModel<InboxMviModel.Intent, InboxMviModel.UiState, InboxMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val coordinator: InboxCoordinator,
    private val settingsRepository: SettingsRepository,
    private val contentResetCoordinator: ContentResetCoordinator,
) : InboxMviModel,
    MviModel<InboxMviModel.Intent, InboxMviModel.UiState, InboxMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            identityRepository.isLogged.onEach { logged ->
                mvi.updateState { it.copy(isLogged = logged) }
            }.launchIn(this)
            val settingsUnreadOnly =
                settingsRepository.currentSettings.value.defaultInboxType.toInboxUnreadOnly()
            if (uiState.value.unreadOnly != settingsUnreadOnly) {
                changeUnreadOnly(settingsUnreadOnly)
            }
        }
    }

    override fun reduce(intent: InboxMviModel.Intent) {
        when (intent) {
            is InboxMviModel.Intent.ChangeSection -> mvi.updateState {
                it.copy(section = intent.value)
            }

            is InboxMviModel.Intent.ChangeUnreadOnly -> changeUnreadOnly(intent.unread)
            InboxMviModel.Intent.ReadAll -> markAllRead()
        }
    }

    private fun changeUnreadOnly(value: Boolean) {
        mvi.updateState {
            it.copy(unreadOnly = value)
        }
        coordinator.setUnreadOnly(value)
    }

    private fun markAllRead() {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            userRepository.readAll(auth)
            mvi.emitEffect(InboxMviModel.Effect.Refresh)
            coordinator.emitEffect(InboxMviModel.Effect.Refresh)
        }
    }
}
