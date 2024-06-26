package com.github.diegoberaldin.raccoonforlemmy.unit.explore

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SearchField
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeActionCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.UserItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ResultTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResult
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.unit.explore.components.ExploreTopBar
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class ExploreScreen(
    private val otherInstance: String = "",
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model =
            getScreenModel<ExploreMviModel>(
                tag = otherInstance,
                parameters = { parametersOf(otherInstance) },
            )
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val focusManager = LocalFocusManager.current
        val keyboardScrollConnection =
            remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource,
                    ): Offset {
                        focusManager.clearFocus()
                        return Offset.Zero
                    }
                }
            }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val themeRepository = remember { getThemeRepository() }
        val upVoteColor by themeRepository.upVoteColor.collectAsState()
        val downVoteColor by themeRepository.downVoteColor.collectAsState()
        val replyColor by themeRepository.replyColor.collectAsState()
        val saveColor by themeRepository.saveColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultReplyColor = MaterialTheme.colorScheme.secondary
        val defaultSaveColor = MaterialTheme.colorScheme.secondaryContainer
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val lazyListState = rememberLazyListState()
        val detailOpener = remember { getDetailOpener() }
        val connection = navigationCoordinator.getBottomBarScrollConnection()
        val scope = rememberCoroutineScope()
        val isOnOtherInstance = remember { otherInstance.isNotEmpty() }
        val otherInstanceName = remember { otherInstance }
        val snackbarHostState = remember { SnackbarHostState() }
        val errorMessage = LocalStrings.current.messageGenericError
        val notificationEventKey =
            buildString {
                append("explore")
                if (isOnOtherInstance) {
                    append("-")
                    append(otherInstanceName)
                }
            }

        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.onDoubleTabSelection
                .onEach { section ->
                    runCatching {
                        if (section == TabNavigationSection.Explore) {
                            lazyListState.scrollToItem(0)
                            topAppBarState.heightOffset = 0f
                            topAppBarState.contentOffset = 0f
                        }
                    }
                }.launchIn(this)
        }
        LaunchedEffect(model) {
            model.effects
                .onEach {
                    when (it) {
                        ExploreMviModel.Effect.BackToTop -> {
                            runCatching {
                                lazyListState.scrollToItem(0)
                                topAppBarState.heightOffset = 0f
                                topAppBarState.contentOffset = 0f
                            }
                        }

                        ExploreMviModel.Effect.OperationFailure -> {
                            snackbarHostState.showSnackbar(errorMessage)
                        }
                    }
                }.launchIn(this)
        }

        Scaffold(
            topBar = {
                ExploreTopBar(
                    topAppBarState = topAppBarState,
                    scrollBehavior = scrollBehavior,
                    listingType = uiState.listingType,
                    sortType = uiState.sortType,
                    resultType = uiState.resultType,
                    otherInstance = otherInstanceName,
                    edgeToEdge = settings.edgeToEdge,
                    onSelectListingType =
                        rememberCallback {
                            focusManager.clearFocus()
                            val sheet =
                                ListingTypeBottomSheet(
                                    isLogged = uiState.isLogged,
                                    screenKey = notificationEventKey,
                                )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    onSelectSortType =
                        rememberCallback {
                            focusManager.clearFocus()
                            val sheet =
                                SortBottomSheet(
                                    values = uiState.availableSortTypes.map { it.toInt() },
                                    expandTop = true,
                                    screenKey = notificationEventKey,
                                )
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    onSelectResultTypeType =
                        rememberCallback {
                            val sheet = ResultTypeBottomSheet(screenKey = notificationEventKey)
                            navigationCoordinator.showBottomSheet(sheet)
                        },
                    onHamburgerTapped =
                        rememberCallback {
                            scope.launch {
                                drawerCoordinator.toggleDrawer()
                            }
                        },
                    onBack =
                        rememberCallback {
                            navigationCoordinator.popScreen()
                        },
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        snackbarData = data,
                    )
                }
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier.padding(
                        top = padding.calculateTopPadding(),
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                SearchField(
                    modifier =
                        Modifier
                            .padding(
                                horizontal = Spacing.s,
                                vertical = Spacing.s,
                            ).fillMaxWidth(),
                    hint = LocalStrings.current.exploreSearchPlaceholder,
                    value = uiState.searchText,
                    onValueChange = { value ->
                        model.reduce(ExploreMviModel.Intent.SetSearch(value))
                    },
                    onClear = {
                        model.reduce(ExploreMviModel.Intent.SetSearch(""))
                    },
                )

                val pullRefreshState =
                    rememberPullRefreshState(
                        uiState.refreshing,
                        { model.reduce(ExploreMviModel.Intent.Refresh) },
                    )
                Box(
                    modifier =
                        Modifier
                            .padding(top = Spacing.xs)
                            .then(
                                if (connection != null && settings.hideNavigationBarWhileScrolling) {
                                    Modifier.nestedScroll(connection)
                                } else {
                                    Modifier
                                },
                            ).then(
                                if (settings.hideNavigationBarWhileScrolling) {
                                    Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                                } else {
                                    Modifier
                                },
                            ).nestedScroll(keyboardScrollConnection)
                            .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                    ) {
                        if (uiState.results.isEmpty() && uiState.initial) {
                            items(5) {
                                PostCardPlaceholder(
                                    postLayout = uiState.postLayout,
                                )
                                if (uiState.postLayout != PostLayout.Card) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.interItem))
                                }
                            }
                        }
                        items(uiState.results, key = { getItemKey(it) }) { result ->
                            when (result) {
                                is SearchResult.Community -> {
                                    CommunityItem(
                                        modifier =
                                            Modifier.fillMaxWidth().onClick(
                                                onClick = {
                                                    detailOpener.openCommunityDetail(
                                                        community = result.model,
                                                        otherInstance = otherInstanceName,
                                                    )
                                                },
                                            ),
                                        community = result.model,
                                        autoLoadImages = uiState.autoLoadImages,
                                        preferNicknames = uiState.preferNicknames,
                                        showSubscribeButton = !isOnOtherInstance,
                                        onSubscribe =
                                            rememberCallback(model) {
                                                model.reduce(
                                                    ExploreMviModel.Intent.ToggleSubscription(
                                                        result.model.id,
                                                    ),
                                                )
                                            },
                                        highlightText = uiState.searchText
                                    )
                                }

                                is SearchResult.Post -> {
                                    @Composable
                                    fun List<ActionOnSwipe>.toSwipeActions(): List<SwipeAction> =
                                        mapNotNull {
                                            when (it) {
                                                ActionOnSwipe.UpVote ->
                                                    SwipeAction(
                                                        swipeContent = {
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowCircleUp,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                            )
                                                        },
                                                        backgroundColor =
                                                            upVoteColor
                                                                ?: defaultUpvoteColor,
                                                        onTriggered =
                                                            rememberCallback {
                                                                model.reduce(
                                                                    ExploreMviModel.Intent.UpVotePost(
                                                                        result.model.id,
                                                                    ),
                                                                )
                                                            },
                                                    )

                                                ActionOnSwipe.DownVote ->
                                                    if (!uiState.downVoteEnabled) {
                                                        null
                                                    } else {
                                                        SwipeAction(
                                                            swipeContent = {
                                                                Icon(
                                                                    imageVector = Icons.Default.ArrowCircleDown,
                                                                    contentDescription = null,
                                                                    tint = Color.White,
                                                                )
                                                            },
                                                            backgroundColor =
                                                                downVoteColor
                                                                    ?: defaultDownVoteColor,
                                                            onTriggered =
                                                                rememberCallback {
                                                                    model.reduce(
                                                                        ExploreMviModel.Intent.DownVotePost(
                                                                            result.model.id,
                                                                        ),
                                                                    )
                                                                },
                                                        )
                                                    }

                                                ActionOnSwipe.Reply ->
                                                    SwipeAction(
                                                        swipeContent = {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Default.Reply,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                            )
                                                        },
                                                        backgroundColor =
                                                            replyColor
                                                                ?: defaultReplyColor,
                                                        onTriggered =
                                                            rememberCallback {
                                                                detailOpener.openReply(
                                                                    originalPost = result.model,
                                                                )
                                                            },
                                                    )

                                                ActionOnSwipe.Save ->
                                                    SwipeAction(
                                                        swipeContent = {
                                                            Icon(
                                                                imageVector = Icons.Default.Bookmark,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                            )
                                                        },
                                                        backgroundColor =
                                                            saveColor
                                                                ?: defaultSaveColor,
                                                        onTriggered =
                                                            rememberCallback {
                                                                model.reduce(
                                                                    ExploreMviModel.Intent.SavePost(
                                                                        id = result.model.id,
                                                                    ),
                                                                )
                                                            },
                                                    )

                                                else -> null
                                            }
                                        }

                                    SwipeActionCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = uiState.swipeActionsEnabled && !isOnOtherInstance,
                                        onGestureBegin =
                                            rememberCallback(model) {
                                                model.reduce(ExploreMviModel.Intent.HapticIndication)
                                            },
                                        swipeToStartActions =
                                            if (uiState.isLogged) {
                                                uiState.actionsOnSwipeToStartPosts.toSwipeActions()
                                            } else {
                                                emptyList()
                                            },
                                        swipeToEndActions =
                                            if (uiState.isLogged) {
                                                uiState.actionsOnSwipeToEndPosts.toSwipeActions()
                                            } else {
                                                emptyList()
                                            },
                                        content = {
                                            PostCard(
                                                post = result.model,
                                                postLayout = uiState.postLayout,
                                                limitBodyHeight = true,
                                                fullHeightImage = uiState.fullHeightImages,
                                                fullWidthImage = uiState.fullWidthImages,
                                                voteFormat = uiState.voteFormat,
                                                autoLoadImages = uiState.autoLoadImages,
                                                preferNicknames = uiState.preferNicknames,
                                                showScores = uiState.showScores,
                                                blurNsfw = uiState.blurNsfw,
                                                actionButtonsActive = uiState.isLogged,
                                                downVoteEnabled = uiState.downVoteEnabled,
                                                highlightText = uiState.searchText,
                                                onClick = {
                                                    detailOpener.openPostDetail(
                                                        post = result.model,
                                                        otherInstance = otherInstanceName,
                                                    )
                                                },
                                                onDoubleClick =
                                                    if (!uiState.doubleTapActionEnabled || isOnOtherInstance) {
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
                                                onOpenCommunity =
                                                    rememberCallbackArgs { community, instance ->
                                                        detailOpener.openCommunityDetail(
                                                            community = community,
                                                            otherInstance =
                                                                instance.takeIf {
                                                                    it.isNotEmpty()
                                                                } ?: otherInstanceName,
                                                        )
                                                    },
                                                onOpenCreator =
                                                    rememberCallbackArgs { user, instance ->
                                                        detailOpener.openUserDetail(
                                                            user = user,
                                                            otherInstance =
                                                                instance.takeIf {
                                                                    it.isNotEmpty()
                                                                } ?: otherInstanceName,
                                                        )
                                                    },
                                                onUpVote =
                                                    rememberCallback(model) {
                                                        if (uiState.isLogged) {
                                                            model.reduce(
                                                                ExploreMviModel.Intent.UpVotePost(result.model.id),
                                                            )
                                                        }
                                                    },
                                                onDownVote =
                                                    rememberCallback(model) {
                                                        if (uiState.isLogged) {
                                                            model.reduce(
                                                                ExploreMviModel.Intent.DownVotePost(
                                                                    result.model.id,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                onSave =
                                                    rememberCallback(model) {
                                                        if (uiState.isLogged) {
                                                            model.reduce(
                                                                ExploreMviModel.Intent.SavePost(result.model.id),
                                                            )
                                                        }
                                                    },
                                                onOpenImage =
                                                    rememberCallbackArgs { url ->
                                                        navigationCoordinator.pushScreen(
                                                            ZoomableImageScreen(
                                                                url = url,
                                                                source =
                                                                    result.model.community
                                                                        ?.readableHandle
                                                                        .orEmpty(),
                                                            ),
                                                        )
                                                    },
                                                onOpenPost =
                                                    rememberCallbackArgs { post, instance ->
                                                        detailOpener.openPostDetail(
                                                            post = post,
                                                            otherInstance =
                                                                instance.takeIf {
                                                                    it.isNotEmpty()
                                                                } ?: otherInstanceName,
                                                        )
                                                    },
                                                onOpenWeb =
                                                    rememberCallbackArgs { url ->
                                                        navigationCoordinator.pushScreen(
                                                            WebViewScreen(url),
                                                        )
                                                    },
                                            )
                                        },
                                    )
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
                                    }
                                }

                                is SearchResult.Comment -> {
                                    @Composable
                                    fun List<ActionOnSwipe>.toSwipeActions(): List<SwipeAction> =
                                        mapNotNull {
                                            when (it) {
                                                ActionOnSwipe.UpVote ->
                                                    SwipeAction(
                                                        swipeContent = {
                                                            Icon(
                                                                imageVector = Icons.Default.ArrowCircleUp,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                            )
                                                        },
                                                        backgroundColor =
                                                            upVoteColor
                                                                ?: defaultUpvoteColor,
                                                        onTriggered =
                                                            rememberCallback {
                                                                model.reduce(
                                                                    ExploreMviModel.Intent.UpVoteComment(
                                                                        result.model.id,
                                                                    ),
                                                                )
                                                            },
                                                    )

                                                ActionOnSwipe.DownVote ->
                                                    if (!uiState.downVoteEnabled) {
                                                        null
                                                    } else {
                                                        SwipeAction(
                                                            swipeContent = {
                                                                Icon(
                                                                    imageVector = Icons.Default.ArrowCircleDown,
                                                                    contentDescription = null,
                                                                    tint = Color.White,
                                                                )
                                                            },
                                                            backgroundColor =
                                                                downVoteColor
                                                                    ?: defaultDownVoteColor,
                                                            onTriggered =
                                                                rememberCallback {
                                                                    model.reduce(
                                                                        ExploreMviModel.Intent.DownVoteComment(
                                                                            result.model.id,
                                                                        ),
                                                                    )
                                                                },
                                                        )
                                                    }

                                                ActionOnSwipe.Reply ->
                                                    SwipeAction(
                                                        swipeContent = {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Default.Reply,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                            )
                                                        },
                                                        backgroundColor =
                                                            replyColor
                                                                ?: defaultReplyColor,
                                                        onTriggered =
                                                            rememberCallback {
                                                                detailOpener.openPostDetail(
                                                                    post = PostModel(id = result.model.postId),
                                                                    highlightCommentId = result.model.id,
                                                                    otherInstance = otherInstanceName,
                                                                )
                                                            },
                                                    )

                                                ActionOnSwipe.Save ->
                                                    SwipeAction(
                                                        swipeContent = {
                                                            Icon(
                                                                imageVector = Icons.Default.Bookmark,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                            )
                                                        },
                                                        backgroundColor =
                                                            saveColor
                                                                ?: defaultSaveColor,
                                                        onTriggered =
                                                            rememberCallback {
                                                                model.reduce(
                                                                    ExploreMviModel.Intent.SaveComment(
                                                                        result.model.id,
                                                                    ),
                                                                )
                                                            },
                                                    )

                                                else -> null
                                            }
                                        }

                                    SwipeActionCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = uiState.swipeActionsEnabled,
                                        onGestureBegin =
                                            rememberCallback(model) {
                                                model.reduce(ExploreMviModel.Intent.HapticIndication)
                                            },
                                        swipeToStartActions =
                                            if (uiState.isLogged) {
                                                uiState.actionsOnSwipeToStartComments.toSwipeActions()
                                            } else {
                                                emptyList()
                                            },
                                        swipeToEndActions =
                                            if (uiState.isLogged) {
                                                uiState.actionsOnSwipeToEndComments.toSwipeActions()
                                            } else {
                                                emptyList()
                                            },
                                        content = {
                                            CommentCard(
                                                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                                                comment = result.model,
                                                voteFormat = uiState.voteFormat,
                                                autoLoadImages = uiState.autoLoadImages,
                                                preferNicknames = uiState.preferNicknames,
                                                showScores = uiState.showScores,
                                                showBot = true,
                                                showExpandedIndicator = false,
                                                indentAmount = 0,
                                                actionButtonsActive = uiState.isLogged,
                                                downVoteEnabled = uiState.downVoteEnabled,
                                                highlightText = uiState.searchText,
                                                onClick = {
                                                    detailOpener.openPostDetail(
                                                        post = PostModel(id = result.model.postId),
                                                        highlightCommentId = result.model.id,
                                                        otherInstance = otherInstanceName,
                                                    )
                                                },
                                                onDoubleClick =
                                                    if (!uiState.doubleTapActionEnabled) {
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
                                                onUpVote =
                                                    rememberCallback(model) {
                                                        if (uiState.isLogged) {
                                                            model.reduce(
                                                                ExploreMviModel.Intent.UpVoteComment(
                                                                    id = result.model.id,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                onDownVote =
                                                    rememberCallback(model) {
                                                        if (uiState.isLogged) {
                                                            model.reduce(
                                                                ExploreMviModel.Intent.DownVoteComment(
                                                                    id = result.model.id,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                onSave =
                                                    rememberCallback(model) {
                                                        if (uiState.isLogged) {
                                                            model.reduce(
                                                                ExploreMviModel.Intent.SaveComment(
                                                                    id = result.model.id,
                                                                ),
                                                            )
                                                        }
                                                    },
                                                onOpenCommunity =
                                                    rememberCallbackArgs { community, instance ->
                                                        detailOpener.openCommunityDetail(
                                                            community = community,
                                                            otherInstance =
                                                                instance.takeIf {
                                                                    it.isNotEmpty()
                                                                } ?: otherInstanceName,
                                                        )
                                                    },
                                                onOpenCreator =
                                                    rememberCallbackArgs { user, instance ->
                                                        detailOpener.openUserDetail(
                                                            user = user,
                                                            otherInstance =
                                                                instance.takeIf {
                                                                    it.isNotEmpty()
                                                                } ?: otherInstanceName,
                                                        )
                                                    },
                                                onOpenPost =
                                                    rememberCallbackArgs { post, instance ->
                                                        detailOpener.openPostDetail(
                                                            post = post,
                                                            otherInstance =
                                                                instance.takeIf {
                                                                    it.isNotEmpty()
                                                                } ?: otherInstanceName,
                                                        )
                                                    },
                                                onOpenWeb =
                                                    rememberCallbackArgs { url ->
                                                        navigationCoordinator.pushScreen(
                                                            WebViewScreen(
                                                                url,
                                                            ),
                                                        )
                                                    },
                                            )
                                        },
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = Spacing.interItem),
                                        thickness = 0.25.dp,
                                    )
                                }

                                is SearchResult.User -> {
                                    UserItem(
                                        modifier =
                                            Modifier.fillMaxWidth().onClick(
                                                onClick = {
                                                    detailOpener.openUserDetail(
                                                        user = result.model,
                                                        otherInstance = otherInstanceName,
                                                    )
                                                },
                                            ),
                                        user = result.model,
                                        preferNicknames = uiState.preferNicknames,
                                        highlightText = uiState.searchText,
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
                                Text(
                                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                    textAlign = TextAlign.Center,
                                    text = LocalStrings.current.messageEmptyList,
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
