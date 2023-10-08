package com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Unsubscribe
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.MultiCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.getManageSubscriptionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail.MultiCommunityScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.editor.MultiCommunityEditorScreen
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class ManageSubscriptionsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getManageSubscriptionsViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = stringResource(MR.strings.navigation_drawer_title_subscriptions),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick {
                                navigator?.pop()
                            },
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                )
            },
        ) { paddingValues ->
            val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                model.reduce(ManageSubscriptionsMviModel.Intent.Refresh)
            })
            Box(
                modifier = Modifier.padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.s),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.manage_subscriptions_header_multicommunities),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                modifier = Modifier.onClick {
                                    navigator?.push(MultiCommunityEditorScreen())
                                },
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                    itemsIndexed(uiState.multiCommunities) { idx, community ->
                        SwipeableCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = {
                                when (it) {
                                    DismissValue.DismissedToStart -> MaterialTheme.colorScheme.surfaceTint
                                    DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.tertiary
                                    else -> Color.Transparent
                                }
                            },
                            onGestureBegin = {
                                model.reduce(ManageSubscriptionsMviModel.Intent.HapticIndication)
                            },
                            onDismissToStart = {
                                navigator?.push(
                                    MultiCommunityEditorScreen(community),
                                )
                            },
                            onDismissToEnd = {
                                model.reduce(
                                    ManageSubscriptionsMviModel.Intent.DeleteMultiCommunity(idx),
                                )
                            },
                            swipeContent = { direction ->
                                val icon = when (direction) {
                                    DismissDirection.StartToEnd -> Icons.Default.Delete
                                    DismissDirection.EndToStart -> Icons.Default.Edit
                                }
                                Icon(
                                    modifier = Modifier.padding(Spacing.xs),
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            },
                            content = {
                                MultiCommunityItem(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background).onClick {
                                            navigator?.push(
                                                MultiCommunityScreen(community),
                                            )
                                        },
                                    community = community,
                                    autoLoadImages = uiState.autoLoadImages,
                                )
                            },
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.s),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(MR.strings.manage_subscriptions_header_subscriptions),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                    itemsIndexed(uiState.communities) { idx, community ->
                        SwipeableCard(
                            modifier = Modifier.fillMaxWidth(),
                            directions = setOf(DismissDirection.EndToStart),
                            backgroundColor = {
                                when (it) {
                                    DismissValue.DismissedToStart -> MaterialTheme.colorScheme.surfaceTint
                                    else -> Color.Transparent
                                }
                            },
                            onGestureBegin = {
                                model.reduce(ManageSubscriptionsMviModel.Intent.HapticIndication)
                            },
                            onDismissToStart = {
                                model.reduce(
                                    ManageSubscriptionsMviModel.Intent.Unsubscribe(idx),
                                )
                            },
                            swipeContent = { _ ->
                                Icon(
                                    modifier = Modifier.padding(Spacing.xs),
                                    imageVector = Icons.Default.Unsubscribe,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            },
                            content = {
                                CommunityItem(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background).onClick {
                                            navigator?.push(
                                                CommunityDetailScreen(community),
                                            )
                                        },
                                    community = community,
                                    autoLoadImages = uiState.autoLoadImages,
                                )
                            },
                        )
                    }
                }

                PullRefreshIndicator(
                    refreshing = uiState.refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}