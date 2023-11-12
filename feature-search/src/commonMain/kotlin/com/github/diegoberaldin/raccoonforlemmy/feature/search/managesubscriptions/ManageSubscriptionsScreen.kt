package com.github.diegoberaldin.raccoonforlemmy.feature.search.managesubscriptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Unsubscribe
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.MultiCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.selectcommunity.CommunityItemPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.getManageSubscriptionsViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail.MultiCommunityScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.editor.MultiCommunityEditorScreen
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

class ManageSubscriptionsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getManageSubscriptionsViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigatorCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val drawerCoordinator = remember { getDrawerCoordinator() }
        DisposableEffect(key) {
            drawerCoordinator.setGesturesEnabled(false)
            onDispose {
                drawerCoordinator.setGesturesEnabled(true)
            }
        }

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
                            modifier = Modifier.onClick(
                                rememberCallback {
                                    navigatorCoordinator.getRootNavigator()?.pop()
                                },
                            ),
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isFabVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it * 2 },
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it * 2 },
                    ),
                ) {
                    FloatingActionButtonMenu(
                        items = buildList {
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.ExpandLess,
                                text = stringResource(MR.strings.action_back_to_top),
                                onSelected = rememberCallback {
                                    scope.launch {
                                        lazyListState.scrollToItem(0)
                                        topAppBarState.heightOffset = 0f
                                        topAppBarState.contentOffset = 0f
                                    }
                                },
                            )
                        }
                    )
                }
            },
        ) { paddingValues ->
            val pullRefreshState = rememberPullRefreshState(
                refreshing = uiState.refreshing,
                onRefresh = rememberCallback(model) {
                    model.reduce(ManageSubscriptionsMviModel.Intent.Refresh)
                },
            )
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(fabNestedScrollConnection)
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
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
                                modifier = Modifier.onClick(
                                    rememberCallback {
                                        navigatorCoordinator.getRootNavigator()?.push(
                                            MultiCommunityEditorScreen()
                                        )
                                    },
                                ),
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                    items(uiState.multiCommunities) { community ->
                        val endColor = MaterialTheme.colorScheme.secondary
                        val startColor = MaterialTheme.colorScheme.tertiary
                        SwipeableCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = rememberCallbackArgs {
                                when (it) {
                                    DismissValue.DismissedToStart -> endColor
                                    DismissValue.DismissedToEnd -> startColor
                                    else -> Color.Transparent
                                }
                            },
                            onGestureBegin = rememberCallback(model) {
                                model.reduce(ManageSubscriptionsMviModel.Intent.HapticIndication)
                            },
                            onDismissToStart = rememberCallback {
                                navigatorCoordinator.getRootNavigator()?.push(
                                    MultiCommunityEditorScreen(community),
                                )
                            },
                            onDismissToEnd = rememberCallback(model) {
                                model.reduce(
                                    ManageSubscriptionsMviModel.Intent.DeleteMultiCommunity(
                                        (community.id ?: 0).toInt()
                                    ),
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
                                        .background(MaterialTheme.colorScheme.background).onClick(
                                            rememberCallback {
                                                navigatorCoordinator.getRootNavigator()?.push(
                                                    MultiCommunityScreen(community),
                                                )
                                            },
                                        ),
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
                    if (uiState.initial) {
                        items(5) {
                            CommunityItemPlaceholder()
                        }
                    }
                    items(uiState.communities) { community ->
                        val endColor = MaterialTheme.colorScheme.secondary
                        SwipeableCard(
                            modifier = Modifier.fillMaxWidth(),
                            directions = setOf(DismissDirection.EndToStart),
                            backgroundColor = rememberCallbackArgs {
                                when (it) {
                                    DismissValue.DismissedToStart -> endColor
                                    else -> Color.Transparent
                                }
                            },
                            onGestureBegin = rememberCallback(model) {
                                model.reduce(ManageSubscriptionsMviModel.Intent.HapticIndication)
                            },
                            onDismissToStart = rememberCallback(model) {
                                model.reduce(
                                    ManageSubscriptionsMviModel.Intent.Unsubscribe(community.id),
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
                                        .background(MaterialTheme.colorScheme.background)
                                        .onClick(
                                            rememberCallback {
                                                navigatorCoordinator.getRootNavigator()?.push(
                                                    CommunityDetailScreen(community),
                                                )
                                            },
                                        ),
                                    community = community,
                                    autoLoadImages = uiState.autoLoadImages,
                                )
                            },
                        )
                    }

                    if (uiState.multiCommunities.isEmpty() && uiState.communities.isEmpty() && !uiState.initial) {
                        item {
                            Text(
                                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                textAlign = TextAlign.Center,
                                text = stringResource(MR.strings.message_empty_list),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }

                if (!uiState.initial) {
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
}