package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.material.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.ThemeState
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.md_theme_black_surface
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.md_theme_dark_surface
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.md_theme_light_surface
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.home.ui.HomeTab
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.ui.InboxTab
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.ui.ProfileTab
import com.github.diegoberaldin.raccoonforlemmy.feature.search.ui.SearchTab
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.SettingsTab
import com.github.diegoberaldin.raccoonforlemmy.ui.navigation.TabNavigationItem
import kotlin.math.roundToInt

internal class MainScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val themeRepository = remember { getThemeRepository() }
        var bottomBarHeightPx by remember { mutableStateOf(0f) }
        var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }
        val bottomBarNestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newOffset = bottomBarOffsetHeightPx + delta
                    bottomBarOffsetHeightPx = newOffset.coerceIn(-bottomBarHeightPx, 0f)
                    return Offset.Zero
                }
            }
        }

        val bottomNavBarCoordinator = remember { getNavigationCoordinator() }
        LaunchedEffect(bottomNavBarCoordinator) {
            bottomNavBarCoordinator.setBottomBarScrollConnection(bottomBarNestedScrollConnection)
            bottomNavBarCoordinator.setCurrentSection(HomeTab)
        }

        TabNavigator(HomeTab) {
            Scaffold(
                content = {
                    CurrentTab()
                },
                bottomBar = {
                    val themeState by themeRepository.state.collectAsState()
                    val titleVisible by themeRepository.navItemTitles.collectAsState()
                    BottomAppBar(
                        modifier = Modifier
                            .onGloballyPositioned {
                                bottomBarHeightPx = it.size.toSize().height
                            }
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = -bottomBarOffsetHeightPx.roundToInt()
                                )
                            },
                        contentPadding = PaddingValues(0.dp),
                        backgroundColor = when (themeState) {
                            ThemeState.Light -> {
                                md_theme_light_surface
                            }

                            ThemeState.Dark -> {
                                md_theme_dark_surface
                            }

                            else -> {
                                md_theme_black_surface
                            }
                        },
                    ) {
                        TabNavigationItem(HomeTab, withText = titleVisible)
                        TabNavigationItem(SearchTab, withText = titleVisible)
                        TabNavigationItem(ProfileTab, withText = titleVisible)
                        TabNavigationItem(InboxTab, withText = titleVisible)
                        TabNavigationItem(SettingsTab, withText = titleVisible)
                    }
                },
            )
        }
    }
}