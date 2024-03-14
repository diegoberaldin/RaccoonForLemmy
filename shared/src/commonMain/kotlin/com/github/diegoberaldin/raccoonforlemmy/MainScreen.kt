package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.material.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerEvent
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.feature.home.ui.HomeTab
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.ui.InboxTab
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.ui.ProfileTab
import com.github.diegoberaldin.raccoonforlemmy.feature.search.ui.ExploreTab
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.SettingsTab
import com.github.diegoberaldin.raccoonforlemmy.ui.navigation.TabNavigationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal object MainScreen : Screen {

    @Composable
    override fun Content() {
        val themeRepository = remember { getThemeRepository() }
        var bottomBarHeightPx by remember { mutableStateOf(0f) }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val model = getScreenModel<MainScreenMviModel>()
        val uiState by model.uiState.collectAsState()
        val uiFontScale by themeRepository.uiFontScale.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val exitMessage = LocalXmlStrings.current.messageConfirmExit
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        val bottomNavigationInsetPx = with(LocalDensity.current) {
            WindowInsets.navigationBars.getBottom(this)
        }
        val bottomNavigationInset = with(LocalDensity.current) {
            bottomNavigationInsetPx.toDp()
        }

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    is MainScreenMviModel.Effect.UnreadItemsDetected -> {
                        navigationCoordinator.setInboxUnread(it.value)
                    }
                }
            }.launchIn(this)
        }

        val scrollConnection = remember {
            object : NestedScrollConnection {

                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newOffset =
                        (uiState.bottomBarOffsetHeightPx + delta).coerceIn(
                            -(bottomBarHeightPx + bottomNavigationInsetPx),
                            0f,
                        )
                    model.reduce(MainScreenMviModel.Intent.SetBottomBarOffsetHeightPx(newOffset))
                    return Offset.Zero
                }
            }
        }
        navigationCoordinator.setBottomBarScrollConnection(scrollConnection)

        LaunchedEffect(navigationCoordinator) {
            with(navigationCoordinator) {
                if (currentSection.value == null) {
                    navigationCoordinator.setCurrentSection(TabNavigationSection.Home)
                }
            }

            navigationCoordinator.exitMessageVisible.onEach {
                if (it) {
                    snackbarHostState.showSnackbar(
                        message = exitMessage,
                        duration = SnackbarDuration.Short,
                    )
                    navigationCoordinator.setExitMessageVisible(false)
                }
            }.launchIn(this)
        }

        TabNavigator(HomeTab) { tabNavigator ->
            navigationCoordinator.setTabNavigator(tabNavigator)

            LaunchedEffect(tabNavigator.current) {
                // when the current tab chanes, reset the bottom bar offset to the default value
                model.reduce(MainScreenMviModel.Intent.SetBottomBarOffsetHeightPx(0f))
            }

            LaunchedEffect(drawerCoordinator) {
                drawerCoordinator.events.onEach { evt ->
                    when (evt) {
                        is DrawerEvent.ChangeListingType -> {
                            if (tabNavigator.current == HomeTab) {
                                notificationCenter.send(NotificationCenterEvent.ChangeFeedType(evt.value, "postList"))
                            } else {
                                with(navigationCoordinator) {
                                    changeTab(HomeTab)
                                    setCurrentSection(TabNavigationSection.Home)
                                }
                                launch {
                                    // wait for transition to finish
                                    delay(750)
                                    notificationCenter.send(
                                        NotificationCenterEvent.ChangeFeedType(evt.value, "postList")
                                    )
                                }
                            }
                        }

                        else -> Unit
                    }
                }.launchIn(this)
            }

            Scaffold(
                content = {
                    CurrentTab()
                },
                snackbarHost = {
                    SnackbarHost(snackbarHostState) { data ->
                        Snackbar(
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY =
                                        (-uiState.bottomBarOffsetHeightPx)
                                            .coerceAtMost(bottomBarHeightPx - bottomNavigationInsetPx)
                                },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            snackbarData = data,
                        )
                    }
                },
                bottomBar = {
                    CompositionLocalProvider(
                        LocalDensity provides Density(
                            density = LocalDensity.current.density,
                            fontScale = uiFontScale,
                        ),
                    ) {
                        val titleVisible by themeRepository.navItemTitles.collectAsState()
                        var uiFontSizeWorkaround by remember { mutableStateOf(true) }
                        LaunchedEffect(themeRepository) {
                            themeRepository.uiFontScale.drop(1).onEach {
                                uiFontSizeWorkaround = false
                                delay(50)
                                uiFontSizeWorkaround = true
                            }.launchIn(this)
                        }
                        if (uiFontSizeWorkaround) {
                            BottomAppBar(
                                modifier = Modifier
                                    .onGloballyPositioned {
                                        if (bottomBarHeightPx == 0f) {
                                            bottomBarHeightPx = it.size.toSize().height
                                        }
                                    }
                                    .offset {
                                        IntOffset(
                                            x = 0,
                                            y = -uiState.bottomBarOffsetHeightPx.roundToInt()
                                        )
                                    },
                                contentPadding = PaddingValues(
                                    start = 0.dp,
                                    top = 0.dp,
                                    end = 0.dp,
                                    bottom = bottomNavigationInset,
                                ),
                                backgroundColor = MaterialTheme.colorScheme.background,
                            ) {
                                TabNavigationItem(HomeTab, withText = titleVisible)
                                TabNavigationItem(ExploreTab, withText = titleVisible)
                                TabNavigationItem(ProfileTab, withText = titleVisible)
                                TabNavigationItem(InboxTab, withText = titleVisible)
                                TabNavigationItem(SettingsTab, withText = titleVisible)
                            }
                        }
                    }
                },
            )
        }
    }
}