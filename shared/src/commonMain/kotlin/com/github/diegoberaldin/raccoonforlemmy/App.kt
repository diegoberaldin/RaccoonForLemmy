package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toThemeState
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.AppTheme
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.KeyStoreKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.di.getTemporaryKeyStore
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
    val keyStore = remember { getTemporaryKeyStore() }
    val systemDarkTheme = isSystemInDarkTheme()
    val currentTheme = keyStore[KeyStoreKeys.UiTheme, if (systemDarkTheme) 1 else 0].let {
        it.toThemeState()
    }

    val defaultLocale = stringResource(MR.strings.lang)
    val langCode = keyStore[KeyStoreKeys.Locale, defaultLocale]
    val fontScale = keyStore[KeyStoreKeys.ContentFontScale, 1f]
    val languageRepository = remember { getLanguageRepository() }
    LaunchedEffect(Unit) {
        delay(100)
        languageRepository.changeLanguage(langCode)
    }
    val scope = rememberCoroutineScope()
    languageRepository.currentLanguage.onEach { lang ->
        StringDesc.localeType = StringDesc.LocaleType.Custom(lang)
    }.launchIn(scope)

    val lastInstance = keyStore[KeyStoreKeys.LastIntance, ""]
    val apiConfigurationRepository = remember { getApiConfigurationRepository() }
    if (lastInstance.isEmpty()) {
        val instance = apiConfigurationRepository.getInstance()
        keyStore.save(KeyStoreKeys.LastIntance, instance)
    } else {
        apiConfigurationRepository.changeInstance(lastInstance)
    }

    val themeRepository = remember { getThemeRepository() }
    val navTitles = keyStore[KeyStoreKeys.NavItemTitlesVisible, false]
    val dynamicColors = keyStore[KeyStoreKeys.DynamicColors, false]
    LaunchedEffect(Unit) {
        themeRepository.changeNavItemTitles(navTitles)
        themeRepository.changeDynamicColors(dynamicColors)
    }
    val useDynamicColors by themeRepository.dynamicColors.collectAsState()

    AppTheme(
        theme = currentTheme,
        contentFontScale = fontScale,
        useDynamicColors = useDynamicColors,
    ) {
        val lang by languageRepository.currentLanguage.collectAsState()
        LaunchedEffect(lang) {}

        BottomSheetNavigator {
            Navigator(MainScreen())
        }
    }
}
