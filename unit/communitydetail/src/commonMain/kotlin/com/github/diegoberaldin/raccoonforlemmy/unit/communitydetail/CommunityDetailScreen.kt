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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Pending
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomDropDown
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ProgressHud
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeActionCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.PostCardPlaceholder
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
import com.github.diegoberaldin.raccoonforlemmy.core.utils.keepscreenon.rememberKeepScreenOn
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
import com.github.diegoberaldin.raccoonforlemmy.unit.communityinfo.CommunityInfoScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.createreport.CreateReportScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.editcommunity.EditCommunityScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.explore.ExploreScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo.InstanceInfoScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.ModlogScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.rawcontent.RawContentDialog
import com.github.diegoberaldin.raccoonforlemmy.unit.remove.RemoveScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.ReportListScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.web.WebViewScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

class CommunityDetailScreen(
    private val communityId: Long,
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
        val uiState by model.uiState.collectAsState()
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val genericError = LocalXmlStrings.current.messageGenericError
        val successMessage = LocalXmlStrings.current.messageOperationSuccessful
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
        val saveColor by themeRepository.saveColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultReplyColor = MaterialTheme.colorScheme.secondary
        val defaultSaveColor = MaterialTheme.colorScheme.secondaryContainer
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary
        var rawContent by remember { mutableStateOf<Any?>(null) }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val keepScreenOn = rememberKeepScreenOn()
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
        var itemIdToDelete by remember { mutableStateOf<Long?>(null) }
        val statusBarInset = with(LocalDensity.current) {
            WindowInsets.statusBars.getTop(this)
        }

        LaunchedEffect(notificationCenter) {
            notificationCenter.resetCache()
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    is CommunityDetailMviModel.Effect.Error -> {
                        snackbarHostState.showSnackbar(effect.message ?: genericError)
                    }

                    CommunityDetailMviModel.Effect.Success -> {
                        snackbarHostState.showSnackbar(successMessage)
                    }

                    CommunityDetailMviModel.Effect.BackToTop -> {
                        runCatching {
                            lazyListState.scrollToItem(0)
                            topAppBarState.heightOffset = 0f
                            topAppBarState.contentOffset = 0f
                        }
                    }

                    is CommunityDetailMviModel.Effect.ZombieModeTick -> {
                        runCatching {
                            if (effect.index >= 0) {
                                lazyListState.animateScrollBy(
                                    value = settings.zombieModeScrollAmount,
                                    animationSpec = tween(350),
                                )
                            }
                        }
                    }

                    is CommunityDetailMviModel.Effect.TriggerCopy -> {
                        clipboardManager.setText(AnnotatedString(text = effect.text))
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
        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.globalMessage.onEach { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                )
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            topBar = {
                val maxTopInset = Dimensions.maxTopBarInset.toLocalPixel()
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
                            text = uiState.community.readableName(uiState.preferNicknames),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    actions = {
                        // subscribe button
                        if (!isOnOtherInstance && uiState.isLogged) {
                            Image(
                                modifier = Modifier
                                    .padding(horizontal = Spacing.xs)
                                    .onClick(
                                        onClick = {
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
                        }

                        // sort button
                        Image(
                            modifier = Modifier
                                .padding(horizontal = Spacing.xs)
                                .onClick(
                                    onClick = {
                                        val sheet = SortBottomSheet(
                                            values = uiState.availableSortTypes.map { it.toInt() },
                                            expandTop = true,
                                            screenKey = uiState.community.readableHandle,
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
                                if (!isOnOtherInstance) {
                                    this += Option(
                                        OptionId.Search,
                                        if (uiState.searching) {
                                            LocalXmlStrings.current.actionExitSearch
                                        } else {
                                            buildString {
                                                append(LocalXmlStrings.current.actionSearchInCommunity)
                                            }
                                        },
                                    )
                                }
                                this += Option(
                                    OptionId.SetCustomSort,
                                    LocalXmlStrings.current.communitySetCustomSort,
                                )
                                this += Option(
                                    OptionId.InfoInstance,
                                    LocalXmlStrings.current.communityDetailInstanceInfo,
                                )
                                this += Option(
                                    OptionId.ExploreInstance,
                                    buildString {
                                        append(LocalXmlStrings.current.navigationSearch)
                                        append(" ")
                                        append(uiState.community.host)
                                        append(" (")
                                        append(LocalXmlStrings.current.beta)
                                        append(")")
                                    },
                                )
                                this += Option(
                                    OptionId.Share, LocalXmlStrings.current.postActionShare,
                                )
                                if (uiState.isLogged) {
                                    this += Option(
                                        OptionId.Block,
                                        LocalXmlStrings.current.communityDetailBlock,
                                    )
                                    this += Option(
                                        OptionId.BlockInstance,
                                        LocalXmlStrings.current.communityDetailBlockInstance,
                                    )
                                }
                                if (uiState.currentUserId != null && otherInstanceName.isEmpty()) {
                                    this += Option(
                                        OptionId.Favorite,
                                        if (uiState.community.favorite) {
                                            LocalXmlStrings.current.communityActionRemoveFavorite
                                        } else {
                                            LocalXmlStrings.current.communityActionAddFavorite
                                        },
                                    )
                                }

                                this += Option(
                                    OptionId.ViewModlog,
                                    LocalXmlStrings.current.communityActionViewModlog,
                                )

                                if (uiState.moderators.containsId(uiState.currentUserId)) {
                                    this += Option(
                                        OptionId.OpenReports,
                                        LocalXmlStrings.current.modActionOpenReports,
                                    )

                                    this += Option(
                                        OptionId.Edit,
                                        LocalXmlStrings.current.communityActionEdit,
                                    )
                                }
                            }
                            var optionsExpanded by remember { mutableStateOf(false) }
                            var optionsOffset by remember { mutableStateOf(Offset.Zero) }
                            Image(
                                modifier = Modifier.onGloballyPositioned {
                                    optionsOffset = it.positionInParent()
                                }.onClick(
                                    onClick = {
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
                                        onClick = {
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

                                                OptionId.Share -> {
                                                    val urls = buildList {
                                                        if (uiState.community.host != uiState.instance) {
                                                            this += "https://${uiState.instance}/c/${uiState.community.readableHandle}"
                                                        }
                                                        this += "https://${uiState.community.host}/c/${uiState.community.name}"
                                                    }
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

                                                OptionId.SetCustomSort -> {
                                                    val screen = SortBottomSheet(
                                                        values = uiState.availableSortTypes.map { it.toInt() },
                                                        defaultForCommunity = true,
                                                        expandTop = true,
                                                        screenKey = uiState.community.readableHandle,
                                                    )
                                                    navigationCoordinator.showBottomSheet(screen)
                                                }

                                                OptionId.Search -> {
                                                    model.reduce(
                                                        CommunityDetailMviModel.Intent.ChangeSearching(
                                                            !uiState.searching
                                                        )
                                                    )
                                                }

                                                OptionId.ExploreInstance -> {
                                                    val screen =
                                                        ExploreScreen(otherInstance = uiState.community.host)
                                                    navigationCoordinator.pushScreen(screen)
                                                }

                                                OptionId.Edit -> {
                                                    scope.launch { }
                                                    val screen =
                                                        EditCommunityScreen(uiState.community.id)
                                                    navigationCoordinator.pushScreen(screen)
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
                                    onClick = {
                                        navigationCoordinator.popScreen()
                                    },
                                ),
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
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
                                    text = LocalXmlStrings.current.actionDeactivateZombieMode,
                                    onSelected = rememberCallback(model) {
                                        model.reduce(CommunityDetailMviModel.Intent.PauseZombieMode)
                                    },
                                )
                            } else {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.Sync,
                                    text = LocalXmlStrings.current.actionActivateZombieMode,
                                    onSelected = rememberCallback(model) {
                                        model.reduce(
                                            CommunityDetailMviModel.Intent.StartZombieMode(-1)
                                        )
                                    },
                                )
                            }
                            this += FloatingActionButtonMenuItem(
                                icon = Icons.Default.ExpandLess,
                                text = LocalXmlStrings.current.actionBackToTop,
                                onSelected = rememberCallback {
                                    scope.launch {
                                        runCatching {
                                            lazyListState.scrollToItem(0)
                                            topAppBarState.heightOffset = 0f
                                            topAppBarState.contentOffset = 0f
                                        }
                                    }
                                },
                            )
                            if (uiState.isLogged && !isOnOtherInstance) {
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.ClearAll,
                                    text = LocalXmlStrings.current.actionClearRead,
                                    onSelected = rememberCallback {
                                        model.reduce(CommunityDetailMviModel.Intent.ClearRead)
                                        scope.launch {
                                            runCatching {
                                                lazyListState.scrollToItem(0)
                                                topAppBarState.heightOffset = 0f
                                                topAppBarState.contentOffset = 0f
                                            }
                                        }
                                    },
                                )
                                this += FloatingActionButtonMenuItem(
                                    icon = Icons.Default.Create,
                                    text = LocalXmlStrings.current.actionCreatePost,
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
            if (uiState.currentUserId != null) {
                Column(
                    modifier = Modifier.padding(padding),
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
                                model.reduce(CommunityDetailMviModel.Intent.SetSearch(value))
                            },
                            trailingIcon = {
                                Icon(
                                    modifier = Modifier.onClick(
                                        onClick = {
                                            if (uiState.searchText.isNotEmpty()) {
                                                model.reduce(
                                                    CommunityDetailMviModel.Intent.SetSearch(
                                                        ""
                                                    )
                                                )
                                            }
                                        },
                                    ),
                                    imageVector = if (uiState.searchText.isEmpty()) Icons.Default.Search else Icons.Default.Clear,
                                    contentDescription = null,
                                )
                            },
                        )
                    }

                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = uiState.refreshing,
                        onRefresh = rememberCallback(model) {
                            model.reduce(CommunityDetailMviModel.Intent.Refresh)
                        },
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                            state = lazyListState,
                            userScrollEnabled = !uiState.zombieModeActive,
                        ) {
                            if (!uiState.searching) {
                                item {
                                    CommunityHeader(
                                        modifier = Modifier.padding(bottom = Spacing.s),
                                        community = uiState.community,
                                        autoLoadImages = uiState.autoLoadImages,
                                        onOpenImage = rememberCallbackArgs { url ->
                                            navigationCoordinator.pushScreen(
                                                ZoomableImageScreen(
                                                    url = url,
                                                    source = uiState.community.readableHandle,
                                                )
                                            )
                                        },
                                        onInfo = rememberCallback {
                                            val screen = CommunityInfoScreen(
                                                communityId = uiState.community.id,
                                                communityName = uiState.community.name,
                                                otherInstance = otherInstanceName,
                                            )
                                            navigationCoordinator.openSideMenu(screen)
                                        }
                                    )
                                }
                            }
                            if (uiState.posts.isEmpty() && uiState.loading) {
                                items(5) {
                                    PostCardPlaceholder(
                                        modifier = Modifier.padding(horizontal = Spacing.xs),
                                        postLayout = uiState.postLayout,
                                    )
                                    if (uiState.postLayout != PostLayout.Card) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                    } else {
                                        Spacer(modifier = Modifier.height(Spacing.interItem))
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
                                                        CommunityDetailMviModel.Intent.UpVotePost(
                                                            post.id
                                                        ),
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
                                                        CommunityDetailMviModel.Intent.DownVotePost(
                                                            post.id
                                                        ),
                                                    )
                                                },
                                            )

                                            ActionOnSwipe.Reply -> SwipeAction(
                                                swipeContent = {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Default.Reply,
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
                                                        CommunityDetailMviModel.Intent.SavePost(
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
                                        model.reduce(CommunityDetailMviModel.Intent.HapticIndication)
                                    },
                                    swipeToStartActions = if (uiState.isLogged && !isOnOtherInstance) {
                                        uiState.actionsOnSwipeToStartPosts.toSwipeActions()
                                    } else {
                                        emptyList()
                                    },
                                    swipeToEndActions = if (uiState.isLogged && !isOnOtherInstance) {
                                        uiState.actionsOnSwipeToEndPosts.toSwipeActions()
                                    } else {
                                        emptyList()
                                    },
                                    content = {
                                        PostCard(
                                            modifier = Modifier.padding(horizontal = Spacing.xs),
                                            post = post,
                                            isFromModerator = uiState.moderators.containsId(post.creator?.id),
                                            postLayout = uiState.postLayout,
                                            limitBodyHeight = true,
                                            fullHeightImage = uiState.fullHeightImages,
                                            fullWidthImage = uiState.fullWidthImages,
                                            voteFormat = uiState.voteFormat,
                                            autoLoadImages = uiState.autoLoadImages,
                                            preferNicknames = uiState.preferNicknames,
                                            showScores = uiState.showScores,
                                            actionButtonsActive = uiState.isLogged,
                                            blurNsfw = when {
                                                uiState.community.nsfw -> false
                                                else -> uiState.blurNsfw
                                            },
                                            fadeRead = uiState.fadeReadPosts,
                                            showUnreadComments = uiState.showUnreadComments,
                                            onClick = rememberCallback(model) {
                                                model.reduce(
                                                    CommunityDetailMviModel.Intent.MarkAsRead(
                                                        post.id
                                                    )
                                                )
                                                model.reduce(CommunityDetailMviModel.Intent.WillOpenDetail)
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
                                                    model.reduce(CommunityDetailMviModel.Intent.WillOpenDetail)
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
                                                    ZoomableImageScreen(
                                                        url = url,
                                                        source = uiState.community.readableHandle,
                                                    ),
                                                )
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
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    this += Option(
                                                        OptionId.Hide,
                                                        LocalXmlStrings.current.postActionHide,
                                                    )
                                                }
                                                this += Option(
                                                    OptionId.SeeRaw,
                                                    LocalXmlStrings.current.postActionSeeRaw,
                                                )
                                                if (uiState.isLogged && !isOnOtherInstance) {
                                                    this += Option(
                                                        OptionId.CrossPost,
                                                        LocalXmlStrings.current.postActionCrossPost
                                                    )
                                                    this += Option(
                                                        OptionId.Report,
                                                        LocalXmlStrings.current.postActionReport,
                                                    )
                                                }
                                                if (post.creator?.id == uiState.currentUserId && !isOnOtherInstance) {
                                                    this += Option(
                                                        OptionId.Edit,
                                                        LocalXmlStrings.current.postActionEdit,
                                                    )
                                                    this += Option(
                                                        OptionId.Delete,
                                                        LocalXmlStrings.current.commentActionDelete,
                                                    )
                                                }
                                                if (uiState.moderators.containsId(uiState.currentUserId)) {
                                                    this += Option(
                                                        OptionId.FeaturePost,
                                                        if (post.featuredCommunity) {
                                                            LocalXmlStrings.current.modActionUnmarkAsFeatured
                                                        } else {
                                                            LocalXmlStrings.current.modActionMarkAsFeatured
                                                        },
                                                    )
                                                    this += Option(
                                                        OptionId.LockPost,
                                                        if (post.locked) {
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
                                                        if (post.creator?.banned == true) {
                                                            LocalXmlStrings.current.modActionAllow
                                                        } else {
                                                            LocalXmlStrings.current.modActionBan
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
                                                        itemIdToDelete = post.id
                                                    }

                                                    OptionId.Edit -> {
                                                        detailOpener.openCreatePost(editedPost = post)
                                                    }

                                                    OptionId.Report -> {
                                                        navigationCoordinator.pushScreen(
                                                            CreateReportScreen(postId = post.id)
                                                        )
                                                    }

                                                    OptionId.CrossPost -> {
                                                        detailOpener.openCreatePost(
                                                            crossPost = post,
                                                            forceCommunitySelection = true,
                                                        )
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
                                                            val screen =
                                                                ShareBottomSheet(urls = urls)
                                                            navigationCoordinator.showBottomSheet(
                                                                screen
                                                            )
                                                        }
                                                    }

                                                    OptionId.FeaturePost -> model.reduce(
                                                        CommunityDetailMviModel.Intent.ModFeaturePost(
                                                            post.id
                                                        )
                                                    )

                                                    OptionId.LockPost -> model.reduce(
                                                        CommunityDetailMviModel.Intent.ModLockPost(
                                                            post.id
                                                        )
                                                    )

                                                    OptionId.Remove -> {
                                                        val screen = RemoveScreen(postId = post.id)
                                                        navigationCoordinator.pushScreen(screen)
                                                    }

                                                    OptionId.BanUser -> {
                                                        post.creator?.id?.also { userId ->
                                                            val screen = BanUserScreen(
                                                                userId = userId,
                                                                communityId = uiState.community.id,
                                                                newValue = post.creator?.banned != true,
                                                                postId = post.id,
                                                            )
                                                            navigationCoordinator.pushScreen(screen)
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

                                                    OptionId.Copy -> {
                                                        val texts = listOfNotNull(
                                                            post.title.takeIf { it.isNotBlank() },
                                                            post.text.takeIf { it.isNotBlank() },
                                                        ).distinct()
                                                        if (texts.size == 1) {
                                                            model.reduce(
                                                                CommunityDetailMviModel.Intent.Copy(
                                                                    texts.first()
                                                                )
                                                            )
                                                        } else {
                                                            val screen = CopyPostBottomSheet(
                                                                post.title,
                                                                post.text
                                                            )
                                                            navigationCoordinator.showBottomSheet(
                                                                screen
                                                            )
                                                        }
                                                    }

                                                    else -> Unit
                                                }
                                            })
                                    },
                                )
                                if (uiState.postLayout != PostLayout.Card) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.interItem))
                                }
                            }
                            item {
                                if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                    if (settings.infiniteScrollEnabled) {
                                        model.reduce(CommunityDetailMviModel.Intent.LoadNextPage)
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(top = Spacing.s),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Button(
                                                onClick = rememberCallback(model) {
                                                    model.reduce(CommunityDetailMviModel.Intent.LoadNextPage)
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

                        if (uiState.asyncInProgress) {
                            ProgressHud()
                        }
                    }
                }
            }
        }

        if (rawContent != null) {
            when (val content = rawContent) {
                is PostModel -> {
                    RawContentDialog(
                        title = content.title,
                        text = content.text,
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        url = content.url,
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
                        publishDate = content.publishDate,
                        updateDate = content.updateDate,
                        upVotes = content.upvotes,
                        downVotes = content.downvotes,
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

        itemIdToDelete?.also { itemId ->
            AlertDialog(
                onDismissRequest = {
                    itemIdToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            itemIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(CommunityDetailMviModel.Intent.DeletePost(itemId))
                            itemIdToDelete = null
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
