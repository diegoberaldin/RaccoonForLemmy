package com.github.diegoberaldin.raccoonforlemmy

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.notification.InboxNotificationChecker
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyValueCache
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    private val inboxCoordinator: InboxCoordinator,
    private val identityRepository: IdentityRepository,
    private val settingRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val notificationChecker: InboxNotificationChecker,
    private val lemmyValueCache: LemmyValueCache,
) : DefaultMviModel<MainMviModel.Intent, MainMviModel.UiState, MainMviModel.Effect>(
        initialState = MainMviModel.UiState(),
    ),
    MainMviModel {
    init {
        screenModelScope.launch {
            identityRepository.startup()
            val auth = identityRepository.authToken.value
            lemmyValueCache.refresh(auth)

            inboxCoordinator.totalUnread
                .onEach { unreadCount ->
                    emitEffect(MainMviModel.Effect.UnreadItemsDetected(unreadCount))
                }.launchIn(this)

            settingRepository.currentSettings
                .map {
                    it.inboxBackgroundCheckPeriod
                }.distinctUntilChanged()
                .onEach {
                    val minutes = it?.inWholeMinutes
                    if (minutes != null) {
                        notificationChecker.setPeriod(minutes)
                        notificationChecker.start()
                    } else {
                        notificationChecker.stop()
                    }
                }.launchIn(this)

            settingRepository.currentSettings
                .onEach {
                    updateCustomProfileIcon()
                }.launchIn(this)

            identityRepository.isLogged
                .onEach { isLogged ->
                    updateState { it.copy(isLogged = isLogged ?: false) }
                    updateCustomProfileIcon()
                }.launchIn(this)
        }
    }

    override fun reduce(intent: MainMviModel.Intent) {
        when (intent) {
            is MainMviModel.Intent.SetBottomBarOffsetHeightPx -> {
                screenModelScope.launch {
                    updateState { it.copy(bottomBarOffsetHeightPx = intent.value) }
                }
            }

            MainMviModel.Intent.ReadAllInbox -> markAllRead()
        }
    }

    private suspend fun updateCustomProfileIcon() {
        val settings = settingRepository.currentSettings.value
        updateState {
            it.copy(
                customProfileUrl =
                    if (settings.useAvatarAsProfileNavigationIcon) {
                        identityRepository.cachedUser?.avatar
                    } else {
                        null
                    },
            )
        }
    }

    private fun markAllRead() {
        if (inboxCoordinator.totalUnread.value == 0) {
            return
        }

        screenModelScope.launch {
            val auth = identityRepository.authToken.value
            userRepository.readAll(auth)
            inboxCoordinator.sendEvent(InboxCoordinator.Event.Refresh)
            emitEffect(MainMviModel.Effect.ReadAllInboxSuccess)
        }
    }
}
