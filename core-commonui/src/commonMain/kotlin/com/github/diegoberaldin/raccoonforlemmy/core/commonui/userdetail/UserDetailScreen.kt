package com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat.InboxChatScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getUserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.report.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserDetailScreen(
    private val user: UserModel,
    private val otherInstance: String = "",
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel(user.id.toString()) {
            getUserDetailViewModel(user, otherInstance)
        }
        model.bindToLifecycle(key + user.id.toString())
        val uiState by model.uiState.collectAsState()
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)
        val successMessage = stringResource(MR.strings.message_operation_successful)
        val isOnOtherInstance = otherInstance.isNotEmpty()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val notificationCenter = remember { getNotificationCenter() }
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
        val themeRepository = remember { getThemeRepository() }
        val upvoteColor by themeRepository.upvoteColor.collectAsState()
        val downvoteColor by themeRepository.downvoteColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val drawerCoordinator = remember { getDrawerCoordinator() }
        var rawContent by remember { mutableStateOf<Any?>(null) }

        DisposableEffect(key) {
            drawerCoordinator.setGesturesEnabled(false)
            onDispose {
                notificationCenter.removeObserver(key)
                drawerCoordinator.setGesturesEnabled(true)
            }
        }
        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    is UserDetailMviModel.Effect.BlockError -> {
                        snackbarHostState.showSnackbar(it.message ?: genericError)
                    }

                    UserDetailMviModel.Effect.BlockSuccess -> {
                        snackbarHostState.showSnackbar(successMessage)
                    }

                    UserDetailMviModel.Effect.BackToTop -> {
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
                val userName = user.name
                val userHost = user.host
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = buildString {
                                append(userName)
                                if (userHost.isNotEmpty()) {
                                    append("@$userHost")
                                }
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        Image(
                            modifier = Modifier.onClick {
                                val sheet = SortBottomSheet(
                                    expandTop = true,
                                )
                                notificationCenter.addObserver({
                                    (it as? SortType)?.also { sortType ->
                                        model.reduce(UserDetailMviModel.Intent.ChangeSort(sortType))
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
                    FloatingActionButtonMenu(
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
                            if (!isOnOtherInstance) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.Chat,
                                    text = stringResource(MR.strings.action_chat),
                                    onSelected = {
                                        val screen = InboxChatScreen(otherUserId = user.id)
                                        navigator?.push(screen)
                                    },
                                )
                            }
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                model.reduce(UserDetailMviModel.Intent.Refresh)
            })
            Box(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(fabNestedScrollConnection).padding(padding)
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    state = lazyListState,
                ) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        ) {
                            UserHeader(
                                user = uiState.user,
                                autoLoadImages = uiState.autoLoadImages,
                                options = listOf(
                                    stringResource(MR.strings.community_detail_block),
                                    stringResource(MR.strings.community_detail_block_instance),
                                ),
                                onOpenImage = { url ->
                                    navigator?.push(ZoomableImageScreen(url))
                                },
                                onOptionSelected = { optionIdx ->
                                    when (optionIdx) {
                                        1 -> model.reduce(UserDetailMviModel.Intent.BlockInstance)
                                        else -> model.reduce(UserDetailMviModel.Intent.Block)
                                    }
                                },
                            )
                            SectionSelector(
                                titles = listOf(
                                    stringResource(MR.strings.profile_section_posts),
                                    stringResource(MR.strings.profile_section_comments),
                                ),
                                currentSection = when (uiState.section) {
                                    UserDetailSection.Comments -> 1
                                    else -> 0
                                },
                                onSectionSelected = {
                                    val section = when (it) {
                                        1 -> UserDetailSection.Comments
                                        else -> UserDetailSection.Posts
                                    }
                                    model.reduce(UserDetailMviModel.Intent.ChangeSection(section))
                                },
                            )
                            Spacer(modifier = Modifier.height(Spacing.m))
                        }
                    }
                    if (uiState.section == UserDetailSection.Posts) {
                        if (uiState.posts.isEmpty() && uiState.loading && uiState.initial) {
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
                                directions = if (isOnOtherInstance) {
                                    emptySet()
                                } else {
                                    setOf(
                                        DismissDirection.StartToEnd,
                                        DismissDirection.EndToStart,
                                    )
                                },
                                backgroundColor = {
                                    when (it) {
                                        DismissValue.DismissedToStart -> upvoteColor
                                            ?: defaultUpvoteColor

                                        DismissValue.DismissedToEnd -> downvoteColor
                                            ?: defaultDownVoteColor

                                        else -> Color.Transparent
                                    }
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
                                onGestureBegin = {
                                    model.reduce(UserDetailMviModel.Intent.HapticIndication)
                                },
                                onDismissToStart = {
                                    model.reduce(
                                        UserDetailMviModel.Intent.UpVotePost(idx),
                                    )
                                },
                                onDismissToEnd = {
                                    model.reduce(
                                        UserDetailMviModel.Intent.DownVotePost(idx),
                                    )
                                },
                                content = {
                                    PostCard(
                                        post = post,
                                        hideAuthor = true,
                                        postLayout = uiState.postLayout,
                                        fullHeightImage = uiState.fullHeightImages,
                                        blurNsfw = uiState.blurNsfw,
                                        separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                        autoLoadImages = uiState.autoLoadImages,
                                        onClick = {
                                            navigator?.push(PostDetailScreen(post = post))
                                        },
                                        onUpVote = if (isOnOtherInstance) {
                                            null
                                        } else {
                                            {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.UpVotePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            }
                                        },
                                        onDownVote = if (isOnOtherInstance) {
                                            null
                                        } else {
                                            {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.DownVotePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            }
                                        },
                                        onSave = if (isOnOtherInstance) {
                                            null
                                        } else {
                                            {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.SavePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            }
                                        },
                                        onOpenCommunity = { community ->
                                            navigator?.push(CommunityDetailScreen(community))
                                        },
                                        onReply = if (isOnOtherInstance) {
                                            null
                                        } else {
                                            {
                                                val screen = CreateCommentScreen(
                                                    originalPost = post,
                                                )
                                                notificationCenter.addObserver(
                                                    {
                                                        model.reduce(UserDetailMviModel.Intent.Refresh)
                                                    },
                                                    key,
                                                    NotificationCenterContractKeys.CommentCreated
                                                )
                                                bottomSheetNavigator.show(screen)
                                            }
                                        },
                                        onImageClick = { url ->
                                            navigator?.push(
                                                ZoomableImageScreen(url),
                                            )
                                        },
                                        options = buildList {
                                            add(stringResource(MR.strings.post_action_share))
                                            add(stringResource(MR.strings.post_action_see_raw))
                                            add(stringResource(MR.strings.post_action_report))
                                        },
                                        onOptionSelected = { optionIdx ->
                                            when (optionIdx) {
                                                2 -> {
                                                    bottomSheetNavigator.show(
                                                        CreateReportScreen(
                                                            postId = post.id
                                                        )
                                                    )
                                                }

                                                1 -> {
                                                    rawContent = post
                                                }

                                                else -> model.reduce(
                                                    UserDetailMviModel.Intent.SharePost(idx)
                                                )
                                            }
                                        })
                                },
                            )
                            if (uiState.postLayout != PostLayout.Card) {
                                Divider(modifier = Modifier.padding(vertical = Spacing.s))
                            } else {
                                Spacer(modifier = Modifier.height(Spacing.s))
                            }
                        }

                        if (uiState.posts.isEmpty() && !uiState.loading) {
                            item {
                                androidx.compose.material.Text(
                                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                    textAlign = TextAlign.Center,
                                    text = stringResource(MR.strings.message_empty_list),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    } else {
                        if (uiState.comments.isEmpty() && uiState.loading && uiState.initial) {
                            items(5) {
                                CommentCardPlaceholder()
                                Divider(
                                    modifier = Modifier.padding(vertical = Spacing.xxxs),
                                    thickness = 0.25.dp
                                )
                            }
                        }
                        itemsIndexed(uiState.comments) { idx, comment ->
                            SwipeableCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.swipeActionsEnabled,
                                directions = if (isOnOtherInstance) {
                                    emptySet()
                                } else {
                                    setOf(
                                        DismissDirection.StartToEnd,
                                        DismissDirection.EndToStart,
                                    )
                                },
                                backgroundColor = {
                                    when (it) {
                                        DismissValue.DismissedToStart -> upvoteColor
                                            ?: defaultUpvoteColor

                                        DismissValue.DismissedToEnd -> downvoteColor
                                            ?: defaultDownVoteColor

                                        else -> Color.Transparent
                                    }
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
                                onGestureBegin = {
                                    model.reduce(UserDetailMviModel.Intent.HapticIndication)
                                },
                                onDismissToStart = {
                                    model.reduce(
                                        UserDetailMviModel.Intent.UpVoteComment(idx),
                                    )
                                },
                                onDismissToEnd = {
                                    model.reduce(
                                        UserDetailMviModel.Intent.DownVoteComment(idx),
                                    )
                                },
                                content = {
                                    CommentCard(
                                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                        comment = comment,
                                        separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                        autoLoadImages = uiState.autoLoadImages,
                                        hideCommunity = false,
                                        hideAuthor = true,
                                        hideIndent = true,
                                        onClick = {
                                            navigator?.push(
                                                PostDetailScreen(
                                                    post = PostModel(id = comment.postId),
                                                    highlightCommentId = comment.id,
                                                )
                                            )
                                        },
                                        onSave = if (isOnOtherInstance) {
                                            null
                                        } else {
                                            {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.SaveComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            }
                                        },
                                        onUpVote = if (isOnOtherInstance) {
                                            null
                                        } else {
                                            {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.UpVoteComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            }
                                        },
                                        onDownVote = if (isOnOtherInstance) {
                                            null
                                        } else {
                                            {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.DownVoteComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            }
                                        },
                                        onReply = if (isOnOtherInstance) {
                                            null
                                        } else {
                                            {
                                                val screen = CreateCommentScreen(
                                                    originalPost = PostModel(id = comment.postId),
                                                    originalComment = comment,
                                                )
                                                notificationCenter.addObserver(
                                                    {
                                                        model.reduce(UserDetailMviModel.Intent.Refresh)
                                                    },
                                                    key,
                                                    NotificationCenterContractKeys.CommentCreated
                                                )
                                                bottomSheetNavigator.show(screen)
                                            }
                                        },
                                        onOpenCommunity = { community ->
                                            navigator?.push(CommunityDetailScreen(community))
                                        },
                                        options = buildList {
                                            add(stringResource(MR.strings.post_action_see_raw))
                                            add(stringResource(MR.strings.post_action_report))
                                        },
                                        onOptionSelected = { optionId ->
                                            when (optionId) {
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
                                    Divider(
                                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                                        thickness = 0.25.dp
                                    )
                                },
                            )
                        }

                        if (uiState.comments.isEmpty() && !uiState.loading) {
                            item {
                                androidx.compose.material.Text(
                                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                    textAlign = TextAlign.Center,
                                    text = stringResource(MR.strings.message_empty_list),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                    item {
                        if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                            model.reduce(UserDetailMviModel.Intent.LoadNextPage)
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

                if (uiState.asyncInProgress) {
                    ProgressHud()
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
