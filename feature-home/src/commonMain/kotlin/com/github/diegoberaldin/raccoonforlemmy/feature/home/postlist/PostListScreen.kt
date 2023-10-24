package com.github.diegoberaldin.raccoonforlemmy.feature.home.postlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.report.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.feature.home.di.getHomeScreenModel
import com.github.diegoberaldin.raccoonforlemmy.feature.home.ui.HomeTab
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getHomeScreenModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val isFabVisible = remember { mutableStateOf(true) }
        val fabNestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y < -1) {
                        isFabVisible.value = false
                    }
                    if (available.y > 1) {
                        isFabVisible.value = true
                    }
                    return Offset.Zero
                }
            }
        }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val navigator = remember { navigationCoordinator.getRootNavigator() }
        val notificationCenter = remember { getNotificationCenter() }
        val themeRepository = remember { getThemeRepository() }
        val upvoteColor by themeRepository.upvoteColor.collectAsState()
        val downvoteColor by themeRepository.downvoteColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }

        val lazyListState = rememberLazyListState()
        LaunchedEffect(navigator) {
            navigationCoordinator.onDoubleTabSelection.onEach { tab ->
                if (tab == HomeTab) {
                    lazyListState.scrollToItem(0)
                    topAppBarState.heightOffset = 0f
                    topAppBarState.contentOffset = 0f
                }
            }.launchIn(this)
        }
        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    PostListMviModel.Effect.BackToTop -> {
                        lazyListState.scrollToItem(0)
                    }
                }
            }.launchIn(this)
        }
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val scope = rememberCoroutineScope()
        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                PostsTopBar(
                    currentInstance = uiState.instance,
                    listingType = uiState.listingType,
                    sortType = uiState.sortType,
                    scrollBehavior = scrollBehavior,
                    onHamburgerTapped = {
                        scope.launch {
                            drawerCoordinator.toggleDrawer()
                        }
                    },
                    onSelectListingType = {
                        val sheet = ListingTypeBottomSheet(
                            isLogged = uiState.isLogged,
                        )
                        notificationCenter.addObserver({ result ->
                            (result as? ListingType)?.also {
                                model.reduce(PostListMviModel.Intent.ChangeListing(it))
                            }
                        }, key, NotificationCenterContractKeys.ChangeFeedType)
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
                        }, key, NotificationCenterContractKeys.ChangeSortType)
                        bottomSheetNavigator.show(sheet)
                    },
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isFabVisible.value,
                    enter = slideInVertically(
                        initialOffsetY = { it * 2 },
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it * 2 },
                    ),
                ) {
                    FloatingActionButtonMenu(
                        modifier = Modifier.padding(bottom = Dimensions.topBarHeight),
                        items = buildList {
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.ExpandLess,
                                text = stringResource(MR.strings.action_back_to_top),
                                onSelected = {
                                    scope.launch {
                                        lazyListState.scrollToItem(0)
                                        topAppBarState.heightOffset = 0f
                                        topAppBarState.contentOffset = 0f
                                    }
                                },
                            )
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.ClearAll,
                                text = stringResource(MR.strings.action_clear_read),
                                onSelected = {
                                    model.reduce(PostListMviModel.Intent.ClearRead)
                                    scope.launch {
                                        lazyListState.scrollToItem(0)
                                    }
                                },
                            )
                        }
                    )
                }
            }
        ) { padding ->
            if (uiState.currentUserId != null) {
                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(PostListMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth()
                        .nestedScroll(scrollBehavior.nestedScrollConnection).let {
                            val connection = navigationCoordinator.getBottomBarScrollConnection()
                            if (connection != null) {
                                it.nestedScroll(connection)
                            } else it
                        }
                        .nestedScroll(fabNestedScrollConnection)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                    ) {
                        if (uiState.posts.isEmpty() && uiState.loading) {
                            items(5) {
                                PostCardPlaceholder(
                                    postLayout = uiState.postLayout,
                                )
                                if (uiState.postLayout != PostLayout.Card) {
                                    Divider(modifier = Modifier.padding(vertical = Spacing.s))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.s))
                                }
                            }
                        }
                        itemsIndexed(uiState.posts) { idx, post ->
                            SwipeableCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.swipeActionsEnabled,
                                backgroundColor = {
                                    when (it) {
                                        DismissValue.DismissedToStart -> upvoteColor
                                            ?: defaultUpvoteColor

                                        DismissValue.DismissedToEnd -> downvoteColor
                                            ?: defaultDownVoteColor

                                        DismissValue.Default -> Color.Transparent
                                    }
                                },
                                onGestureBegin = {
                                    model.reduce(PostListMviModel.Intent.HapticIndication)
                                },
                                onDismissToStart = {
                                    model.reduce(PostListMviModel.Intent.UpVotePost(idx))
                                },
                                onDismissToEnd = {
                                    model.reduce(PostListMviModel.Intent.DownVotePost(idx))
                                },
                                swipeContent = { direction ->
                                    val icon = when (direction) {
                                        DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                        DismissDirection.EndToStart -> Icons.Default.ArrowCircleUp
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                    )
                                },
                                content = {
                                    PostCard(
                                        post = post,
                                        postLayout = uiState.postLayout,
                                        fullHeightImage = uiState.fullHeightImages,
                                        separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                        autoLoadImages = uiState.autoLoadImages,
                                        blurNsfw = uiState.blurNsfw,
                                        onClick = {
                                            model.reduce(PostListMviModel.Intent.MarkAsRead(idx))
                                            navigator?.push(
                                                PostDetailScreen(post),
                                            )
                                        },
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
                                            notificationCenter.addObserver(
                                                {
                                                    model.reduce(PostListMviModel.Intent.Refresh)
                                                },
                                                key,
                                                NotificationCenterContractKeys.CommentCreated
                                            )
                                            bottomSheetNavigator.show(screen)
                                        },
                                        onImageClick = { url ->
                                            model.reduce(PostListMviModel.Intent.MarkAsRead(idx))
                                            navigator?.push(
                                                ZoomableImageScreen(url),
                                            )
                                        },
                                        options = buildList {
                                            add(stringResource(MR.strings.post_action_share))
                                            add(stringResource(MR.strings.post_action_hide))
                                            add(stringResource(MR.strings.post_action_report))
                                            if (post.creator?.id == uiState.currentUserId) {
                                                add(stringResource(MR.strings.post_action_edit))
                                                add(stringResource(MR.strings.comment_action_delete))
                                            }
                                        },
                                        onOptionSelected = { optionIdx ->
                                            when (optionIdx) {
                                                4 -> model.reduce(
                                                    PostListMviModel.Intent.DeletePost(
                                                        post.id
                                                    )
                                                )

                                                3 -> {
                                                    notificationCenter.addObserver(
                                                        {
                                                            model.reduce(PostListMviModel.Intent.Refresh)
                                                        },
                                                        key,
                                                        NotificationCenterContractKeys.PostCreated
                                                    )
                                                    bottomSheetNavigator.show(
                                                        CreatePostScreen(
                                                            editedPost = post,
                                                        )
                                                    )
                                                }

                                                2 -> {
                                                    bottomSheetNavigator.show(
                                                        CreateReportScreen(
                                                            postId = post.id
                                                        )
                                                    )
                                                }

                                                1 -> model.reduce(
                                                    PostListMviModel.Intent.Hide(idx)
                                                )

                                                else -> model.reduce(
                                                    PostListMviModel.Intent.SharePost(idx)
                                                )
                                            }
                                        }
                                    )
                                },
                            )
                            if (uiState.postLayout != PostLayout.Card) {
                                Divider(modifier = Modifier.padding(vertical = Spacing.s))
                            } else {
                                Spacer(modifier = Modifier.height(Spacing.s))
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
                        if (uiState.posts.isEmpty() && !uiState.loading) {
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
                        item {
                            Spacer(modifier = Modifier.height(Spacing.xxxl))
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
}
