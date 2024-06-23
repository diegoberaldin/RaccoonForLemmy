package com.github.diegoberaldin.raccoonforlemmy.unit.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SearchField
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerEvent
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableName
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.components.DrawerCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.components.DrawerHeader
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.components.DrawerShortcut
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.SelectInstanceBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

object ModalDrawerContent : Tab {
    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ModalDrawerMviModel>()
        val uiState by model.uiState.collectAsState()
        val coordinator = remember { getDrawerCoordinator() }
        val themeRepository = remember { getThemeRepository() }
        val scope = rememberCoroutineScope()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }
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

        var uiFontSizeWorkaround by remember { mutableStateOf(true) }
        LaunchedEffect(themeRepository) {
            themeRepository.uiFontScale
                .drop(1)
                .onEach {
                    uiFontSizeWorkaround = false
                    delay(50)
                    uiFontSizeWorkaround = true
                }.launchIn(this)
        }
        if (!uiFontSizeWorkaround) {
            return
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter
                .subscribe(NotificationCenterEvent.InstanceSelected::class)
                .onEach {
                    // closes the navigation drawer after instance change
                    coordinator.closeDrawer()
                }.launchIn(this)
        }

        ModalDrawerSheet {
            DrawerHeader(
                user = uiState.user,
                instance = uiState.instance,
                autoLoadImages = uiState.autoLoadImages,
                onOpenChangeInstance =
                    rememberCallback(model) {
                        navigationCoordinator.showBottomSheet(SelectInstanceBottomSheet())
                    },
                onOpenSwitchAccount =
                    rememberCallback {
                        navigationCoordinator.showBottomSheet(ManageAccountsScreen())
                    },
            )

            HorizontalDivider(
                modifier =
                    Modifier
                        .padding(
                            top = Spacing.s,
                            bottom = Spacing.s,
                        ),
            )

            if (uiState.user != null) {
                val pullRefreshState =
                    rememberPullRefreshState(
                        refreshing = uiState.refreshing,
                        onRefresh =
                            rememberCallback(model) {
                                model.reduce(ModalDrawerMviModel.Intent.Refresh)
                            },
                    )
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .nestedScroll(keyboardScrollConnection)
                            .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                    ) {
                        item {
                            SearchField(
                                modifier =
                                    Modifier
                                        .scale(0.95f)
                                        .padding(
                                            horizontal = Spacing.xxs,
                                            vertical = Spacing.xxs,
                                        ).fillMaxWidth(),
                                hint = LocalStrings.current.exploreSearchPlaceholder,
                                value = uiState.searchText,
                                onValueChange = { value ->
                                    model.reduce(ModalDrawerMviModel.Intent.SetSearch(value))
                                },
                                onClear = {
                                    model.reduce(ModalDrawerMviModel.Intent.SetSearch(""))
                                },
                            )
                        }

                        if (!uiState.isFiltering) {
                            val listingTypes =
                                listOf(
                                    ListingType.Subscribed,
                                    ListingType.All,
                                    ListingType.Local,
                                )
                            for (listingType in listingTypes) {
                                item {
                                    DrawerShortcut(
                                        title = listingType.toReadableName(),
                                        icon = listingType.toIcon(),
                                        onSelected =
                                            rememberCallback(coordinator) {
                                                scope.launch {
                                                    focusManager.clearFocus()
                                                    coordinator.toggleDrawer()
                                                    coordinator.sendEvent(
                                                        DrawerEvent.ChangeListingType(listingType),
                                                    )
                                                }
                                            },
                                    )
                                }
                            }
                        }

                        items(
                            items = uiState.multiCommunities,
                            key = { it.communityIds.joinToString() },
                        ) { community ->
                            DrawerCommunityItem(
                                title = community.name,
                                url = community.icon,
                                autoLoadImages = uiState.autoLoadImages,
                                onSelected = {
                                    focusManager.clearFocus()
                                    scope.launch {
                                        coordinator.toggleDrawer()
                                        coordinator.sendEvent(
                                            DrawerEvent.OpenMultiCommunity(community),
                                        )
                                    }
                                },
                            )
                        }

                        items(
                            items = uiState.favorites,
                            key = { "${it.id}-favorite" },
                        ) { community ->
                            DrawerCommunityItem(
                                title = community.readableName(uiState.preferNicknames),
                                subtitle = community.readableHandle,
                                url = community.icon,
                                favorite = true,
                                autoLoadImages = uiState.autoLoadImages,
                                onSelected = {
                                    scope.launch {
                                        focusManager.clearFocus()
                                        coordinator.toggleDrawer()
                                        coordinator.sendEvent(
                                            DrawerEvent.OpenCommunity(community),
                                        )
                                    }
                                },
                                onToggleFavorite =
                                    if (!uiState.enableToggleFavorite) {
                                        null
                                    } else {
                                        rememberCallback(model) {
                                            model.reduce(
                                                ModalDrawerMviModel.Intent.ToggleFavorite(community.id),
                                            )
                                        }
                                    },
                            )
                        }

                        items(
                            items = uiState.communities,
                            key = { "${it.id}-community" },
                        ) { community ->
                            DrawerCommunityItem(
                                title = community.readableName(uiState.preferNicknames),
                                subtitle = community.readableHandle,
                                url = community.icon,
                                favorite = false,
                                autoLoadImages = uiState.autoLoadImages,
                                onSelected = {
                                    scope.launch {
                                        focusManager.clearFocus()
                                        coordinator.toggleDrawer()
                                        coordinator.sendEvent(
                                            DrawerEvent.OpenCommunity(community),
                                        )
                                    }
                                },
                                onToggleFavorite =
                                    if (!uiState.enableToggleFavorite) {
                                        null
                                    } else {
                                        rememberCallback(model) {
                                            model.reduce(
                                                ModalDrawerMviModel.Intent.ToggleFavorite(community.id),
                                            )
                                        }
                                    },
                            )
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
            } else {
                Text(
                    modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.s),
                    text = LocalStrings.current.sidebarNotLoggedMessage,
                    style = MaterialTheme.typography.bodySmall,
                )

                Text(
                    modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.s),
                    text = LocalStrings.current.homeListingTitle,
                    style = MaterialTheme.typography.titleMedium,
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxs),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                ) {
                    for (listingType in listOf(
                        ListingType.All,
                        ListingType.Local,
                    )) {
                        item {
                            DrawerShortcut(
                                title = listingType.toReadableName(),
                                icon = listingType.toIcon(),
                                onSelected = {
                                    scope.launch {
                                        coordinator.toggleDrawer()
                                        coordinator.sendEvent(
                                            DrawerEvent.ChangeListingType(listingType),
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
