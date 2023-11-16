package com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CollapsedCommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost.CreatePostScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getPostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.report.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.subscribe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
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
        ExperimentalLayoutApi::class,
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
        val isOnOtherInstance = remember { otherInstance.isNotEmpty() }
        val otherInstanceName = remember { otherInstance }
        val commentIdToHighlight = remember { highlightCommentId }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val themeRepository = remember { getThemeRepository() }
        val upvoteColor by themeRepository.upvoteColor.collectAsState()
        val downvoteColor by themeRepository.downvoteColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()

        DisposableEffect(key) {
            drawerCoordinator.setGesturesEnabled(false)
            onDispose {
                drawerCoordinator.setGesturesEnabled(true)
            }
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.subscribe<NotificationCenterEvent.ChangeSortType>().onEach { evt ->
                if (evt.key == key) {
                    model.reduce(PostDetailMviModel.Intent.ChangeSort(evt.value))
                }
            }.launchIn(this)

            notificationCenter.subscribe<NotificationCenterEvent.CommentCreated>().onEach {
                model.reduce(PostDetailMviModel.Intent.Refresh)
                model.reduce(PostDetailMviModel.Intent.RefreshPost)
            }.launchIn(this)

            notificationCenter.subscribe<NotificationCenterEvent.PostUpdated>().onEach {
                model.reduce(PostDetailMviModel.Intent.RefreshPost)
            }.launchIn(this)
        }
        LaunchedEffect(model) {
            model.effects.onEach { evt ->
                when (evt) {
                    PostDetailMviModel.Effect.Close -> {
                        navigationCoordinator.popScreen()
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

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = uiState.post.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    val sheet = SortBottomSheet(
                                        sheetKey = key,
                                        comments = true,
                                        values = listOf(
                                            SortType.Hot,
                                            SortType.Top.Generic,
                                            SortType.New,
                                            SortType.Old,
                                            SortType.Controversial,
                                        ),
                                    )
                                    navigationCoordinator.showBottomSheet(sheet)
                                },
                            ),
                            imageVector = uiState.sortType.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    navigationIcon = {
                        if (navigationCoordinator.canPop) {
                            Image(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        navigationCoordinator.popScreen()
                                    },
                                ),
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
                    visible = isFabVisible,
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
                            onSelected = rememberCallback {
                                scope.launch {
                                    lazyListState.scrollToItem(0)
                                    topAppBarState.heightOffset = 0f
                                    topAppBarState.contentOffset = 0f
                                }
                            },
                        )
                        if (uiState.isLogged && !isOnOtherInstance) {
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.Reply,
                                text = stringResource(MR.strings.action_reply),
                                onSelected = rememberCallback {
                                    val screen = CreateCommentScreen(
                                        originalPost = uiState.post,
                                    )
                                    navigationCoordinator.showBottomSheet(screen)
                                },
                            )
                        }
                    })
                }
            },
        ) { padding ->
            if (uiState.currentUserId != null) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh = rememberCallback(model) {
                        model.reduce(PostDetailMviModel.Intent.Refresh)
                    },
                )
                Box(
                    modifier = Modifier.padding(padding).let {
                        if (settings.hideNavigationBarWhileScrolling) {
                            it.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            it
                        }
                    }.nestedScroll(fabNestedScrollConnection).pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState
                    ) {
                        item {
                            PostCard(
                                post = uiState.post,
                                postLayout = uiState.postLayout,
                                fullHeightImage = uiState.fullHeightImages,
                                includeFullBody = true,
                                separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                autoLoadImages = uiState.autoLoadImages,
                                blurNsfw = false,
                                onOpenCommunity = rememberCallbackArgs { community ->
                                    navigationCoordinator.pushScreen(
                                        CommunityDetailScreen(community = community)
                                    )
                                },
                                onOpenCreator = rememberCallbackArgs { user ->
                                    navigationCoordinator.pushScreen(
                                        UserDetailScreen(user = user)
                                    )
                                },
                                onUpVote = rememberCallback(model) {
                                    if (uiState.isLogged && !isOnOtherInstance) {
                                        model.reduce(
                                            PostDetailMviModel.Intent.UpVotePost(
                                                feedback = true,
                                            ),
                                        )
                                    }
                                },
                                onDownVote = rememberCallback(model) {
                                    if (uiState.isLogged && !isOnOtherInstance) {
                                        model.reduce(
                                            PostDetailMviModel.Intent.DownVotePost(
                                                feedback = true,
                                            ),
                                        )
                                    }
                                },
                                onSave = rememberCallback(model) {
                                    model.reduce(
                                        PostDetailMviModel.Intent.SavePost(
                                            post = uiState.post,
                                            feedback = true,
                                        ),
                                    )
                                },
                                onReply = rememberCallback {
                                    if (uiState.isLogged && !isOnOtherInstance) {
                                        val screen = CreateCommentScreen(
                                            originalPost = uiState.post,
                                        )
                                        navigationCoordinator.showBottomSheet(screen)
                                    }
                                },
                                options = buildList {
                                    add(
                                        Option(
                                            OptionId.Share,
                                            stringResource(MR.strings.post_action_share)
                                        )
                                    )
                                    add(
                                        Option(
                                            OptionId.SeeRaw,
                                            stringResource(MR.strings.post_action_see_raw)
                                        )
                                    )
                                    if (uiState.isLogged && !isOnOtherInstance) {
                                        add(
                                            Option(
                                                OptionId.CrossPost,
                                                stringResource(MR.strings.post_action_cross_post)
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Report,
                                                stringResource(MR.strings.post_action_report)
                                            )
                                        )
                                    }
                                    if (uiState.post.creator?.id == uiState.currentUserId && !isOnOtherInstance) {
                                        add(
                                            Option(
                                                OptionId.Edit,
                                                stringResource(MR.strings.post_action_edit)
                                            )
                                        )
                                        add(
                                            Option(
                                                OptionId.Delete,
                                                stringResource(MR.strings.comment_action_delete)
                                            )
                                        )
                                    }
                                },
                                onOptionSelected = rememberCallbackArgs(model) { idx ->
                                    when (idx) {
                                        OptionId.Delete -> model.reduce(PostDetailMviModel.Intent.DeletePost)

                                        OptionId.Edit -> {
                                            navigationCoordinator.showBottomSheet(
                                                CreatePostScreen(editedPost = uiState.post)
                                            )
                                        }

                                        OptionId.Report -> {
                                            navigationCoordinator.showBottomSheet(
                                                CreateReportScreen(postId = uiState.post.id)
                                            )
                                        }

                                        OptionId.CrossPost -> {
                                            navigationCoordinator.showBottomSheet(
                                                CreatePostScreen(crossPost = uiState.post)
                                            )
                                        }

                                        OptionId.SeeRaw -> {
                                            rawContent = uiState.post
                                        }

                                        OptionId.Share -> model.reduce(PostDetailMviModel.Intent.SharePost)

                                        else -> Unit
                                    }
                                },
                                onImageClick = rememberCallbackArgs { url ->
                                    navigationCoordinator.pushScreen(
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
                        if (uiState.post.crossPosts.isNotEmpty()) {
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
                                    uiState.post.crossPosts.forEachIndexed { index, crossPost ->
                                        val community = crossPost.community
                                        if (community != null) {
                                            val string = buildAnnotatedString {
                                                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                                    append(community.name)
                                                    append("@")
                                                    append(community.host)
                                                }
                                                if (index < uiState.post.crossPosts.lastIndex) {
                                                    append(", ")
                                                }
                                            }
                                            Text(
                                                modifier = Modifier.onClick(
                                                    onClick = rememberCallback {
                                                        val post = PostModel(
                                                            id = crossPost.id,
                                                            community = community,
                                                        )
                                                        navigationCoordinator.pushScreen(
                                                            PostDetailScreen(post)
                                                        )
                                                    },
                                                ),
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
                        items(
                            uiState.comments.filter { it.visible },
                            key = { c -> c.id }) { comment ->
                            Column {
                                AnimatedContent(
                                    targetState = comment.expanded,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(250))
                                            .togetherWith(fadeOut())
                                    },
                                ) {
                                    if (comment.expanded) {
                                        SwipeableCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = uiState.swipeActionsEnabled,
                                            directions = if (!uiState.isLogged || isOnOtherInstance) {
                                                emptySet()
                                            } else {
                                                setOf(
                                                    DismissDirection.StartToEnd,
                                                    DismissDirection.EndToStart,
                                                )
                                            },
                                            backgroundColor = rememberCallbackArgs {
                                                when (it) {
                                                    DismissValue.DismissedToStart -> upvoteColor
                                                        ?: defaultUpvoteColor

                                                    DismissValue.DismissedToEnd -> downvoteColor
                                                        ?: defaultDownVoteColor

                                                    DismissValue.Default -> Color.Transparent
                                                }
                                            },
                                            onGestureBegin = rememberCallback(model) {
                                                model.reduce(PostDetailMviModel.Intent.HapticIndication)
                                            },
                                            onDismissToStart = rememberCallback(model) {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.UpVoteComment(
                                                        comment.id
                                                    ),
                                                )
                                            },
                                            onDismissToEnd = rememberCallback(model) {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.DownVoteComment(
                                                        comment.id
                                                    ),
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
                                                            if (comment.id == commentIdToHighlight) {
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
                                                        },
                                                    comment = comment,
                                                    separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                                    autoLoadImages = uiState.autoLoadImages,
                                                    onToggleExpanded = rememberCallback(model) {
                                                        model.reduce(
                                                            PostDetailMviModel.Intent.ToggleExpandComment(
                                                                comment.id
                                                            )
                                                        )
                                                    },
                                                    onDoubleClick = if (!uiState.doubleTapActionEnabled) {
                                                        null
                                                    } else {
                                                        rememberCallback(model) {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.UpVoteComment(
                                                                    commentId = comment.id,
                                                                    feedback = true,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    onUpVote = rememberCallback(model) {
                                                        if (uiState.isLogged && !isOnOtherInstance) {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.UpVoteComment(
                                                                    commentId = comment.id,
                                                                    feedback = true,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    onDownVote = rememberCallback(model) {
                                                        if (uiState.isLogged && !isOnOtherInstance) {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.DownVoteComment(
                                                                    commentId = comment.id,
                                                                    feedback = true,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    onSave = rememberCallback(model) {
                                                        if (uiState.isLogged && !isOnOtherInstance) {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.SaveComment(
                                                                    commentId = comment.id,
                                                                    feedback = true,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    onReply = rememberCallback {
                                                        if (uiState.isLogged && !isOnOtherInstance) {
                                                            val screen = CreateCommentScreen(
                                                                originalPost = uiState.post,
                                                                originalComment = comment,
                                                            )
                                                            navigationCoordinator.showBottomSheet(
                                                                screen
                                                            )
                                                        }
                                                    },
                                                    onOpenCreator = rememberCallbackArgs {
                                                        val user = comment.creator
                                                        if (user != null) {
                                                            navigationCoordinator.pushScreen(
                                                                UserDetailScreen(
                                                                    user = user,
                                                                    otherInstance = otherInstanceName,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    onOpenCommunity = rememberCallbackArgs {
                                                        val community = comment.community
                                                        if (community != null) {
                                                            navigationCoordinator.pushScreen(
                                                                CommunityDetailScreen(
                                                                    community = community,
                                                                    otherInstance = otherInstanceName,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    options = buildList {
                                                        add(
                                                            Option(
                                                                OptionId.SeeRaw,
                                                                stringResource(MR.strings.post_action_see_raw)
                                                            )
                                                        )
                                                        add(
                                                            Option(
                                                                OptionId.Report,
                                                                stringResource(MR.strings.post_action_report)
                                                            )
                                                        )
                                                        if (comment.creator?.id == uiState.currentUserId) {
                                                            add(
                                                                Option(
                                                                    OptionId.Edit,
                                                                    stringResource(MR.strings.post_action_edit)
                                                                )
                                                            )
                                                            add(
                                                                Option(
                                                                    OptionId.Delete,
                                                                    stringResource(MR.strings.comment_action_delete)
                                                                )
                                                            )
                                                        }
                                                    },
                                                    onOptionSelected = rememberCallbackArgs(
                                                        model
                                                    ) { optionId ->
                                                        when (optionId) {
                                                            OptionId.Delete -> model.reduce(
                                                                PostDetailMviModel.Intent.DeleteComment(
                                                                    comment.id
                                                                )
                                                            )

                                                            OptionId.Edit -> {
                                                                navigationCoordinator.showBottomSheet(
                                                                    CreateCommentScreen(
                                                                        editedComment = comment,
                                                                    )
                                                                )
                                                            }

                                                            OptionId.Report -> {
                                                                navigationCoordinator.showBottomSheet(
                                                                    CreateReportScreen(
                                                                        commentId = comment.id
                                                                    )
                                                                )
                                                            }

                                                            OptionId.SeeRaw -> {
                                                                rawContent = comment
                                                            }

                                                            else -> Unit
                                                        }
                                                    },
                                                )
                                            },
                                        )
                                    } else {
                                        CollapsedCommentCard(
                                            comment = comment,
                                            onToggleExpanded = rememberCallback(model) {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.ToggleExpandComment(
                                                        comment.id
                                                    )
                                                )
                                            },
                                            onUpVote = rememberCallback(model) {
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.UpVoteComment(
                                                            commentId = comment.id,
                                                            feedback = true,
                                                        ),
                                                    )
                                                }
                                            },
                                            onDownVote = rememberCallback(model) {
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.DownVoteComment(
                                                            commentId = comment.id,
                                                            feedback = true,
                                                        ),
                                                    )
                                                }
                                            },
                                            onSave = rememberCallback(model) {
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.SaveComment(
                                                            commentId = comment.id,
                                                            feedback = true,
                                                        ),
                                                    )
                                                }
                                            },
                                            onReply = rememberCallback(model) {
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    val screen = CreateCommentScreen(
                                                        originalPost = uiState.post,
                                                        originalComment = comment,
                                                    )
                                                    navigationCoordinator.showBottomSheet(screen)
                                                }
                                            },
                                            onOpenCreator = rememberCallbackArgs {
                                                val user = comment.creator
                                                if (user != null) {
                                                    navigationCoordinator.pushScreen(
                                                        UserDetailScreen(
                                                            user = user,
                                                            otherInstance = otherInstanceName,
                                                        ),
                                                    )
                                                }
                                            },
                                            options = buildList {
                                                add(
                                                    Option(
                                                        OptionId.SeeRaw,
                                                        stringResource(MR.strings.post_action_see_raw)
                                                    )
                                                )
                                                add(
                                                    Option(
                                                        OptionId.Report,
                                                        stringResource(MR.strings.post_action_report)
                                                    )
                                                )
                                                if (comment.creator?.id == uiState.currentUserId) {
                                                    add(
                                                        Option(
                                                            OptionId.Edit,
                                                            stringResource(MR.strings.post_action_edit)
                                                        )
                                                    )
                                                    add(
                                                        Option(
                                                            OptionId.Delete,
                                                            stringResource(MR.strings.comment_action_delete)
                                                        )
                                                    )
                                                }
                                            },
                                            onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                                when (optionId) {
                                                    OptionId.Delete -> model.reduce(
                                                        PostDetailMviModel.Intent.DeleteComment(
                                                            comment.id
                                                        )
                                                    )

                                                    OptionId.Edit -> {
                                                        navigationCoordinator.showBottomSheet(
                                                            CreateCommentScreen(
                                                                editedComment = comment,
                                                            )
                                                        )
                                                    }

                                                    OptionId.Report -> {
                                                        navigationCoordinator.showBottomSheet(
                                                            CreateReportScreen(
                                                                commentId = comment.id
                                                            )
                                                        )
                                                    }

                                                    OptionId.SeeRaw -> {
                                                        rawContent = comment
                                                    }

                                                    else -> Unit
                                                }
                                            },
                                        )
                                    }
                                }

                                Divider(
                                    modifier = Modifier.padding(vertical = Spacing.xxxs),
                                    thickness = 0.25.dp
                                )

                                // load more button
                                if (comment.loadMoreButtonVisible) {
                                    Row {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Button(onClick = rememberCallback(model) {
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
                                Column {
                                    if (uiState.post.comments == 0) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(top = Spacing.xs),
                                            textAlign = TextAlign.Center,
                                            text = stringResource(MR.strings.message_empty_comments),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    } else {
                                        Text(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(top = Spacing.xs),
                                            textAlign = TextAlign.Center,
                                            text = stringResource(MR.strings.message_error_loading_comments),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                        Row {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Button(onClick = {
                                                model.reduce(PostDetailMviModel.Intent.Refresh)
                                            }) {
                                                Text(
                                                    text = stringResource(MR.strings.button_retry),
                                                )
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
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
