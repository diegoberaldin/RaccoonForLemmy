package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.getMultiCommunityViewModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class MultiCommunityScreen(
    private val community: MultiCommunityModel,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getMultiCommunityViewModel(community) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val bottomNavCoordinator = remember { getNavigationCoordinator() }
        val navigator = remember { bottomNavCoordinator.getRootNavigator() }
        val notificationCenter = remember { getNotificationCenter() }
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
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
                            modifier = Modifier.onClick {
                                navigator?.pop()
                            },
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
                            }
                            if (sortType != null) {
                                Image(
                                    modifier = Modifier.onClick {
                                        val sheet = SortBottomSheet(
                                            expandTop = true,
                                        )
                                        notificationCenter.addObserver({
                                            (it as? SortType)?.also { sortType ->
                                                model.reduce(
                                                    MultiCommunityMviModel.Intent.ChangeSort(
                                                        sortType
                                                    )
                                                )
                                            }
                                        }, key, NotificationCenterContractKeys.ChangeSortType)
                                        bottomSheetNavigator.show(sheet)
                                    },
                                    imageVector = sortType.toIcon(),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                )
                            }
                        }
                    }
                )
            },
        ) { padding ->
            val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                model.reduce(MultiCommunityMviModel.Intent.Refresh)
            })
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
                    .nestedScroll(scrollBehavior.nestedScrollConnection).let {
                        val connection = bottomNavCoordinator.getBottomBarScrollConnection()
                        if (connection != null) {
                            it.nestedScroll(connection)
                        } else it
                    }
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn {
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
                        val themeRepository = remember { getThemeRepository() }
                        val fontScale by themeRepository.contentFontScale.collectAsState()
                        CompositionLocalProvider(
                            LocalDensity provides Density(
                                density = LocalDensity.current.density,
                                fontScale = fontScale,
                            ),
                        ) {
                            SwipeableCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.swipeActionsEnabled,
                                backgroundColor = {
                                    when (it) {
                                        DismissValue.DismissedToStart -> MaterialTheme.colorScheme.surfaceTint
                                        DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.tertiary
                                        DismissValue.Default -> Color.Transparent
                                    }
                                },
                                onGestureBegin = {
                                    model.reduce(MultiCommunityMviModel.Intent.HapticIndication)
                                },
                                onDismissToStart = {
                                    model.reduce(MultiCommunityMviModel.Intent.UpVotePost(idx))
                                },
                                onDismissToEnd = {
                                    model.reduce(MultiCommunityMviModel.Intent.DownVotePost(idx))
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
                                        modifier = Modifier.onClick {
                                            navigator?.push(
                                                PostDetailScreen(post),
                                            )
                                        },
                                        post = post,
                                        postLayout = uiState.postLayout,
                                        options = buildList {
                                            add(stringResource(MR.strings.post_action_share))
                                        },
                                        blurNsfw = uiState.blurNsfw,
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
                                                MultiCommunityMviModel.Intent.UpVotePost(
                                                    index = idx,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onDownVote = {
                                            model.reduce(
                                                MultiCommunityMviModel.Intent.DownVotePost(
                                                    index = idx,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onSave = {
                                            model.reduce(
                                                MultiCommunityMviModel.Intent.SavePost(
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
                                                    model.reduce(MultiCommunityMviModel.Intent.Refresh)
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
                                                    MultiCommunityMviModel.Intent.SharePost(idx)
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
