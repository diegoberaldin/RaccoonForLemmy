package com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeActionCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.LikedTypeSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents.components.ModdedCommentCard
import com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents.components.ModdedCommentPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class FilteredContentsScreen(
    private val type: Int,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<FilteredContentsMviModel>(parameters = { parametersOf(type) })
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val lazyListState = rememberLazyListState()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.refreshing,
            onRefresh = rememberCallback(model) {
                model.reduce(FilteredContentsMviModel.Intent.Refresh)
            },
        )
        val detailOpener = remember { getDetailOpener() }
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val themeRepository = remember { getThemeRepository() }
        val upVoteColor by themeRepository.upVoteColor.collectAsState()
        val downVoteColor by themeRepository.downVoteColor.collectAsState()
        val replyColor by themeRepository.replyColor.collectAsState()
        val saveColor by themeRepository.saveColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultReplyColor = MaterialTheme.colorScheme.secondary
        val defaultSaveColor = MaterialTheme.colorScheme.secondaryContainer
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val scope = rememberCoroutineScope()

        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    FilteredContentsMviModel.Effect.BackToTop -> {
                        lazyListState.scrollToItem(0)
                        topAppBarState.heightOffset = 0f
                        topAppBarState.contentOffset = 0f
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xxs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    navigationCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Column(modifier = Modifier.padding(horizontal = Spacing.s)) {
                            Text(
                                text = when (uiState.contentsType) {
                                    FilteredContentsType.Moderated -> LocalXmlStrings.current.moderatorZoneActionContents
                                    FilteredContentsType.Votes -> LocalXmlStrings.current.profileUpvotesDownvotes
                                },
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (uiState.contentsType == FilteredContentsType.Votes) {
                                val text = when (uiState.liked) {
                                    true -> LocalXmlStrings.current.actionUpvote
                                    else -> LocalXmlStrings.current.actionDownvote
                                }
                                Text(
                                    modifier = Modifier.onClick(
                                        onClick = rememberCallback {
                                            navigationCoordinator.showBottomSheet(LikedTypeSheet())
                                        },
                                    ),
                                    text = text,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
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
                                text = LocalXmlStrings.current.actionBackToTop,
                                onSelected = rememberCallback {
                                    scope.launch {
                                        lazyListState.scrollToItem(0)
                                        topAppBarState.heightOffset = 0f
                                        topAppBarState.contentOffset = 0f
                                    }
                                },
                            )
                        },
                    )
                }
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues).then(
                    if (settings.hideNavigationBarWhileScrolling) {
                        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        Modifier
                    }
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                SectionSelector(
                    modifier = Modifier.padding(vertical = Spacing.xs),
                    titles = listOf(
                        LocalXmlStrings.current.profileSectionPosts,
                        LocalXmlStrings.current.profileSectionComments,
                    ),
                    currentSection = when (uiState.section) {
                        FilteredContentsSection.Comments -> 1
                        else -> 0
                    },
                    onSectionSelected = {
                        val section = when (it) {
                            1 -> FilteredContentsSection.Comments
                            else -> FilteredContentsSection.Posts
                        }
                        model.reduce(FilteredContentsMviModel.Intent.ChangeSection(section))
                    },
                )

                Box(
                    modifier = Modifier
                        .then(
                            if (settings.hideNavigationBarWhileScrolling) {
                                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                Modifier
                            }
                        )
                        .nestedScroll(fabNestedScrollConnection)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.interItem)
                    ) {
                        if (uiState.section == FilteredContentsSection.Posts) {
                            if (uiState.posts.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    PostCardPlaceholder(uiState.postLayout)
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
                                    }
                                }
                            }
                            if (uiState.posts.isEmpty() && !uiState.initial && !uiState.loading) {
                                item {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        text = LocalXmlStrings.current.messageEmptyList,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                            items(
                                items = uiState.posts,
                                key = {
                                    it.id.toString() + (it.updateDate ?: it.publishDate)
                                },
                            ) { post ->

                                @Composable
                                fun List<ActionOnSwipe>.toSwipeActions(): List<SwipeAction> =
                                    mapNotNull {
                                        when (it) {
                                            ActionOnSwipe.UpVote -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowCircleUp,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                },
                                                backgroundColor = upVoteColor ?: defaultUpvoteColor,
                                                onTriggered = rememberCallback {
                                                    model.reduce(
                                                        FilteredContentsMviModel.Intent.UpVotePost(post.id)
                                                    )
                                                },
                                            )

                                            ActionOnSwipe.DownVote -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowCircleDown,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                },
                                                backgroundColor = downVoteColor ?: defaultDownVoteColor,
                                                onTriggered = rememberCallback {
                                                    model.reduce(
                                                        FilteredContentsMviModel.Intent.DownVotePost(post.id)
                                                    )
                                                },
                                            )

                                            ActionOnSwipe.Reply -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Reply,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                },
                                                backgroundColor = replyColor ?: defaultReplyColor,
                                                onTriggered = rememberCallback {
                                                    detailOpener.openReply(originalPost = post)
                                                },
                                            )

                                            ActionOnSwipe.Save -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.Default.Bookmark,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                },
                                                backgroundColor = saveColor ?: defaultSaveColor,
                                                onTriggered = rememberCallback {
                                                    model.reduce(
                                                        FilteredContentsMviModel.Intent.SavePost(post.id)
                                                    )
                                                },
                                            )


                                            else -> null
                                        }
                                    }
                                SwipeActionCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = uiState.swipeActionsEnabled,
                                    onGestureBegin = rememberCallback(model) {
                                        model.reduce(FilteredContentsMviModel.Intent.HapticIndication)
                                    },
                                    swipeToStartActions = uiState.actionsOnSwipeToStartPosts.toSwipeActions(),
                                    swipeToEndActions = uiState.actionsOnSwipeToEndPosts.toSwipeActions(),
                                    content = {
                                        PostCard(
                                            post = post,
                                            postLayout = uiState.postLayout,
                                            limitBodyHeight = true,
                                            fullHeightImage = uiState.fullHeightImages,
                                            voteFormat = uiState.voteFormat,
                                            autoLoadImages = uiState.autoLoadImages,
                                            preferNicknames = uiState.preferNicknames,
                                            fadeRead = uiState.fadeReadPosts,
                                            showUnreadComments = uiState.showUnreadComments,
                                            onClick = rememberCallback(model) {
                                                detailOpener.openPostDetail(post)
                                            },
                                            onOpenCommunity = rememberCallbackArgs { community, instance ->
                                                detailOpener.openCommunityDetail(
                                                    community,
                                                    instance,
                                                )
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
                                                model.reduce(
                                                    FilteredContentsMviModel.Intent.UpVotePost(post.id),
                                                )
                                            },
                                            onDownVote = rememberCallback(model) {
                                                model.reduce(
                                                    FilteredContentsMviModel.Intent.DownVotePost(post.id),
                                                )
                                            },
                                            onSave = rememberCallback(model) {
                                                model.reduce(
                                                    FilteredContentsMviModel.Intent.SavePost(post.id),
                                                )
                                            },
                                            onReply = rememberCallback(model) {
                                                detailOpener.openPostDetail(post)
                                            },
                                            onOpenImage = rememberCallbackArgs(model, post) { url ->
                                                navigationCoordinator.pushScreen(
                                                    ZoomableImageScreen(
                                                        url = url,
                                                        source = post.community?.readableHandle.orEmpty(),
                                                    )
                                                )
                                            },
                                            options = buildList {
                                                if (uiState.contentsType == FilteredContentsType.Moderated) {
                                                    this += Option(
                                                        OptionId.Remove,
                                                        LocalXmlStrings.current.modActionRemove,
                                                    )
                                                }
                                                this += Option(
                                                    OptionId.SeeRaw,
                                                    LocalXmlStrings.current.postActionSeeRaw,
                                                )
                                                if (uiState.contentsType == FilteredContentsType.Moderated) {
                                                    this += Option(
                                                        OptionId.FeaturePost,
                                                        if (post.featuredCommunity) {
                                                            LocalXmlStrings.current.modActionUnmarkAsFeatured
                                                        } else {
                                                            LocalXmlStrings.current.modActionMarkAsFeatured
                                                        },
                                                    )
                                                    this += Option(
                                                        OptionId.LockPost,
                                                        if (post.locked) {
                                                            LocalXmlStrings.current.modActionUnlock
                                                        } else {
                                                            LocalXmlStrings.current.modActionLock
                                                        },
                                                    )
                                                    this += Option(
                                                        OptionId.BanUser,
                                                        if (post.creator?.banned == true) {
                                                            LocalXmlStrings.current.modActionAllow
                                                        } else {
                                                            LocalXmlStrings.current.modActionBan
                                                        },
                                                    )
                                                }
                                            },
                                            onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                                when (optionId) {
                                                    OptionId.SeeRaw -> {
                                                        rawContent = post
                                                    }

                                                    OptionId.FeaturePost -> model.reduce(
                                                        FilteredContentsMviModel.Intent.ModFeaturePost(post.id)
                                                    )

                                                    OptionId.LockPost -> model.reduce(
                                                        FilteredContentsMviModel.Intent.ModLockPost(post.id)
                                                    )

                                                    OptionId.Remove -> {
                                                        val screen = RemoveScreen(postId = post.id)
                                                        navigationCoordinator.pushScreen(screen)
                                                    }

                                                    OptionId.BanUser -> {
                                                        post.creator?.id?.also { userId ->
                                                            post.community?.id?.also { communityId ->
                                                                val screen = BanUserScreen(
                                                                    userId = userId,
                                                                    communityId = communityId,
                                                                    newValue = post.creator?.banned != true,
                                                                    postId = post.id,
                                                                )
                                                                navigationCoordinator.pushScreen(screen)
                                                            }
                                                        }
                                                    }

                                                    else -> Unit
                                                }
                                            }
                                        )
                                    },
                                )
                            }
                        } else {
                            if (uiState.comments.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    ModdedCommentPlaceholder(postLayout = uiState.postLayout)
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
                                    }
                                }
                            }
                            if (uiState.comments.isEmpty() && !uiState.initial && !uiState.loading) {
                                item {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        text = LocalXmlStrings.current.messageEmptyList,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                            items(
                                uiState.comments,
                                { it.id.toString() + (it.updateDate ?: it.publishDate) },
                            ) { comment ->

                                @Composable
                                fun List<ActionOnSwipe>.toSwipeActions(): List<SwipeAction> =
                                    mapNotNull {
                                        when (it) {
                                            ActionOnSwipe.UpVote -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowCircleUp,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                },
                                                backgroundColor = upVoteColor ?: defaultUpvoteColor,
                                                onTriggered = rememberCallback {
                                                    model.reduce(
                                                        FilteredContentsMviModel.Intent.UpVoteComment(comment.id)
                                                    )
                                                },
                                            )

                                            ActionOnSwipe.DownVote -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowCircleDown,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                },
                                                backgroundColor = downVoteColor ?: defaultDownVoteColor,
                                                onTriggered = rememberCallback {
                                                    model.reduce(
                                                        FilteredContentsMviModel.Intent.DownVoteComment(comment.id),
                                                    )
                                                },
                                            )

                                            ActionOnSwipe.Reply -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Reply,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                },
                                                backgroundColor = replyColor ?: defaultReplyColor,
                                                onTriggered = rememberCallback {
                                                    detailOpener.openReply(originalComment = comment)
                                                },
                                            )

                                            ActionOnSwipe.Save -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.Default.Bookmark,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                    )
                                                },
                                                backgroundColor = saveColor ?: defaultSaveColor,
                                                onTriggered = rememberCallback {
                                                    model.reduce(
                                                        FilteredContentsMviModel.Intent.SaveComment(comment.id),
                                                    )
                                                },
                                            )

                                            else -> null
                                        }
                                    }

                                SwipeActionCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = uiState.swipeActionsEnabled,
                                    onGestureBegin = rememberCallback(model) {
                                        model.reduce(FilteredContentsMviModel.Intent.HapticIndication)
                                    },
                                    swipeToStartActions = uiState.actionsOnSwipeToStartComments.toSwipeActions(),
                                    swipeToEndActions = uiState.actionsOnSwipeToEndComments.toSwipeActions(),
                                    content = {
                                        ModdedCommentCard(
                                            comment = comment,
                                            postLayout = uiState.postLayout,
                                            voteFormat = uiState.voteFormat,
                                            autoLoadImages = uiState.autoLoadImages,
                                            preferNicknames = uiState.preferNicknames,
                                            onOpenUser = rememberCallbackArgs { user, instance ->
                                                detailOpener.openUserDetail(user, instance)
                                            },
                                            onOpen = rememberCallback {
                                                detailOpener.openPostDetail(
                                                    post = PostModel(id = comment.postId),
                                                    highlightCommentId = comment.id,
                                                    isMod = true,
                                                )
                                            },
                                            onUpVote = rememberCallback(model) {
                                                model.reduce(
                                                    FilteredContentsMviModel.Intent.UpVoteComment(comment.id)
                                                )
                                            },
                                            onDownVote = rememberCallback(model) {
                                                model.reduce(
                                                    FilteredContentsMviModel.Intent.DownVoteComment(comment.id)
                                                )
                                            },
                                            onSave = rememberCallback(model) {
                                                model.reduce(
                                                    FilteredContentsMviModel.Intent.SaveComment(comment.id)
                                                )
                                            },
                                            onReply = rememberCallback {
                                                detailOpener.openReply(originalComment = comment)
                                            },
                                            options = buildList {
                                                if (uiState.contentsType == FilteredContentsType.Moderated) {
                                                    this += Option(
                                                        OptionId.Remove,
                                                        LocalXmlStrings.current.modActionRemove,
                                                    )
                                                }
                                                this += Option(
                                                    OptionId.SeeRaw,
                                                    LocalXmlStrings.current.postActionSeeRaw,
                                                )
                                                if (uiState.contentsType == FilteredContentsType.Moderated) {
                                                    this += Option(
                                                        OptionId.DistinguishComment,
                                                        if (comment.distinguished) {
                                                            LocalXmlStrings.current.modActionUnmarkAsDistinguished
                                                        } else {
                                                            LocalXmlStrings.current.modActionMarkAsDistinguished
                                                        },
                                                    )
                                                    this += Option(
                                                        OptionId.BanUser,
                                                        if (comment.creator?.banned == true) {
                                                            LocalXmlStrings.current.modActionAllow
                                                        } else {
                                                            LocalXmlStrings.current.modActionBan
                                                        },
                                                    )
                                                }
                                            },
                                            onOptionSelected = rememberCallbackArgs { optionId ->
                                                when (optionId) {
                                                    OptionId.Remove -> {
                                                        val screen =
                                                            RemoveScreen(commentId = comment.id)
                                                        navigationCoordinator.pushScreen(screen)
                                                    }

                                                    OptionId.SeeRaw -> {
                                                        rawContent = comment
                                                    }

                                                    OptionId.DistinguishComment -> model.reduce(
                                                        FilteredContentsMviModel.Intent.ModDistinguishComment(comment.id)
                                                    )

                                                    OptionId.BanUser -> {
                                                        comment.creator?.id?.also { userId ->
                                                            comment.community?.id?.also { communityId ->
                                                                val screen = BanUserScreen(
                                                                    userId = userId,
                                                                    communityId = communityId,
                                                                    newValue = comment.creator?.banned != true,
                                                                    commentId = comment.id,
                                                                )
                                                                navigationCoordinator.pushScreen(screen)
                                                            }
                                                        }
                                                    }

                                                    else -> Unit
                                                }
                                            }
                                        )
                                    },
                                )
                            }
                        }

                        item {
                            if (!uiState.initial && !uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(FilteredContentsMviModel.Intent.LoadNextPage)
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
                        backgroundColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            if (rawContent != null) {
                when (val content = rawContent) {
                    is PostModel -> {
                        RawContentDialog(
                            title = content.title,
                            publishDate = content.publishDate,
                            updateDate = content.updateDate,
                            url = content.originalUrl,
                            text = content.text,
                            upVotes = content.upvotes,
                            downVotes = content.downvotes,
                            onDismiss = {
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
                            publishDate = content.publishDate,
                            updateDate = content.updateDate,
                            text = content.text,
                            upVotes = content.upvotes,
                            downVotes = content.downvotes,
                            onDismiss = {
                                rawContent = null
                            },
                            onQuote = rememberCallbackArgs { quotation ->
                                rawContent = null
                                if (quotation != null) {
                                    detailOpener.openReply(
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
}
