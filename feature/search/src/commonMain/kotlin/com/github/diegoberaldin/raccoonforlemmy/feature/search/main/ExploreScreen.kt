package com.github.diegoberaldin.raccoonforlemmy.feature.search.main

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
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
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.UserItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ExploreScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ExploreMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val drawerCoordinator = remember { getDrawerCoordinator() }
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
        val themeRepository = remember { getThemeRepository() }
        val upvoteColor by themeRepository.upvoteColor.collectAsState()
        val downvoteColor by themeRepository.downvoteColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val lazyListState = rememberLazyListState()
        val detailOpener = remember { getDetailOpener() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.onDoubleTabSelection.onEach { section ->
                if (section == TabNavigationSection.Explore) {
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
                        topAppBarState.heightOffset = 0f
                        topAppBarState.contentOffset = 0f
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
                            sheetKey = key,
                            isLogged = uiState.isLogged,
                        )
                        navigationCoordinator.showBottomSheet(sheet)
                    },
                    onSelectSortType = rememberCallback {
                        focusManager.clearFocus()
                        val sheet = SortBottomSheet(
                            sheetKey = key,
                            values = uiState.availableSortTypes,
                            comments = false,
                            expandTop = true,
                        )
                        navigationCoordinator.showBottomSheet(sheet)
                    },
                    onHamburgerTapped = rememberCallback {
                        scope.launch {
                            drawerCoordinator.toggleDrawer()
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier.padding(padding),
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
                                onClick = rememberCallback {
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
                var isTowardsStart by remember { mutableStateOf(false) }
                val draggableState = remember {
                    DraggableState { delta ->
                        isTowardsStart = delta > 0
                    }
                }
                val onSectionSelected = { idx: Int ->
                    val section = when (idx) {
                        1 -> SearchResultType.Posts
                        2 -> SearchResultType.Comments
                        3 -> SearchResultType.Communities
                        4 -> SearchResultType.Users
                        else -> SearchResultType.All
                    }
                    model.reduce(ExploreMviModel.Intent.SetResultType(section))
                }
                ScrollableTabRow(selectedTabIndex = currentSection, edgePadding = 0.dp, tabs = {
                    listOf(
                        stringResource(MR.strings.explore_result_type_all),
                        stringResource(MR.strings.explore_result_type_posts),
                        stringResource(MR.strings.explore_result_type_comments),
                        stringResource(MR.strings.explore_result_type_communities),
                        stringResource(MR.strings.explore_result_type_users),
                    ).forEachIndexed { i, title ->
                        Tab(
                            modifier = Modifier.draggable(state = draggableState,
                                orientation = Orientation.Horizontal,
                                onDragStopped = {
                                    if (isTowardsStart) {
                                        onSectionSelected((currentSection - 1).coerceAtLeast(0))
                                    } else {
                                        onSectionSelected(
                                            (currentSection + 1).coerceAtMost(4)
                                        )
                                    }
                                }),
                            selected = i == currentSection,
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            },
                            onClick = rememberCallback {
                                onSectionSelected(i)
                            },
                        )
                    }
                })

                val pullRefreshState = rememberPullRefreshState(
                    uiState.refreshing,
                    { model.reduce(ExploreMviModel.Intent.Refresh) },
                )
                Box(
                    modifier = Modifier.padding(Spacing.xxs).let {
                        if (settings.hideNavigationBarWhileScrolling) {
                            it.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            it
                        }
                    }.nestedScroll(keyboardScrollConnection).pullRefresh(pullRefreshState),
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
                                is SearchResult.Community -> {
                                    CommunityItem(
                                        modifier = Modifier.fillMaxWidth().onClick(
                                            onClick = rememberCallback {
                                                detailOpener.openCommunityDetail(result.model, "")
                                            },
                                        ),
                                        community = result.model,
                                        autoLoadImages = uiState.autoLoadImages,
                                    )
                                }

                                is SearchResult.Post -> {
                                    SwipeableCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = uiState.swipeActionsEnabled,
                                        directions = if (!uiState.isLogged) {
                                            emptySet()
                                        } else {
                                            setOf(
                                                DismissDirection.StartToEnd,
                                                DismissDirection.EndToStart,
                                            )
                                        },
                                        backgroundColor = rememberCallbackArgs { direction ->
                                            when (direction) {
                                                DismissValue.DismissedToStart -> upvoteColor
                                                    ?: defaultUpvoteColor

                                                DismissValue.DismissedToEnd -> downvoteColor
                                                    ?: defaultDownVoteColor

                                                DismissValue.Default -> Color.Transparent
                                            }
                                        },
                                        onGestureBegin = rememberCallback(model) {
                                            model.reduce(ExploreMviModel.Intent.HapticIndication)
                                        },
                                        onDismissToStart = rememberCallback(model) {
                                            model.reduce(ExploreMviModel.Intent.UpVotePost(result.model.id))
                                        },
                                        onDismissToEnd = rememberCallback(model) {
                                            model.reduce(ExploreMviModel.Intent.DownVotePost(result.model.id))
                                        },
                                        swipeContent = { direction ->
                                            val icon = when (direction) {
                                                DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                                DismissDirection.EndToStart -> Icons.Default.ArrowCircleUp
                                            }
                                            androidx.compose.material.Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color.White,
                                            )
                                        },
                                        content = {
                                            PostCard(
                                                post = result.model,
                                                postLayout = uiState.postLayout,
                                                fullHeightImage = uiState.fullHeightImages,
                                                voteFormat = uiState.voteFormat,
                                                autoLoadImages = uiState.autoLoadImages,
                                                blurNsfw = uiState.blurNsfw,
                                                actionButtonsActive = uiState.isLogged,
                                                onClick = rememberCallback {
                                                    detailOpener.openPostDetail(result.model)
                                                },
                                                onDoubleClick = if (!uiState.doubleTapActionEnabled) {
                                                    null
                                                } else {
                                                    rememberCallback(model) {
                                                        if (uiState.isLogged) {
                                                            model.reduce(
                                                                ExploreMviModel.Intent.UpVotePost(
                                                                    id = result.model.id,
                                                                    feedback = true,
                                                                ),
                                                            )
                                                        }
                                                    }
                                                },
                                                onOpenCommunity = rememberCallbackArgs { community, instance ->
                                                    detailOpener.openCommunityDetail(
                                                        community,
                                                        instance
                                                    )
                                                },
                                                onOpenCreator = rememberCallbackArgs { user, instance ->
                                                    detailOpener.openUserDetail(user, instance)
                                                },
                                                onUpVote = rememberCallback(model) {
                                                    if (uiState.isLogged) {
                                                        model.reduce(
                                                            ExploreMviModel.Intent.UpVotePost(
                                                                id = result.model.id,
                                                                feedback = true,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onDownVote = rememberCallback(model) {
                                                    if (uiState.isLogged) {
                                                        model.reduce(
                                                            ExploreMviModel.Intent.DownVotePost(
                                                                id = result.model.id,
                                                                feedback = true,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onSave = rememberCallback(model) {
                                                    if (uiState.isLogged) {
                                                        model.reduce(
                                                            ExploreMviModel.Intent.SavePost(
                                                                id = result.model.id,
                                                                feedback = true,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onOpenImage = rememberCallbackArgs { url ->
                                                    navigationCoordinator.pushScreen(
                                                        ZoomableImageScreen(url),
                                                    )
                                                },
                                                onOpenPost = rememberCallbackArgs { post, instance ->
                                                    detailOpener.openPostDetail(post, instance)

                                                },
                                                onOpenWeb = rememberCallbackArgs { url ->
                                                    navigationCoordinator.pushScreen(
                                                        WebViewScreen(url)
                                                    )
                                                },
                                            )
                                        },
                                    )
                                    if (uiState.postLayout != PostLayout.Card) {
                                        Divider(modifier = Modifier.padding(vertical = Spacing.s))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.s))
                                    }
                                }

                                is SearchResult.Comment -> {
                                    SwipeableCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = uiState.swipeActionsEnabled,
                                        directions = if (!uiState.isLogged) {
                                            emptySet()
                                        } else {
                                            setOf(
                                                DismissDirection.StartToEnd,
                                                DismissDirection.EndToStart,
                                            )
                                        },
                                        backgroundColor = rememberCallbackArgs { direction ->
                                            when (direction) {
                                                DismissValue.DismissedToStart -> upvoteColor
                                                    ?: defaultUpvoteColor

                                                DismissValue.DismissedToEnd -> downvoteColor
                                                    ?: defaultDownVoteColor

                                                DismissValue.Default -> Color.Transparent
                                            }
                                        },
                                        onGestureBegin = rememberCallback(model) {
                                            model.reduce(ExploreMviModel.Intent.HapticIndication)
                                        },
                                        onDismissToStart = rememberCallback(model) {
                                            model.reduce(
                                                ExploreMviModel.Intent.UpVoteComment(
                                                    id = result.model.id
                                                ),
                                            )
                                        },
                                        onDismissToEnd = rememberCallback(model) {
                                            model.reduce(
                                                ExploreMviModel.Intent.DownVoteComment(
                                                    id = result.model.id
                                                ),
                                            )
                                        },
                                        swipeContent = { direction ->
                                            val icon = when (direction) {
                                                DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                                DismissDirection.EndToStart -> Icons.Default.ArrowCircleUp
                                            }
                                            androidx.compose.material.Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color.White,
                                            )
                                        },
                                        content = {
                                            CommentCard(
                                                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                                comment = result.model,
                                                voteFormat = uiState.voteFormat,
                                                autoLoadImages = uiState.autoLoadImages,
                                                hideIndent = true,
                                                actionButtonsActive = uiState.isLogged,
                                                onClick = rememberCallback {
                                                    detailOpener.openPostDetail(
                                                        post = PostModel(id = result.model.postId),
                                                        highlightCommentId = result.model.id,
                                                    )
                                                },
                                                onDoubleClick = if (!uiState.doubleTapActionEnabled) {
                                                    null
                                                } else {
                                                    rememberCallback(model) {
                                                        if (uiState.isLogged) {
                                                            model.reduce(
                                                                ExploreMviModel.Intent.UpVoteComment(
                                                                    id = result.model.id,
                                                                    feedback = true,
                                                                ),
                                                            )
                                                        }
                                                    }
                                                },
                                                onUpVote = rememberCallback(model) {
                                                    if (uiState.isLogged) {
                                                        model.reduce(
                                                            ExploreMviModel.Intent.UpVoteComment(
                                                                id = result.model.id,
                                                                feedback = true,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onDownVote = rememberCallback(model) {
                                                    if (uiState.isLogged) {
                                                        model.reduce(
                                                            ExploreMviModel.Intent.DownVoteComment(
                                                                id = result.model.id,
                                                                feedback = true,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onSave = rememberCallback(model) {
                                                    if (uiState.isLogged) {
                                                        model.reduce(
                                                            ExploreMviModel.Intent.SaveComment(
                                                                id = result.model.id,
                                                                feedback = true,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onOpenCommunity = rememberCallbackArgs { community, instance ->
                                                    detailOpener.openCommunityDetail(
                                                        community,
                                                        instance
                                                    )
                                                },
                                                onOpenCreator = rememberCallbackArgs { user, instance ->
                                                    detailOpener.openUserDetail(user, instance)
                                                },
                                                onOpenPost = rememberCallbackArgs { post, instance ->
                                                    detailOpener.openPostDetail(
                                                        post = post,
                                                        otherInstance = instance,
                                                    )
                                                },
                                                onOpenWeb = rememberCallbackArgs { url ->
                                                    navigationCoordinator.pushScreen(
                                                        WebViewScreen(url)
                                                    )
                                                },
                                            )
                                        },
                                    )
                                    Divider(
                                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                                        thickness = 0.25.dp
                                    )
                                }

                                is SearchResult.User -> {
                                    UserItem(
                                        modifier = Modifier.fillMaxWidth().onClick(
                                            onClick = rememberCallback {
                                                detailOpener.openUserDetail(result.model, "")
                                            },
                                        ),
                                        user = result.model,
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
