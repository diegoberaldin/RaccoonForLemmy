package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.racconforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserCounters
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.ProfileLoggedSection
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.posts.ProfilePostCard
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.posts.ProfilePostsMviModel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.di.getProfilePostsViewModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

internal class ProfileSavedScreen(
    private val user: UserModel,
) : Tab {

    var onSectionSelected: ((ProfileLoggedSection) -> Unit)? = null

    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel {
            getProfilePostsViewModel(
                user = user,
                savedOnly = true,
            )
        }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }

        val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
            model.reduce(ProfilePostsMviModel.Intent.Refresh)
        })
        Box(
            modifier = Modifier.pullRefresh(pullRefreshState),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        UserHeader(user = user)
                        UserCounters(
                            modifier = Modifier.graphicsLayer(translationY = -Spacing.m.toLocalPixel()),
                            user = user,
                        )
                        Spacer(modifier = Modifier.height(Spacing.s))
                        SectionSelector(
                            titles = listOf(
                                stringResource(MR.strings.profile_section_posts),
                                stringResource(MR.strings.profile_section_comments),
                                stringResource(MR.strings.profile_section_saved),
                            ),
                            currentSection = 2,
                            onSectionSelected = {
                                val section = when (it) {
                                    0 -> ProfileLoggedSection.POSTS
                                    1 -> ProfileLoggedSection.COMMENTS
                                    else -> ProfileLoggedSection.SAVED
                                }
                                onSectionSelected?.invoke(section)
                            },
                        )
                        Spacer(modifier = Modifier.height(Spacing.m))
                    }
                }
                items(uiState.posts) { post ->
                    ProfilePostCard(
                        post = post,
                        onOpenCommunity = { community ->
                            navigator?.push(
                                CommunityDetailScreen(
                                    community = community,
                                ).apply {
                                    onBack = {
                                        navigator.pop()
                                    }
                                },
                            )
                        },
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(Spacing.xxxl))
                }
                item {
                    if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                        model.reduce(ProfilePostsMviModel.Intent.LoadNextPage)
                    }
                    if (uiState.loading && !uiState.refreshing) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(Spacing.xs),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(25.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
