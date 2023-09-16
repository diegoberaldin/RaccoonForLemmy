package com.github.diegoberaldin.raccoonforlemmy.feature.home.postlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.feature.home.di.getHomeScreenModel
import com.github.diegoberaldin.raccoonforlemmy.feature.home.ui.HomeTab
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PostListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getHomeScreenModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val bottomNavCoordinator = remember { getNavigationCoordinator() }
        val navigator = remember { bottomNavCoordinator.getRootNavigator() }
        val notificationCenter = remember { getNotificationCenter() }
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }

        val lazyListState = rememberLazyListState()
        LaunchedEffect(navigator) {
            bottomNavCoordinator.onDoubleTabSelection.onEach { tab ->
                if (tab == HomeTab) {
                    lazyListState.scrollToItem(0)
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                PostsTopBar(
                    currentInstance = uiState.instance,
                    listingType = uiState.listingType,
                    sortType = uiState.sortType,
                    scrollBehavior = scrollBehavior,
                    onSelectListingType = {
                        val sheet = ListingTypeBottomSheet(
                            isLogged = uiState.isLogged,
                        )
                        notificationCenter.addObserver({ result ->
                            (result as? ListingType)?.also {
                                model.reduce(PostListMviModel.Intent.ChangeListing(it))
                            }
                        }, key, sheet.key)
                        bottomSheetNavigator.show(sheet)
                    },
                    onSelectSortType = {
                        val sheet = SortBottomSheet(
                            expandTop = true,
                        )
                        notificationCenter.addObserver({
                            (it as? SortType)?.also { sortType ->
                                model.reduce(
                                    PostListMviModel.Intent.ChangeSort(sortType)
                                )
                            }
                        }, key, sheet.key)
                        bottomSheetNavigator.show(sheet)
                    },
                )
            },
        ) { padding ->
            if (uiState.currentUserId != null) {
                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(PostListMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier.padding(padding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection).let {
                            val connection = bottomNavCoordinator.getBottomBarScrollConnection()
                            if (connection != null) {
                                it.nestedScroll(connection)
                            } else it
                        }.pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        itemsIndexed(uiState.posts) { idx, post ->
                            val themeRepository = remember { getThemeRepository() }
                            val fontScale by themeRepository.contentFontScale.collectAsState()
                            CompositionLocalProvider(
                                LocalDensity provides Density(
                                    density = LocalDensity.current.density,
                                    fontScale = fontScale,
                                ),
                            ) {
                                SwipeableCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onGestureBegin = {
                                        model.reduce(PostListMviModel.Intent.HapticIndication)
                                    },
                                    onDismissToStart = {
                                        model.reduce(PostListMviModel.Intent.UpVotePost(idx))
                                    },
                                    onDismissToEnd = {
                                        model.reduce(PostListMviModel.Intent.DownVotePost(idx))
                                    },
                                    backgroundColor = {
                                        when (it) {
                                            DismissValue.DismissedToStart -> MaterialTheme.colorScheme.secondary
                                            DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.tertiary
                                            else -> Color.Transparent
                                        }
                                    },
                                    swipeContent = { direction ->
                                        val icon = when (direction) {
                                            DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                            DismissDirection.EndToStart -> Icons.Default.ArrowCircleUp
                                        }
                                        val (iconModifier, iconTint) = when {
                                            direction == DismissDirection.StartToEnd && post.myVote < 0 -> {
                                                Modifier.background(
                                                    color = Color.Transparent,
                                                    shape = CircleShape,
                                                ) to MaterialTheme.colorScheme.onTertiary
                                            }

                                            direction == DismissDirection.StartToEnd -> {
                                                Modifier.background(
                                                    color = MaterialTheme.colorScheme.onTertiary,
                                                    shape = CircleShape,
                                                ) to MaterialTheme.colorScheme.tertiary
                                            }

                                            direction == DismissDirection.EndToStart && post.myVote > 0 -> {
                                                Modifier.background(
                                                    color = Color.Transparent,
                                                    shape = CircleShape,
                                                ) to MaterialTheme.colorScheme.onSecondary
                                            }

                                            else -> {
                                                Modifier.background(
                                                    color = MaterialTheme.colorScheme.onSecondary,
                                                    shape = CircleShape,
                                                ) to MaterialTheme.colorScheme.secondary
                                            }
                                        }
                                        Icon(
                                            modifier = iconModifier,
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconTint,
                                        )
                                    },
                                    content = {
                                        PostCard(
                                            modifier = Modifier.onClick {
                                                navigator?.push(
                                                    PostDetailScreen(post),
                                                )
                                            },
                                            post = post,
                                            options = buildList {
                                                if (post.creator?.id == uiState.currentUserId) {
                                                    add(stringResource(MR.strings.comment_action_delete))
                                                }
                                            },
                                            blurNsfw = uiState.blurNsfw,
                                            onOpenCommunity = { community ->
                                                navigator?.push(
                                                    CommunityDetailScreen(community),
                                                )
                                            },
                                            onOpenCreator = { user ->
                                                navigator?.push(
                                                    UserDetailScreen(user),
                                                )
                                            },
                                            onUpVote = {
                                                model.reduce(
                                                    PostListMviModel.Intent.UpVotePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onDownVote = {
                                                model.reduce(
                                                    PostListMviModel.Intent.DownVotePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onSave = {
                                                model.reduce(
                                                    PostListMviModel.Intent.SavePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onReply = {
                                                val screen = CreateCommentScreen(
                                                    originalPost = post,
                                                )
                                                notificationCenter.addObserver({
                                                    model.reduce(PostListMviModel.Intent.Refresh)
                                                }, key, screen.key)
                                                bottomSheetNavigator.show(screen)
                                            },
                                            onImageClick = { url ->
                                                navigator?.push(
                                                    ZoomableImageScreen(url),
                                                )
                                            },
                                            onOptionSelected = {idx ->
                                                when(idx) {
                                                    else -> model.reduce(PostListMviModel.Intent.DeletePost(post.id))
                                                }
                                            }
                                        )
                                    },
                                )
                            }
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(PostListMviModel.Intent.LoadNextPage)
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
                        item {
                            Spacer(modifier = Modifier.height(Spacing.xxxl))
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
    }
}
