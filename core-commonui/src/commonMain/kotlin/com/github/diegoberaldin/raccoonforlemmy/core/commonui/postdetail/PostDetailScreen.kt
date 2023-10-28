package com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getPostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.report.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PostDetailScreen(
    private val post: PostModel,
    private val otherInstance: String = "",
    private val highlightCommentId: Int? = null,
) : Screen {

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterialApi::class,
        ExperimentalLayoutApi::class
    )
    @Composable
    override fun Content() {
        val model = rememberScreenModel(
            tag = post.id.toString() + highlightCommentId.toString()
        ) {
            getPostDetailViewModel(
                post = post,
                highlightCommentId = highlightCommentId,
                otherInstance = otherInstance,
            )
        }
        model.bindToLifecycle(key + post.id.toString())
        val uiState by model.uiState.collectAsState()
        val isOnOtherInstance = otherInstance.isNotEmpty()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val navigator = remember { navigationCoordinator.getRootNavigator() }
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
        val notificationCenter = remember { getNotificationCenter() }
        val drawerCoordinator = remember { getDrawerCoordinator() }
        DisposableEffect(key) {
            drawerCoordinator.setGesturesEnabled(false)
            onDispose {
                notificationCenter.removeObserver(key)
                drawerCoordinator.setGesturesEnabled(true)
            }
        }
        val themeRepository = remember { getThemeRepository() }
        val upvoteColor by themeRepository.upvoteColor.collectAsState()
        val downvoteColor by themeRepository.downvoteColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var rawContent by remember { mutableStateOf<Any?>(null) }

        LaunchedEffect(model) {
            model.effects.onEach { evt ->
                when (evt) {
                    PostDetailMviModel.Effect.Close -> {
                        navigator?.pop()
                    }

                    is PostDetailMviModel.Effect.ScrollToComment -> {
                        lazyListState.scrollToItem(evt.index)
                    }

                    PostDetailMviModel.Effect.BackToTop -> {
                        scope.launch {
                            lazyListState.scrollToItem(0)
                        }
                    }
                }
            }.launchIn(this)
        }

        val statePost = uiState.post
        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = statePost.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        Image(
                            modifier = Modifier.onClick {
                                val sheet = SortBottomSheet(
                                    values = listOf(
                                        SortType.Hot,
                                        SortType.Top.Generic,
                                        SortType.New,
                                        SortType.Old,
                                        SortType.Controversial,
                                    ),
                                )
                                notificationCenter.addObserver({
                                    (it as? SortType)?.also { sortType ->
                                        model.reduce(PostDetailMviModel.Intent.ChangeSort(sortType))
                                    }
                                }, key, NotificationCenterContractKeys.ChangeSortType)
                                bottomSheetNavigator.show(sheet)
                            },
                            imageVector = uiState.sortType.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    navigationIcon = {
                        if (navigator?.canPop == true) {
                            Image(
                                modifier = Modifier.onClick {
                                    navigator.pop()
                                },
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                        }
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
                    FloatingActionButtonMenu(items = buildList {
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
                        if (!isOnOtherInstance) {
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.Reply,
                                text = stringResource(MR.strings.action_reply),
                                onSelected = {
                                    val screen = CreateCommentScreen(
                                        originalPost = statePost,
                                    )
                                    notificationCenter.addObserver({
                                        model.reduce(PostDetailMviModel.Intent.Refresh)
                                        model.reduce(PostDetailMviModel.Intent.RefreshPost)
                                    }, key, NotificationCenterContractKeys.CommentCreated)
                                    bottomSheetNavigator.show(screen)
                                },
                            )
                        }
                    })
                }
            },
        ) { padding ->
            if (uiState.currentUserId != null) {
                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(PostDetailMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier.padding(padding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .nestedScroll(fabNestedScrollConnection).pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState
                    ) {
                        item {
                            PostCard(
                                post = statePost,
                                postLayout = uiState.postLayout,
                                fullHeightImage = uiState.fullHeightImages,
                                includeFullBody = true,
                                separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                autoLoadImages = uiState.autoLoadImages,
                                blurNsfw = false,
                                onOpenCommunity = { community ->
                                    navigator?.push(
                                        CommunityDetailScreen(community = community)
                                    )
                                },
                                onOpenCreator = { user ->
                                    navigator?.push(
                                        UserDetailScreen(user = user)
                                    )
                                },
                                onUpVote = {
                                    if (!isOnOtherInstance) {
                                        model.reduce(
                                            PostDetailMviModel.Intent.UpVotePost(
                                                feedback = true,
                                            ),
                                        )
                                    }
                                },
                                onDownVote = {
                                    if (!isOnOtherInstance) {
                                        model.reduce(
                                            PostDetailMviModel.Intent.DownVotePost(
                                                feedback = true,
                                            ),
                                        )
                                    }
                                },
                                onSave = {
                                    model.reduce(
                                        PostDetailMviModel.Intent.SavePost(
                                            post = statePost,
                                            feedback = true,
                                        ),
                                    )
                                },
                                onReply = {
                                    if (!isOnOtherInstance) {
                                        val screen = CreateCommentScreen(
                                            originalPost = statePost,
                                        )
                                        notificationCenter.addObserver({
                                            model.reduce(PostDetailMviModel.Intent.Refresh)
                                            model.reduce(PostDetailMviModel.Intent.RefreshPost)
                                        }, key, NotificationCenterContractKeys.CommentCreated)
                                        bottomSheetNavigator.show(screen)
                                    }
                                },
                                options = buildList {
                                    add(stringResource(MR.strings.post_action_share))
                                    add(stringResource(MR.strings.post_action_see_raw))
                                    add(stringResource(MR.strings.post_action_report))
                                    if (statePost.creator?.id == uiState.currentUserId && !isOnOtherInstance) {
                                        add(stringResource(MR.strings.post_action_edit))
                                        add(stringResource(MR.strings.comment_action_delete))
                                    }
                                },
                                onOptionSelected = { idx ->
                                    when (idx) {
                                        4 -> model.reduce(PostDetailMviModel.Intent.DeletePost)

                                        3 -> {
                                            notificationCenter.addObserver(
                                                {
                                                    model.reduce(PostDetailMviModel.Intent.RefreshPost)
                                                }, key, NotificationCenterContractKeys.PostCreated
                                            )
                                            bottomSheetNavigator.show(
                                                CreatePostScreen(
                                                    editedPost = statePost,
                                                )
                                            )
                                        }

                                        2 -> {
                                            bottomSheetNavigator.show(CreateReportScreen(postId = statePost.id))
                                        }

                                        1 -> {
                                            rawContent = statePost
                                        }

                                        else -> model.reduce(PostDetailMviModel.Intent.SharePost)
                                    }
                                },
                                onImageClick = { url ->
                                    navigator?.push(
                                        ZoomableImageScreen(url),
                                    )
                                },
                            )
                            if (uiState.postLayout != PostLayout.Card) {
                                Divider(modifier = Modifier.padding(vertical = Spacing.s))
                            } else {
                                Spacer(modifier = Modifier.height(Spacing.s))
                            }
                        }
                        if (statePost.crossPosts.isNotEmpty()) {
                            item {
                                FlowRow(
                                    modifier = Modifier.padding(
                                        top = Spacing.xxs,
                                        bottom = Spacing.s,
                                        start = Spacing.s,
                                        end = Spacing.s,
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                                ) {
                                    Text(
                                        text = stringResource(MR.strings.post_detail_cross_posts),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    statePost.crossPosts.forEachIndexed { index, crossPost ->
                                        val community = crossPost.community
                                        if (community != null) {
                                            val string = buildAnnotatedString {
                                                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                                    append(community.name)
                                                    append("@")
                                                    append(community.host)
                                                }
                                                if (index < statePost.crossPosts.lastIndex) {
                                                    append(", ")
                                                }
                                            }
                                            Text(
                                                modifier = Modifier.onClick {
                                                    navigator?.push(
                                                        CommunityDetailScreen(
                                                            community = community
                                                        )
                                                    )
                                                },
                                                text = string,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (uiState.comments.isEmpty() && uiState.loading && uiState.initial) {
                            items(5) {
                                CommentCardPlaceholder()
                                Divider(
                                    modifier = Modifier.padding(vertical = Spacing.xxxs),
                                    thickness = 0.25.dp
                                )
                            }
                        }
                        itemsIndexed(uiState.comments, key = { _, c -> c.id }) { idx, comment ->
                            val commentId = comment.id
                            AnimatedVisibility(
                                visible = comment.visible,
                                exit = fadeOut(),
                                enter = fadeIn(),

                                ) {
                                SwipeableCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = uiState.swipeActionsEnabled && !isOnOtherInstance,
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
                                        model.reduce(PostDetailMviModel.Intent.HapticIndication)
                                    },
                                    onDismissToStart = {
                                        model.reduce(
                                            PostDetailMviModel.Intent.UpVoteComment(commentId),
                                        )
                                    },
                                    onDismissToEnd = {
                                        model.reduce(
                                            PostDetailMviModel.Intent.DownVoteComment(commentId),
                                        )
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
                                        CommentCard(
                                            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                                                .let {
                                                    if (comment.id == highlightCommentId) {
                                                        it.background(
                                                            MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                                5.dp
                                                            ).copy(
                                                                alpha = 0.75f
                                                            )
                                                        )
                                                    } else {
                                                        it
                                                    }
                                                }.onClick {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.ToggleExpandComment(
                                                            commentId,
                                                        )
                                                    )
                                                },
                                            comment = comment,
                                            separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                            autoLoadImages = uiState.autoLoadImages,
                                            onUpVote = {
                                                if (!isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.UpVoteComment(
                                                            commentId = commentId,
                                                            feedback = true,
                                                        ),
                                                    )
                                                }
                                            },
                                            onDownVote = {
                                                if (!isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.DownVoteComment(
                                                            commentId = commentId,
                                                            feedback = true,
                                                        ),
                                                    )
                                                }
                                            },
                                            onSave = {
                                                if (!isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.SaveComment(
                                                            commentId = commentId,
                                                            feedback = true,
                                                        ),
                                                    )
                                                }
                                            },
                                            onReply = {
                                                if (!isOnOtherInstance) {
                                                    val screen = CreateCommentScreen(
                                                        originalPost = statePost,
                                                        originalComment = comment,
                                                    )
                                                    notificationCenter.addObserver(
                                                        {
                                                            model.reduce(PostDetailMviModel.Intent.Refresh)
                                                            model.reduce(PostDetailMviModel.Intent.RefreshPost)
                                                        },
                                                        key,
                                                        NotificationCenterContractKeys.CommentCreated
                                                    )
                                                    bottomSheetNavigator.show(screen)
                                                }
                                            },
                                            onOpenCreator = {
                                                val user = comment.creator
                                                if (user != null) {
                                                    navigator?.push(
                                                        UserDetailScreen(
                                                            user = user,
                                                            otherInstance = otherInstance,
                                                        ),
                                                    )
                                                }
                                            },
                                            onOpenCommunity = {
                                                val community = comment.community
                                                if (community != null) {
                                                    navigator?.push(
                                                        CommunityDetailScreen(
                                                            community = community,
                                                            otherInstance = otherInstance,
                                                        ),
                                                    )
                                                }
                                            },
                                            options = buildList {
                                                add(stringResource(MR.strings.post_action_see_raw))
                                                add(stringResource(MR.strings.post_action_report))
                                                if (comment.creator?.id == uiState.currentUserId) {
                                                    add(stringResource(MR.strings.post_action_edit))
                                                    add(stringResource(MR.strings.comment_action_delete))
                                                }
                                            },
                                            onOptionSelected = { optionId ->
                                                when (optionId) {
                                                    3 -> model.reduce(
                                                        PostDetailMviModel.Intent.DeleteComment(
                                                            comment.id
                                                        )
                                                    )

                                                    2 -> {
                                                        notificationCenter.addObserver(
                                                            {
                                                                model.reduce(PostDetailMviModel.Intent.Refresh)
                                                                model.reduce(PostDetailMviModel.Intent.RefreshPost)
                                                            },
                                                            key,
                                                            NotificationCenterContractKeys.CommentCreated
                                                        )
                                                        bottomSheetNavigator.show(
                                                            CreateCommentScreen(
                                                                editedComment = comment,
                                                            )
                                                        )
                                                    }

                                                    1 -> {
                                                        bottomSheetNavigator.show(
                                                            CreateReportScreen(
                                                                commentId = comment.id
                                                            )
                                                        )
                                                    }

                                                    else -> {
                                                        rawContent = comment
                                                    }
                                                }
                                            },
                                        )
                                    },
                                )
                                Divider(
                                    modifier = Modifier.padding(vertical = Spacing.xxxs),
                                    thickness = 0.25.dp
                                )
                            }
                            if (comment.loadMoreButtonVisible) {
                                Row {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(onClick = {
                                        model.reduce(
                                            PostDetailMviModel.Intent.FetchMoreComments(
                                                parentId = comment.id
                                            )
                                        )
                                    }) {
                                        Text(
                                            text = stringResource(MR.strings.post_detail_load_more_comments),
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(PostDetailMviModel.Intent.LoadNextPage)
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
                        if (uiState.comments.isEmpty() && !uiState.loading && !uiState.initial) {
                            item {
                                androidx.compose.material.Text(
                                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                    textAlign = TextAlign.Center,
                                    text = stringResource(MR.strings.message_empty_comments),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(Spacing.s))
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
                        url = content.url,
                        text = content.text,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }

                is CommentModel -> {
                    RawContentDialog(
                        text = content.text,
                        onDismiss = {
                            rawContent = null
                        },
                    )
                }
            }
        }
    }
}
