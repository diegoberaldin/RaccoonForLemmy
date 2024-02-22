package com.github.diegoberaldin.raccoonforlemmy.feature.settings.advanced

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxDefaultType
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxUnreadOnly
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration

class AdvancedSettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
    private val contentResetCoordinator: ContentResetCoordinator,
) : AdvancedSettingsMviModel,
    DefaultMviModel<AdvancedSettingsMviModel.Intent, AdvancedSettingsMviModel.UiState, AdvancedSettingsMviModel.Effect>(
        initialState = AdvancedSettingsMviModel.UiState(),
    ) {

    override fun onStarted() {
        super.onStarted()
        scope?.launch {
            themeRepository.navItemTitles.onEach { value ->
                updateState { it.copy(navBarTitlesVisible = value) }
            }.launchIn(this)

            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.ChangeZombieInterval::class)
                .onEach { evt ->
                    changeZombieModeInterval(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeZombieScrollAmount::class)
                .onEach { evt ->
                    changeZombieModeScrollAmount(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeInboxType::class)
                .onEach { evt ->
                    changeDefaultInboxUnreadOnly(evt.unreadOnly)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSystemBarTheme::class)
                .onEach { evt ->
                    changeSystemBarTheme(evt.value)
                }.launchIn(this)
        }

        val settings = settingsRepository.currentSettings.value
        updateState {
            it.copy(
                defaultInboxUnreadOnly = settings.defaultInboxType.toInboxUnreadOnly(),
                enableDoubleTapAction = settings.enableDoubleTapAction,
                autoLoadImages = settings.autoLoadImages,
                autoExpandComments = settings.autoExpandComments,
                hideNavigationBarWhileScrolling = settings.hideNavigationBarWhileScrolling,
                zombieModeInterval = settings.zombieModeInterval,
                zombieModeScrollAmount = settings.zombieModeScrollAmount,
                markAsReadWhileScrolling = settings.markAsReadWhileScrolling,
                searchPostTitleOnly = settings.searchPostTitleOnly,
                edgeToEdge = settings.edgeToEdge,
                infiniteScrollDisabled = !settings.infiniteScrollEnabled,
                opaqueSystemBars = settings.opaqueSystemBars,

                )
        }
    }

    override fun reduce(intent: AdvancedSettingsMviModel.Intent) {
        when (intent) {
            is AdvancedSettingsMviModel.Intent.ChangeNavBarTitlesVisible -> changeNavBarTitlesVisible(
                intent.value
            )

            is AdvancedSettingsMviModel.Intent.ChangeEnableDoubleTapAction ->
                changeEnableDoubleTapAction(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeAutoLoadImages -> changeAutoLoadImages(intent.value)
            is AdvancedSettingsMviModel.Intent.ChangeAutoExpandComments -> changeAutoExpandComments(
                intent.value
            )

            is AdvancedSettingsMviModel.Intent.ChangeHideNavigationBarWhileScrolling ->
                changeHideNavigationBarWhileScrolling(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeMarkAsReadWhileScrolling ->
                changeMarkAsReadWhileScrolling(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeSearchPostTitleOnly -> changeSearchPostTitleOnly(
                intent.value
            )

            is AdvancedSettingsMviModel.Intent.ChangeEdgeToEdge -> changeEdgeToEdge(intent.value)
            is AdvancedSettingsMviModel.Intent.ChangeInfiniteScrollDisabled ->
                changeInfiniteScrollDisabled(intent.value)
        }
    }

    private fun changeNavBarTitlesVisible(value: Boolean) {
        themeRepository.changeNavItemTitles(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                navigationTitlesVisible = value
            )
            saveSettings(settings)
        }
    }

    private fun changeEnableDoubleTapAction(value: Boolean) {
        updateState { it.copy(enableDoubleTapAction = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                enableDoubleTapAction = value
            )
            saveSettings(settings)
        }
    }

    private fun changeAutoLoadImages(value: Boolean) {
        updateState { it.copy(autoLoadImages = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                autoLoadImages = value
            )
            saveSettings(settings)
        }
    }

    private fun changeAutoExpandComments(value: Boolean) {
        updateState { it.copy(autoExpandComments = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                autoExpandComments = value
            )
            saveSettings(settings)
        }
    }

    private fun changeHideNavigationBarWhileScrolling(value: Boolean) {
        updateState { it.copy(hideNavigationBarWhileScrolling = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                hideNavigationBarWhileScrolling = value
            )
            saveSettings(settings)
        }
    }

    private fun changeMarkAsReadWhileScrolling(value: Boolean) {
        updateState { it.copy(markAsReadWhileScrolling = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                markAsReadWhileScrolling = value
            )
            saveSettings(settings)
        }
    }

    private fun changeZombieModeInterval(value: Duration) {
        updateState { it.copy(zombieModeInterval = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                zombieModeInterval = value
            )
            saveSettings(settings)
        }
    }

    private fun changeZombieModeScrollAmount(value: Float) {
        updateState { it.copy(zombieModeScrollAmount = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                zombieModeScrollAmount = value
            )
            saveSettings(settings)
        }
    }

    private fun changeDefaultInboxUnreadOnly(value: Boolean) {
        updateState { it.copy(defaultInboxUnreadOnly = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultInboxType = value.toInboxDefaultType(),
            )
            saveSettings(settings)
            contentResetCoordinator.resetInbox = true
        }
    }

    private fun changeSearchPostTitleOnly(value: Boolean) {
        updateState { it.copy(searchPostTitleOnly = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                searchPostTitleOnly = value
            )
            saveSettings(settings)
        }
    }

    private fun changeEdgeToEdge(value: Boolean) {
        updateState { it.copy(edgeToEdge = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                edgeToEdge = value
            )
            saveSettings(settings)
        }
    }

    private fun changeInfiniteScrollDisabled(value: Boolean) {
        updateState { it.copy(infiniteScrollDisabled = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                infiniteScrollEnabled = !value
            )
            saveSettings(settings)
        }
    }

    private fun changeSystemBarTheme(value: UiBarTheme) {
        val opaque = when (value) {
            UiBarTheme.Opaque -> true
            else -> false
        }
        updateState { it.copy(opaqueSystemBars = opaque) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                opaqueSystemBars = opaque
            )
            saveSettings(settings)
        }
    }

    private suspend fun saveSettings(settings: SettingsModel) {
        val accountId = accountRepository.getActive()?.id
        settingsRepository.updateSettings(settings, accountId)
        settingsRepository.changeCurrentSettings(settings)
    }
}
