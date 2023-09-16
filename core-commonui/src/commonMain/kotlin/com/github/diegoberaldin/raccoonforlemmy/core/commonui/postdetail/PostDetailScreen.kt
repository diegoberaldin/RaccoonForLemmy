package com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.CornerSize
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardBody
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardFooter
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardImage
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardSubtitle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardTitle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostLinkBanner
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getPostDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class PostDetailScreen(
    private val post: PostModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getPostDetailViewModel(post) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }

        val statePost = uiState.post
        Scaffold(modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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
                                    ),
                                )
                                notificationCenter.addObserver({
                                    (it as? SortType)?.also { sortType ->
                                        model.reduce(PostDetailMviModel.Intent.ChangeSort(sortType))
                                    }
                                }, key, sheet.key)
                                bottomSheetNavigator.show(sheet)
                            },
                            imageVector = uiState.sortType.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick {
                                navigator?.pop()
                            },
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        )
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
                    FloatingActionButton(
                        shape = CircleShape,
                        backgroundColor = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            val screen = CreateCommentScreen(
                                originalPost = statePost,
                            )
                            notificationCenter.addObserver({
                                model.reduce(PostDetailMviModel.Intent.Refresh)
                            }, key, screen.key)
                            bottomSheetNavigator.show(screen)
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }) { padding ->
            if (uiState.currentUserId != 0) {
                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(PostDetailMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .nestedScroll(fabNestedScrollConnection)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        item {
                            val themeRepository = remember { getThemeRepository() }
                            val fontScale by themeRepository.contentFontScale.collectAsState()
                            CompositionLocalProvider(
                                LocalDensity provides Density(
                                    density = LocalDensity.current.density,
                                    fontScale = fontScale,
                                ),
                            ) {
                                Card(
                                    modifier = Modifier.background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(CornerSize.m),
                                    ).padding(
                                        vertical = Spacing.lHalf,
                                        horizontal = Spacing.s,
                                    ),
                                ) {
                                    PostCardTitle(
                                        text = statePost.title
                                    )
                                    PostCardSubtitle(
                                        community = statePost.community,
                                        creator = statePost.creator?.copy(avatar = null),
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
                                    )
                                    PostCardImage(
                                        imageUrl = statePost.thumbnailUrl.orEmpty(),
                                        onImageClick = {
                                            navigator?.push(
                                                ZoomableImageScreen(
                                                    url = statePost.thumbnailUrl.orEmpty()
                                                ),
                                            )
                                        }
                                    )
                                    PostCardBody(
                                        text = statePost.text,
                                    )
                                    PostLinkBanner(
                                        modifier = Modifier.padding(vertical = Spacing.xs),
                                        url = statePost.url.takeIf {
                                            it?.contains("pictrs/image") == false
                                        }.orEmpty(),
                                    )
                                    PostCardFooter(
                                        comments = statePost.comments,
                                        score = statePost.score,
                                        upVoted = statePost.myVote > 0,
                                        downVoted = statePost.myVote < 0,
                                        saved = statePost.saved,
                                        date = statePost.publishDate,
                                        onUpVote = {
                                            model.reduce(
                                                PostDetailMviModel.Intent.UpVotePost(
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onDownVote = {
                                            model.reduce(
                                                PostDetailMviModel.Intent.DownVotePost(
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onSave = {
                                            model.reduce(
                                                PostDetailMviModel.Intent.SavePost(
                                                    post = statePost,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                    )
                                }
                            }
                        }
                        itemsIndexed(uiState.comments) { idx, comment ->
                            Column {
                                SwipeableCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = {
                                        when (it) {
                                            DismissValue.DismissedToStart -> MaterialTheme.colorScheme.secondary
                                            DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.tertiary
                                            DismissValue.Default -> Color.Transparent
                                        }
                                    },
                                    onGestureBegin = {
                                        model.reduce(PostDetailMviModel.Intent.HapticIndication)
                                    },
                                    onDismissToStart = {
                                        model.reduce(
                                            PostDetailMviModel.Intent.UpVoteComment(idx),
                                        )
                                    },
                                    onDismissToEnd = {
                                        model.reduce(
                                            PostDetailMviModel.Intent.DownVoteComment(idx),
                                        )
                                    },
                                    swipeContent = { direction ->
                                        val icon = when (direction) {
                                            DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                            DismissDirection.EndToStart -> Icons.Default.ArrowCircleUp
                                        }
                                        val (iconModifier, iconTint) = when {
                                            direction == DismissDirection.StartToEnd && statePost.myVote < 0 -> {
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

                                            direction == DismissDirection.EndToStart && statePost.myVote > 0 -> {
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
                                        CommentCard(
                                            comment = comment,
                                            options = buildList {
                                                if (comment.creator?.id == uiState.currentUserId) {
                                                    add(stringResource(MR.strings.comment_action_delete))
                                                }
                                            },
                                            onUpVote = {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.UpVoteComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onDownVote = {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.DownVoteComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onSave = {
                                                model.reduce(
                                                    PostDetailMviModel.Intent.SaveComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onReply = {
                                                val screen = CreateCommentScreen(
                                                    originalPost = statePost,
                                                    originalComment = comment,
                                                )
                                                notificationCenter.addObserver({
                                                    model.reduce(PostDetailMviModel.Intent.Refresh)
                                                }, key, screen.key)
                                                bottomSheetNavigator.show(screen)
                                            },
                                            onOptionSelected = { idx ->
                                                when (idx) {
                                                    else -> model.reduce(
                                                        PostDetailMviModel.Intent.DeleteComment(
                                                            comment.id
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                    },
                                )
                                if ((comment.comments
                                        ?: 0) > 0 && comment.depth == CommentRepository.MAX_COMMENT_DEPTH && (idx < uiState.comments.lastIndex && uiState.comments[idx + 1].depth < comment.depth)
                                ) {
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
                                                style = MaterialTheme.typography.labelMedium,
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
                        item {
                            Spacer(modifier = Modifier.height(Spacing.s))
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
