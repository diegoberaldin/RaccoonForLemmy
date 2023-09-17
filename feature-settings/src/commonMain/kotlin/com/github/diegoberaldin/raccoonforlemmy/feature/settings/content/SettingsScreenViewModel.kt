package com.github.diegoberaldin.raccoonforlemmy.feature.settings.content

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.AppInfo
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.ThemeState
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toFontScale
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ColorSchemeProvider
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.KeyStoreKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
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
    private val keyStore: TemporaryKeyStore,
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
            languageRepository.currentLanguage.onEach { lang ->
                mvi.updateState { it.copy(lang = lang) }
            }.launchIn(this)
            identityRepository.authToken.onEach { auth ->
                mvi.updateState { it.copy(isLogged = !auth.isNullOrEmpty()) }
            }.launchIn(this)
        }

        val listingType = keyStore[KeyStoreKeys.DefaultListingType, 0].toListingType()
        val postSortType = keyStore[KeyStoreKeys.DefaultPostSortType, 0].toSortType()
        val commentSortType = keyStore[KeyStoreKeys.DefaultCommentSortType, 3].toSortType()
        val openUrlsInExternalBrowser = keyStore[KeyStoreKeys.OpenUrlsInExternalBrowser, false]
        mvi.updateState {
            it.copy(
                defaultListingType = listingType,
                defaultPostSortType = postSortType,
                defaultCommentSortType = commentSortType,
                includeNsfw = keyStore[KeyStoreKeys.IncludeNsfw, true],
                blurNsfw = keyStore[KeyStoreKeys.BlurNsfw, true],
                supportsDynamicColors = colorSchemeProvider.supportsDynamicColors,
                openUrlsInExternalBrowser = openUrlsInExternalBrowser,
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
        }
    }

    private fun changeTheme(value: ThemeState) {
        themeRepository.changeTheme(value)
        keyStore.save(KeyStoreKeys.UiTheme, value.toInt())
    }

    private fun changeContentFontScale(value: Float) {
        themeRepository.changeContentFontScale(value)
        keyStore.save(KeyStoreKeys.ContentFontScale, value)
    }

    private fun changeLanguage(value: String) {
        languageRepository.changeLanguage(value)
        keyStore.save(KeyStoreKeys.Locale, value)
    }

    private fun changeDefaultListingType(value: ListingType) {
        mvi.updateState { it.copy(defaultListingType = value) }
        keyStore.save(KeyStoreKeys.DefaultListingType, value.toInt())
    }

    private fun changeDefaultPostSortType(value: SortType) {
        mvi.updateState { it.copy(defaultPostSortType = value) }
        keyStore.save(KeyStoreKeys.DefaultPostSortType, value.toInt())
    }

    private fun changeDefaultCommentSortType(value: SortType) {
        mvi.updateState { it.copy(defaultCommentSortType = value) }
        keyStore.save(KeyStoreKeys.DefaultCommentSortType, value.toInt())
    }

    private fun changeNavBarTitlesVisible(value: Boolean) {
        keyStore.save(KeyStoreKeys.NavItemTitlesVisible, value)
        themeRepository.changeNavItemTitles(value)
    }

    private fun changeIncludeNsfw(value: Boolean) {
        mvi.updateState { it.copy(includeNsfw = value) }
        keyStore.save(KeyStoreKeys.IncludeNsfw, value)
    }

    private fun changeBlurNsfw(value: Boolean) {
        mvi.updateState { it.copy(blurNsfw = value) }
        keyStore.save(KeyStoreKeys.BlurNsfw, value)
    }

    private fun changeDynamicColors(value: Boolean) {
        themeRepository.changeDynamicColors(value)
        keyStore.save(KeyStoreKeys.DynamicColors, value)
    }

    private fun changeOpenUrlsInExternalBrowser(value: Boolean) {
        mvi.updateState { it.copy(openUrlsInExternalBrowser = value) }
        keyStore.save(KeyStoreKeys.OpenUrlsInExternalBrowser, value)
    }

    private fun handleLogout() {
        val listingType =
            keyStore[KeyStoreKeys.DefaultListingType, 0].toListingType()
        val postSortType =
            keyStore[KeyStoreKeys.DefaultPostSortType, 0].toSortType()
        val commentSortType =
            keyStore[KeyStoreKeys.DefaultCommentSortType, 3].toSortType()
        mvi.updateState {
            it.copy(
                defaultListingType = listingType,
                defaultPostSortType = postSortType,
                defaultCommentSortType = commentSortType,
            )
        }
    }
}
