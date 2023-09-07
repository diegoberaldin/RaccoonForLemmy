package com.github.diegoberaldin.raccoonforlemmy

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.ThemeState
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.md_theme_black_surface
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.md_theme_dark_surface
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.md_theme_light_surface
import com.github.diegoberaldin.raccoonforlemmy.feature.home.ui.HomeTab
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.ui.InboxTab
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.ui.ProfileTab
import com.github.diegoberaldin.raccoonforlemmy.feature.search.ui.SearchTab
import com.github.diegoberaldin.raccoonforlemmy.feature.settings.ui.SettingsTab
import com.github.diegoberaldin.raccoonforlemmy.ui.navigation.TabNavigationItem

internal class MainScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val themeRepository = remember { getThemeRepository() }

        TabNavigator(HomeTab) {
            Scaffold(
                content = {
                    CurrentTab()
                },
                bottomBar = {
                    val themeState by themeRepository.state.collectAsState()
                    val titleVisible by themeRepository.navItemTitles.collectAsState()
                    BottomAppBar(
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