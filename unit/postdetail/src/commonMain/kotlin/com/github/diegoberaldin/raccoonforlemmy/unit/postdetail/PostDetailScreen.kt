package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeActionCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CollapsedCommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommentCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.CopyPostBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ShareBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalDp
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.containsId
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableName
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.unit.ban.BanUserScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

class PostDetailScreen(
    private val postId: Long,
    private val otherInstance: String = "",
    private val highlightCommentId: Long? = null,
    private val isMod: Boolean = false,
) : Screen {
    override val key: ScreenKey
        get() = super.key + postId.toString()

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterialApi::class,
        ExperimentalLayoutApi::class,
    )
    @Composable
    override fun Content() {
        val model = getScreenModel<PostDetailMviModel>(
            tag = postId.toString() + highlightCommentId.toString(),
            parameters = {
                parametersOf(
                    postId,
                    otherInstance,
                    highlightCommentId,
                    isMod,
                )
            })
        val uiState by model.uiState.collectAsState()
        val isOnOtherInstance = remember { otherInstance.isNotEmpty() }
        val otherInstanceName = remember { otherInstance }
        val commentIdToHighlight = remember { highlightCommentId }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }
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
        val scope = rememberCoroutineScope()
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val detailOpener = remember { getDetailOpener() }
        val clipboardManager = LocalClipboardManager.current
        val focusManager = LocalFocusManager.current
        val keyboardScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    focusManager.clearFocus()
                    return Offset.Zero
                }
            }
        }
        var postToDelete by remember { mutableStateOf<Unit?>(null) }
        var commentIdToDelete by remember { mutableStateOf<Long?>(null) }
        val statusBarInset = with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(this)
        }
        val bottomNavigationInsetPx = with(LocalDensity.current) {
            WindowInsets.navigationBars.getBottom(this)
        }
        var bottomBarHeightPx by remember { mutableStateOf(0f) }
        var bottomBarOffsetHeightPx by remember { mutableStateOf(0f) }
        val bottomNavigationInset = with(LocalDensity.current) {
            bottomNavigationInsetPx.toDp()
        }
        val buttonBarScrollConnection = remember {
            object : NestedScrollConnection {

                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newOffset =
                        (bottomBarOffsetHeightPx + delta).coerceIn(
                            -(bottomBarHeightPx + bottomNavigationInsetPx),
                            0f,
                        )
                    bottomBarOffsetHeightPx = newOffset
                    return Offset.Zero
                }
            }
        }

        LaunchedEffect(notificationCenter) {
            notificationCenter.resetCache()
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    PostDetailMviModel.Effect.Close -> {
                        navigationCoordinator.popScreen()
                    }

                    is PostDetailMviModel.Effect.ScrollToComment -> {
                        lazyListState.scrollToItem(effect.index)
                    }

                    PostDetailMviModel.Effect.BackToTop -> {
                        scope.launch {
                            lazyListState.scrollToItem(0)
                        }
                    }

                    is PostDetailMviModel.Effect.TriggerCopy -> {
                        clipboardManager.setText(AnnotatedString(text = effect.text))
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                val maxTopInset = Dimensions.topBarHeight.toLocalPixel()
                var topInset by remember { mutableStateOf(maxTopInset) }
                snapshotFlow { topAppBarState.collapsedFraction }.onEach {
                    topInset = (maxTopInset * (1 - it)).let { insetValue ->
                        if (uiState.searching) {
                            insetValue.coerceAtLeast(statusBarInset.toFloat())
                        } else {
                            insetValue
                        }
                    }
                }.launchIn(scope)

                TopAppBar(
                    windowInsets = if (settings.edgeToEdge) {
                        WindowInsets(0, topInset.roundToInt(), 0, 0)
                    } else {
                        TopAppBarDefaults.windowInsets
                    },
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = uiState.post.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    actions = {
                        Image(
                            modifier = Modifier
                                .padding(horizontal = Spacing.xs)
                                .onClick(
                                    onClick = rememberCallback {
                                        val sheet = SortBottomSheet(
                                            comments = true,
                                            values = uiState.availableSortTypes.map { it.toInt() },
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
                                    OptionId.Search,
                                    if (uiState.searching) {
                                        LocalXmlStrings.current.actionExitSearch
                                    } else {
                                        buildString {
                                            append(LocalXmlStrings.current.actionSearchInComments)
                                        }
                                    },
                                )
                            }
                            var optionsExpanded by remember { mutableStateOf(false) }
                            var optionsOffset by remember { mutableStateOf(Offset.Zero) }
                            Image(
                                modifier = Modifier.onGloballyPositioned {
                                    optionsOffset = it.positionInParent()
                                }.onClick(
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
                                    DropdownMenuItem(
                                        text = {
                                            Text(option.text)
                                        },
                                        onClick = rememberCallback {
                                            optionsExpanded = false
                                            when (option.id) {
                                                OptionId.Search -> {
                                                    model.reduce(PostDetailMviModel.Intent.ChangeSearching(!uiState.searching))
                                                }

                                                else -> Unit
                                            }
                                        },
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
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        modifier = Modifier.then(
                            if (uiState.enableButtonsToScrollBetweenComments) {
                                Modifier.padding(
                                    bottom = Spacing.s + bottomNavigationInset,
                                )
                            } else {
                                Modifier
                            }
                        ),
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
                            if (uiState.isLogged && !isOnOtherInstance) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.AutoMirrored.Filled.Reply,
                                    text = LocalXmlStrings.current.actionReply,
                                    onSelected = rememberCallback {
                                        detailOpener.openReply(
                                            originalPost = uiState.post,
                                        )
                                    },
                                )
                            }
                        },
                    )
                }
            },
        ) { padding ->
            if (uiState.currentUserId == null) {
                return@Scaffold
            }
            Box {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .then(
                            if (uiState.enableButtonsToScrollBetweenComments) {
                                Modifier.nestedScroll(buttonBarScrollConnection)
                            } else {
                                Modifier
                            }
                        ),
                ) {
                    if (uiState.searching) {
                        TextField(
                            modifier = Modifier
                                .padding(
                                    horizontal = Spacing.xs,
                                    vertical = Spacing.s,
                                ).fillMaxWidth(),
                            label = {
                                Text(text = LocalXmlStrings.current.exploreSearchPlaceholder)
                            },
                            singleLine = true,
                            value = uiState.searchText,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Search,
                            ),
                            onValueChange = { value ->
                                model.reduce(PostDetailMviModel.Intent.SetSearch(value))
                            },
                            trailingIcon = {
                                Icon(
                                    modifier = Modifier.onClick(
                                        onClick = rememberCallback {
                                            if (uiState.searchText.isNotEmpty()) {
                                                model.reduce(PostDetailMviModel.Intent.SetSearch(""))
                                            }
                                        },
                                    ),
                                    imageVector = if (uiState.searchText.isEmpty()) {
                                        Icons.Default.Search
                                    } else {
                                        Icons.Default.Clear
                                    },
                                    contentDescription = null,
                                )
                            },
                        )
                    }
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = uiState.refreshing,
                        onRefresh = rememberCallback(model) {
                            model.reduce(PostDetailMviModel.Intent.Refresh)
                        },
                    )

                    Box(
                        modifier = Modifier
                            .then(
                                if (settings.hideNavigationBarWhileScrolling) {
                                    Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                                } else {
                                    Modifier
                                }
                            )
                            .nestedScroll(fabNestedScrollConnection)
                            .nestedScroll(keyboardScrollConnection)
                            .pullRefresh(pullRefreshState),
                    ) {
                        LazyColumn(
                            state = lazyListState
                        ) {
                            item {
                                PostCard(
                                    post = uiState.post,
                                    isFromModerator = uiState.post.creator?.id.let { creatorId ->
                                        uiState.isModerator && uiState.moderators.containsId(creatorId)
                                    },
                                    postLayout = if (uiState.postLayout == PostLayout.Card) {
                                        uiState.postLayout
                                    } else {
                                        PostLayout.Full
                                    },
                                    fullHeightImage = uiState.fullHeightImages,
                                    includeFullBody = true,
                                    voteFormat = uiState.voteFormat,
                                    autoLoadImages = uiState.autoLoadImages,
                                    preferNicknames = uiState.preferNicknames,
                                    showScores = uiState.showScores,
                                    actionButtonsActive = uiState.isLogged,
                                    blurNsfw = false,
                                    onOpenCommunity = rememberCallbackArgs { community, instance ->
                                        detailOpener.openCommunityDetail(community, instance)
                                    },
                                    onOpenCreator = rememberCallbackArgs { user, instance ->
                                        detailOpener.openUserDetail(user, instance)
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
                                                PostDetailMviModel.Intent.UpVotePost(),
                                            )
                                        }
                                    },
                                    onDownVote = rememberCallback(model) {
                                        if (uiState.isLogged && !isOnOtherInstance) {
                                            model.reduce(
                                                PostDetailMviModel.Intent.DownVotePost(),
                                            )
                                        }
                                    },
                                    onSave = rememberCallback(model) {
                                        model.reduce(
                                            PostDetailMviModel.Intent.SavePost(
                                                post = uiState.post,
                                            ),
                                        )
                                    },
                                    onReply = rememberCallback {
                                        if (uiState.isLogged && !isOnOtherInstance) {
                                            detailOpener.openReply(
                                                originalPost = uiState.post,
                                            )
                                        }
                                    },
                                    options = buildList {
                                        this += Option(
                                            OptionId.Share,
                                            LocalXmlStrings.current.postActionShare,
                                        )
                                        this += Option(
                                            OptionId.Copy,
                                            LocalXmlStrings.current.actionCopyClipboard,
                                        )
                                        this += Option(
                                            OptionId.SeeRaw,
                                            LocalXmlStrings.current.postActionSeeRaw,
                                        )
                                        if (uiState.isLogged && !isOnOtherInstance) {
                                            this += Option(
                                                OptionId.CrossPost,
                                                LocalXmlStrings.current.postActionCrossPost,
                                            )
                                            this += Option(
                                                OptionId.Report,
                                                LocalXmlStrings.current.postActionReport,
                                            )
                                        }
                                        if (uiState.post.creator?.id == uiState.currentUserId && !isOnOtherInstance) {
                                            this += Option(
                                                OptionId.Edit,
                                                LocalXmlStrings.current.postActionEdit,
                                            )
                                            this += Option(
                                                OptionId.Delete,
                                                LocalXmlStrings.current.commentActionDelete,
                                            )
                                        }
                                        if (uiState.isModerator) {
                                            this += Option(
                                                OptionId.FeaturePost,
                                                if (uiState.post.featuredCommunity) {
                                                    LocalXmlStrings.current.modActionUnmarkAsFeatured
                                                } else {
                                                    LocalXmlStrings.current.modActionMarkAsFeatured
                                                },
                                            )
                                            this += Option(
                                                OptionId.LockPost,
                                                if (uiState.post.locked) {
                                                    LocalXmlStrings.current.modActionUnlock
                                                } else {
                                                    LocalXmlStrings.current.modActionLock
                                                },
                                            )
                                            this += Option(
                                                OptionId.Remove,
                                                LocalXmlStrings.current.modActionRemove,
                                            )
                                            this += Option(
                                                OptionId.BanUser,
                                                if (uiState.post.creator?.banned == true) {
                                                    LocalXmlStrings.current.modActionAllow
                                                } else {
                                                    LocalXmlStrings.current.modActionBan
                                                },
                                            )
                                            uiState.post.creator?.id?.also { creatorId ->
                                                if (uiState.currentUserId != creatorId) {
                                                    this += Option(
                                                        OptionId.AddMod,
                                                        if (uiState.moderators.containsId(creatorId)) {
                                                            LocalXmlStrings.current.modActionRemoveMod
                                                        } else {
                                                            LocalXmlStrings.current.modActionAddMod
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onOptionSelected = rememberCallbackArgs(model) { idx ->
                                        when (idx) {
                                            OptionId.Delete -> {
                                                postToDelete = Unit
                                            }

                                            OptionId.Edit -> {
                                                detailOpener.openCreatePost(editedPost = uiState.post)
                                            }

                                            OptionId.Report -> {
                                                navigationCoordinator.pushScreen(
                                                    CreateReportScreen(postId = uiState.post.id),
                                                )
                                            }

                                            OptionId.CrossPost -> {
                                                detailOpener.openCreatePost(
                                                    crossPost = uiState.post,
                                                    forceCommunitySelection = true,
                                                )
                                            }

                                            OptionId.SeeRaw -> {
                                                rawContent = uiState.post
                                            }

                                            OptionId.Share -> {
                                                val urls = listOfNotNull(
                                                    uiState.post.originalUrl,
                                                    "https://${uiState.instance}/post/${uiState.post.id}"
                                                ).distinct()
                                                if (urls.size == 1) {
                                                    model.reduce(PostDetailMviModel.Intent.Share(urls.first()))
                                                } else {
                                                    val screen = ShareBottomSheet(urls = urls)
                                                    navigationCoordinator.showBottomSheet(screen)
                                                }
                                            }

                                            OptionId.FeaturePost -> model.reduce(
                                                PostDetailMviModel.Intent.ModFeaturePost,
                                            )

                                            OptionId.LockPost -> model.reduce(
                                                PostDetailMviModel.Intent.ModLockPost,
                                            )

                                            OptionId.Remove -> {
                                                val screen = RemoveScreen(postId = uiState.post.id)
                                                navigationCoordinator.pushScreen(screen)
                                            }

                                            OptionId.BanUser -> {
                                                uiState.post.creator?.id?.also { userId ->
                                                    val screen = BanUserScreen(
                                                        userId = userId,
                                                        communityId = uiState.post.community?.id ?: 0,
                                                        newValue = uiState.post.creator?.banned != true,
                                                        postId = uiState.post.id,
                                                    )
                                                    navigationCoordinator.pushScreen(screen)
                                                }
                                            }

                                            OptionId.AddMod -> {
                                                uiState.post.creator?.id?.also { userId ->
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.ModToggleModUser(
                                                            userId
                                                        )
                                                    )
                                                }
                                            }

                                            OptionId.Copy -> {
                                                val texts = listOfNotNull(
                                                    uiState.post.title.takeIf { it.isNotBlank() },
                                                    uiState.post.text.takeIf { it.isNotBlank() },
                                                ).distinct()
                                                if (texts.size == 1) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.Copy(texts.first())
                                                    )
                                                } else {
                                                    val screen =
                                                        CopyPostBottomSheet(uiState.post.title, uiState.post.text)
                                                    navigationCoordinator.showBottomSheet(screen)
                                                }
                                            }

                                            else -> Unit
                                        }
                                    },
                                    onOpenImage = rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(
                                            ZoomableImageScreen(
                                                url = url,
                                                source = uiState.post.community?.readableHandle.orEmpty(),
                                            ),
                                        )
                                    },
                                )
                                if (uiState.postLayout != PostLayout.Card) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.interItem))
                                }
                            }
                            if (uiState.post.crossPosts.isNotEmpty()) {
                                item {
                                    FlowRow(
                                        modifier = Modifier.padding(
                                            top = Spacing.xxs,
                                            bottom = Spacing.s,
                                            start = Spacing.s,
                                            end = Spacing.s,
                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                                    ) {
                                        Text(
                                            text = LocalXmlStrings.current.postDetailCrossPosts,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        uiState.post.crossPosts.forEachIndexed { index, crossPost ->
                                            val community = crossPost.community
                                            if (community != null) {
                                                val string = buildAnnotatedString {
                                                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                                        append(community.readableName(uiState.preferNicknames))
                                                    }
                                                    if (index < uiState.post.crossPosts.lastIndex) {
                                                        append(", ")
                                                    }
                                                }
                                                Text(
                                                    modifier = Modifier.onClick(
                                                        onClick = rememberCallback {
                                                            val post = PostModel(
                                                                id = crossPost.id,
                                                                community = community,
                                                            )
                                                            detailOpener.openPostDetail(
                                                                post = post,
                                                                otherInstance = otherInstanceName
                                                            )
                                                        },
                                                    ),
                                                    text = string,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (uiState.comments.isEmpty() && uiState.loading && uiState.initial) {
                                items(5) {
                                    CommentCardPlaceholder()
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                                        thickness = 0.25.dp
                                    )
                                }
                            }
                            items(
                                items = uiState.comments.filter { it.visible },
                                key = { c -> c.id.toString() + (c.updateDate ?: c.publishDate) },
                            ) { comment ->
                                Column {
                                    AnimatedContent(
                                        targetState = comment.expanded,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(250)).togetherWith(fadeOut())
                                        },
                                    ) {

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
                                                        backgroundColor = upVoteColor
                                                            ?: defaultUpvoteColor,
                                                        onTriggered = rememberCallback {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.UpVoteComment(
                                                                    comment.id,
                                                                )
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
                                                        backgroundColor = downVoteColor
                                                            ?: defaultDownVoteColor,
                                                        onTriggered = rememberCallback {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.DownVoteComment(
                                                                    comment.id
                                                                ),
                                                            )
                                                        },
                                                    )

                                                    ActionOnSwipe.Reply -> SwipeAction(
                                                        swipeContent = {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                            )
                                                        },
                                                        backgroundColor = replyColor
                                                            ?: defaultReplyColor,
                                                        onTriggered = rememberCallback {
                                                            detailOpener.openReply(
                                                                originalPost = uiState.post,
                                                                originalComment = comment,
                                                            )
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
                                                        backgroundColor = saveColor
                                                            ?: defaultSaveColor,
                                                        onTriggered = rememberCallback {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.SaveComment(
                                                                    commentId = comment.id,
                                                                ),
                                                            )
                                                        },
                                                    )


                                                    else -> null
                                                }
                                            }

                                        if (comment.expanded) {
                                            SwipeActionCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = uiState.swipeActionsEnabled,
                                                onGestureBegin = rememberCallback(model) {
                                                    model.reduce(PostDetailMviModel.Intent.HapticIndication)
                                                },
                                                swipeToStartActions = if (uiState.isLogged && !isOnOtherInstance) {
                                                    uiState.actionsOnSwipeToStartComments.toSwipeActions()
                                                } else {
                                                    emptyList()
                                                },
                                                swipeToEndActions = if (uiState.isLogged && !isOnOtherInstance) {
                                                    uiState.actionsOnSwipeToEndComments.toSwipeActions()
                                                } else {
                                                    emptyList()
                                                },
                                                content = {
                                                    CommentCard(
                                                        modifier = Modifier
                                                            .background(MaterialTheme.colorScheme.background)
                                                            .then(
                                                                if (comment.id == commentIdToHighlight) {
                                                                    Modifier.background(
                                                                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                                            5.dp
                                                                        ).copy(alpha = 0.75f)
                                                                    )
                                                                } else {
                                                                    Modifier
                                                                }
                                                            ),
                                                        comment = comment,
                                                        isOp = comment.creator?.id == uiState.post.creator?.id,
                                                        voteFormat = uiState.voteFormat,
                                                        autoLoadImages = uiState.autoLoadImages,
                                                        preferNicknames = uiState.preferNicknames,
                                                        showScores = uiState.showScores,
                                                        actionButtonsActive = uiState.isLogged,
                                                        onToggleExpanded = rememberCallback(model) {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.ToggleExpandComment(
                                                                    comment.id
                                                                )
                                                            )
                                                        },
                                                        onClick = rememberCallback(model) {
                                                            model.reduce(
                                                                PostDetailMviModel.Intent.ToggleExpandComment(
                                                                    comment.id
                                                                )
                                                            )
                                                        },
                                                        onDoubleClick = if (!uiState.doubleTapActionEnabled) {
                                                            null
                                                        } else {
                                                            rememberCallback(model) {
                                                                model.reduce(
                                                                    PostDetailMviModel.Intent.UpVoteComment(
                                                                        commentId = comment.id,
                                                                        feedback = true,
                                                                    ),
                                                                )
                                                            }
                                                        },
                                                        onUpVote = rememberCallback(model) {
                                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                                model.reduce(
                                                                    PostDetailMviModel.Intent.UpVoteComment(
                                                                        commentId = comment.id,
                                                                    ),
                                                                )
                                                            }
                                                        },
                                                        onDownVote = rememberCallback(model) {
                                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                                model.reduce(
                                                                    PostDetailMviModel.Intent.DownVoteComment(
                                                                        commentId = comment.id,
                                                                    ),
                                                                )
                                                            }
                                                        },
                                                        onSave = rememberCallback(model) {
                                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                                model.reduce(
                                                                    PostDetailMviModel.Intent.SaveComment(
                                                                        commentId = comment.id,
                                                                    ),
                                                                )
                                                            }
                                                        },
                                                        onReply = rememberCallback {
                                                            if (uiState.isLogged && !isOnOtherInstance) {
                                                                detailOpener.openReply(
                                                                    originalPost = uiState.post,
                                                                    originalComment = comment,
                                                                )
                                                            }
                                                        },
                                                        onOpenCreator = rememberCallbackArgs { user, instance ->
                                                            detailOpener.openUserDetail(user, instance)
                                                        },
                                                        onOpenCommunity = rememberCallbackArgs { community, instance ->
                                                            detailOpener.openCommunityDetail(
                                                                community,
                                                                instance,
                                                            )
                                                        },
                                                        onOpenPost = rememberCallbackArgs { p, instance ->
                                                            detailOpener.openPostDetail(p, instance)
                                                        },
                                                        onOpenWeb = rememberCallbackArgs { url ->
                                                            navigationCoordinator.pushScreen(WebViewScreen(url))
                                                        },
                                                        onImageClick = rememberCallbackArgs { url ->
                                                            navigationCoordinator.pushScreen(
                                                                ZoomableImageScreen(
                                                                    url = url,
                                                                    source = uiState.post.community?.readableHandle.orEmpty(),
                                                                )
                                                            )
                                                        },
                                                        options = buildList {
                                                            this += Option(
                                                                OptionId.SeeRaw,
                                                                LocalXmlStrings.current.postActionSeeRaw,
                                                            )
                                                            this += Option(
                                                                OptionId.Report,
                                                                LocalXmlStrings.current.postActionReport,
                                                            )
                                                            if (comment.creator?.id == uiState.currentUserId) {
                                                                this += Option(
                                                                    OptionId.Edit,
                                                                    LocalXmlStrings.current.postActionEdit,
                                                                )
                                                                this += Option(
                                                                    OptionId.Delete,
                                                                    LocalXmlStrings.current.commentActionDelete,
                                                                )
                                                            }
                                                            if (uiState.isModerator) {
                                                                this += Option(
                                                                    OptionId.DistinguishComment,
                                                                    if (comment.distinguished) {
                                                                        LocalXmlStrings.current.modActionUnmarkAsDistinguished
                                                                    } else {
                                                                        LocalXmlStrings.current.modActionMarkAsDistinguished
                                                                    },
                                                                )
                                                                this += Option(
                                                                    OptionId.Remove,
                                                                    LocalXmlStrings.current.modActionRemove,
                                                                )
                                                                this += Option(
                                                                    OptionId.BanUser,
                                                                    if (comment.creator?.banned == true) {
                                                                        LocalXmlStrings.current.modActionAllow
                                                                    } else {
                                                                        LocalXmlStrings.current.modActionBan
                                                                    },
                                                                )
                                                                comment.creator?.id?.also { creatorId ->
                                                                    if (uiState.currentUserId != creatorId) {
                                                                        this += Option(
                                                                            OptionId.AddMod,
                                                                            if (uiState.moderators.containsId(
                                                                                    creatorId
                                                                                )
                                                                            ) {
                                                                                LocalXmlStrings.current.modActionRemoveMod
                                                                            } else {
                                                                                LocalXmlStrings.current.modActionAddMod
                                                                            },
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                                            when (optionId) {
                                                                OptionId.Delete -> {
                                                                    commentIdToDelete = comment.id
                                                                }

                                                                OptionId.Edit -> {
                                                                    detailOpener.openReply(
                                                                        editedComment = comment,
                                                                    )
                                                                }

                                                                OptionId.Report -> {
                                                                    navigationCoordinator.pushScreen(
                                                                        CreateReportScreen(
                                                                            commentId = comment.id,
                                                                        )
                                                                    )
                                                                }

                                                                OptionId.SeeRaw -> {
                                                                    rawContent = comment
                                                                }

                                                                OptionId.DistinguishComment -> model.reduce(
                                                                    PostDetailMviModel.Intent.ModDistinguishComment(
                                                                        comment.id,
                                                                    )
                                                                )

                                                                OptionId.Remove -> {
                                                                    val screen =
                                                                        RemoveScreen(commentId = comment.id)
                                                                    navigationCoordinator.pushScreen(
                                                                        screen,
                                                                    )
                                                                }

                                                                OptionId.BanUser -> {
                                                                    comment.creator?.id?.also { userId ->
                                                                        val screen = BanUserScreen(
                                                                            userId = userId,
                                                                            communityId = uiState.post.community?.id
                                                                                ?: 0,
                                                                            newValue = comment.creator?.banned != true,
                                                                            commentId = comment.id,
                                                                        )
                                                                        navigationCoordinator.pushScreen(
                                                                            screen,
                                                                        )
                                                                    }
                                                                }

                                                                OptionId.AddMod -> {
                                                                    comment.creator?.id?.also { userId ->
                                                                        model.reduce(
                                                                            PostDetailMviModel.Intent.ModToggleModUser(
                                                                                userId,
                                                                            )
                                                                        )
                                                                    }
                                                                }

                                                                else -> Unit
                                                            }
                                                        },
                                                    )
                                                },
                                            )
                                        } else {
                                            CollapsedCommentCard(
                                                comment = comment,
                                                isOp = comment.creator?.id == uiState.post.creator?.id,
                                                voteFormat = uiState.voteFormat,
                                                autoLoadImages = uiState.autoLoadImages,
                                                showScores = uiState.showScores,
                                                actionButtonsActive = uiState.isLogged,
                                                onToggleExpanded = rememberCallback(model) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.ToggleExpandComment(
                                                            comment.id,
                                                        )

                                                    )
                                                },
                                                onClick = rememberCallback(model) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.ToggleExpandComment(
                                                            comment.id,
                                                        )
                                                    )
                                                },
                                                onUpVote = rememberCallback(model) {
                                                    if (uiState.isLogged && !isOnOtherInstance) {
                                                        model.reduce(
                                                            PostDetailMviModel.Intent.UpVoteComment(
                                                                commentId = comment.id,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onDownVote = rememberCallback(model) {
                                                    if (uiState.isLogged && !isOnOtherInstance) {
                                                        model.reduce(
                                                            PostDetailMviModel.Intent.DownVoteComment(
                                                                commentId = comment.id,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onSave = rememberCallback(model) {
                                                    if (uiState.isLogged && !isOnOtherInstance) {
                                                        model.reduce(
                                                            PostDetailMviModel.Intent.SaveComment(
                                                                commentId = comment.id,
                                                            ),
                                                        )
                                                    }
                                                },
                                                onReply = rememberCallback(model) {
                                                    if (uiState.isLogged && !isOnOtherInstance) {
                                                        detailOpener.openReply(
                                                            originalPost = uiState.post,
                                                            originalComment = comment,
                                                        )
                                                    }
                                                },
                                                onOpenCreator = rememberCallbackArgs { user ->
                                                    detailOpener.openUserDetail(user, otherInstanceName)
                                                },
                                                options = buildList {
                                                    this += Option(
                                                        OptionId.SeeRaw,
                                                        LocalXmlStrings.current.postActionSeeRaw,
                                                    )
                                                    this += Option(
                                                        OptionId.Report,
                                                        LocalXmlStrings.current.postActionReport,
                                                    )
                                                    if (comment.creator?.id == uiState.currentUserId) {
                                                        this += Option(
                                                            OptionId.Edit,
                                                            LocalXmlStrings.current.postActionEdit,
                                                        )
                                                        this += Option(
                                                            OptionId.Delete,
                                                            LocalXmlStrings.current.commentActionDelete,
                                                        )
                                                    }
                                                    if (uiState.isModerator) {
                                                        this += Option(
                                                            OptionId.DistinguishComment,
                                                            if (comment.distinguished) {
                                                                LocalXmlStrings.current.modActionUnmarkAsDistinguished
                                                            } else {
                                                                LocalXmlStrings.current.modActionMarkAsDistinguished
                                                            },
                                                        )
                                                        this += Option(
                                                            OptionId.Remove,
                                                            LocalXmlStrings.current.modActionRemove,
                                                        )
                                                        this += Option(
                                                            OptionId.BanUser,
                                                            if (comment.creator?.banned == true) {
                                                                LocalXmlStrings.current.modActionAllow
                                                            } else {
                                                                LocalXmlStrings.current.modActionBan
                                                            },
                                                        )
                                                        comment.creator?.id?.also { creatorId ->
                                                            if (uiState.currentUserId != creatorId) {
                                                                this += Option(
                                                                    OptionId.AddMod,
                                                                    if (uiState.moderators.containsId(
                                                                            creatorId,
                                                                        )
                                                                    ) {
                                                                        LocalXmlStrings.current.modActionRemoveMod
                                                                    } else {
                                                                        LocalXmlStrings.current.modActionAddMod
                                                                    },
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                                onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                                    when (optionId) {
                                                        OptionId.Delete -> {
                                                            commentIdToDelete = comment.id
                                                        }

                                                        OptionId.Edit -> {
                                                            detailOpener.openReply(
                                                                editedComment = comment,
                                                            )
                                                        }

                                                        OptionId.Report -> {
                                                            navigationCoordinator.pushScreen(
                                                                CreateReportScreen(
                                                                    commentId = comment.id,
                                                                )
                                                            )
                                                        }

                                                        OptionId.SeeRaw -> {
                                                            rawContent = comment
                                                        }

                                                        OptionId.DistinguishComment -> model.reduce(
                                                            PostDetailMviModel.Intent.ModDistinguishComment(
                                                                comment.id,
                                                            )
                                                        )

                                                        OptionId.Remove -> {
                                                            val screen =
                                                                RemoveScreen(commentId = comment.id)
                                                            navigationCoordinator.pushScreen(screen)
                                                        }

                                                        OptionId.BanUser -> {
                                                            comment.creator?.id?.also { userId ->
                                                                val screen = BanUserScreen(
                                                                    userId = userId,
                                                                    communityId = uiState.post.community?.id
                                                                        ?: 0,
                                                                    newValue = comment.creator?.banned != true,
                                                                    commentId = comment.id,
                                                                )
                                                                navigationCoordinator.pushScreen(
                                                                    screen,
                                                                )
                                                            }
                                                        }

                                                        OptionId.AddMod -> {
                                                            comment.creator?.id?.also { userId ->
                                                                model.reduce(
                                                                    PostDetailMviModel.Intent.ModToggleModUser(
                                                                        userId,
                                                                    )
                                                                )
                                                            }
                                                        }

                                                        else -> Unit
                                                    }
                                                },
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = Spacing.xxxs),
                                        thickness = 0.25.dp
                                    )

                                    // load more button
                                    if (comment.loadMoreButtonVisible && comment.expanded) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            Button(
                                                onClick = rememberCallback(model) {
                                                    model.reduce(
                                                        PostDetailMviModel.Intent.FetchMoreComments(
                                                            parentId = comment.id,
                                                        )
                                                    )
                                                },
                                            ) {
                                                Text(
                                                    text = buildString {
                                                        append(LocalXmlStrings.current.postDetailLoadMoreComments)
                                                        comment.comments?.takeIf { it > 0 }
                                                            ?.also { count ->
                                                                append(" (")
                                                                append(count)
                                                                append(")")
                                                            }
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
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
                            if (uiState.comments.isEmpty() && !uiState.loading && !uiState.initial) {
                                item {
                                    Column {
                                        if (uiState.post.comments == 0) {
                                            Text(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = Spacing.xs),
                                                textAlign = TextAlign.Center,
                                                text = LocalXmlStrings.current.messageEmptyComments,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        } else if (uiState.searching) {
                                            Text(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = Spacing.xs),
                                                textAlign = TextAlign.Center,
                                                text = LocalXmlStrings.current.messageEmptyList,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        } else {
                                            Text(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = Spacing.xs),
                                                textAlign = TextAlign.Center,
                                                text = LocalXmlStrings.current.messageErrorLoadingComments,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                            Row {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Button(
                                                    onClick = rememberCallback(model) {
                                                        model.reduce(PostDetailMviModel.Intent.Refresh)
                                                    },
                                                ) {
                                                    Text(
                                                        text = LocalXmlStrings.current.buttonRetry,
                                                    )
                                                }
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
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

                if (uiState.enableButtonsToScrollBetweenComments) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                if (bottomBarHeightPx == 0f) {
                                    bottomBarHeightPx = it.size.toSize().height
                                }
                            }
                            .padding(bottom = bottomNavigationInset)
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = -bottomBarOffsetHeightPx.roundToInt()
                                )
                            }
                            .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.45f)),
                    ) {
                        Icon(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = Spacing.xs)
                                .onClick(
                                    onClick = rememberCallback {
                                        val index = lazyListState.firstVisibleItemIndex
                                        scope.launch {
                                            lazyListState.animateScrollToItem((index - 1).coerceAtLeast(0))
                                        }
                                    },
                                ),
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                        Icon(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = Spacing.xs)
                                .onClick(
                                    onClick = rememberCallback {
                                        val index = lazyListState.layoutInfo.visibleItemsInfo.lastIndex
                                        scope.launch {
                                            lazyListState.animateScrollToItem((index + 1))
                                        }
                                    },
                                ),
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
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
                        upVotes = content.upvotes,
                        downVotes = content.downvotes,
                        isLogged = uiState.isLogged,
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
                        isLogged = uiState.isLogged,
                        upVotes = content.upvotes,
                        downVotes = content.downvotes,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        onDismiss = rememberCallback {
                            rawContent = null
                        },
                        onQuote = rememberCallbackArgs { quotation ->
                            rawContent = null
                            if (quotation != null) {
                                detailOpener.openReply(
                                    originalPost = uiState.post,
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

        postToDelete?.also {
            AlertDialog(
                onDismissRequest = {
                    postToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            postToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(PostDetailMviModel.Intent.DeletePost)
                            postToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonConfirm)
                    }
                },
                text = {
                    Text(text = LocalXmlStrings.current.messageAreYouSure)
                },
            )
        }
        commentIdToDelete?.also { itemId ->
            AlertDialog(
                onDismissRequest = {
                    commentIdToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            commentIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(PostDetailMviModel.Intent.DeleteComment(itemId))
                            commentIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonConfirm)
                    }
                },
                text = {
                    Text(text = LocalXmlStrings.current.messageAreYouSure)
                },
            )
        }
    }
}
