package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.report.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.getMultiCommunityViewModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

class MultiCommunityScreen(
    private val community: MultiCommunityModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getMultiCommunityViewModel(community) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
        val themeRepository = remember { getThemeRepository() }
        val upvoteColor by themeRepository.upvoteColor.collectAsState()
        val downvoteColor by themeRepository.downvoteColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()

        DisposableEffect(key) {
            drawerCoordinator.setGesturesEnabled(false)
            onDispose {
                notificationCenter.removeObserver(key)
                drawerCoordinator.setGesturesEnabled(true)
            }
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.addObserver(
                {
                    (it as? SortType)?.also { sortType ->
                        model.reduce(
                            MultiCommunityMviModel.Intent.ChangeSort(
                                sortType
                            )
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeSortType
            )
            notificationCenter.addObserver(
                {
                    model.reduce(MultiCommunityMviModel.Intent.Refresh)
                },
                key,
                NotificationCenterContractKeys.CommentCreated
            )
        }

        Scaffold(
            topBar = {
                val sortType = uiState.sortType
                TopAppBar(
                    title = {
                        Text(
                            text = community.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                rememberCallback {
                                    navigationCoordinator.getRootNavigator()?.pop()
                                },
                            ),
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    actions = {
                        Row {
                            val additionalLabel = when (sortType) {
                                SortType.Top.Day -> stringResource(MR.strings.home_sort_type_top_day_short)
                                SortType.Top.Month -> stringResource(MR.strings.home_sort_type_top_month_short)
                                SortType.Top.Past12Hours -> stringResource(MR.strings.home_sort_type_top_12_hours_short)
                                SortType.Top.Past6Hours -> stringResource(MR.strings.home_sort_type_top_6_hours_short)
                                SortType.Top.PastHour -> stringResource(MR.strings.home_sort_type_top_hour_short)
                                SortType.Top.Week -> stringResource(MR.strings.home_sort_type_top_week_short)
                                SortType.Top.Year -> stringResource(MR.strings.home_sort_type_top_year_short)
                                else -> ""
                            }
                            if (additionalLabel.isNotEmpty()) {
                                Text(
                                    text = buildString {
                                        append("(")
                                        append(additionalLabel)
                                        append(")")
                                    }
                                )
                                Spacer(modifier = Modifier.width(Spacing.s))
                            }
                            if (sortType != null) {
                                Image(
                                    modifier = Modifier.onClick(
                                        rememberCallback {
                                            val sheet = SortBottomSheet(
                                                expandTop = true,
                                            )
                                            navigationCoordinator.getBottomNavigator()?.show(sheet)
                                        },
                                    ),
                                    imageVector = sortType.toIcon(),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                )
                            }
                        }
                    }
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
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.ClearAll,
                                text = stringResource(MR.strings.action_clear_read),
                                onSelected = rememberCallback {
                                    model.reduce(MultiCommunityMviModel.Intent.ClearRead)
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
            val pullRefreshState = rememberPullRefreshState(
                refreshing = uiState.refreshing,
                onRefresh = rememberCallback(model) {
                    model.reduce(MultiCommunityMviModel.Intent.Refresh)
                },
            )
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
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
                    items(uiState.posts) { post ->
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
                                model.reduce(MultiCommunityMviModel.Intent.HapticIndication)
                            },
                            onDismissToStart = {
                                model.reduce(MultiCommunityMviModel.Intent.UpVotePost(post.id))
                            },
                            onDismissToEnd = {
                                model.reduce(MultiCommunityMviModel.Intent.DownVotePost(post.id))
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
                                        model.reduce(MultiCommunityMviModel.Intent.MarkAsRead(post.id))
                                        navigationCoordinator.getRootNavigator()?.push(
                                            PostDetailScreen(post),
                                        )
                                    },
                                    onOpenCommunity = { community ->
                                        navigationCoordinator.getRootNavigator()?.push(
                                            CommunityDetailScreen(community),
                                        )
                                    },
                                    onOpenCreator = { user ->
                                        navigationCoordinator.getRootNavigator()?.push(
                                            UserDetailScreen(user),
                                        )
                                    },
                                    onUpVote = {
                                        model.reduce(
                                            MultiCommunityMviModel.Intent.UpVotePost(
                                                id = post.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onDownVote = {
                                        model.reduce(
                                            MultiCommunityMviModel.Intent.DownVotePost(
                                                id = post.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onSave = {
                                        model.reduce(
                                            MultiCommunityMviModel.Intent.SavePost(
                                                id = post.id,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onReply = {
                                        val screen = CreateCommentScreen(
                                            originalPost = post,
                                        )
                                        navigationCoordinator.getBottomNavigator()?.show(screen)
                                    },
                                    onImageClick = { url ->
                                        model.reduce(MultiCommunityMviModel.Intent.MarkAsRead(post.id))
                                        navigationCoordinator.getRootNavigator()?.push(
                                            ZoomableImageScreen(url),
                                        )
                                    },
                                    options = buildList {
                                        add(
                                            Option(
                                                OptionId.Share,
                                                stringResource(MR.strings.post_action_share)
                                            )
                                        )
                                        if (uiState.currentUserId != null) {
                                            add(
                                                Option(
                                                    OptionId.Hide,
                                                    stringResource(MR.strings.post_action_hide)
                                                )
                                            )
                                            add(
                                                Option(
                                                    OptionId.Report,
                                                    stringResource(MR.strings.post_action_report)
                                                )
                                            )
                                        }
                                    },
                                    onOptionSelected = { optionId ->
                                        when (optionId) {
                                            OptionId.Report -> {
                                                navigationCoordinator.getBottomNavigator()?.show(
                                                    CreateReportScreen(
                                                        postId = post.id
                                                    )
                                                )
                                            }

                                            OptionId.Hide -> model.reduce(
                                                MultiCommunityMviModel.Intent.Hide(
                                                    post.id
                                                )
                                            )

                                            OptionId.Share -> model.reduce(
                                                MultiCommunityMviModel.Intent.SharePost(post.id)
                                            )

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
                            model.reduce(MultiCommunityMviModel.Intent.LoadNextPage)
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
                            androidx.compose.material.Text(
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
