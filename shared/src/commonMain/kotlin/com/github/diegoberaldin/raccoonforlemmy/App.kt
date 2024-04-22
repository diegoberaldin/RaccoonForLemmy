package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.UiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toCommentBarTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toPostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toUiFontFamily
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.toUiTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.AppTheme
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.DraggableSideMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.getCommunityFromUrl
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.getPostFromUrl
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.getUserFromUrl
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.ProvideXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.di.getL10nManager
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.ComposeEvent
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerEvent
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.SideMenuEvents
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getAccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLanguageDirection
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.di.getApiConfigurationRepository
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.ModalDrawerContent
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail.MultiCommunityScreen
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class)
@Composable
fun App(onLoadingFinished: () -> Unit = {}) {
    val accountRepository = remember { getAccountRepository() }
    val settingsRepository = remember { getSettingsRepository() }
    val settings by settingsRepository.currentSettings.collectAsState()
    var hasBeenInitialized by remember { mutableStateOf(false) }
    val apiConfigurationRepository = remember { getApiConfigurationRepository() }
    val themeRepository = remember { getThemeRepository() }
    val defaultTheme = if (isSystemInDarkTheme()) {
        UiTheme.Dark
    } else {
        UiTheme.Light
    }.toInt()
    val locale by derivedStateOf { settings.locale }
    val useDynamicColors by themeRepository.dynamicColors.collectAsState()
    val uiFontScale by themeRepository.uiFontScale.collectAsState()
    val navigationCoordinator = remember { getNavigationCoordinator() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerCoordinator = remember { getDrawerCoordinator() }
    val drawerGesturesEnabled by drawerCoordinator.gesturesEnabled.collectAsState()
    val detailOpener = remember { getDetailOpener() }
    val l10nManager = remember { getL10nManager() }
    val l10nState by l10nManager.lyricist.state.collectAsState()
    val barTheme: UiBarTheme = when {
        settings.edgeToEdge && settings.opaqueSystemBars -> UiBarTheme.Opaque
        settings.edgeToEdge && !settings.opaqueSystemBars -> UiBarTheme.Transparent
        else -> UiBarTheme.Solid
    }
    var screenWidth by remember { mutableStateOf(0f) }
    var sideMenuOpened by remember { mutableStateOf(false) }
    var sideMenuContent by remember { mutableStateOf<@Composable (() -> Unit)?>(null) }
    val scope = rememberCoroutineScope()

    fun closeSideMenu() {
        sideMenuOpened = false
        scope.launch {
            delay(250)
            sideMenuContent = null
        }
    }

    LaunchedEffect(Unit) {
        val accountId = accountRepository.getActive()?.id
        val currentSettings = settingsRepository.getSettings(accountId)
        settingsRepository.changeCurrentSettings(currentSettings)
        val lastActiveAccount = accountRepository.getActive()
        val lastInstance = lastActiveAccount?.instance?.takeIf { it.isNotEmpty() }
        if (lastInstance != null) {
            apiConfigurationRepository.changeInstance(lastInstance)
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
            changeContentFontFamily(currentSettings.contentFontFamily.toUiFontFamily())
            changeCommentBarTheme(currentSettings.commentBarTheme.toCommentBarTheme())
            changeCommentBarThickness(currentSettings.commentBarThickness)

            with(themeRepository) {
                changeUpVoteColor(currentSettings.upVoteColor?.let { Color(it) })
                changeDownVoteColor(currentSettings.downVoteColor?.let { Color(it) })
                changeReplyColor(currentSettings.replyColor?.let { Color(it) })
                changeSaveColor(currentSettings.saveColor?.let { Color(it) })
            }
        }

        hasBeenInitialized = true
        launch {
            delay(50)
            onLoadingFinished()
        }
    }

    LaunchedEffect(locale) {
        l10nManager.changeLanguage(locale ?: "en")
    }

    LaunchedEffect(settings) {
        with(themeRepository) {
            changeUiTheme(settings.theme.toUiTheme())
            changeNavItemTitles(settings.navigationTitlesVisible)
            changeDynamicColors(settings.dynamicColors)
            changeCustomSeedColor(settings.customSeedColor?.let { Color(it) })
            changePostLayout(settings.postLayout.toPostLayout())
            changeContentFontScale(settings.contentFontScale)
            changeUiFontScale(settings.uiFontScale)
            changeUiFontFamily(settings.uiFontFamily.toUiFontFamily())
            changeContentFontFamily(settings.contentFontFamily.toUiFontFamily())

            with(themeRepository) {
                changeUpVoteColor(settings.upVoteColor?.let { Color(it) })
                changeDownVoteColor(settings.downVoteColor?.let { Color(it) })
                changeReplyColor(settings.replyColor?.let { Color(it) })
                changeSaveColor(settings.saveColor?.let { Color(it) })
            }
        }
    }

    LaunchedEffect(navigationCoordinator) {
        navigationCoordinator.deepLinkUrl.debounce(750).onEach { url ->
            val community = getCommunityFromUrl(url)
            val user = getUserFromUrl(url)
            val postAndInstance = getPostFromUrl(url)
            when {
                community != null -> {
                    detailOpener.openCommunityDetail(community, community.host)
                }

                user != null -> {
                    detailOpener.openUserDetail(user, user.host)
                }

                postAndInstance != null -> {
                    val (post, otherInstance) = postAndInstance
                    detailOpener.openPostDetail(post, otherInstance)
                }

                else -> Unit
            }
        }.launchIn(this)
        navigationCoordinator.composeEvents.debounce(750).onEach { event ->
            when (event) {
                is ComposeEvent.WithText -> detailOpener.openCreatePost(
                    initialText = event.text,
                    forceCommunitySelection = true,
                )

                is ComposeEvent.WithUrl -> detailOpener.openCreatePost(
                    initialUrl = event.url,
                    forceCommunitySelection = true,
                )

                else -> Unit
            }
        }.launchIn(this)
        navigationCoordinator.sideMenuEvents.onEach { evt ->
            when (evt) {
                is SideMenuEvents.Open -> {
                    sideMenuContent = @Composable {
                        evt.screen.Content()
                    }
                    sideMenuOpened = true
                }

                SideMenuEvents.Close -> {
                    closeSideMenu()
                }
            }
        }.launchIn(this)
    }

    LaunchedEffect(drawerCoordinator) {
        // centralizes the information about drawer opening
        snapshotFlow {
            drawerState.isClosed
        }.onEach { closed ->
            drawerCoordinator.changeDrawerOpened(!closed)
        }.launchIn(this)

        drawerCoordinator.events.onEach { evt ->
            when (evt) {
                DrawerEvent.Toggle -> {
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

                DrawerEvent.Close -> {
                    drawerState.apply {
                        launch {
                            if (!isClosed) {
                                close()
                            }
                        }
                    }
                }

                is DrawerEvent.OpenCommunity -> {
                    detailOpener.openCommunityDetail(community = evt.community)
                }

                is DrawerEvent.OpenMultiCommunity -> {
                    evt.community.id?.also {
                        navigationCoordinator.pushScreen(MultiCommunityScreen(it))
                    }
                }

                else -> Unit
            }
        }.launchIn(this)
    }

    AppTheme(
        useDynamicColors = useDynamicColors,
        barTheme = barTheme,
    ) {
        ProvideXmlStrings(
            lyricist = l10nManager.lyricist,
        ) {
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = LocalDensity.current.density,
                    fontScale = uiFontScale,
                ),
                LocalLayoutDirection provides l10nState.languageTag.toLanguageDirection(),
            ) {
                BottomSheetNavigator(
                    sheetShape = RoundedCornerShape(
                        topStart = CornerSize.xl,
                        topEnd = CornerSize.xl
                    ),
                    sheetBackgroundColor = MaterialTheme.colorScheme.background,
                ) { bottomNavigator ->
                    navigationCoordinator.setBottomNavigator(bottomNavigator)

                    Navigator(
                        screen = MainScreen,
                        onBackPressed = {
                            val callback = navigationCoordinator.getCanGoBackCallback()
                            callback?.let { it() } ?: true
                        },
                    ) { navigator ->
                        LaunchedEffect(Unit) {
                            navigationCoordinator.setRootNavigator(navigator)
                        }

                        ModalNavigationDrawer(
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned {
                                    screenWidth = it.size.toSize().width
                                },
                            drawerState = drawerState,
                            gesturesEnabled = drawerGesturesEnabled,
                            drawerContent = {
                                ModalDrawerSheet {
                                    TabNavigator(ModalDrawerContent)
                                }
                            },
                        ) {
                            if (hasBeenInitialized) {
                                SlideTransition(
                                    animationSpec = tween(
                                        durationMillis = 250,
                                        easing = FastOutSlowInEasing,
                                    ),
                                    navigator = navigator,
                                )
                            }
                        }

                        DraggableSideMenu(
                            availableWidth = screenWidth.toLocalDp(),
                            opened = sideMenuOpened,
                            onDismiss = {
                                closeSideMenu()
                            },
                            content = {
                                sideMenuContent?.invoke()
                            }
                        )
                    }
                }
            }
        }
    }
}
