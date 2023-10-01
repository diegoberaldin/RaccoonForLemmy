package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.ThemeState
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toPostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toThemeState
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.AppTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getAccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.di.getApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App() {
    val accountRepository = remember { getAccountRepository() }
    val settingsRepository = remember { getSettingsRepository() }
    val settings by settingsRepository.currentSettings.collectAsState()
    var hasBeenInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(settingsRepository) {
        val accountId = accountRepository.getActive()?.id
        val currentSettings = settingsRepository.getSettings(accountId)
        settingsRepository.changeCurrentSettings(currentSettings)
        // debounce time for setting
        delay(50)
        hasBeenInitialized = true
    }
    val defaultTheme = if (isSystemInDarkTheme()) {
        ThemeState.Dark.toInt()
    } else {
        ThemeState.Light.toInt()
    }

    val defaultLocale = stringResource(MR.strings.lang)
    val languageRepository = remember { getLanguageRepository() }
    val locale by derivedStateOf { settings.locale }
    LaunchedEffect(locale) {
        languageRepository.changeLanguage(locale ?: defaultLocale)
    }
    val scope = rememberCoroutineScope()
    languageRepository.currentLanguage.onEach { lang ->
        StringDesc.localeType = StringDesc.LocaleType.Custom(lang)
    }.launchIn(scope)

    val apiConfigurationRepository = remember { getApiConfigurationRepository() }
    LaunchedEffect(Unit) {
        val lastActiveAccount = accountRepository.getActive()
        val lastInstance = lastActiveAccount?.instance
        if (lastInstance != null) {
            apiConfigurationRepository.changeInstance(lastInstance)
        }
    }

    val themeRepository = remember { getThemeRepository() }
    LaunchedEffect(settings) {
        with(themeRepository) {
            changeTheme((settings.theme ?: defaultTheme).toThemeState())
            changeNavItemTitles(settings.navigationTitlesVisible)
            changeDynamicColors(settings.dynamicColors)
            changeCustomSeedColor(settings.customSeedColor?.let { Color(it) })
            changePostLayout(settings.postLayout.toPostLayout())
            changeContentFontScale(settings.contentFontScale)
        }
    }
    val currentTheme by themeRepository.state.collectAsState()
    val useDynamicColors by themeRepository.dynamicColors.collectAsState()
    val fontScale by themeRepository.contentFontScale.collectAsState()
    val navigationCoordinator = remember { getNavigationCoordinator() }

    AppTheme(
        theme = currentTheme,
        contentFontScale = fontScale,
        useDynamicColors = useDynamicColors,
    ) {
        val lang by languageRepository.currentLanguage.collectAsState()
        LaunchedEffect(lang) {}

        BottomSheetNavigator(
            sheetShape = RoundedCornerShape(topStart = CornerSize.xl, topEnd = CornerSize.xl),
            sheetBackgroundColor = MaterialTheme.colorScheme.background,
        ) {
            Navigator(
                screen = MainScreen(),
                onBackPressed = {
                    val callback = navigationCoordinator.getCanGoBackCallback()
                    callback?.let { it() } ?: true
                }
            ) {
                val navigator = LocalNavigator.current
                navigationCoordinator.setRootNavigator(navigator)
                if (hasBeenInitialized) {
                    CurrentScreen()
                }
            }
        }
    }
}
