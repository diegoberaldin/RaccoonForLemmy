package com.github.diegoberaldin.raccoonforlemmy.feature.settings.content

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.ThemeState
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toFontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.AppInfo
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

class SettingsScreenViewModel(
    private val mvi: DefaultMviModel<SettingsScreenMviModel.Intent, SettingsScreenMviModel.UiState, SettingsScreenMviModel.Effect>,
    private val themeRepository: ThemeRepository,
    private val colorSchemeProvider: ColorSchemeProvider,
    private val languageRepository: LanguageRepository,
    private val identityRepository: IdentityRepository,
    private val settingsRepository: SettingsRepository,
    private val accountRepository: AccountRepository,
    private val notificationCenter: NotificationCenter,
) : ScreenModel,
    MviModel<SettingsScreenMviModel.Intent, SettingsScreenMviModel.UiState, SettingsScreenMviModel.Effect> by mvi {

    init {
        notificationCenter.addObserver(
            { handleLogout() },
            this::class.simpleName.orEmpty(),
            NotificationCenterContractKeys.Logout
        )
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch(Dispatchers.Main) {
            themeRepository.state.onEach { currentTheme ->
                mvi.updateState { it.copy(currentTheme = currentTheme) }
            }.launchIn(this)
            themeRepository.contentFontScale.onEach { value ->
                mvi.updateState { it.copy(currentFontScale = value.toFontScale()) }
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
            languageRepository.currentLanguage.onEach { lang ->
                mvi.updateState { it.copy(lang = lang) }
            }.launchIn(this)
            identityRepository.authToken.onEach { auth ->
                mvi.updateState { it.copy(isLogged = !auth.isNullOrEmpty()) }
            }.launchIn(this)
        }

        val settings = settingsRepository.currentSettings.value
        mvi.updateState {
            it.copy(
                defaultListingType = settings.defaultListingType.toListingType(),
                defaultPostSortType = settings.defaultPostSortType.toSortType(),
                defaultCommentSortType = settings.defaultCommentSortType.toSortType(),
                includeNsfw = settings.includeNsfw,
                blurNsfw = settings.blurNsfw,
                supportsDynamicColors = colorSchemeProvider.supportsDynamicColors,
                openUrlsInExternalBrowser = settings.openUrlsInExternalBrowser,
                enableSwipeActions = settings.enableSwipeActions,
                appVersion = AppInfo.versionCode,
            )
        }
    }

    override fun reduce(intent: SettingsScreenMviModel.Intent) {
        when (intent) {
            is SettingsScreenMviModel.Intent.ChangeTheme -> {
                changeTheme(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeContentFontSize -> {
                changeContentFontScale(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeLanguage -> {
                changeLanguage(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeDefaultCommentSortType -> {
                changeDefaultCommentSortType(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeDefaultListingType -> {
                changeDefaultListingType(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeDefaultPostSortType -> {
                changeDefaultPostSortType(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeBlurNsfw -> {
                changeBlurNsfw(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeIncludeNsfw -> {
                changeIncludeNsfw(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeNavBarTitlesVisible -> {
                changeNavBarTitlesVisible(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeDynamicColors -> {
                changeDynamicColors(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeOpenUrlsInExternalBrowser -> {
                changeOpenUrlsInExternalBrowser(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeEnableSwipeActions -> {
                changeEnableSwipeActions(intent.value)
            }

            is SettingsScreenMviModel.Intent.ChangeCustomSeedColor -> changeCustomSeedColor(
                intent.value
            )

            is SettingsScreenMviModel.Intent.ChangePostLayout -> changePostLayout(intent.value)
        }
    }

    private fun changeTheme(value: ThemeState) {
        themeRepository.changeTheme(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                theme = value.toInt()
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeContentFontScale(value: Float) {
        themeRepository.changeContentFontScale(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                contentFontScale = value
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeLanguage(value: String) {
        languageRepository.changeLanguage(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                locale = value
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeDefaultListingType(value: ListingType) {
        mvi.updateState { it.copy(defaultListingType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultListingType = value.toInt()
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeDefaultPostSortType(value: SortType) {
        mvi.updateState { it.copy(defaultPostSortType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultPostSortType = value.toInt()
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeDefaultCommentSortType(value: SortType) {
        mvi.updateState { it.copy(defaultCommentSortType = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                defaultCommentSortType = value.toInt()
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeNavBarTitlesVisible(value: Boolean) {
        themeRepository.changeNavItemTitles(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                navigationTitlesVisible = value
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeIncludeNsfw(value: Boolean) {
        mvi.updateState { it.copy(includeNsfw = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                includeNsfw = value
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeBlurNsfw(value: Boolean) {
        mvi.updateState { it.copy(blurNsfw = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                blurNsfw = value
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeDynamicColors(value: Boolean) {
        themeRepository.changeDynamicColors(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                dynamicColors = value
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeCustomSeedColor(value: Color?) {
        themeRepository.changeCustomSeedColor(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                customSeedColor = value?.toArgb()
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeOpenUrlsInExternalBrowser(value: Boolean) {
        mvi.updateState { it.copy(openUrlsInExternalBrowser = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                openUrlsInExternalBrowser = value
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changeEnableSwipeActions(value: Boolean) {
        mvi.updateState { it.copy(enableSwipeActions = value) }
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                enableSwipeActions = value
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
    }

    private fun changePostLayout(value: PostLayout) {
        themeRepository.changePostLayout(value)
        mvi.scope?.launch {
            val settings = settingsRepository.currentSettings.value.copy(
                postLayout = value.toInt()
            )
            val accountId = accountRepository.getActive()?.id
            settingsRepository.updateSettings(settings, accountId)
        }
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
