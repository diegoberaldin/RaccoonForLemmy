package com.github.diegoberaldin.raccoonforlemmy.feature.search.main

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.getExploreViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.ui.ExploreTab
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ExploreScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getExploreViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val notificationCenter = remember { getNotificationCenter() }
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val scope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current
        val keyboardScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    focusManager.clearFocus()
                    return Offset.Zero
                }
            }
        }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.addObserver(
                { result ->
                    (result as? ListingType)?.also {
                        model.reduce(ExploreMviModel.Intent.SetListingType(it))
                    }
                }, key, NotificationCenterContractKeys.ChangeFeedType
            )
            notificationCenter.addObserver(
                {
                    (it as? SortType)?.also { sortType ->
                        model.reduce(
                            ExploreMviModel.Intent.SetSortType(sortType)
                        )
                    }
                }, key, NotificationCenterContractKeys.ChangeSortType
            )
            notificationCenter.addObserver(
                {
                    model.reduce(ExploreMviModel.Intent.Refresh)
                },
                key,
                NotificationCenterContractKeys.CommentCreated
            )
        }
        val lazyListState = rememberLazyListState()
        LaunchedEffect(Unit) {
            navigationCoordinator.onDoubleTabSelection.onEach { tab ->
                if (tab == ExploreTab) {
                    lazyListState.scrollToItem(0)
                    topAppBarState.heightOffset = 0f
                    topAppBarState.contentOffset = 0f
                }
            }.launchIn(this)
        }
        LaunchedEffect(model) {
            model.effects.onEach {
                when (it) {
                    ExploreMviModel.Effect.BackToTop -> {
                        lazyListState.scrollToItem(0)
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                ExploreTopBar(
                    scrollBehavior = scrollBehavior,
                    listingType = uiState.listingType,
                    sortType = uiState.sortType,
                    onSelectListingType = rememberCallback {
                        focusManager.clearFocus()
                        val sheet = ListingTypeBottomSheet(
                            isLogged = uiState.isLogged,
                        )
                        navigationCoordinator.getBottomNavigator()?.show(sheet)
                    },
                    onSelectSortType = rememberCallback {
                        focusManager.clearFocus()
                        val sheet = SortBottomSheet(
                            expandTop = true,
                        )
                        navigationCoordinator.getBottomNavigator()?.show(sheet)
                    },
                    onHamburgerTapped = rememberCallback {
                        scope.launch {
                            drawerCoordinator.toggleDrawer()
                        }
                    }
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                TextField(
                    modifier = Modifier.padding(
                        horizontal = Spacing.m,
                        vertical = Spacing.s,
                    ).fillMaxWidth(),
                    label = {
                        Text(text = stringResource(MR.strings.explore_search_placeholder))
                    },
                    singleLine = true,
                    value = uiState.searchText,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                    ),
                    onValueChange = { value ->
                        model.reduce(ExploreMviModel.Intent.SetSearch(value))
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.onClick(
                                rememberCallback {
                                    if (uiState.searchText.isNotEmpty()) {
                                        model.reduce(ExploreMviModel.Intent.SetSearch(""))
                                    }
                                },
                            ),
                            imageVector = if (uiState.searchText.isEmpty()) Icons.Default.Search else Icons.Default.Clear,
                            contentDescription = null,
                        )
                    },
                )
                val currentSection = when (uiState.resultType) {
                    SearchResultType.Posts -> 1
                    SearchResultType.Comments -> 2
                    SearchResultType.Communities -> 3
                    SearchResultType.Users -> 4
                    else -> 0
                }
                ScrollableTabRow(
                    selectedTabIndex = currentSection,
                    edgePadding = 0.dp,
                    tabs = {
                        listOf(
                            stringResource(MR.strings.explore_result_type_all),
                            stringResource(MR.strings.explore_result_type_posts),
                            stringResource(MR.strings.explore_result_type_comments),
                            stringResource(MR.strings.explore_result_type_communities),
                            stringResource(MR.strings.explore_result_type_users),
                        ).forEachIndexed { i, title ->
                            Tab(
                                selected = i == currentSection,
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                },
                                onClick = {
                                    val section = when (i) {
                                        1 -> SearchResultType.Posts
                                        2 -> SearchResultType.Comments
                                        3 -> SearchResultType.Communities
                                        4 -> SearchResultType.Users
                                        else -> SearchResultType.All
                                    }
                                    model.reduce(ExploreMviModel.Intent.SetResultType(section))
                                },
                            )
                        }
                    }
                )

                val pullRefreshState = rememberPullRefreshState(
                    uiState.refreshing,
                    { model.reduce(ExploreMviModel.Intent.Refresh) },
                )
                Box(
                    modifier = Modifier.padding(Spacing.xxs)
                        .let {
                            if (settings.hideNavigationBarWhileScrolling) {
                                it.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                it
                            }
                        }
                        .nestedScroll(keyboardScrollConnection)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                    ) {
                        if (uiState.results.isEmpty() && uiState.loading) {
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
                        items(uiState.results, key = { getItemKey(it) }) { result ->
                            when (result) {
                                is CommunityModel -> {
                                    CommunityItem(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onClick(
                                                rememberCallback {
                                                    navigationCoordinator.getRootNavigator()?.push(
                                                        CommunityDetailScreen(result),
                                                    )
                                                },
                                            ),
                                        community = result,
                                        autoLoadImages = uiState.autoLoadImages,
                                    )
                                }

                                is PostModel -> {
                                    PostCard(
                                        post = result,
                                        postLayout = uiState.postLayout,
                                        fullHeightImage = uiState.fullHeightImages,
                                        separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                        autoLoadImages = uiState.autoLoadImages,
                                        blurNsfw = uiState.blurNsfw,
                                        onClick = rememberCallback {
                                            navigationCoordinator.getRootNavigator()?.push(
                                                PostDetailScreen(result),
                                            )
                                        },
                                        onOpenCommunity = rememberCallbackArgs { community ->
                                            navigationCoordinator.getRootNavigator()?.push(
                                                CommunityDetailScreen(community),
                                            )
                                        },
                                        onOpenCreator = rememberCallbackArgs { user ->
                                            navigationCoordinator.getRootNavigator()?.push(
                                                UserDetailScreen(user),
                                            )
                                        },
                                        onUpVote = rememberCallback(model) {
                                            model.reduce(
                                                ExploreMviModel.Intent.UpVotePost(
                                                    id = result.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onDownVote = rememberCallback(model) {
                                            model.reduce(
                                                ExploreMviModel.Intent.DownVotePost(
                                                    id = result.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onSave = rememberCallback(model) {
                                            model.reduce(
                                                ExploreMviModel.Intent.SavePost(
                                                    id = result.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onReply = rememberCallback {
                                            val screen = CreateCommentScreen(
                                                originalPost = result,
                                            )
                                            navigationCoordinator.getBottomNavigator()?.show(screen)
                                        },
                                        onImageClick = rememberCallbackArgs { url ->
                                            navigationCoordinator.getRootNavigator()?.push(
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

                                is CommentModel -> {
                                    CommentCard(
                                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                        comment = result,
                                        separateUpAndDownVotes = uiState.separateUpAndDownVotes,
                                        autoLoadImages = uiState.autoLoadImages,
                                        hideIndent = true,
                                        onClick = rememberCallback {
                                            navigationCoordinator.getRootNavigator()?.push(
                                                PostDetailScreen(
                                                    post = PostModel(id = result.postId),
                                                    highlightCommentId = result.id,
                                                ),
                                            )
                                        },
                                        onUpVote = rememberCallback(model) {
                                            model.reduce(
                                                ExploreMviModel.Intent.UpVoteComment(
                                                    id = result.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onDownVote = rememberCallback(model) {
                                            model.reduce(
                                                ExploreMviModel.Intent.DownVoteComment(
                                                    id = result.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onSave = rememberCallback(model) {
                                            model.reduce(
                                                ExploreMviModel.Intent.SaveComment(
                                                    id = result.id,
                                                    feedback = true,
                                                ),
                                            )
                                        },
                                        onReply = rememberCallback {
                                            val screen = CreateCommentScreen(
                                                originalPost = PostModel(id = result.postId),
                                                originalComment = result,
                                            )
                                            navigationCoordinator.getBottomNavigator()?.show(screen)
                                        },
                                        onOpenCommunity = rememberCallbackArgs {
                                            navigationCoordinator.getRootNavigator()?.push(
                                                CommunityDetailScreen(it)
                                            )
                                        },
                                        onOpenCreator = rememberCallbackArgs {
                                            navigationCoordinator.getRootNavigator()?.push(
                                                UserDetailScreen(it)
                                            )
                                        },
                                    )
                                    Divider(
                                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                                        thickness = 0.25.dp
                                    )
                                }

                                is UserModel -> {
                                    UserItem(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onClick(
                                                rememberCallback {
                                                    navigationCoordinator.getRootNavigator()?.push(
                                                        UserDetailScreen(result),
                                                    )
                                                },
                                            ),
                                        user = result,
                                    )
                                }
                            }
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(ExploreMviModel.Intent.LoadNextPage)
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

                        if (uiState.results.isEmpty() && !uiState.loading) {
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
}
