package com.github.diegoberaldin.raccoonforlemmy.feature.settings.main

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.L10nManager
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.CrashReportConfiguration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxDefaultType
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxUnreadOnly
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
    private val crashReportConfiguration: CrashReportConfiguration,
    private val contentResetCoordinator: ContentResetCoordinator,
    private val l10nManager: L10nManager,
    private val getSortTypesUseCase: GetSortTypesUseCase,
) : SettingsMviModel,
    DefaultMviModel<SettingsMviModel.Intent, SettingsMviModel.UiState, SettingsMviModel.Effect>(
        initialState = SettingsMviModel.UiState(),
    ) {

    override fun onStarted() {
        super.onStarted()
        scope?.launch {
            themeRepository.uiTheme.onEach { value ->
                updateState { it.copy(uiTheme = value) }
            }.launchIn(this)
            themeRepository.navItemTitles.onEach { value ->
                updateState { it.copy(navBarTitlesVisible = value) }
            }.launchIn(this)

            l10nManager.lyricist.state.onEach { lang ->
                updateState { it.copy(lang = lang.languageTag) }
            }.launchIn(this)

            identityRepository.isLogged.onEach { logged ->
                updateState { it.copy(isLogged = logged ?: false) }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                handleLogout()
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeLanguage::class)
                .onEach { evt ->
                    changeLanguage(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeTheme::class).onEach { evt ->
                changeTheme(evt.value)
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeFeedType::class)
                .onEach { evt ->
                    changeDefaultListingType(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    changeDefaultPostSortType(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeCommentSortType::class)
                .onEach { evt ->
                    changeDefaultCommentSortType(evt.value)
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

            val availableSortTypesForPosts = getSortTypesUseCase.getTypesForPosts()
            val availableSortTypesForComments = getSortTypesUseCase.getTypesForComments()
            updateState {
                it.copy(
                    availableSortTypesForPosts = availableSortTypesForPosts,
                    availableSortTypesForComments = availableSortTypesForComments,
                )
            }
        }

        val settings = settingsRepository.currentSettings.value
        updateState {
            it.copy(
                defaultListingType = settings.defaultListingType.toListingType(),
                defaultPostSortType = settings.defaultPostSortType.toSortType(),
                defaultCommentSortType = settings.defaultCommentSortType.toSortType(),
                defaultInboxUnreadOnly = settings.defaultInboxType.toInboxUnreadOnly(),
                includeNsfw = settings.includeNsfw,
                blurNsfw = settings.blurNsfw,
                openUrlsInExternalBrowser = settings.openUrlsInExternalBrowser,
                enableSwipeActions = settings.enableSwipeActions,
                enableDoubleTapAction = settings.enableDoubleTapAction,
                crashReportEnabled = crashReportConfiguration.isEnabled(),
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

    override fun reduce(intent: SettingsMviModel.Intent) {
        when (intent) {
            is SettingsMviModel.Intent.ChangeUiTheme -> changeTheme(intent.value)
            is SettingsMviModel.Intent.ChangeLanguage -> changeLanguage(intent.value)
            is SettingsMviModel.Intent.ChangeBlurNsfw -> changeBlurNsfw(intent.value)
            is SettingsMviModel.Intent.ChangeIncludeNsfw -> changeIncludeNsfw(intent.value)
            is SettingsMviModel.Intent.ChangeNavBarTitlesVisible -> changeNavBarTitlesVisible(intent.value)
            is SettingsMviModel.Intent.ChangeOpenUrlsInExternalBrowser ->
                changeOpenUrlsInExternalBrowser(intent.value)

            is SettingsMviModel.Intent.ChangeEnableSwipeActions -> changeEnableSwipeActions(intent.value)
            is SettingsMviModel.Intent.ChangeEnableDoubleTapAction ->
                changeEnableDoubleTapAction(intent.value)

            is SettingsMviModel.Intent.ChangeCrashReportEnabled -> changeCrashReportEnabled(intent.value)
            is SettingsMviModel.Intent.ChangeAutoLoadImages -> changeAutoLoadImages(intent.value)
            is SettingsMviModel.Intent.ChangeAutoExpandComments -> changeAutoExpandComments(intent.value)
            is SettingsMviModel.Intent.ChangeHideNavigationBarWhileScrolling ->
                changeHideNavigationBarWhileScrolling(intent.value)

            is SettingsMviModel.Intent.ChangeMarkAsReadWhileScrolling ->
                changeMarkAsReadWhileScrolling(intent.value)

            is SettingsMviModel.Intent.ChangeDefaultInboxUnreadOnly ->
                changeDefaultInboxUnreadOnly(intent.value)

            is SettingsMviModel.Intent.ChangeSearchPostTitleOnly -> changeSearchPostTitleOnly(intent.value)
            is SettingsMviModel.Intent.ChangeEdgeToEdge -> changeEdgeToEdge(intent.value)
            is SettingsMviModel.Intent.ChangeInfiniteScrollDisabled ->
                changeInfiniteScrollDisabled(intent.value)
        }
    }

    private fun changeTheme(value: UiTheme?) {
        themeRepository.changeUiTheme(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                theme = value?.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeLanguage(value: String) {
        l10nManager.changeLanguage(value)
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                locale = value
            )
            saveSettings(settings)
        }
    }

    private fun changeDefaultListingType(value: ListingType) {
        updateState { it.copy(defaultListingType = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultListingType = value.toInt()
            )
            saveSettings(settings)
            contentResetCoordinator.resetHome = true
            contentResetCoordinator.resetExplore = true
        }
    }

    private fun changeDefaultPostSortType(value: SortType) {
        updateState { it.copy(defaultPostSortType = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultPostSortType = value.toInt()
            )
            saveSettings(settings)
            contentResetCoordinator.resetHome = true
            contentResetCoordinator.resetExplore = true
        }
    }

    private fun changeDefaultCommentSortType(value: SortType) {
        updateState { it.copy(defaultCommentSortType = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultCommentSortType = value.toInt()
            )
            saveSettings(settings)
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

    private fun changeIncludeNsfw(value: Boolean) {
        updateState { it.copy(includeNsfw = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                includeNsfw = value
            )
            saveSettings(settings)
        }
    }

    private fun changeBlurNsfw(value: Boolean) {
        updateState { it.copy(blurNsfw = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                blurNsfw = value
            )
            saveSettings(settings)
        }
    }

    private fun changeOpenUrlsInExternalBrowser(value: Boolean) {
        updateState { it.copy(openUrlsInExternalBrowser = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                openUrlsInExternalBrowser = value
            )
            saveSettings(settings)
        }
    }

    private fun changeEnableSwipeActions(value: Boolean) {
        updateState { it.copy(enableSwipeActions = value) }
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(
                enableSwipeActions = value
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

    private fun changeCrashReportEnabled(value: Boolean) {
        crashReportConfiguration.setEnabled(value)
        updateState { it.copy(crashReportEnabled = value) }
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

    private fun handleLogout() {
        scope?.launch(Dispatchers.IO) {
            val settings = settingsRepository.getSettings(null)
            updateState {
                it.copy(
                    defaultListingType = settings.defaultListingType.toListingType(),
                    defaultPostSortType = settings.defaultPostSortType.toSortType(),
                    defaultCommentSortType = settings.defaultCommentSortType.toSortType(),
                )
            }
        }
    }
}
