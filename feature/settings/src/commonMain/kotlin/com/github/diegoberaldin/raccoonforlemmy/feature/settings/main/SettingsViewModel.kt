package com.github.diegoberaldin.raccoonforlemmy.feature.settings.main

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.L10nManager
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.CrashReportConfiguration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.CustomTabsHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.UrlOpeningMode
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.utils.url.toUrlOpeningMode
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.GetSortTypesUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
    private val crashReportConfiguration: CrashReportConfiguration,
    private val l10nManager: L10nManager,
    private val getSortTypesUseCase: GetSortTypesUseCase,
    private val customTabsHelper: CustomTabsHelper,
) : SettingsMviModel,
    DefaultMviModel<SettingsMviModel.Intent, SettingsMviModel.UiState, SettingsMviModel.Effect>(
        initialState = SettingsMviModel.UiState(),
    ) {
    init {
        screenModelScope.launch {
            themeRepository.uiTheme.onEach { value ->
                updateState { it.copy(uiTheme = value) }
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
                    if (evt.screenKey == "settings") {
                        changeDefaultListingType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == "settings") {
                        changeDefaultPostSortType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeCommentSortType::class)
                .onEach { evt ->
                    if (evt.screenKey == "settings") {
                        changeDefaultCommentSortType(evt.value)
                    }
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.ChangeUrlOpeningMode::class)
                .onEach { evt ->
                    changeUrlOpeningMode(evt.value.toUrlOpeningMode())
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
                includeNsfw = settings.includeNsfw,
                blurNsfw = settings.blurNsfw,
                urlOpeningMode = settings.urlOpeningMode.toUrlOpeningMode(),
                enableSwipeActions = settings.enableSwipeActions,
                crashReportEnabled = crashReportConfiguration.isEnabled(),
                customTabsEnabled = customTabsHelper.isSupported,
            )
        }
    }

    override fun reduce(intent: SettingsMviModel.Intent) {
        when (intent) {
            is SettingsMviModel.Intent.ChangeUiTheme -> changeTheme(intent.value)
            is SettingsMviModel.Intent.ChangeLanguage -> changeLanguage(intent.value)
            is SettingsMviModel.Intent.ChangeBlurNsfw -> changeBlurNsfw(intent.value)
            is SettingsMviModel.Intent.ChangeIncludeNsfw -> changeIncludeNsfw(intent.value)
            is SettingsMviModel.Intent.ChangeEnableSwipeActions -> changeEnableSwipeActions(intent.value)
            is SettingsMviModel.Intent.ChangeCrashReportEnabled -> changeCrashReportEnabled(intent.value)
        }
    }

    private fun changeTheme(value: UiTheme?) {
        themeRepository.changeUiTheme(value)
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    theme = value?.toInt(),
                )
            saveSettings(settings)
        }
    }

    private fun changeLanguage(value: String) {
        l10nManager.changeLanguage(value)
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    locale = value,
                )
            saveSettings(settings)
        }
    }

    private fun changeDefaultListingType(value: ListingType) {
        updateState { it.copy(defaultListingType = value) }
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    defaultListingType = value.toInt(),
                )
            saveSettings(settings)
            notificationCenter.send(NotificationCenterEvent.ResetHome)
        }
    }

    private fun changeDefaultPostSortType(value: SortType) {
        updateState { it.copy(defaultPostSortType = value) }
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    defaultPostSortType = value.toInt(),
                )
            saveSettings(settings)
            notificationCenter.send(NotificationCenterEvent.ResetHome)
        }
    }

    private fun changeDefaultCommentSortType(value: SortType) {
        updateState { it.copy(defaultCommentSortType = value) }
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    defaultCommentSortType = value.toInt(),
                )
            saveSettings(settings)
        }
    }

    private fun changeIncludeNsfw(value: Boolean) {
        updateState { it.copy(includeNsfw = value) }
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    includeNsfw = value,
                )
            saveSettings(settings)
            notificationCenter.send(NotificationCenterEvent.ResetHome)
            notificationCenter.send(NotificationCenterEvent.ResetExplore)
        }
    }

    private fun changeBlurNsfw(value: Boolean) {
        updateState { it.copy(blurNsfw = value) }
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    blurNsfw = value,
                )
            saveSettings(settings)
        }
    }

    private fun changeUrlOpeningMode(value: UrlOpeningMode) {
        updateState { it.copy(urlOpeningMode = value) }
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    urlOpeningMode = value.toInt(),
                )
            saveSettings(settings)
        }
    }

    private fun changeEnableSwipeActions(value: Boolean) {
        updateState { it.copy(enableSwipeActions = value) }
        screenModelScope.launch {
            val settings =
                settingsRepository.currentSettings.value.copy(
                    enableSwipeActions = value,
                )
            saveSettings(settings)
        }
    }

    private fun changeCrashReportEnabled(value: Boolean) {
        crashReportConfiguration.setEnabled(value)
        updateState { it.copy(crashReportEnabled = value) }
    }

    private suspend fun saveSettings(settings: SettingsModel) {
        val accountId = accountRepository.getActive()?.id
        settingsRepository.updateSettings(settings, accountId)
        settingsRepository.changeCurrentSettings(settings)
    }

    private fun handleLogout() {
        screenModelScope.launch {
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
