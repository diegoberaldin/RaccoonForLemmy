package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.material.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.di.getMainViewModel
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
import kotlin.math.roundToInt

internal object MainScreen : Screen {

    @Composable
    override fun Content() {
        val themeRepository = remember { getThemeRepository() }
        var bottomBarHeightPx by remember { mutableStateOf(0f) }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val model = rememberScreenModel { getMainViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val uiFontScale by themeRepository.uiFontScale.collectAsState()

        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    is MainScreenMviModel.Effect.UnreadItemsDetected -> {
                        navigationCoordinator.setInboxUnread(it.value)
                    }
                }
            }.launchIn(this)
        }

        LaunchedEffect(navigationCoordinator) {
            val scrollConnection = object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newOffset =
                        (uiState.bottomBarOffsetHeightPx + delta).coerceIn(
                            -bottomBarHeightPx,
                            0f
                        )
                    model.reduce(MainScreenMviModel.Intent.SetBottomBarOffsetHeightPx(newOffset))
                    return Offset.Zero
                }
            }
            navigationCoordinator.apply {
                setBottomBarScrollConnection(scrollConnection)
                setCurrentSection(HomeTab)
            }
        }

        TabNavigator(HomeTab) { tabNavigator ->
            LaunchedEffect(tabNavigator.current) {
                // when the current tab chanes, reset the bottom bar offset to the default value
                model.reduce(MainScreenMviModel.Intent.SetBottomBarOffsetHeightPx(0f))
            }

            Scaffold(
                content = {
                    CurrentTab()
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
                                        bottomBarHeightPx = it.size.toSize().height
                                    }
                                    .offset {
                                        IntOffset(
                                            x = 0,
                                            y = -uiState.bottomBarOffsetHeightPx.roundToInt()
                                        )
                                    },
                                contentPadding = PaddingValues(0.dp),
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