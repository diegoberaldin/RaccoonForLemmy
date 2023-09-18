package com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.racconforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserCounters
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getUserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class UserDetailScreen(
    private val user: UserModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel(user.id.toString()) { getUserDetailViewModel(user) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val notificationCenter = remember { getNotificationCenter() }
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(Spacing.xs),
            topBar =
            {
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
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(UserDetailMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier.pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                            ) {
                                UserHeader(user = uiState.user)
                                UserCounters(
                                    modifier = Modifier.graphicsLayer(translationY = -Spacing.m.toLocalPixel()),
                                    user = uiState.user,
                                )
                                Spacer(modifier = Modifier.height(Spacing.s))
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
                            }
                        }
                        if (uiState.section == UserDetailSection.Posts) {
                            itemsIndexed(uiState.posts) { idx, post ->
                                SwipeableCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = uiState.swipeActionsEnabled,
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
                                            modifier = Modifier.onClick {
                                                navigator?.push(
                                                    PostDetailScreen(post),
                                                )
                                            },
                                            post = post,
                                            blurNsfw = uiState.blurNsfw,
                                            options = buildList {
                                                add(stringResource(MR.strings.post_action_share))
                                            },
                                            onUpVote = {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.UpVotePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onDownVote = {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.DownVotePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onSave = {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.SavePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onOpenCommunity = { community ->
                                                navigator?.push(
                                                    CommunityDetailScreen(community),
                                                )
                                            },
                                            onReply = {
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
                                            },
                                            onImageClick = { url ->
                                                navigator?.push(
                                                    ZoomableImageScreen(url),
                                                )
                                            },
                                            onOptionSelected = { optionIdx ->
                                                when (optionIdx) {
                                                    else -> model.reduce(
                                                        UserDetailMviModel.Intent.SharePost(idx)
                                                    )
                                                }
                                            }
                                        )
                                    },
                                )
                            }
                        } else {
                            itemsIndexed(uiState.comments) { idx, comment ->
                                SwipeableCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = uiState.swipeActionsEnabled,
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
                                            direction == DismissDirection.StartToEnd && comment.myVote < 0 -> {
                                                Modifier.background(
                                                    color = Color.Transparent,
                                                    shape = CircleShape,
                                                ) to MaterialTheme.colorScheme.onSecondary
                                            }

                                            direction == DismissDirection.StartToEnd -> {
                                                Modifier.background(
                                                    color = MaterialTheme.colorScheme.onTertiary,
                                                    shape = CircleShape,
                                                ) to MaterialTheme.colorScheme.tertiary
                                            }

                                            direction == DismissDirection.EndToStart && comment.myVote > 0 -> {
                                                Modifier.background(
                                                    color = Color.Transparent,
                                                    shape = CircleShape,
                                                ) to MaterialTheme.colorScheme.onTertiary
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
                                            comment = comment,
                                            onSave = {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.SaveComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onUpVote = {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.UpVoteComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onDownVote = {
                                                model.reduce(
                                                    UserDetailMviModel.Intent.DownVoteComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onReply = {
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
                                        )
                                    },
                                )
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
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
