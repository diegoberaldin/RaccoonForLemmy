package com.github.diegoberaldin.raccoonforlemmy

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.notification.InboxNotificationChecker
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    private val inboxCoordinator: InboxCoordinator,
    private val identityRepository: IdentityRepository,
    private val settingRepository: SettingsRepository,
    private val notificationChecker: InboxNotificationChecker,
) : MainScreenMviModel,
    DefaultMviModel<MainScreenMviModel.Intent, MainScreenMviModel.UiState, MainScreenMviModel.Effect>(
        initialState = MainScreenMviModel.UiState(),
    ) {
    init {
        screenModelScope.launch {
            identityRepository.startup()

            inboxCoordinator.totalUnread.onEach { unreadCount ->
                emitEffect(MainScreenMviModel.Effect.UnreadItemsDetected(unreadCount))
            }.launchIn(this)

            settingRepository.currentSettings.map {
                it.inboxBackgroundCheckPeriod
            }.distinctUntilChanged().onEach {
                val minutes = it?.inWholeMinutes
                if (minutes != null) {
                    notificationChecker.setPeriod(minutes)
                    notificationChecker.start()
                } else {
                    notificationChecker.stop()
                }
            }
        }
    }

    override fun reduce(intent: MainScreenMviModel.Intent) {
        when (intent) {
            is MainScreenMviModel.Intent.SetBottomBarOffsetHeightPx -> {
                screenModelScope.launch {
                    updateState { it.copy(bottomBarOffsetHeightPx = intent.value) }
                }
            }
        }
    }
}
