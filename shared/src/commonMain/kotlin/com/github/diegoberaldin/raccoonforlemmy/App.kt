package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toPostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toUiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toUiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.AppTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.ic_launcher_background
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.md_theme_dark_onPrimary
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.getCommunityFromUrl
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.getPostFromUrl
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.getUserFromUrl
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.DrawerEvent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.drawer.ModalDrawerContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems.SavedItemsScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getAccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.getCrashReportConfiguration
import com.github.diegoberaldin.raccoonforlemmy.core.utils.debug.getCrashReportSender
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.di.getApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions.ManageSubscriptionsScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail.MultiCommunityScreen
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class)
@Composable
fun App() {
    val accountRepository = remember { getAccountRepository() }
    val settingsRepository = remember { getSettingsRepository() }
    val settings by settingsRepository.currentSettings.collectAsState()
    val apiConfigurationRepository = remember { getApiConfigurationRepository() }
    val crashReportSender = remember { getCrashReportSender() }
    val crashReportConfiguration = remember { getCrashReportConfiguration() }
    val themeRepository = remember { getThemeRepository() }
    val defaultLocale = stringResource(MR.strings.lang)
    val languageRepository = remember { getLanguageRepository() }
    val locale by derivedStateOf { settings.locale }
    val defaultTheme = if (isSystemInDarkTheme()) {
        UiTheme.Dark.toInt()
    } else {
        UiTheme.Light.toInt()
    }
    val currentTheme by themeRepository.uiTheme.collectAsState()
    val useDynamicColors by themeRepository.dynamicColors.collectAsState()
    val fontScale by themeRepository.contentFontScale.collectAsState()
    val uiFontScale by themeRepository.uiFontScale.collectAsState()
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerCoordinator = remember { getDrawerCoordinator() }
    val drawerGestureEnabled by drawerCoordinator.gesturesEnabled.collectAsState()
    var isInitialized by remember { mutableStateOf(false) }

    languageRepository.currentLanguage.onEach { lang ->
        StringDesc.localeType = StringDesc.LocaleType.Custom(lang)
    }.launchIn(scope)

    LaunchedEffect(Unit) {
        val accountId = accountRepository.getActive()?.id
        val currentSettings = settingsRepository.getSettings(accountId)
        settingsRepository.changeCurrentSettings(currentSettings)
        val lastActiveAccount = accountRepository.getActive()
        val lastInstance = lastActiveAccount?.instance?.takeIf { it.isNotEmpty() }
        if (lastInstance != null) {
            apiConfigurationRepository.changeInstance(lastInstance)
        }
        launch {
            with(crashReportSender) {
                initialize()
                setEnabled(crashReportConfiguration.isEnabled())
            }
        }

        with(themeRepository) {
            changeUiTheme((currentSettings.theme ?: defaultTheme).toUiTheme())
            changeNavItemTitles(currentSettings.navigationTitlesVisible)
            changeDynamicColors(currentSettings.dynamicColors)
            changeCustomSeedColor(currentSettings.customSeedColor?.let { Color(it) })
            changePostLayout(currentSettings.postLayout.toPostLayout())
            changeContentFontScale(currentSettings.contentFontScale)
            changeUiFontScale(currentSettings.uiFontScale)
            changeUiFontFamily(currentSettings.uiFontFamily.toUiFontFamily())

            with(themeRepository) {
                changeUpvoteColor(currentSettings.upvoteColor?.let { Color(it) })
                changeDownvoteColor(currentSettings.downvoteColor?.let { Color(it) })
            }
        }

        isInitialized = true
    }
    LaunchedEffect(locale) {
        languageRepository.changeLanguage(locale ?: defaultLocale)
    }
    LaunchedEffect(navigationCoordinator) {
        navigationCoordinator.deepLinkUrl.debounce(750).onEach { url ->
            val community = getCommunityFromUrl(url)
            val user = getUserFromUrl(url)
            val postAndInstance = getPostFromUrl(url)
            val newScreen = when {
                community != null -> {
                    CommunityDetailScreen(
                        community = community,
                        otherInstance = community.host,
                    )
                }

                user != null -> {
                    UserDetailScreen(
                        user = user,
                        otherInstance = user.host,
                    )
                }

                postAndInstance != null -> {
                    val (post, otherInstance) = postAndInstance
                    PostDetailScreen(
                        post = post,
                        otherInstance = otherInstance,
                    )
                }

                else -> null
            }
            if (newScreen != null) {
                navigationCoordinator.pushScreen(newScreen)
            }
        }.launchIn(this)
    }
    LaunchedEffect(drawerCoordinator) {
        drawerCoordinator.toggleEvents.onEach { evt ->
            when (evt) {
                DrawerEvent.Toggled -> {
                    drawerState.apply {
                        launch {
                            if (isClosed) {
                                open()
                            } else {
                                close()
                            }
                        }
                    }
                }

                is DrawerEvent.OpenCommunity -> {
                    navigationCoordinator.pushScreen(CommunityDetailScreen(evt.community))
                }

                is DrawerEvent.OpenMultiCommunity -> {
                    navigationCoordinator.pushScreen(MultiCommunityScreen(evt.community))
                }

                DrawerEvent.ManageSubscriptions -> {
                    navigationCoordinator.pushScreen(ManageSubscriptionsScreen())
                }

                DrawerEvent.OpenBookmarks -> {
                    navigationCoordinator.pushScreen(SavedItemsScreen())
                }
            }
        }.launchIn(this)
    }

    AppTheme(
        theme = currentTheme,
        contentFontScale = fontScale,
        useDynamicColors = useDynamicColors,
    ) {
        val lang by languageRepository.currentLanguage.collectAsState()
        LaunchedEffect(lang) {}
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = LocalDensity.current.density,
                fontScale = uiFontScale,
            ),
        ) {
            if (isInitialized) {
                BottomSheetNavigator(
                    sheetShape = RoundedCornerShape(
                        topStart = CornerSize.xl, topEnd = CornerSize.xl
                    ),
                    sheetBackgroundColor = MaterialTheme.colorScheme.background,
                ) { bottomNavigator ->
                    navigationCoordinator.setBottomNavigator(bottomNavigator)

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = drawerGestureEnabled,
                        drawerContent = {
                            ModalDrawerSheet {
                                TabNavigator(ModalDrawerContent)
                            }
                        },
                    ) {
                        Navigator(screen = MainScreen, onBackPressed = {
                            val callback = navigationCoordinator.getCanGoBackCallback()
                            callback?.let { it() } ?: true
                        }) { navigator ->
                            LaunchedEffect(Unit) {
                                navigationCoordinator.setRootNavigator(navigator)
                            }


                            CurrentScreen()

                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = ic_launcher_background),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    ) {
                        Image(
                            painter = painterResource(MR.images.icon),
                            contentDescription = null,
                        )
                        CircularProgressIndicator(
                            color = md_theme_dark_onPrimary,
                        )
                    }
                }
            }
        }
    }
}
