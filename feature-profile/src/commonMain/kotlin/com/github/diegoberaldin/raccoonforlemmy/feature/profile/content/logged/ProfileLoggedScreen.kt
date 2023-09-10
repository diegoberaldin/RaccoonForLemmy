package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.comments.ProfileCommentsScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.posts.ProfilePostsScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.saved.ProfileSavedScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.getProfileLoggedViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal object ProfileLoggedScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxxs),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val model = rememberScreenModel { getProfileLoggedViewModel() }
            model.bindToLifecycle(key)
            val uiState by model.uiState.collectAsState()
            val user = uiState.user
            if (user != null) {
                val screens = listOf(
                    ProfilePostsScreen(
                        user = user,
                    ).apply {
                        onSectionSelected = {
                            model.reduce(ProfileLoggedMviModel.Intent.SelectTab(it))
                        }
                    },
                    ProfileCommentsScreen(
                        user = user,
                    ).apply {
                        onSectionSelected = {
                            model.reduce(ProfileLoggedMviModel.Intent.SelectTab(it))
                        }
                    },
                    ProfileSavedScreen(
                        user = user,
                    ).apply {
                        onSectionSelected = {
                            model.reduce(ProfileLoggedMviModel.Intent.SelectTab(it))
                        }
                    },
                )

                TabNavigator(screens.first()) {
                    CurrentScreen()
                    val navigator = LocalTabNavigator.current
                    LaunchedEffect(model) {
                        model.uiState.map { it.currentTab }
                            .distinctUntilChanged()
                            .onEach { section ->
                                val index = when (section) {
                                    ProfileLoggedSection.POSTS -> 0
                                    ProfileLoggedSection.COMMENTS -> 1
                                    ProfileLoggedSection.SAVED -> 2
                                }
                                navigator.current = screens[index]
                            }.launchIn(this)
                    }
                }
            }
        }
    }
}
