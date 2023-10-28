package com.github.diegoberaldin.raccoonforlemmy.core.commonui.saveditems

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getSavedItemsViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
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
import kotlinx.coroutines.launch

class SavedItemsScreen : Screen {

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getSavedItemsViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val notificationCenter = remember { getNotificationCenter() }
        val isFabVisible = remember { mutableStateOf(true) }
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
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
        val drawerCoordinator = remember { getDrawerCoordinator() }
        var rawContent by remember { mutableStateOf<Any?>(null) }

        DisposableEffect(key) {
            drawerCoordinator.setGesturesEnabled(false)
            onDispose {
                drawerCoordinator.setGesturesEnabled(true)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = stringResource(MR.strings.navigation_drawer_title_bookmarks),
                        )
                    },
                    actions = {
                        Image(
                            modifier = Modifier.onClick {
                                val sheet = SortBottomSheet(
                                    values = listOf(
                                        SortType.Hot,
                                        SortType.New,
                                        SortType.Old,
                                    ),
                                )
                                notificationCenter.addObserver({
                                    (it as? SortType)?.also { sortType ->
                                        model.reduce(SavedItemsMviModel.Intent.ChangeSort(sortType))
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
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
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
                        }
                    )
                }
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                SectionSelector(
                    modifier = Modifier.padding(vertical = Spacing.s),
                    titles = listOf(
                        stringResource(MR.strings.profile_section_posts),
                        stringResource(MR.strings.profile_section_comments),
                    ),
                    currentSection = when (uiState.section) {
                        SavedItemsSection.Comments -> 1
                        else -> 0
                    },
                    onSectionSelected = {
                        val section = when (it) {
                            1 -> SavedItemsSection.Comments
                            else -> SavedItemsSection.Posts
                        }
                        model.reduce(SavedItemsMviModel.Intent.ChangeSection(section))
                    },
                )
                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(SavedItemsMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .pullRefresh(pullRefreshState)
                        .nestedScroll(fabNestedScrollConnection),
                ) {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.padding(horizontal = Spacing.xxxs),
                    ) {
                        if (uiState.section == SavedItemsSection.Posts) {
                            itemsIndexed(uiState.posts) { idx, post ->
                                PostCard(
                                    post = post,
                                    postLayout = uiState.postLayout,
                                    fullHeightImage = uiState.fullHeightImages,
                                    separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                    autoLoadImages = uiState.autoLoadImages,
                                    blurNsfw = uiState.blurNsfw,
                                    onClick = {
                                        navigator?.push(
                                            PostDetailScreen(post),
                                        )
                                    },
                                    onOpenCommunity = { community ->
                                        navigator?.push(
                                            CommunityDetailScreen(community),
                                        )
                                    },
                                    onOpenCreator = { u ->
                                        if (u.id != uiState.user?.id) {
                                            navigator?.push(UserDetailScreen(u))
                                        }
                                    },
                                    onUpVote = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.UpVotePost(
                                                index = idx,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onDownVote = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.DownVotePost(
                                                index = idx,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onSave = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.SavePost(
                                                index = idx,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onReply = {
                                        val screen = CreateCommentScreen(
                                            originalPost = post,
                                        )
                                        bottomSheetNavigator.show(screen)
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
                                    onOptionSelected = { optionIndex ->
                                        when (optionIndex) {
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

                                            else -> {
                                                model.reduce(SavedItemsMviModel.Intent.SharePost(idx))
                                            }
                                        }
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
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(top = Spacing.xs),
                                        textAlign = TextAlign.Center,
                                        text = stringResource(MR.strings.message_empty_list),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(uiState.comments) { idx, comment ->
                                CommentCard(
                                    comment = comment,
                                    separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                    autoLoadImages = uiState.autoLoadImages,
                                    hideIndent = true,
                                    onClick = {
                                        navigator?.push(
                                            PostDetailScreen(
                                                post = PostModel(id = comment.postId),
                                                highlightCommentId = comment.id,
                                            ),
                                        )
                                    },
                                    onUpVote = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.UpVoteComment(
                                                index = idx,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onDownVote = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.DownVoteComment(
                                                index = idx,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onSave = {
                                        model.reduce(
                                            SavedItemsMviModel.Intent.SaveComment(
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
                                        bottomSheetNavigator.show(screen)
                                    },
                                    options = buildList {
                                        add(stringResource(MR.strings.post_action_see_raw))
                                        add(stringResource(MR.strings.post_action_report))
                                    },
                                    onOptionSelected = { optionIndex ->
                                        when (optionIndex) {
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
                            }

                            if (uiState.comments.isEmpty() && !uiState.loading) {
                                item {
                                    androidx.compose.material.Text(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(top = Spacing.xs),
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
                                model.reduce(SavedItemsMviModel.Intent.LoadNextPage)
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
