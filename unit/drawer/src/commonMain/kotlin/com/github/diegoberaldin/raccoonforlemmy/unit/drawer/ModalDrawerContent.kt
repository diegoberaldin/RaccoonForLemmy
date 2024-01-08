package com.github.diegoberaldin.raccoonforlemmy.unit.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.MultiCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.DrawerEvent
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toReadableName
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.resources.di.getLanguageRepository
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.components.DrawerHeader
import com.github.diegoberaldin.raccoonforlemmy.unit.drawer.components.DrawerShortcut
import com.github.diegoberaldin.raccoonforlemmy.unit.selectinstance.SelectInstanceBottomSheet
import dev.icerock.moko.resources.compose.stringResource
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
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val coordinator = remember { getDrawerCoordinator() }
        val languageRepository = remember { getLanguageRepository() }
        val themeRepository = remember { getThemeRepository() }
        val scope = rememberCoroutineScope()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val notificationCenter = remember { getNotificationCenter() }

        var uiFontSizeWorkaround by remember { mutableStateOf(true) }
        LaunchedEffect(themeRepository) {
            themeRepository.uiFontScale.drop(1).onEach {
                uiFontSizeWorkaround = false
                delay(50)
                uiFontSizeWorkaround = true
            }.launchIn(this)
        }
        LaunchedEffect(languageRepository) {
            languageRepository.currentLanguage.drop(1).onEach {
                uiFontSizeWorkaround = false
                delay(50)
                uiFontSizeWorkaround = true
            }.launchIn(this)
        }
        if (!uiFontSizeWorkaround) {
            return
        }
        LaunchedEffect(notificationCenter) {
            notificationCenter.subscribe(NotificationCenterEvent.InstanceSelected::class).onEach {
                // closes the navigation drawer after instance change
                coordinator.toggleDrawer()
            }.launchIn(this)
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            DrawerHeader(
                user = uiState.user,
                instance = uiState.instance,
                autoLoadImages = uiState.autoLoadImages,
                onOpenChangeInstance = rememberCallback(model) {
                   navigationCoordinator.showBottomSheet(SelectInstanceBottomSheet())
                },
            )

            Divider(
                modifier = Modifier.padding(
                    top = Spacing.m,
                    bottom = Spacing.s,
                )
            )

            if (uiState.user != null) {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh = rememberCallback(model) {
                        model.reduce(ModalDrawerMviModel.Intent.Refresh)
                    },
                )
                Box(
                    modifier = Modifier.weight(1f).pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                    ) {
                        for (listingType in listOf(
                            ListingType.Subscribed,
                            ListingType.All,
                            ListingType.Local,
                        )) {
                            item {
                                DrawerShortcut(
                                    title = listingType.toReadableName(),
                                    icon = listingType.toIcon(),
                                    onSelected = rememberCallback(coordinator) {
                                        scope.launch {
                                            coordinator.toggleDrawer()
                                            coordinator.sendEvent(
                                                DrawerEvent.ChangeListingType(listingType)
                                            )
                                        }
                                    },
                                )
                            }
                        }
                        item {
                            DrawerShortcut(
                                title = stringResource(MR.strings.navigation_drawer_title_bookmarks),
                                icon = Icons.Default.Bookmarks,
                                onSelected = rememberCallback(coordinator) {
                                    scope.launch {
                                        coordinator.toggleDrawer()
                                        coordinator.sendEvent(DrawerEvent.OpenBookmarks)
                                    }
                                },
                            )
                        }
                        item {
                            DrawerShortcut(
                                title = stringResource(MR.strings.navigation_drawer_title_subscriptions),
                                icon = Icons.Default.ManageAccounts,
                                onSelected = rememberCallback(coordinator) {
                                    scope.launch {
                                        coordinator.toggleDrawer()
                                        coordinator.sendEvent(DrawerEvent.ManageSubscriptions)
                                    }
                                },
                            )
                        }

                        items(
                            items = uiState.multiCommunities,
                            key = { it.communityIds.joinToString() },
                        ) { community ->
                            MultiCommunityItem(
                                modifier = Modifier.fillMaxWidth().onClick(
                                    onClick = rememberCallback {
                                        scope.launch {
                                            coordinator.toggleDrawer()
                                            coordinator.sendEvent(
                                                DrawerEvent.OpenMultiCommunity(community),
                                            )
                                        }
                                    },
                                ),
                                community = community,
                                small = true,
                                autoLoadImages = uiState.autoLoadImages,
                            )
                        }
                        items(
                            items = uiState.communities,
                            key = { it.id.toString() + it.favorite.toString() },
                        ) { community ->
                            CommunityItem(
                                modifier = Modifier.fillMaxWidth().onClick(
                                    onClick = rememberCallback {
                                        scope.launch {
                                            coordinator.toggleDrawer()
                                            coordinator.sendEvent(
                                                DrawerEvent.OpenCommunity(community),
                                            )
                                        }
                                    },
                                ),
                                community = community,
                                small = true,
                                showFavorite = true,
                                autoLoadImages = uiState.autoLoadImages,
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
                    text = stringResource(MR.strings.sidebar_not_logged_message),
                    style = MaterialTheme.typography.bodySmall,
                )

                Text(
                    modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.s),
                    text = stringResource(MR.strings.home_listing_title),
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
                                            DrawerEvent.ChangeListingType(listingType)
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