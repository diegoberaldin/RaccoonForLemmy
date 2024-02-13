package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeActionCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ShareBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.getAdditionalLabel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class MultiCommunityScreen(
    private val communityId: Int,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<MultiCommunityMviModel>(parameters = {
            parametersOf(communityId)
        })
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val themeRepository = remember { getThemeRepository() }
        val upVoteColor by themeRepository.upVoteColor.collectAsState()
        val downVoteColor by themeRepository.downVoteColor.collectAsState()
        val replyColor by themeRepository.replyColor.collectAsState()
        val saveColor by themeRepository.saveColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultReplyColor = MaterialTheme.colorScheme.secondary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        val defaultSaveColor = MaterialTheme.colorScheme.secondaryContainer
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val detailOpener = remember { getDetailOpener() }

        Scaffold(
            contentWindowInsets = if (settings.edgeToEdge) {
                WindowInsets(0, 0, 0, 0)
            } else {
                WindowInsets.navigationBars
            },
            topBar = {
                val sortType = uiState.sortType
                val maxTopInset = Dimensions.topBarHeight.value.toInt()
                var topInset by remember { mutableStateOf(maxTopInset) }
                snapshotFlow { topAppBarState.collapsedFraction }.onEach {
                    topInset = (maxTopInset * (1 - it)).toInt()
                }.launchIn(scope)
                TopAppBar(
                    windowInsets = if (settings.edgeToEdge) {
                        WindowInsets(0, topInset, 0, 0)
                    } else {
                        TopAppBarDefaults.windowInsets
                    },
                    title = {
                        Text(
                            text = uiState.community.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    navigationCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    actions = {
                        Row {
                            val additionalLabel = sortType.getAdditionalLabel()
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
                                        onClick = rememberCallback {
                                            val sheet = SortBottomSheet(
                                                sheetKey = key,
                                                values = uiState.availableSortTypes.map { it.toInt() },
                                                comments = false,
                                                expandTop = true,
                                            )
                                            navigationCoordinator.showBottomSheet(sheet)
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
                                text = LocalXmlStrings.current.actionBackToTop,
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
                                text = LocalXmlStrings.current.actionClearRead,
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
                    items(
                        uiState.posts,
                        { it.id.toString() + (it.updateDate ?: it.publishDate) }) { post ->
                        LaunchedEffect(post.id) {
                            if (settings.markAsReadWhileScrolling && !post.read) {
                                model.reduce(MultiCommunityMviModel.Intent.MarkAsRead(post.id))
                            }
                        }

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
                                                MultiCommunityMviModel.Intent.UpVotePost(post.id),
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
                                                MultiCommunityMviModel.Intent.DownVotePost(
                                                    post.id,
                                                )
                                            )
                                        },
                                    )

                                    ActionOnSwipe.Reply -> SwipeAction(
                                        swipeContent = {
                                            Icon(
                                                imageVector = Icons.Filled.Reply,
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
                                                MultiCommunityMviModel.Intent.SavePost(
                                                    id = post.id,
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
                            onGestureBegin = rememberCallback(model) {
                                model.reduce(MultiCommunityMviModel.Intent.HapticIndication)
                            },
                            swipeToStartActions = if (uiState.isLogged) {
                                uiState.actionsOnSwipeToStartPosts.toSwipeActions()
                            } else {
                                emptyList()
                            },
                            swipeToEndActions = if (uiState.isLogged) {
                                uiState.actionsOnSwipeToEndPosts.toSwipeActions()
                            } else {
                                emptyList()
                            },
                            content = {
                                PostCard(
                                    post = post,
                                    postLayout = uiState.postLayout,
                                    limitBodyHeight = true,
                                    fullHeightImage = uiState.fullHeightImages,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    preferNicknames = uiState.preferNicknames,
                                    showScores = uiState.showScores,
                                    blurNsfw = uiState.blurNsfw,
                                    onClick = rememberCallback {
                                        model.reduce(MultiCommunityMviModel.Intent.MarkAsRead(post.id))
                                        detailOpener.openPostDetail(post)
                                    },
                                    onDoubleClick = if (uiState.swipeActionsEnabled) {
                                        null
                                    } else {
                                        rememberCallback(model) {
                                            model.reduce(
                                                MultiCommunityMviModel.Intent.UpVotePost(
                                                    id = post.id,
                                                    feedback = true,
                                                ),
                                            )
                                        }
                                    },
                                    onOpenCommunity = rememberCallbackArgs { community, instance ->
                                        detailOpener.openCommunityDetail(community, instance)
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
                                    onUpVote = rememberCallback(model) {
                                        model.reduce(
                                            MultiCommunityMviModel.Intent.UpVotePost(
                                                id = post.id,
                                            ),
                                        )
                                    },
                                    onDownVote = rememberCallback(model) {
                                        model.reduce(
                                            MultiCommunityMviModel.Intent.DownVotePost(
                                                id = post.id,
                                            ),
                                        )
                                    },
                                    onSave = rememberCallback(model) {
                                        model.reduce(
                                            MultiCommunityMviModel.Intent.SavePost(
                                                id = post.id,
                                            ),
                                        )
                                    },
                                    onReply = rememberCallback {
                                        detailOpener.openPostDetail(post)
                                    },
                                    onOpenImage = rememberCallbackArgs { url ->
                                        model.reduce(MultiCommunityMviModel.Intent.MarkAsRead(post.id))
                                        navigationCoordinator.pushScreen(ZoomableImageScreen(url))
                                    },
                                    options = buildList {
                                        add(
                                            Option(
                                                OptionId.Share,
                                                LocalXmlStrings.current.postActionShare
                                            )
                                        )
                                        if (uiState.currentUserId != null) {
                                            add(
                                                Option(
                                                    OptionId.Hide,
                                                    LocalXmlStrings.current.postActionHide
                                                )
                                            )
                                            add(
                                                Option(
                                                    OptionId.Report,
                                                    LocalXmlStrings.current.postActionReport
                                                )
                                            )
                                        }
                                    },
                                    onOptionSelected = { optionId ->
                                        when (optionId) {
                                            OptionId.Report -> {
                                                navigationCoordinator.showBottomSheet(
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

                                            OptionId.Share -> {
                                                val urls = listOfNotNull(
                                                    post.originalUrl,
                                                    "https://${uiState.instance}/post/${post.id}"
                                                ).distinct()
                                                if (urls.size == 1) {
                                                    model.reduce(
                                                        MultiCommunityMviModel.Intent.Share(
                                                            urls.first()
                                                        )
                                                    )
                                                } else {
                                                    val screen = ShareBottomSheet(urls = urls)
                                                    navigationCoordinator.showBottomSheet(screen)
                                                }
                                            }

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
                            if (settings.infiniteScrollEnabled) {
                                model.reduce(MultiCommunityMviModel.Intent.LoadNextPage)
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Button(
                                        onClick = rememberCallback(model) {
                                            model.reduce(MultiCommunityMviModel.Intent.LoadNextPage)
                                        },
                                    ) {
                                        Text(
                                            text = LocalXmlStrings.current.postListLoadMorePosts,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                }
                            }
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
                            Text(
                                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                                textAlign = TextAlign.Center,
                                text = LocalXmlStrings.current.messageEmptyList,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Spacing.xxl))
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
