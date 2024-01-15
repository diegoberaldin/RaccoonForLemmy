package com.github.diegoberaldin.raccoonforlemmy.unit.communitydetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ShareBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.keepscreenon.rememberKeepScreenOn
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.containsId
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.InstanceInfoScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.ModlogScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.ReportListScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class CommunityDetailScreen(
    private val communityId: Int,
    private val otherInstance: String = "",
) : Screen {

    override val key: ScreenKey
        get() = super.key + communityId.toString()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<CommunityDetailMviModel>(
            tag = communityId.toString(),
            parameters = { parametersOf(communityId, otherInstance) },
        )
        model.bindToLifecycle(key + communityId.toString())
        val uiState by model.uiState.collectAsState()
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = stringResource(MR.strings.message_generic_error)
        val successMessage = stringResource(MR.strings.message_operation_successful)
        val isOnOtherInstance = remember { otherInstance.isNotEmpty() }
        val otherInstanceName = remember { otherInstance }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val themeRepository = remember { getThemeRepository() }
        val upVoteColor by themeRepository.upVoteColor.collectAsState()
        val downVoteColor by themeRepository.downVoteColor.collectAsState()
        val replyColor by themeRepository.replyColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultReplyColor = MaterialTheme.colorScheme.secondary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val keepScreenOn = rememberKeepScreenOn()
        val detailOpener = remember { getDetailOpener() }

        LaunchedEffect(notificationCenter) {
            notificationCenter.resetCache()
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    is CommunityDetailMviModel.Effect.BlockError -> {
                        snackbarHostState.showSnackbar(effect.message ?: genericError)
                    }

                    CommunityDetailMviModel.Effect.BlockSuccess -> {
                        snackbarHostState.showSnackbar(successMessage)
                    }

                    CommunityDetailMviModel.Effect.BackToTop -> {
                        lazyListState.scrollToItem(0)
                        topAppBarState.heightOffset = 0f
                        topAppBarState.contentOffset = 0f
                    }

                    is CommunityDetailMviModel.Effect.ZombieModeTick -> {
                        if (effect.index >= 0) {
                            lazyListState.animateScrollBy(
                                value = settings.zombieModeScrollAmount,
                                animationSpec = tween(350),
                            )
                        }
                    }
                }
            }.launchIn(this)
        }
        LaunchedEffect(uiState.zombieModeActive) {
            if (uiState.zombieModeActive) {
                keepScreenOn.activate()
            } else {
                keepScreenOn.deactivate()
            }
        }

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
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
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = uiState.community.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        // subscribe button
                        if (!isOnOtherInstance && uiState.isLogged) {
                            Image(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        when (uiState.community.subscribed) {
                                            true -> model.reduce(CommunityDetailMviModel.Intent.Unsubscribe)
                                            false -> model.reduce(CommunityDetailMviModel.Intent.Subscribe)
                                            else -> Unit
                                        }
                                    },
                                ),
                                imageVector = when (uiState.community.subscribed) {
                                    true -> Icons.Outlined.CheckCircle
                                    false -> Icons.Outlined.AddCircleOutline
                                    else -> Icons.Outlined.Pending
                                },
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onBackground),
                            )
                            Spacer(Modifier.width(Spacing.m))
                        }

                        // sort button
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
                            imageVector = uiState.sortType.toIcon(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )

                        // options menu
                        Box {
                            val options = buildList {
                                this += Option(
                                    OptionId.Info, stringResource(MR.strings.community_detail_info)
                                )
                                this += Option(
                                    OptionId.InfoInstance,
                                    stringResource(MR.strings.community_detail_instance_info)
                                )
                                this += Option(
                                    OptionId.Block,
                                    stringResource(MR.strings.community_detail_block)
                                )
                                this += Option(
                                    OptionId.BlockInstance,
                                    stringResource(MR.strings.community_detail_block_instance)
                                )
                                if (uiState.currentUserId != null && otherInstanceName.isEmpty()) {
                                    this += Option(
                                        OptionId.Favorite,
                                        if (uiState.community.favorite) {
                                            stringResource(MR.strings.community_action_remove_favorite)
                                        } else {
                                            stringResource(MR.strings.community_action_add_favorite)
                                        },
                                    )
                                }

                                this += Option(
                                    OptionId.ViewModlog,
                                    stringResource(MR.strings.community_action_view_modlog),
                                )

                                if (uiState.moderators.containsId(uiState.currentUserId)) {
                                    this += Option(
                                        OptionId.OpenReports,
                                        stringResource(MR.strings.mod_action_open_reports)
                                    )
                                }
                            }
                            var optionsExpanded by remember { mutableStateOf(false) }
                            var optionsOffset by remember { mutableStateOf(Offset.Zero) }
                            Image(
                                modifier = Modifier.onGloballyPositioned {
                                    optionsOffset = it.positionInParent()
                                }.padding(start = Spacing.s).onClick(
                                    onClick = rememberCallback {
                                        optionsExpanded = true
                                    },
                                ),
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                            CustomDropDown(
                                expanded = optionsExpanded,
                                onDismiss = {
                                    optionsExpanded = false
                                },
                                offset = DpOffset(
                                    x = optionsOffset.x.toLocalDp(),
                                    y = optionsOffset.y.toLocalDp(),
                                ),
                            ) {
                                options.forEach { option ->
                                    Text(
                                        modifier = Modifier.padding(
                                            horizontal = Spacing.m,
                                            vertical = Spacing.s,
                                        ).onClick(
                                            onClick = rememberCallback {
                                                optionsExpanded = false
                                                when (option.id) {
                                                    OptionId.BlockInstance -> model.reduce(
                                                        CommunityDetailMviModel.Intent.BlockInstance
                                                    )

                                                    OptionId.Block -> model.reduce(
                                                        CommunityDetailMviModel.Intent.Block
                                                    )

                                                    OptionId.InfoInstance -> {
                                                        navigationCoordinator.pushScreen(
                                                            InstanceInfoScreen(
                                                                url = uiState.community.instanceUrl,
                                                            ),
                                                        )
                                                    }

                                                    OptionId.Info -> {
                                                        navigationCoordinator.showBottomSheet(
                                                            CommunityInfoScreen(uiState.community.id),
                                                        )
                                                    }

                                                    OptionId.OpenReports -> {
                                                        val screen = ReportListScreen(
                                                            communityId = uiState.community.id
                                                        )
                                                        navigationCoordinator.pushScreen(screen)
                                                    }

                                                    OptionId.Favorite -> {
                                                        model.reduce(
                                                            CommunityDetailMviModel.Intent.ToggleFavorite,
                                                        )
                                                    }

                                                    OptionId.ViewModlog -> {
                                                        val screen = ModlogScreen(
                                                            communityId = uiState.community.id,
                                                        )
                                                        navigationCoordinator.pushScreen(screen)
                                                    }

                                                    else -> Unit
                                                }
                                            },
                                        ),
                                        text = option.text,
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        if (navigationCoordinator.canPop.value) {
                            Image(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        navigationCoordinator.popScreen()
                                    },
                                ),
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
                            if (uiState.zombieModeActive) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.SyncDisabled,
                                    text = stringResource(MR.strings.action_deactivate_zombie_mode),
                                    onSelected = rememberCallback(model) {
                                        model.reduce(CommunityDetailMviModel.Intent.PauseZombieMode)
                                    },
                                )
                            } else {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.Sync,
                                    text = stringResource(MR.strings.action_activate_zombie_mode),
                                    onSelected = rememberCallback(model) {
                                        model.reduce(
                                            CommunityDetailMviModel.Intent.StartZombieMode(-1)
                                        )
                                    },
                                )
                            }
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
                            if (uiState.isLogged && !isOnOtherInstance) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.ClearAll,
                                    text = stringResource(MR.strings.action_clear_read),
                                    onSelected = rememberCallback {
                                        model.reduce(CommunityDetailMviModel.Intent.ClearRead)
                                        scope.launch {
                                            lazyListState.scrollToItem(0)
                                            topAppBarState.heightOffset = 0f
                                            topAppBarState.contentOffset = 0f
                                        }
                                    },
                                )
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.Create,
                                    text = stringResource(MR.strings.action_create_post),
                                    onSelected = rememberCallback {
                                        detailOpener.openCreatePost(
                                            communityId = uiState.community.id,
                                        )
                                    },
                                )
                            }
                        },
                    )
                }
            },
        ) { padding ->
            if (uiState.currentUserId != null) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh = rememberCallback(model) {
                        model.reduce(CommunityDetailMviModel.Intent.Refresh)
                    },
                )
                Box(
                    modifier = Modifier
                        .padding(padding)
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
                        userScrollEnabled = !uiState.zombieModeActive,
                    ) {
                        item {
                            Column {
                                CommunityHeader(
                                    community = uiState.community,
                                    autoLoadImages = uiState.autoLoadImages,
                                    onOpenImage = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(ZoomableImageScreen(url))
                                    },
                                )
                                Spacer(modifier = Modifier.height(Spacing.m))
                            }
                        }
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
                            items = uiState.posts,
                            key = { it.id.toString() + (it.updateDate ?: it.publishDate) },
                        ) { post ->
                            LaunchedEffect(post.id) {
                                if (settings.markAsReadWhileScrolling && !post.read) {
                                    model.reduce(CommunityDetailMviModel.Intent.MarkAsRead(post.id))
                                }
                            }
                            SwipeableCard(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.swipeActionsEnabled,
                                directions = if (!uiState.isLogged || isOnOtherInstance) {
                                    emptySet()
                                } else {
                                    setOf(
                                        DismissDirection.StartToEnd,
                                        DismissDirection.EndToStart,
                                    )
                                },
                                enableSecondAction = rememberCallbackArgs { value ->
                                    if (!uiState.isLogged) {
                                        false
                                    } else {
                                        value == DismissValue.DismissedToStart
                                    }
                                },
                                backgroundColor = rememberCallbackArgs { direction ->
                                    when (direction) {
                                        DismissValue.DismissedToStart -> upVoteColor
                                            ?: defaultUpvoteColor

                                        DismissValue.DismissedToEnd -> downVoteColor
                                            ?: defaultDownVoteColor

                                        else -> Color.Transparent
                                    }
                                },
                                secondBackgroundColor = rememberCallbackArgs { direction ->
                                    when (direction) {
                                        DismissValue.DismissedToStart -> replyColor
                                            ?: defaultReplyColor

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
                                secondSwipeContent = { direction ->
                                    val icon = when (direction) {
                                        DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                        DismissDirection.EndToStart -> Icons.Default.Reply
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                    )
                                },
                                onGestureBegin = rememberCallback(model) {
                                    model.reduce(CommunityDetailMviModel.Intent.HapticIndication)
                                },
                                onDismissToStart = rememberCallback(model) {
                                    model.reduce(
                                        CommunityDetailMviModel.Intent.UpVotePost(post.id),
                                    )
                                },
                                onSecondDismissToStart = rememberCallback(model) {
                                    detailOpener.openReply(
                                        originalPost = post,
                                    )
                                },
                                onDismissToEnd = rememberCallback(model) {
                                    model.reduce(
                                        CommunityDetailMviModel.Intent.DownVotePost(post.id),
                                    )
                                },
                                content = {
                                    PostCard(
                                        post = post,
                                        isFromModerator = uiState.moderators.containsId(post.creator?.id),
                                        postLayout = uiState.postLayout,
                                        limitBodyHeight = true,
                                        fullHeightImage = uiState.fullHeightImages,
                                        voteFormat = uiState.voteFormat,
                                        autoLoadImages = uiState.autoLoadImages,
                                        actionButtonsActive = uiState.isLogged,
                                        blurNsfw = when {
                                            uiState.community.nsfw -> false
                                            else -> uiState.blurNsfw
                                        },
                                        onClick = rememberCallback(model) {
                                            model.reduce(
                                                CommunityDetailMviModel.Intent.MarkAsRead(
                                                    post.id
                                                )
                                            )
                                            detailOpener.openPostDetail(
                                                post = post,
                                                otherInstance = otherInstanceName,
                                                isMod = uiState.moderators.containsId(uiState.currentUserId),
                                            )
                                        },
                                        onDoubleClick = if (!uiState.doubleTapActionEnabled || !uiState.isLogged || isOnOtherInstance) {
                                            null
                                        } else {
                                            rememberCallback(model) {
                                                model.reduce(
                                                    CommunityDetailMviModel.Intent.UpVotePost(
                                                        id = post.id,
                                                        feedback = true,
                                                    ),
                                                )
                                            }
                                        },
                                        onOpenCreator = rememberCallbackArgs { user, _ ->
                                            detailOpener.openUserDetail(user, otherInstanceName)
                                        },
                                        onOpenPost = rememberCallbackArgs { p, instance ->
                                            detailOpener.openPostDetail(p, instance)
                                        },
                                        onOpenWeb = rememberCallbackArgs { url ->
                                            navigationCoordinator.pushScreen(
                                                WebViewScreen(url)
                                            )
                                        },
                                        onUpVote = rememberCallback(model) {
                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                model.reduce(
                                                    CommunityDetailMviModel.Intent.UpVotePost(
                                                        id = post.id,
                                                    ),
                                                )
                                            }
                                        },
                                        onDownVote = rememberCallback(model) {
                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                model.reduce(
                                                    CommunityDetailMviModel.Intent.DownVotePost(
                                                        id = post.id,
                                                    ),
                                                )
                                            }
                                        },
                                        onSave = rememberCallback(model) {
                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                model.reduce(
                                                    CommunityDetailMviModel.Intent.SavePost(
                                                        id = post.id,
                                                    ),
                                                )
                                            }
                                        },
                                        onReply = rememberCallback {
                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                model.reduce(
                                                    CommunityDetailMviModel.Intent.MarkAsRead(
                                                        post.id
                                                    )
                                                )
                                                detailOpener.openPostDetail(post)
                                            }
                                        },
                                        onOpenImage = rememberCallbackArgs(model) { url ->
                                            model.reduce(
                                                CommunityDetailMviModel.Intent.MarkAsRead(
                                                    post.id
                                                )
                                            )
                                            navigationCoordinator.pushScreen(
                                                ZoomableImageScreen(url),
                                            )
                                        },
                                        options = buildList {
                                            this += Option(
                                                OptionId.Share,
                                                stringResource(MR.strings.post_action_share),
                                            )
                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                this += Option(
                                                    OptionId.Hide,
                                                    stringResource(MR.strings.post_action_hide),
                                                )
                                            }
                                            this += Option(
                                                OptionId.SeeRaw,
                                                stringResource(MR.strings.post_action_see_raw),
                                            )
                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                this += Option(
                                                    OptionId.CrossPost,
                                                    stringResource(MR.strings.post_action_cross_post)
                                                )
                                                this += Option(
                                                    OptionId.Report,
                                                    stringResource(MR.strings.post_action_report),
                                                )
                                            }
                                            if (post.creator?.id == uiState.currentUserId && !isOnOtherInstance) {
                                                this += Option(
                                                    OptionId.Edit,
                                                    stringResource(MR.strings.post_action_edit),
                                                )
                                                this += Option(
                                                    OptionId.Delete,
                                                    stringResource(MR.strings.comment_action_delete),
                                                )
                                            }
                                            if (uiState.moderators.containsId(uiState.currentUserId)) {
                                                this += Option(
                                                    OptionId.FeaturePost,
                                                    if (post.featuredCommunity) {
                                                        stringResource(MR.strings.mod_action_unmark_as_featured)
                                                    } else {
                                                        stringResource(MR.strings.mod_action_mark_as_featured)
                                                    },
                                                )
                                                this += Option(
                                                    OptionId.LockPost,
                                                    if (post.locked) {
                                                        stringResource(MR.strings.mod_action_unlock)
                                                    } else {
                                                        stringResource(MR.strings.mod_action_lock)
                                                    },
                                                )
                                                this += Option(
                                                    OptionId.Remove,
                                                    stringResource(MR.strings.mod_action_remove),
                                                )
                                                this += Option(
                                                    OptionId.BanUser,
                                                    if (post.creator?.banned == true) {
                                                        stringResource(MR.strings.mod_action_allow)
                                                    } else {
                                                        stringResource(MR.strings.mod_action_ban)
                                                    },
                                                )
                                                post.creator?.id?.also { creatorId ->
                                                    if (uiState.currentUserId != creatorId) {
                                                        this += Option(
                                                            OptionId.AddMod,
                                                            if (uiState.moderators.containsId(
                                                                    creatorId
                                                                )
                                                            ) {
                                                                stringResource(MR.strings.mod_action_remove_mod)
                                                            } else {
                                                                stringResource(MR.strings.mod_action_add_mod)
                                                            },
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                            when (optionId) {
                                                OptionId.Delete -> model.reduce(
                                                    CommunityDetailMviModel.Intent.DeletePost(post.id)
                                                )

                                                OptionId.Edit -> {
                                                    detailOpener.openCreatePost(editedPost = post)
                                                }

                                                OptionId.Report -> {
                                                    navigationCoordinator.showBottomSheet(
                                                        CreateReportScreen(postId = post.id)
                                                    )
                                                }

                                                OptionId.CrossPost -> {
                                                    detailOpener.openCreatePost(crossPost = post)
                                                }

                                                OptionId.SeeRaw -> {
                                                    rawContent = post
                                                }

                                                OptionId.Hide -> model.reduce(
                                                    CommunityDetailMviModel.Intent.Hide(post.id)
                                                )

                                                OptionId.Share -> {
                                                    val urls = listOfNotNull(
                                                        post.originalUrl,
                                                        "https://${uiState.instance}/post/${post.id}"
                                                    ).distinct()
                                                    if (urls.size == 1) {
                                                        model.reduce(
                                                            CommunityDetailMviModel.Intent.Share(
                                                                urls.first()
                                                            )
                                                        )
                                                    } else {
                                                        val screen = ShareBottomSheet(urls = urls)
                                                        navigationCoordinator.showBottomSheet(screen)
                                                    }
                                                }

                                                OptionId.FeaturePost -> model.reduce(
                                                    CommunityDetailMviModel.Intent.ModFeaturePost(
                                                        post.id
                                                    )
                                                )

                                                OptionId.LockPost -> model.reduce(
                                                    CommunityDetailMviModel.Intent.ModLockPost(post.id)
                                                )

                                                OptionId.Remove -> {
                                                    val screen = RemoveScreen(postId = post.id)
                                                    navigationCoordinator.showBottomSheet(screen)
                                                }

                                                OptionId.BanUser -> {
                                                    post.creator?.id?.also { userId ->
                                                        val screen = BanUserScreen(
                                                            userId = userId,
                                                            communityId = uiState.community.id,
                                                            newValue = post.creator?.banned != true,
                                                            postId = post.id,
                                                        )
                                                        navigationCoordinator.showBottomSheet(screen)
                                                    }
                                                }

                                                OptionId.AddMod -> {
                                                    post.creator?.id?.also { userId ->
                                                        model.reduce(
                                                            CommunityDetailMviModel.Intent.ModToggleModUser(
                                                                userId
                                                            )
                                                        )
                                                    }
                                                }

                                                else -> Unit
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
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                if (settings.infiniteScrollEnabled) {
                                    model.reduce(CommunityDetailMviModel.Intent.LoadNextPage)
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Button(
                                            onClick = rememberCallback(model) {
                                                model.reduce(CommunityDetailMviModel.Intent.LoadNextPage)
                                            },
                                        ) {
                                            Text(
                                                text = stringResource(MR.strings.post_list_load_more_posts),
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

                    if (uiState.asyncInProgress) {
                        ProgressHud()
                    }
                }
            }
        }

        if (rawContent != null) {
            when (val content = rawContent) {
                is PostModel -> {
                    RawContentDialog(
                        title = content.title,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        url = content.url,
                        text = content.text,
                        onDismiss = rememberCallback {
                            rawContent = null
                        },
                        onQuote = rememberCallbackArgs { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                detailOpener.openReply(
                                    originalPost = content,
                                    initialText = buildString {
                                        append("> ")
                                        append(quotation)
                                        append("\n\n")
                                    },
                                )
                            }
                        },
                    )
                }

                is CommentModel -> {
                    RawContentDialog(
                        text = content.text,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        onDismiss = rememberCallback {
                            rawContent = null
                        },
                        onQuote = rememberCallbackArgs { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                detailOpener.openReply(
                                    originalComment = content,
                                    initialText = buildString {
                                        append("> ")
                                        append(quotation)
                                        append("\n\n")
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
