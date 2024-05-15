package com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenu
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.FloatingActionButtonMenuItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItemPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.MultiCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail.MultiCommunityScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.editor.MultiCommunityEditorScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ManageSubscriptionsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ManageSubscriptionsMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigatorCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val lazyListState = rememberLazyListState()
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val scope = rememberCoroutineScope()
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        val isFabVisible by fabNestedScrollConnection.isFabVisible.collectAsState()
        val detailOpener = remember { getDetailOpener() }
        val focusManager = LocalFocusManager.current
        val keyboardScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    focusManager.clearFocus()
                    return Offset.Zero
                }
            }
        }
        var multiCommunityIdToDelete by remember { mutableStateOf<Long?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }
        val successMessage = LocalXmlStrings.current.messageOperationSuccessful

        LaunchedEffect(model) {
            model.effects.onEach { event ->
                when (event) {
                    ManageSubscriptionsMviModel.Effect.BackToTop -> {
                        runCatching {
                            lazyListState.scrollToItem(0)
                            topAppBarState.heightOffset = 0f
                            topAppBarState.contentOffset = 0f
                        }
                    }

                    ManageSubscriptionsMviModel.Effect.Success -> {
                        snackbarHostState.showSnackbar(successMessage)
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.navigationDrawerTitleSubscriptions,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = {
                                    navigatorCoordinator.popScreen()
                                },
                            ),
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
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
            Column(
                modifier = Modifier.padding(padding),
            ) {
                TextField(
                    modifier = Modifier
                        .padding(
                            horizontal = Spacing.s,
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
                        model.reduce(ManageSubscriptionsMviModel.Intent.SetSearch(value))
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.onClick(
                                onClick = {
                                    if (uiState.searchText.isNotEmpty()) {
                                        model.reduce(ManageSubscriptionsMviModel.Intent.SetSearch(""))
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

                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh = rememberCallback(model) {
                        model.reduce(ManageSubscriptionsMviModel.Intent.Refresh)
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
                            },
                        )
                        .nestedScroll(fabNestedScrollConnection)
                        .nestedScroll(keyboardScrollConnection)
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.s),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = LocalXmlStrings.current.manageSubscriptionsHeaderMulticommunities,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    modifier = Modifier
                                        .onClick(
                                            onClick = {
                                                navigatorCoordinator.pushScreen(
                                                    MultiCommunityEditorScreen(),
                                                )
                                            },
                                        ),
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                        items(uiState.multiCommunities) { community ->
                            MultiCommunityItem(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background).onClick(
                                        onClick = {
                                            community.id?.also {
                                                navigatorCoordinator.pushScreen(
                                                    MultiCommunityScreen(it),
                                                )
                                            }
                                        },
                                    ),
                                community = community,
                                autoLoadImages = uiState.autoLoadImages,
                                options = buildList {
                                    this += Option(
                                        OptionId.Edit,
                                        LocalXmlStrings.current.postActionEdit,
                                    )
                                    this += Option(
                                        OptionId.Delete,
                                        LocalXmlStrings.current.commentActionDelete,
                                    )
                                },
                                onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                    when (optionId) {
                                        OptionId.Edit -> {
                                            navigatorCoordinator.pushScreen(
                                                MultiCommunityEditorScreen(community.id),
                                            )
                                        }

                                        OptionId.Delete -> {
                                            community.id?.also {
                                                multiCommunityIdToDelete = it
                                            }
                                        }

                                        else -> Unit
                                    }
                                },
                            )
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.s),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = LocalXmlStrings.current.manageSubscriptionsHeaderSubscriptions,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                        if (uiState.initial) {
                            items(5) {
                                CommunityItemPlaceholder()
                            }
                        }
                        items(uiState.communities) { community ->
                            CommunityItem(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .onClick(
                                        onClick = {
                                            detailOpener.openCommunityDetail(community = community)
                                        },
                                    ),
                                community = community,
                                autoLoadImages = uiState.autoLoadImages,
                                showFavorite = true,
                                options = buildList {
                                    this += Option(
                                        OptionId.Unsubscribe,
                                        LocalXmlStrings.current.communityActionUnsubscribe,
                                    )
                                    this += Option(
                                        OptionId.Favorite,
                                        if (community.favorite) {
                                            LocalXmlStrings.current.communityActionRemoveFavorite
                                        } else {
                                            LocalXmlStrings.current.communityActionAddFavorite
                                        },
                                    )
                                },
                                onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                    when (optionId) {
                                        OptionId.Unsubscribe -> {
                                            model.reduce(ManageSubscriptionsMviModel.Intent.Unsubscribe(community.id))
                                        }

                                        OptionId.Favorite -> {
                                            model.reduce(
                                                ManageSubscriptionsMviModel.Intent.ToggleFavorite(community.id),
                                            )
                                        }

                                        else -> Unit
                                    }
                                },
                            )
                        }

                        if (uiState.multiCommunities.isEmpty() && uiState.communities.isEmpty() && !uiState.initial) {
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
                    }

                    if (!uiState.initial) {
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

        multiCommunityIdToDelete?.also { itemId ->
            AlertDialog(
                onDismissRequest = {
                    multiCommunityIdToDelete = null
                },
                dismissButton = {
                    Button(
                        onClick = {
                            multiCommunityIdToDelete = null
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            model.reduce(ManageSubscriptionsMviModel.Intent.DeleteMultiCommunity(itemId))
                            multiCommunityIdToDelete = null
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
