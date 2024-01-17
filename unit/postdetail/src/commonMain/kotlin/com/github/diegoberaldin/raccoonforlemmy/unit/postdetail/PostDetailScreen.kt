package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CollapsedCommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ShareBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.containsId
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class PostDetailScreen(
    private val postId: Int,
    private val otherInstance: String = "",
    private val highlightCommentId: Int? = null,
    private val isMod: Boolean = false,
) : Screen {
    override val key: ScreenKey
        get() = super.key + postId.toString()

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterialApi::class,
        ExperimentalLayoutApi::class,
    )
    @Composable
    override fun Content() {
        val model = getScreenModel<PostDetailMviModel>(
            tag = postId.toString() + highlightCommentId.toString(),
            parameters = {
                parametersOf(
                    postId,
                    otherInstance,
                    highlightCommentId,
                    isMod,
                )
            })
        model.bindToLifecycle(key + postId.toString())
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
        val themeRepository = remember { getThemeRepository() }
        val upVoteColor by themeRepository.upVoteColor.collectAsState()
        val downVoteColor by themeRepository.downVoteColor.collectAsState()
        val replyColor by themeRepository.replyColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultReplyColor = MaterialTheme.colorScheme.secondary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val detailOpener = remember { getDetailOpener() }

        LaunchedEffect(notificationCenter) {
            notificationCenter.resetCache()
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
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            contentWindowInsets = if (settings.edgeToEdge) {
                WindowInsets(0, 0, 0, 0)
            } else {
                WindowInsets.navigationBars
            },
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
                                        values = uiState.availableSortTypes.map { it.toInt() },
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
                        if (navigationCoordinator.canPop.value) {
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
                            if (uiState.isLogged && !isOnOtherInstance) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.Reply,
                                    text = stringResource(MR.strings.action_reply),
                                    onSelected = rememberCallback {
                                        detailOpener.openReply(
                                            originalPost = uiState.post,
                                        )
                                    },
                                )
                            }
                        },
                    )
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
                    modifier = Modifier.padding(padding).then(
                        if (settings.hideNavigationBarWhileScrolling) {
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            Modifier
                        }
                    ).nestedScroll(fabNestedScrollConnection).pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState
                    ) {
                        item {
                            PostCard(
                                post = uiState.post,
                                isFromModerator = uiState.post.creator?.id.let { creatorId ->
                                    uiState.isModerator && uiState.moderators.containsId(creatorId)
                                },
                                postLayout = if (uiState.postLayout == PostLayout.Card) {
                                    uiState.postLayout
                                } else {
                                    PostLayout.Full
                                },
                                fullHeightImage = uiState.fullHeightImages,
                                includeFullBody = true,
                                voteFormat = uiState.voteFormat,
                                autoLoadImages = uiState.autoLoadImages,
                                actionButtonsActive = uiState.isLogged,
                                blurNsfw = false,
                                onOpenCommunity = rememberCallbackArgs { community, _ ->
                                    detailOpener.openCommunityDetail(community, otherInstanceName)
                                },
                                onOpenCreator = rememberCallbackArgs { user, _ ->
                                    detailOpener.openUserDetail(user, otherInstanceName)
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
                                    if (uiState.isLogged && !isOnOtherInstance) {
                                        model.reduce(
                                            PostDetailMviModel.Intent.UpVotePost(),
                                        )
                                    }
                                },
                                onDownVote = rememberCallback(model) {
                                    if (uiState.isLogged && !isOnOtherInstance) {
                                        model.reduce(
                                            PostDetailMviModel.Intent.DownVotePost(),
                                        )
                                    }
                                },
                                onSave = rememberCallback(model) {
                                    model.reduce(
                                        PostDetailMviModel.Intent.SavePost(
                                            post = uiState.post,
                                        ),
                                    )
                                },
                                onReply = rememberCallback {
                                    if (uiState.isLogged && !isOnOtherInstance) {
                                        detailOpener.openReply(
                                            originalPost = uiState.post,
                                        )
                                    }
                                },
                                options = buildList {
                                    this += Option(
                                        OptionId.Share, stringResource(MR.strings.post_action_share)
                                    )
                                    this += Option(
                                        OptionId.SeeRaw,
                                        stringResource(MR.strings.post_action_see_raw)
                                    )
                                    if (uiState.isLogged && !isOnOtherInstance) {
                                        this += Option(
                                            OptionId.CrossPost,
                                            stringResource(MR.strings.post_action_cross_post)
                                        )
                                        this += Option(
                                            OptionId.Report,
                                            stringResource(MR.strings.post_action_report)
                                        )
                                    }
                                    if (uiState.post.creator?.id == uiState.currentUserId && !isOnOtherInstance) {
                                        this += Option(
                                            OptionId.Edit,
                                            stringResource(MR.strings.post_action_edit)
                                        )
                                        this += Option(
                                            OptionId.Delete,
                                            stringResource(MR.strings.comment_action_delete)
                                        )
                                    }
                                    if (uiState.isModerator) {
                                        this += Option(
                                            OptionId.FeaturePost,
                                            if (uiState.post.featuredCommunity) {
                                                stringResource(MR.strings.mod_action_unmark_as_featured)
                                            } else {
                                                stringResource(MR.strings.mod_action_mark_as_featured)
                                            }
                                        )
                                        this += Option(
                                            OptionId.LockPost, if (uiState.post.locked) {
                                                stringResource(MR.strings.mod_action_unlock)
                                            } else {
                                                stringResource(MR.strings.mod_action_lock)
                                            }
                                        )
                                        this += Option(
                                            OptionId.Remove,
                                            stringResource(MR.strings.mod_action_remove)
                                        )
                                        this += Option(
                                            OptionId.BanUser,
                                            if (uiState.post.creator?.banned == true) {
                                                stringResource(MR.strings.mod_action_allow)
                                            } else {
                                                stringResource(MR.strings.mod_action_ban)
                                            },
                                        )
                                        uiState.post.creator?.id?.also { creatorId ->
                                            if (uiState.currentUserId != creatorId) {
                                                this += Option(
                                                    OptionId.AddMod,
                                                    if (uiState.moderators.containsId(creatorId)) {
                                                        stringResource(MR.strings.mod_action_remove_mod)
                                                    } else {
                                                        stringResource(MR.strings.mod_action_add_mod)
                                                    },
                                                )
                                            }
                                        }
                                    }
                                },
                                onOptionSelected = rememberCallbackArgs(model) { idx ->
                                    when (idx) {
                                        OptionId.Delete -> model.reduce(PostDetailMviModel.Intent.DeletePost)

                                        OptionId.Edit -> {
                                            detailOpener.openCreatePost(editedPost = uiState.post)
                                        }

                                        OptionId.Report -> {
                                            navigationCoordinator.showBottomSheet(
                                                CreateReportScreen(postId = uiState.post.id),
                                            )
                                        }

                                        OptionId.CrossPost -> {
                                            detailOpener.openCreatePost(crossPost = uiState.post)
                                        }

                                        OptionId.SeeRaw -> {
                                            rawContent = uiState.post
                                        }

                                        OptionId.Share -> {
                                            val urls = listOfNotNull(
                                                uiState.post.originalUrl,
                                                "https://${uiState.instance}/post/${uiState.post.id}"
                                            ).distinct()
                                            if (urls.size == 1) {
                                                model.reduce(PostDetailMviModel.Intent.Share(urls.first()))
                                            } else {
                                                val screen = ShareBottomSheet(urls = urls)
                                                navigationCoordinator.showBottomSheet(screen)
                                            }
                                        }

                                        OptionId.FeaturePost -> model.reduce(
                                            PostDetailMviModel.Intent.ModFeaturePost,
                                        )

                                        OptionId.LockPost -> model.reduce(
                                            PostDetailMviModel.Intent.ModLockPost,
                                        )

                                        OptionId.Remove -> {
                                            val screen = RemoveScreen(postId = uiState.post.id)
                                            navigationCoordinator.showBottomSheet(screen)
                                        }

                                        OptionId.BanUser -> {
                                            uiState.post.creator?.id?.also { userId ->
                                                val screen = BanUserScreen(
                                                    userId = userId,
                                                    communityId = uiState.post.community?.id ?: 0,
                                                    newValue = uiState.post.creator?.banned != true,
                                                    postId = uiState.post.id,
                                                )
                                                navigationCoordinator.showBottomSheet(screen)
                                            }
                                        }

                                        OptionId.AddMod -> {
                                            uiState.post.creator?.id?.also { userId ->
                                                model.reduce(
                                                    PostDetailMviModel.Intent.ModToggleModUser(
                                                        userId
                                                    )
                                                )
                                            }
                                        }

                                        else -> Unit
                                    }
                                },
                                onOpenImage = rememberCallbackArgs { url ->
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
                                                        detailOpener.openPostDetail(
                                                            post = post,
                                                            otherInstance = otherInstanceName
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
                            items = uiState.comments.filter { it.visible },
                            key = { c -> c.id.toString() + (c.updateDate ?: c.publishDate) },
                        ) { comment ->
                            Column {
                                AnimatedContent(
                                    targetState = comment.expanded,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(250)).togetherWith(fadeOut())
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
                                            enableSecondAction = rememberCallbackArgs { value ->
                                                if (!uiState.isLogged) {
                                                    false
                                                } else {
                                                    value == DismissValue.DismissedToStart
                                                }
                                            },
                                            backgroundColor = rememberCallbackArgs { direction ->
                                                when (direction) {
                                                    DismissValue.DismissedToStart -> upVoteColor
                                                        ?: defaultUpvoteColor

                                                    DismissValue.DismissedToEnd -> downVoteColor
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
                                                model.reduce(PostDetailMviModel.Intent.HapticIndication)
                                            },
                                            onDismissToStart = rememberCallback(model) {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.UpVoteComment(
                                                        comment.id
                                                    ),
                                                )
                                            },
                                            onSecondDismissToStart = rememberCallback(model) {
                                                detailOpener.openReply(
                                                    originalPost = uiState.post,
                                                    originalComment = comment,
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
                                                CommentCard(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.background)
                                                        .then(
                                                            if (comment.id == commentIdToHighlight) {
                                                                Modifier.background(
                                                                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                                        5.dp
                                                                    ).copy(alpha = 0.75f)
                                                                )
                                                            } else {
                                                                Modifier
                                                            }
                                                        ),
                                                    comment = comment,
                                                    isOp = comment.creator?.id == uiState.post.creator?.id,
                                                    voteFormat = uiState.voteFormat,
                                                    autoLoadImages = uiState.autoLoadImages,
                                                    actionButtonsActive = uiState.isLogged,
                                                    onToggleExpanded = rememberCallback(model) {
                                                        model.reduce(
                                                            PostDetailMviModel.Intent.ToggleExpandComment(
                                                                comment.id
                                                            )
                                                        )
                                                    },
                                                    onClick = rememberCallback(model) {
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
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    onDownVote = rememberCallback(model) {
                                                        if (uiState.isLogged && !isOnOtherInstance) {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.DownVoteComment(
                                                                    commentId = comment.id,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    onSave = rememberCallback(model) {
                                                        if (uiState.isLogged && !isOnOtherInstance) {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.SaveComment(
                                                                    commentId = comment.id,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                    onReply = rememberCallback {
                                                        if (uiState.isLogged && !isOnOtherInstance) {
                                                            detailOpener.openReply(
                                                                originalPost = uiState.post,
                                                                originalComment = comment,
                                                            )
                                                        }
                                                    },
                                                    onOpenCreator = rememberCallbackArgs { user, instance ->
                                                        detailOpener.openUserDetail(user, instance)
                                                    },
                                                    onOpenCommunity = rememberCallbackArgs { community, instance ->
                                                        detailOpener.openCommunityDetail(
                                                            community, instance
                                                        )
                                                    },
                                                    onOpenPost = rememberCallbackArgs { p, instance ->
                                                        detailOpener.openPostDetail(p, instance)
                                                    },
                                                    onOpenWeb = rememberCallbackArgs { url ->
                                                        navigationCoordinator.pushScreen(
                                                            WebViewScreen(url)
                                                        )
                                                    },
                                                    onImageClick = rememberCallbackArgs { url ->
                                                        navigationCoordinator.pushScreen(
                                                            ZoomableImageScreen(url)
                                                        )
                                                    },
                                                    options = buildList {
                                                        this += Option(
                                                            OptionId.SeeRaw,
                                                            stringResource(MR.strings.post_action_see_raw),
                                                        )
                                                        this += Option(
                                                            OptionId.Report,
                                                            stringResource(MR.strings.post_action_report),
                                                        )
                                                        if (comment.creator?.id == uiState.currentUserId) {
                                                            this += Option(
                                                                OptionId.Edit,
                                                                stringResource(MR.strings.post_action_edit),
                                                            )
                                                            this += Option(
                                                                OptionId.Delete,
                                                                stringResource(MR.strings.comment_action_delete),
                                                            )
                                                        }
                                                        if (uiState.isModerator) {
                                                            this += Option(
                                                                OptionId.DistinguishComment,
                                                                if (comment.distinguished) {
                                                                    stringResource(MR.strings.mod_action_unmark_as_distinguished)
                                                                } else {
                                                                    stringResource(MR.strings.mod_action_mark_as_distinguished)
                                                                },
                                                            )
                                                            this += Option(
                                                                OptionId.Remove,
                                                                stringResource(MR.strings.mod_action_remove),
                                                            )
                                                            this += Option(
                                                                OptionId.BanUser,
                                                                if (comment.creator?.banned == true) {
                                                                    stringResource(MR.strings.mod_action_allow)
                                                                } else {
                                                                    stringResource(MR.strings.mod_action_ban)
                                                                },
                                                            )
                                                            comment.creator?.id?.also { creatorId ->
                                                                if (uiState.currentUserId != creatorId) {
                                                                    this += Option(
                                                                        OptionId.AddMod,
                                                                        if (uiState.moderators.containsId(
                                                                                creatorId
                                                                            )
                                                                        ) {
                                                                            stringResource(MR.strings.mod_action_remove_mod)
                                                                        } else {
                                                                            stringResource(MR.strings.mod_action_add_mod)
                                                                        },
                                                                    )
                                                                }
                                                            }
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
                                                                detailOpener.openReply(
                                                                    editedComment = comment,
                                                                )
                                                            }

                                                            OptionId.Report -> {
                                                                navigationCoordinator.showBottomSheet(
                                                                    CreateReportScreen(
                                                                        commentId = comment.id,
                                                                    )
                                                                )
                                                            }

                                                            OptionId.SeeRaw -> {
                                                                rawContent = comment
                                                            }

                                                            OptionId.DistinguishComment -> model.reduce(
                                                                PostDetailMviModel.Intent.ModDistinguishComment(
                                                                    comment.id,
                                                                )
                                                            )

                                                            OptionId.Remove -> {
                                                                val screen =
                                                                    RemoveScreen(commentId = comment.id)
                                                                navigationCoordinator.showBottomSheet(
                                                                    screen,
                                                                )
                                                            }

                                                            OptionId.BanUser -> {
                                                                comment.creator?.id?.also { userId ->
                                                                    val screen = BanUserScreen(
                                                                        userId = userId,
                                                                        communityId = uiState.post.community?.id
                                                                            ?: 0,
                                                                        newValue = comment.creator?.banned != true,
                                                                        commentId = comment.id,
                                                                    )
                                                                    navigationCoordinator.showBottomSheet(
                                                                        screen,
                                                                    )
                                                                }
                                                            }

                                                            OptionId.AddMod -> {
                                                                comment.creator?.id?.also { userId ->
                                                                    model.reduce(
                                                                        PostDetailMviModel.Intent.ModToggleModUser(
                                                                            userId,
                                                                        )
                                                                    )
                                                                }
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
                                            isOp = comment.creator?.id == uiState.post.creator?.id,
                                            voteFormat = uiState.voteFormat,
                                            autoLoadImages = uiState.autoLoadImages,
                                            actionButtonsActive = uiState.isLogged,
                                            onToggleExpanded = rememberCallback(model) {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.ToggleExpandComment(
                                                        comment.id,
                                                    )

                                                )
                                            },
                                            onClick = rememberCallback(model) {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.ToggleExpandComment(
                                                        comment.id,
                                                    )
                                                )
                                            },
                                            onUpVote = rememberCallback(model) {
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.UpVoteComment(
                                                            commentId = comment.id,
                                                        ),
                                                    )
                                                }
                                            },
                                            onDownVote = rememberCallback(model) {
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.DownVoteComment(
                                                            commentId = comment.id,
                                                        ),
                                                    )
                                                }
                                            },
                                            onSave = rememberCallback(model) {
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.SaveComment(
                                                            commentId = comment.id,
                                                        ),
                                                    )
                                                }
                                            },
                                            onReply = rememberCallback(model) {
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    detailOpener.openReply(
                                                        originalPost = uiState.post,
                                                        originalComment = comment,
                                                    )
                                                }
                                            },
                                            onOpenCreator = rememberCallbackArgs { user ->
                                                detailOpener.openUserDetail(user, otherInstanceName)
                                            },
                                            options = buildList {
                                                this += Option(
                                                    OptionId.SeeRaw,
                                                    stringResource(MR.strings.post_action_see_raw),
                                                )
                                                this += Option(
                                                    OptionId.Report,
                                                    stringResource(MR.strings.post_action_report),
                                                )
                                                if (comment.creator?.id == uiState.currentUserId) {
                                                    this += Option(
                                                        OptionId.Edit,
                                                        stringResource(MR.strings.post_action_edit),
                                                    )
                                                    this += Option(
                                                        OptionId.Delete,
                                                        stringResource(MR.strings.comment_action_delete),
                                                    )
                                                }
                                                if (uiState.isModerator) {
                                                    this += Option(
                                                        OptionId.DistinguishComment,
                                                        if (comment.distinguished) {
                                                            stringResource(MR.strings.mod_action_unmark_as_distinguished)
                                                        } else {
                                                            stringResource(MR.strings.mod_action_mark_as_distinguished)
                                                        },
                                                    )
                                                    this += Option(
                                                        OptionId.Remove,
                                                        stringResource(MR.strings.mod_action_remove),
                                                    )
                                                    this += Option(
                                                        OptionId.BanUser,
                                                        if (comment.creator?.banned == true) {
                                                            stringResource(MR.strings.mod_action_allow)
                                                        } else {
                                                            stringResource(MR.strings.mod_action_ban)
                                                        },
                                                    )
                                                    comment.creator?.id?.also { creatorId ->
                                                        if (uiState.currentUserId != creatorId) {
                                                            this += Option(
                                                                OptionId.AddMod,
                                                                if (uiState.moderators.containsId(
                                                                        creatorId,
                                                                    )
                                                                ) {
                                                                    stringResource(MR.strings.mod_action_remove_mod)
                                                                } else {
                                                                    stringResource(MR.strings.mod_action_add_mod)
                                                                },
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                                when (optionId) {
                                                    OptionId.Delete -> model.reduce(
                                                        PostDetailMviModel.Intent.DeleteComment(
                                                            comment.id,
                                                        )
                                                    )

                                                    OptionId.Edit -> {
                                                        detailOpener.openReply(
                                                            editedComment = comment,
                                                        )
                                                    }

                                                    OptionId.Report -> {
                                                        navigationCoordinator.showBottomSheet(
                                                            CreateReportScreen(
                                                                commentId = comment.id,
                                                            )
                                                        )
                                                    }

                                                    OptionId.SeeRaw -> {
                                                        rawContent = comment
                                                    }

                                                    OptionId.DistinguishComment -> model.reduce(
                                                        PostDetailMviModel.Intent.ModDistinguishComment(
                                                            comment.id,
                                                        )
                                                    )

                                                    OptionId.Remove -> {
                                                        val screen =
                                                            RemoveScreen(commentId = comment.id)
                                                        navigationCoordinator.showBottomSheet(
                                                            screen,
                                                        )
                                                    }

                                                    OptionId.BanUser -> {
                                                        comment.creator?.id?.also { userId ->
                                                            val screen = BanUserScreen(
                                                                userId = userId,
                                                                communityId = uiState.post.community?.id
                                                                    ?: 0,
                                                                newValue = comment.creator?.banned != true,
                                                                commentId = comment.id,
                                                            )
                                                            navigationCoordinator.showBottomSheet(
                                                                screen,
                                                            )
                                                        }
                                                    }

                                                    OptionId.AddMod -> {
                                                        comment.creator?.id?.also { userId ->
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.ModToggleModUser(
                                                                    userId,
                                                                )
                                                            )
                                                        }
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
                                if (comment.loadMoreButtonVisible && comment.expanded) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Button(
                                            onClick = rememberCallback(model) {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.FetchMoreComments(
                                                        parentId = comment.id,
                                                    )
                                                )
                                            },
                                        ) {
                                            Text(
                                                text = buildString {
                                                    append(stringResource(MR.strings.post_detail_load_more_comments))
                                                    comment.comments?.takeIf { it > 0 }
                                                        ?.also { count ->
                                                            append(" (")
                                                            append(count)
                                                            append(")")
                                                        }
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                        }
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
                                            Button(
                                                onClick = rememberCallback(model) {
                                                    model.reduce(PostDetailMviModel.Intent.Refresh)
                                                },
                                            ) {
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
                        },
                    )
                }

                is CommentModel -> {
                    RawContentDialog(
                        text = content.text,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        onDismiss = rememberCallback {
                            rawContent = null
                        },
                        onQuote = rememberCallbackArgs { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                detailOpener.openReply(
                                    originalPost = uiState.post,
                                    originalComment = content,
                                    initialText = buildString {
                                        append("> ")
                                        append(quotation)
                                        append("\n\n")
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}