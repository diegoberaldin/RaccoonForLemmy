package com.github.diegoberaldin.raccoonforlemmy.feature.settings.advanced

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.usecase.ExportSettingsUseCase
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.usecase.ImportSettingsUseCase
import com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon.AppIconManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon.AppIconVariant
import com.github.diegoberaldin.raccoonforlemmy.core.utils.appicon.toAppIconVariant
import com.github.diegoberaldin.raccoonforlemmy.core.utils.fs.FileSystemManager
import com.github.diegoberaldin.raccoonforlemmy.core.utils.gallery.GalleryHelper
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxDefaultType
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxUnreadOnly
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
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
    private val siteRepository: SiteRepository,
    private val notificationCenter: NotificationCenter,
    private val galleryHelper: GalleryHelper,
    private val appIconManager: AppIconManager,
    private val fileSystemManager: FileSystemManager,
    private val importSettings: ImportSettingsUseCase,
    private val exportSettings: ExportSettingsUseCase,
) : AdvancedSettingsMviModel,
    DefaultMviModel<AdvancedSettingsMviModel.Intent, AdvancedSettingsMviModel.UiState, AdvancedSettingsMviModel.Effect>(
        initialState = AdvancedSettingsMviModel.UiState(),
    ) {

    init {
        screenModelScope.launch {
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
            notificationCenter.subscribe(NotificationCenterEvent.ChangeFeedType::class)
                .onEach { evt ->
                    if (evt.screenKey == "advancedSettings") {
                        changeExploreType(evt.value)
                    }
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
            notificationCenter.subscribe(NotificationCenterEvent.ChangeInboxBackgroundCheckPeriod::class)
                .onEach { evt ->
                    changeInboxBackgroundCheckPeriod(evt.value)
                }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.AppIconVariantSelected::class)
                .onEach { evt ->
                    changeAppIconVariant(evt.value.toAppIconVariant())
                }.launchIn(this)

            updateAvailableLanguages()
        }

        val settings = settingsRepository.currentSettings.value
        updateState {
            it.copy(
                defaultExploreType = settings.defaultExploreType.toListingType(),
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
                imageSourceSupported = galleryHelper.supportsCustomPath,
                imageSourcePath = settings.imageSourcePath,
                defaultLanguageId = settings.defaultLanguageId,
                appIconChangeSupported = appIconManager.supportsMultipleIcons,
                fadeReadPosts = settings.fadeReadPosts,
                showUnreadComments = settings.showUnreadComments,
                supportSettingsImportExport = fileSystemManager.isSupported,
                enableButtonsToScrollBetweenComments = settings.enableButtonsToScrollBetweenComments,
            )
        }
    }

    override fun reduce(intent: AdvancedSettingsMviModel.Intent) {
        when (intent) {
            is AdvancedSettingsMviModel.Intent.ChangeNavBarTitlesVisible ->
                changeNavBarTitlesVisible(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeEnableDoubleTapAction ->
                changeEnableDoubleTapAction(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeAutoLoadImages -> changeAutoLoadImages(intent.value)
            is AdvancedSettingsMviModel.Intent.ChangeAutoExpandComments ->
                changeAutoExpandComments(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeHideNavigationBarWhileScrolling ->
                changeHideNavigationBarWhileScrolling(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeMarkAsReadWhileScrolling ->
                changeMarkAsReadWhileScrolling(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeSearchPostTitleOnly -> changeSearchPostTitleOnly(intent.value)
            is AdvancedSettingsMviModel.Intent.ChangeEdgeToEdge -> changeEdgeToEdge(intent.value)
            is AdvancedSettingsMviModel.Intent.ChangeInfiniteScrollDisabled ->
                changeInfiniteScrollDisabled(intent.value)

            is AdvancedSettingsMviModel.Intent.ChangeImageSourcePath -> changeImageSourcePath(intent.value)
            is AdvancedSettingsMviModel.Intent.ChangeDefaultLanguage -> changeDefaultLanguageId(intent.value)
            is AdvancedSettingsMviModel.Intent.ChangeFadeReadPosts -> changeFadeReadPosts(intent.value)
            is AdvancedSettingsMviModel.Intent.ChangeShowUnreadComments -> changeShowUnreadPosts(intent.value)
            is AdvancedSettingsMviModel.Intent.ExportSettings -> handleExportSettings()
            is AdvancedSettingsMviModel.Intent.ImportSettings -> handleImportSettings(intent.content)
            is AdvancedSettingsMviModel.Intent.ChangeEnableButtonsToScrollBetweenComments ->
                changeEnableButtonsToScrollBetweenComments(intent.value)
        }
    }

    private fun changeNavBarTitlesVisible(value: Boolean) {
        themeRepository.changeNavItemTitles(value)
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(navigationTitlesVisible = value)
            saveSettings(settings)
        }
    }

    private fun changeEnableDoubleTapAction(value: Boolean) {
        updateState { it.copy(enableDoubleTapAction = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(enableDoubleTapAction = value)
            saveSettings(settings)
        }
    }

    private fun changeAutoLoadImages(value: Boolean) {
        updateState { it.copy(autoLoadImages = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(autoLoadImages = value)
            saveSettings(settings)
        }
    }

    private fun changeAutoExpandComments(value: Boolean) {
        updateState { it.copy(autoExpandComments = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(autoExpandComments = value)
            saveSettings(settings)
        }
    }

    private fun changeHideNavigationBarWhileScrolling(value: Boolean) {
        updateState { it.copy(hideNavigationBarWhileScrolling = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(hideNavigationBarWhileScrolling = value)
            saveSettings(settings)
        }
    }

    private fun changeMarkAsReadWhileScrolling(value: Boolean) {
        updateState { it.copy(markAsReadWhileScrolling = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(markAsReadWhileScrolling = value)
            saveSettings(settings)
        }
    }

    private fun changeZombieModeInterval(value: Duration) {
        updateState { it.copy(zombieModeInterval = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(zombieModeInterval = value)
            saveSettings(settings)
        }
    }

    private fun changeZombieModeScrollAmount(value: Float) {
        updateState { it.copy(zombieModeScrollAmount = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(zombieModeScrollAmount = value)
            saveSettings(settings)
        }
    }

    private fun changeDefaultInboxUnreadOnly(value: Boolean) {
        updateState { it.copy(defaultInboxUnreadOnly = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(defaultInboxType = value.toInboxDefaultType())
            saveSettings(settings)
            notificationCenter.send(NotificationCenterEvent.ResetInbox)
        }
    }

    private fun changeSearchPostTitleOnly(value: Boolean) {
        updateState { it.copy(searchPostTitleOnly = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(searchPostTitleOnly = value)
            saveSettings(settings)
        }
    }

    private fun changeExploreType(value: ListingType) {
        updateState { it.copy(defaultExploreType = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(defaultExploreType = value.toInt())
            saveSettings(settings)
        }

    }

    private fun changeEdgeToEdge(value: Boolean) {
        updateState { it.copy(edgeToEdge = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(edgeToEdge = value)
            saveSettings(settings)
        }
    }

    private fun changeInfiniteScrollDisabled(value: Boolean) {
        updateState { it.copy(infiniteScrollDisabled = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(infiniteScrollEnabled = !value)
            saveSettings(settings)
        }
    }

    private fun changeSystemBarTheme(value: UiBarTheme) {
        val opaque = when (value) {
            UiBarTheme.Opaque -> true
            else -> false
        }
        updateState { it.copy(opaqueSystemBars = opaque) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(opaqueSystemBars = opaque)
            saveSettings(settings)
        }
    }

    private fun changeImageSourcePath(value: Boolean) {
        updateState { it.copy(imageSourcePath = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(imageSourcePath = value)
            saveSettings(settings)
        }
    }

    private fun updateAvailableLanguages() {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value
            val languages = siteRepository.getLanguages(auth)
            updateState { it.copy(availableLanguages = languages) }
        }
    }

    private fun changeDefaultLanguageId(value: Long?) {
        updateState { it.copy(defaultLanguageId = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(defaultLanguageId = value)
            saveSettings(settings)
        }
    }

    private fun changeInboxBackgroundCheckPeriod(value: Duration) {
        updateState { it.copy(inboxBackgroundCheckPeriod = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(inboxBackgroundCheckPeriod = value)
            saveSettings(settings)
        }
    }

    private fun changeFadeReadPosts(value: Boolean) {
        updateState { it.copy(fadeReadPosts = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(fadeReadPosts = value)
            saveSettings(settings)
        }
    }

    private fun changeShowUnreadPosts(value: Boolean) {
        updateState { it.copy(showUnreadComments = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(showUnreadComments = value)
            saveSettings(settings)
        }
    }

    private fun changeEnableButtonsToScrollBetweenComments(value: Boolean) {
        updateState { it.copy(enableButtonsToScrollBetweenComments = value) }
        screenModelScope.launch(Dispatchers.IO) {
            val settings = settingsRepository.currentSettings.value.copy(enableButtonsToScrollBetweenComments = value)
            saveSettings(settings)
        }
    }

    private suspend fun saveSettings(settings: SettingsModel) {
        val accountId = accountRepository.getActive()?.id
        settingsRepository.updateSettings(settings, accountId)
        settingsRepository.changeCurrentSettings(settings)
    }

    private fun changeAppIconVariant(value: AppIconVariant) {
        appIconManager.changeIcon(value)
    }

    private fun handleImportSettings(content: String) {
        screenModelScope.launch {
            updateState { it.copy(loading = true) }
            importSettings(content)
            updateState { it.copy(loading = false) }
        }
    }

    private fun handleExportSettings() {
        screenModelScope.launch {
            updateState { it.copy(loading = true) }
            val content = exportSettings()
            updateState { it.copy(loading = false) }
            emitEffect(AdvancedSettingsMviModel.Effect.SaveSettings(content))
        }
    }
}
