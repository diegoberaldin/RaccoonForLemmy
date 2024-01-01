package com.github.diegoberaldin.raccoonforlemmy.unit.postlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.BlockBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ShareBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.keepscreenon.rememberKeepScreenOn
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostListScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<PostListMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val themeRepository = remember { getThemeRepository() }
        val upvoteColor by themeRepository.upvoteColor.collectAsState()
        val downvoteColor by themeRepository.downvoteColor.collectAsState()
        val replyColor by themeRepository.replyColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultReplyColor = MaterialTheme.colorScheme.secondary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val lazyListState = rememberLazyListState()
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val scope = rememberCoroutineScope()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val keepScreenOn = rememberKeepScreenOn()
        val detailOpener = remember { getDetailOpener() }

        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.onDoubleTabSelection.onEach { section ->
                if (section == TabNavigationSection.Home) {
                    lazyListState.scrollToItem(0)
                    topAppBarState.heightOffset = 0f
                    topAppBarState.contentOffset = 0f
                }
            }.launchIn(this)
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    PostListMviModel.Effect.BackToTop -> {
                        lazyListState.scrollToItem(0)
                        topAppBarState.heightOffset = 0f
                        topAppBarState.contentOffset = 0f
                    }

                    is PostListMviModel.Effect.ZombieModeTick -> {
                        if (effect.index >= 0) {
                            lazyListState.animateScrollBy(
                                value = settings.zombieModeScrollAmount,
                                animationSpec = tween(350),
                            )
                        }
                    }
                }
            }.launchIn(this)
        }
        LaunchedEffect(uiState.zombieModeActive) {
            if (uiState.zombieModeActive) {
                keepScreenOn.activate()
            } else {
                keepScreenOn.deactivate()
            }
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                PostsTopBar(
                    currentInstance = uiState.instance,
                    listingType = uiState.listingType,
                    sortType = uiState.sortType,
                    scrollBehavior = scrollBehavior,
                    topAppBarState = topAppBarState,
                    edgeToEdge = settings.edgeToEdge,
                    onHamburgerTapped = rememberCallback {
                        scope.launch {
                            drawerCoordinator.toggleDrawer()
                        }
                    },
                    onSelectListingType = rememberCallback {
                        val sheet = ListingTypeBottomSheet(
                            isLogged = uiState.isLogged,
                            sheetKey = key,
                        )
                        navigationCoordinator.showBottomSheet(sheet)
                    },
                    onSelectSortType = rememberCallback {
                        val sheet = SortBottomSheet(
                            sheetKey = key,
                            values = uiState.availableSortTypes.map { it.toInt() },
                            comments = false,
                            expandTop = true,
                        )
                        navigationCoordinator.showBottomSheet(sheet)
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
                        modifier = Modifier.padding(bottom = Dimensions.topBarHeight),
                        items = buildList {
                            if (uiState.zombieModeActive) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.SyncDisabled,
                                    text = stringResource(MR.strings.action_deactivate_zombie_mode),
                                    onSelected = rememberCallback(model) {
                                        model.reduce(PostListMviModel.Intent.PauseZombieMode)
                                    },
                                )
                            } else {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.Sync,
                                    text = stringResource(MR.strings.action_activate_zombie_mode),
                                    onSelected = rememberCallback(model) {
                                        model.reduce(PostListMviModel.Intent.StartZombieMode(-1))
                                    },
                                )
                            }
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
                            if (uiState.isLogged) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.ClearAll,
                                    text = stringResource(MR.strings.action_clear_read),
                                    onSelected = rememberCallback {
                                        model.reduce(PostListMviModel.Intent.ClearRead)
                                        scope.launch {
                                            lazyListState.scrollToItem(0)
                                            topAppBarState.heightOffset = 0f
                                            topAppBarState.contentOffset = 0f
                                        }
                                    },
                                )
                            }
                        }
                    )
                }
            },
        ) { padding ->
            if (uiState.currentUserId != null) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh = rememberCallback(model) {
                        model.reduce(PostListMviModel.Intent.Refresh)
                    },
                )
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxWidth()
                        .let {
                            val connection = navigationCoordinator.getBottomBarScrollConnection()
                            if (connection != null && settings.hideNavigationBarWhileScrolling) {
                                it.nestedScroll(connection)
                            } else {
                                it
                            }
                        }
                        .let {
                            if (settings.hideNavigationBarWhileScrolling) {
                                it.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                it
                            }
                        }
                        .nestedScroll(fabNestedScrollConnection)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                        userScrollEnabled = !uiState.zombieModeActive,
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
                        items(uiState.posts, key = { it.id.toString() + it.updateDate }) { post ->
                            LaunchedEffect(post.id) {
                                if (settings.markAsReadWhileScrolling && !post.read) {
                                    model.reduce(PostListMviModel.Intent.MarkAsRead(post.id))
                                }
                            }
                            SwipeableCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.swipeActionsEnabled,
                                directions = if (!uiState.isLogged) {
                                    emptySet()
                                } else {
                                    setOf(
                                        DismissDirection.StartToEnd,
                                        DismissDirection.EndToStart,
                                    )
                                },
                                enableSecondAction = rememberCallbackArgs { value ->
                                    if (!uiState.isLogged) {
                                        false
                                    } else {
                                        value == DismissValue.DismissedToStart
                                    }
                                },
                                backgroundColor = rememberCallbackArgs { direction ->
                                    when (direction) {
                                        DismissValue.DismissedToStart -> upvoteColor
                                            ?: defaultUpvoteColor

                                        DismissValue.DismissedToEnd -> downvoteColor
                                            ?: defaultDownVoteColor

                                        DismissValue.Default -> Color.Transparent
                                    }
                                },
                                secondBackgroundColor = rememberCallbackArgs { direction ->
                                    when (direction) {
                                        DismissValue.DismissedToStart -> replyColor
                                            ?: defaultReplyColor

                                        else -> Color.Transparent
                                    }
                                },
                                onGestureBegin = rememberCallback(model) {
                                    model.reduce(PostListMviModel.Intent.HapticIndication)
                                },
                                onDismissToStart = rememberCallback(model) {
                                    model.reduce(PostListMviModel.Intent.UpVotePost(post.id))
                                },
                                onSecondDismissToStart = rememberCallback(model) {
                                    detailOpener.openReply(
                                        originalPost = post,
                                    )
                                },
                                onDismissToEnd = rememberCallback(model) {
                                    model.reduce(PostListMviModel.Intent.DownVotePost(post.id))
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
                                secondSwipeContent = { direction ->
                                    val icon = when (direction) {
                                        DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                        DismissDirection.EndToStart -> Icons.Default.Reply
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
                                        limitBodyHeight = true,
                                        fullHeightImage = uiState.fullHeightImages,
                                        voteFormat = uiState.voteFormat,
                                        autoLoadImages = uiState.autoLoadImages,
                                        actionButtonsActive = uiState.isLogged,
                                        blurNsfw = uiState.blurNsfw,
                                        onClick = rememberCallback(model) {
                                            model.reduce(PostListMviModel.Intent.MarkAsRead(post.id))
                                            detailOpener.openPostDetail(post)
                                        },
                                        onDoubleClick = if (!uiState.doubleTapActionEnabled || !uiState.isLogged) {
                                            null
                                        } else {
                                            rememberCallback(model) {
                                                model.reduce(
                                                    PostListMviModel.Intent.UpVotePost(
                                                        id = post.id,
                                                    ),
                                                )
                                            }
                                        },
                                        onOpenCommunity = rememberCallbackArgs { community, instance ->
                                            detailOpener.openCommunityDetail(community, instance)
                                        },
                                        onOpenCreator = rememberCallbackArgs { user, instance ->
                                            detailOpener.openUserDetail(user, instance)
                                        },
                                        onOpenPost = rememberCallbackArgs { p, instance ->
                                            detailOpener.openPostDetail(p, instance)
                                        },
                                        onOpenWeb = rememberCallbackArgs { url ->
                                            navigationCoordinator.pushScreen(
                                                WebViewScreen(url)
                                            )
                                        },
                                        onUpVote = rememberCallback(model) {
                                            if (uiState.isLogged) {
                                                model.reduce(
                                                    PostListMviModel.Intent.UpVotePost(
                                                        id = post.id,
                                                    ),
                                                )
                                            }
                                        },
                                        onDownVote = rememberCallback(model) {
                                            if (uiState.isLogged) {
                                                model.reduce(
                                                    PostListMviModel.Intent.DownVotePost(
                                                        id = post.id,
                                                    ),
                                                )
                                            }
                                        },
                                        onSave = rememberCallback(model) {
                                            if (uiState.isLogged) {
                                                model.reduce(
                                                    PostListMviModel.Intent.SavePost(
                                                        id = post.id,
                                                    ),
                                                )
                                            }
                                        },
                                        onReply = rememberCallback(model) {
                                            if (uiState.isLogged) {
                                                model.reduce(PostListMviModel.Intent.MarkAsRead(post.id))
                                                detailOpener.openPostDetail(post)
                                            }
                                        },
                                        onOpenImage = rememberCallbackArgs(model, post) { url ->
                                            model.reduce(PostListMviModel.Intent.MarkAsRead(post.id))
                                            navigationCoordinator.pushScreen(
                                                ZoomableImageScreen(url)
                                            )
                                        },
                                        options = buildList {
                                            add(
                                                Option(
                                                    OptionId.Share,
                                                    stringResource(MR.strings.post_action_share),
                                                ),
                                            )
                                            if (uiState.isLogged) {
                                                add(
                                                    Option(
                                                        OptionId.Hide,
                                                        stringResource(MR.strings.post_action_hide),
                                                    ),
                                                )
                                                add(
                                                    Option(
                                                        OptionId.Block,
                                                        stringResource(MR.strings.community_detail_block),
                                                    ),
                                                )
                                            }
                                            add(
                                                Option(
                                                    OptionId.SeeRaw,
                                                    stringResource(MR.strings.post_action_see_raw),
                                                ),
                                            )
                                            if (uiState.isLogged) {
                                                add(
                                                    Option(
                                                        OptionId.CrossPost,
                                                        stringResource(MR.strings.post_action_cross_post),
                                                    ),
                                                )
                                                add(
                                                    Option(
                                                        OptionId.Report,
                                                        stringResource(MR.strings.post_action_report),
                                                    ),
                                                )
                                            }
                                            if (post.creator?.id == uiState.currentUserId) {
                                                add(
                                                    Option(
                                                        OptionId.Edit,
                                                        stringResource(MR.strings.post_action_edit),
                                                    ),
                                                )
                                                add(
                                                    Option(
                                                        OptionId.Delete,
                                                        stringResource(MR.strings.comment_action_delete),
                                                    ),
                                                )
                                            }
                                        },
                                        onOptionSelected = rememberCallbackArgs(model) { optinId ->
                                            when (optinId) {
                                                OptionId.Delete -> model.reduce(
                                                    PostListMviModel.Intent.DeletePost(post.id)
                                                )

                                                OptionId.Edit -> {
                                                    detailOpener.openCreatePost(editedPost = post)
                                                }

                                                OptionId.Report -> {
                                                    navigationCoordinator.showBottomSheet(
                                                        CreateReportScreen(postId = post.id)
                                                    )
                                                }

                                                OptionId.CrossPost -> {
                                                    detailOpener.openCreatePost(crossPost = post)
                                                }

                                                OptionId.SeeRaw -> {
                                                    rawContent = post
                                                }

                                                OptionId.Hide -> model.reduce(
                                                    PostListMviModel.Intent.Hide(
                                                        post.id
                                                    )
                                                )

                                                OptionId.Share -> {
                                                    val urls = listOfNotNull(
                                                        post.originalUrl,
                                                        "https://${uiState.instance}/post/${post.id}"
                                                    ).distinct()
                                                    if (urls.size == 1) {
                                                        model.reduce(
                                                            PostListMviModel.Intent.Share(
                                                                urls.first()
                                                            )
                                                        )
                                                    } else {
                                                        val screen = ShareBottomSheet(urls = urls)
                                                        navigationCoordinator.showBottomSheet(screen)
                                                    }
                                                }

                                                OptionId.Block -> {
                                                    val screen = BlockBottomSheet(
                                                        userName = buildString {
                                                            post.creator?.also {
                                                                append(it.name)
                                                                if (it.host.isNotEmpty()) {
                                                                    append("@")
                                                                    append(it.host)
                                                                }
                                                            }
                                                        },
                                                        userId = post.creator?.id,
                                                        communityName = buildString {
                                                            post.community?.also {
                                                                append(it.name)
                                                                if (it.host.isNotEmpty()) {
                                                                    append("@")
                                                                    append(it.host)
                                                                }
                                                            }
                                                        },
                                                        communityId = post.community?.id,
                                                        instanceName = post.community?.host,
                                                        instanceId = post.community?.instanceId,
                                                        userInstanceName = post.creator?.host,
                                                        userInstanceId = post.creator?.instanceId,
                                                    )
                                                    navigationCoordinator.showBottomSheet(screen)
                                                }

                                                else -> Unit
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

        if (rawContent != null) {
            when (val content = rawContent) {
                is PostModel -> {
                    RawContentDialog(
                        title = content.title,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        url = content.url,
                        text = content.text,
                        onDismiss = rememberCallback {
                            rawContent = null
                        },
                        onQuote = rememberCallbackArgs { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                detailOpener.openReply(
                                    originalPost = content,
                                    initialText = buildString {
                                        append("> ")
                                        append(quotation)
                                        append("\n\n")
                                    },
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
