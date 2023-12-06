package com.github.diegoberaldin.raccoonforlemmy.feature.settings.main

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toFontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.ContentResetCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.SettingsModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.CrashReportConfiguration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.CrashReportSender
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxDefaultType
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toInboxUnreadOnly
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.resources.LanguageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration

class SettingsViewModel(
    private val mvi: DefaultMviModel<SettingsMviModel.Intent, SettingsMviModel.UiState, SettingsMviModel.Effect>,
    private val themeRepository: ThemeRepository,
    private val colorSchemeProvider: ColorSchemeProvider,
    private val languageRepository: LanguageRepository,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
    private val crashReportConfiguration: CrashReportConfiguration,
    private val crashReportSender: CrashReportSender,
    private val contentResetCoordinator: ContentResetCoordinator,
) : SettingsMviModel,
    MviModel<SettingsMviModel.Intent, SettingsMviModel.UiState, SettingsMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch(Dispatchers.Main) {
            themeRepository.uiTheme.onEach { currentTheme ->
                mvi.updateState { it.copy(uiTheme = currentTheme) }
            }.launchIn(this)
            themeRepository.uiFontFamily.onEach { fontFamily ->
                mvi.updateState { it.copy(uiFontFamily = fontFamily) }
            }.launchIn(this)
            themeRepository.contentFontScale.onEach { value ->
                mvi.updateState { it.copy(contentFontScale = value.toFontScale()) }
            }.launchIn(this)
            themeRepository.uiFontScale.onEach { value ->
                mvi.updateState { it.copy(uiFontScale = value.toFontScale()) }
            }.launchIn(this)
            themeRepository.navItemTitles.onEach { value ->
                mvi.updateState { it.copy(navBarTitlesVisible = value) }
            }.launchIn(this)
            themeRepository.dynamicColors.onEach { value ->
                mvi.updateState { it.copy(dynamicColors = value) }
            }.launchIn(this)
            themeRepository.customSeedColor.onEach { value ->
                mvi.updateState { it.copy(customSeedColor = value) }
            }.launchIn(this)
            themeRepository.postLayout.onEach { value ->
                mvi.updateState { it.copy(postLayout = value) }
            }.launchIn(this)
            themeRepository.upvoteColor.onEach { value ->
                mvi.updateState { it.copy(upvoteColor = value) }
            }.launchIn(this)
            themeRepository.downvoteColor.onEach { value ->
                mvi.updateState { it.copy(downvoteColor = value) }
            }.launchIn(this)
            languageRepository.currentLanguage.onEach { lang ->
                mvi.updateState { it.copy(lang = lang) }
            }.launchIn(this)
            identityRepository.authToken.onEach { auth ->
                mvi.updateState { it.copy(isLogged = !auth.isNullOrEmpty()) }
            }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                handleLogout()
            }.launchIn(this)
        }

        val settings = settingsRepository.currentSettings.value
        mvi.updateState {
            it.copy(
                defaultListingType = settings.defaultListingType.toListingType(),
                defaultPostSortType = settings.defaultPostSortType.toSortType(),
                defaultCommentSortType = settings.defaultCommentSortType.toSortType(),
                defaultInboxUnreadOnly = settings.defaultInboxType.toInboxUnreadOnly(),
                includeNsfw = settings.includeNsfw,
                blurNsfw = settings.blurNsfw,
                supportsDynamicColors = colorSchemeProvider.supportsDynamicColors,
                openUrlsInExternalBrowser = settings.openUrlsInExternalBrowser,
                enableSwipeActions = settings.enableSwipeActions,
                enableDoubleTapAction = settings.enableDoubleTapAction,
                crashReportEnabled = crashReportConfiguration.isEnabled(),
                voteFormat = settings.voteFormat,
                autoLoadImages = settings.autoLoadImages,
                autoExpandComments = settings.autoExpandComments,
                fullHeightImages = settings.fullHeightImages,
                hideNavigationBarWhileScrolling = settings.hideNavigationBarWhileScrolling,
                zombieModeInterval = settings.zombieModeInterval,
                zombieModeScrollAmount = settings.zombieModeScrollAmount,
                markAsReadWhileScrolling = settings.markAsReadWhileScrolling,
            )
        }
    }

    override fun reduce(intent: SettingsMviModel.Intent) {
        when (intent) {
            is SettingsMviModel.Intent.ChangeUiTheme -> {
                changeTheme(intent.value)
            }

            is SettingsMviModel.Intent.ChangeUiFontFamily -> {
                changeFontFamily(intent.value)
            }

            is SettingsMviModel.Intent.ChangeContentFontSize -> {
                changeContentFontScale(intent.value)
            }

            is SettingsMviModel.Intent.ChangeUiFontSize -> {
                changeUiFontScale(intent.value)
            }

            is SettingsMviModel.Intent.ChangeLanguage -> {
                changeLanguage(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDefaultCommentSortType -> {
                changeDefaultCommentSortType(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDefaultListingType -> {
                changeDefaultListingType(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDefaultPostSortType -> {
                changeDefaultPostSortType(intent.value)
            }

            is SettingsMviModel.Intent.ChangeBlurNsfw -> {
                changeBlurNsfw(intent.value)
            }

            is SettingsMviModel.Intent.ChangeIncludeNsfw -> {
                changeIncludeNsfw(intent.value)
            }

            is SettingsMviModel.Intent.ChangeNavBarTitlesVisible -> {
                changeNavBarTitlesVisible(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDynamicColors -> {
                changeDynamicColors(intent.value)
            }

            is SettingsMviModel.Intent.ChangeOpenUrlsInExternalBrowser -> {
                changeOpenUrlsInExternalBrowser(intent.value)
            }

            is SettingsMviModel.Intent.ChangeEnableSwipeActions -> {
                changeEnableSwipeActions(intent.value)
            }

            is SettingsMviModel.Intent.ChangeEnableDoubleTapAction -> {
                changeEnableDoubleTapAction(intent.value)
            }

            is SettingsMviModel.Intent.ChangeCustomSeedColor -> {
                changeCustomSeedColor(intent.value)
            }

            is SettingsMviModel.Intent.ChangePostLayout -> {
                changePostLayout(intent.value)
            }

            is SettingsMviModel.Intent.ChangeCrashReportEnabled -> {
                changeCrashReportEnabled(intent.value)
            }

            is SettingsMviModel.Intent.ChangeVoteFormat -> {
                changeVoteFormat(intent.value)
            }

            is SettingsMviModel.Intent.ChangeAutoLoadImages -> {
                changeAutoLoadImages(intent.value)
            }

            is SettingsMviModel.Intent.ChangeAutoExpandComments -> {
                changeAutoExpandComments(intent.value)
            }

            is SettingsMviModel.Intent.ChangeFullHeightImages -> {
                changeFullHeightImages(intent.value)
            }

            is SettingsMviModel.Intent.ChangeUpvoteColor -> {
                changeUpvoteColor(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDownvoteColor -> {
                changeDownvoteColor(intent.value)
            }

            is SettingsMviModel.Intent.ChangeHideNavigationBarWhileScrolling -> {
                changeHideNavigationBarWhileScrolling(intent.value)
            }

            is SettingsMviModel.Intent.ChangeZombieModeInterval -> {
                changeZombieModeInterval(intent.value)
            }

            is SettingsMviModel.Intent.ChangeZombieModeScrollAmount -> {
                changeZombieModeScrollAmount(intent.value)
            }

            is SettingsMviModel.Intent.ChangeMarkAsReadWhileScrolling -> {
                changeMarkAsReadWhileScrolling(intent.value)
            }

            is SettingsMviModel.Intent.ChangeDefaultInboxUnreadOnly -> {
                changeDefaultInboxUnreadOnly(intent.value)
            }
        }
    }

    private fun changeTheme(value: UiTheme?) {
        themeRepository.changeUiTheme(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                theme = value?.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeFontFamily(value: UiFontFamily) {
        themeRepository.changeUiFontFamily(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                uiFontFamily = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeUiFontScale(value: Float) {
        themeRepository.changeUiFontScale(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                uiFontScale = value
            )
            saveSettings(settings)
        }
    }

    private fun changeContentFontScale(value: Float) {
        themeRepository.changeContentFontScale(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                contentFontScale = value
            )
            saveSettings(settings)
        }
    }

    private fun changeLanguage(value: String) {
        languageRepository.changeLanguage(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                locale = value
            )
            saveSettings(settings)
        }
    }

    private fun changeDefaultListingType(value: ListingType) {
        mvi.updateState { it.copy(defaultListingType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultListingType = value.toInt()
            )
            saveSettings(settings)
            contentResetCoordinator.resetHome = true
            contentResetCoordinator.resetExplore = true
        }
    }

    private fun changeDefaultPostSortType(value: SortType) {
        mvi.updateState { it.copy(defaultPostSortType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultPostSortType = value.toInt()
            )
            saveSettings(settings)
            contentResetCoordinator.resetHome = true
            contentResetCoordinator.resetExplore = true
        }
    }

    private fun changeDefaultCommentSortType(value: SortType) {
        mvi.updateState { it.copy(defaultCommentSortType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultCommentSortType = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeNavBarTitlesVisible(value: Boolean) {
        themeRepository.changeNavItemTitles(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                navigationTitlesVisible = value
            )
            saveSettings(settings)
        }
    }

    private fun changeIncludeNsfw(value: Boolean) {
        mvi.updateState { it.copy(includeNsfw = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                includeNsfw = value
            )
            saveSettings(settings)
        }
    }

    private fun changeBlurNsfw(value: Boolean) {
        mvi.updateState { it.copy(blurNsfw = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                blurNsfw = value
            )
            saveSettings(settings)
        }
    }

    private fun changeDynamicColors(value: Boolean) {
        themeRepository.changeDynamicColors(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                dynamicColors = value
            )
            saveSettings(settings)
        }
    }

    private fun changeCustomSeedColor(value: Color?) {
        themeRepository.changeCustomSeedColor(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                customSeedColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeUpvoteColor(value: Color?) {
        themeRepository.changeUpvoteColor(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                upvoteColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeDownvoteColor(value: Color?) {
        themeRepository.changeDownvoteColor(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                downvoteColor = value?.toArgb()
            )
            saveSettings(settings)
        }
    }

    private fun changeOpenUrlsInExternalBrowser(value: Boolean) {
        mvi.updateState { it.copy(openUrlsInExternalBrowser = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                openUrlsInExternalBrowser = value
            )
            saveSettings(settings)
        }
    }

    private fun changeEnableSwipeActions(value: Boolean) {
        mvi.updateState { it.copy(enableSwipeActions = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                enableSwipeActions = value
            )
            saveSettings(settings)
        }
    }

    private fun changeEnableDoubleTapAction(value: Boolean) {
        mvi.updateState { it.copy(enableDoubleTapAction = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                enableDoubleTapAction = value
            )
            saveSettings(settings)
        }
    }

    private fun changePostLayout(value: PostLayout) {
        themeRepository.changePostLayout(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                postLayout = value.toInt()
            )
            saveSettings(settings)
        }
    }

    private fun changeCrashReportEnabled(value: Boolean) {
        crashReportConfiguration.setEnabled(value)
        crashReportSender.setEnabled(value)
        mvi.updateState { it.copy(crashReportEnabled = value) }
    }

    private fun changeVoteFormat(value: VoteFormat) {
        mvi.updateState { it.copy(voteFormat = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                voteFormat = value
            )
            saveSettings(settings)
        }
    }

    private fun changeAutoLoadImages(value: Boolean) {
        mvi.updateState { it.copy(autoLoadImages = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                autoLoadImages = value
            )
            saveSettings(settings)
        }
    }

    private fun changeAutoExpandComments(value: Boolean) {
        mvi.updateState { it.copy(autoExpandComments = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                autoExpandComments = value
            )
            saveSettings(settings)
        }
    }

    private fun changeFullHeightImages(value: Boolean) {
        mvi.updateState { it.copy(fullHeightImages = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                fullHeightImages = value
            )
            saveSettings(settings)
        }
    }

    private fun changeHideNavigationBarWhileScrolling(value: Boolean) {
        mvi.updateState { it.copy(hideNavigationBarWhileScrolling = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                hideNavigationBarWhileScrolling = value
            )
            saveSettings(settings)
        }
    }

    private fun changeMarkAsReadWhileScrolling(value: Boolean) {
        mvi.updateState { it.copy(markAsReadWhileScrolling = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                markAsReadWhileScrolling = value
            )
            saveSettings(settings)
        }
    }

    private fun changeZombieModeInterval(value: Duration) {
        mvi.updateState { it.copy(zombieModeInterval = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                zombieModeInterval = value
            )
            saveSettings(settings)
        }
    }

    private fun changeZombieModeScrollAmount(value: Float) {
        mvi.updateState { it.copy(zombieModeScrollAmount = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                zombieModeScrollAmount = value
            )
            saveSettings(settings)
        }
    }

    private fun changeDefaultInboxUnreadOnly(value: Boolean) {
        mvi.updateState { it.copy(defaultInboxUnreadOnly = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultInboxType = value.toInboxDefaultType(),
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
        mvi.scope?.launch {
            val settings = settingsRepository.getSettings(null)
            mvi.updateState {
                it.copy(
                    defaultListingType = settings.defaultListingType.toListingType(),
                    defaultPostSortType = settings.defaultPostSortType.toSortType(),
                    defaultCommentSortType = settings.defaultCommentSortType.toSortType(),
                )
            }
        }
    }
}
