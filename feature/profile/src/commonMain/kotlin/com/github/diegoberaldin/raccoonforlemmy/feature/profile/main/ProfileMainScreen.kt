package com.github.diegoberaldin.raccoonforlemmy.feature.profile.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Dimensions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.ModeratorZoneAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.di.getFabNestedScrollConnection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.toModeratorZoneAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ModeratorZoneBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged.ProfileNotLoggedScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.drafts.DraftsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents.FilteredContentsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents.FilteredContentsType
import com.github.diegoberaldin.raccoonforlemmy.unit.filteredcontents.toInt
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.managesubscriptions.ManageSubscriptionsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.ModlogScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.ProfileLoggedScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.reportlist.ReportListScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.saveditems.SavedItemsScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal object ProfileMainScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ProfileMainMviModel>()
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val scope = rememberCoroutineScope()
        val notificationCenter = remember { getNotificationCenter() }
        val fabNestedScrollConnection = remember { getFabNestedScrollConnection() }
        var logoutConfirmDialogOpen by remember { mutableStateOf(false) }

        LaunchedEffect(notificationCenter) {
            notificationCenter.subscribe(NotificationCenterEvent.ModeratorZoneActionSelected::class)
                .onEach {
                    val action = it.value.toModeratorZoneAction()
                    when (action) {
                        ModeratorZoneAction.GlobalModLog -> {
                            navigationCoordinator.pushScreen(ModlogScreen())
                        }

                        ModeratorZoneAction.GlobalReports -> {
                            navigationCoordinator.pushScreen(ReportListScreen())
                        }

                        ModeratorZoneAction.ModeratedContents -> {
                            val screen = FilteredContentsScreen(type = FilteredContentsType.Moderated.toInt())
                            navigationCoordinator.pushScreen(screen)
                        }
                    }
                }.launchIn(this)

            notificationCenter.subscribe(NotificationCenterEvent.ProfileSideMenuAction::class).onEach { evt ->
                navigationCoordinator.closeSideMenu()

                when (evt) {
                    NotificationCenterEvent.ProfileSideMenuAction.ManageAccounts -> {
                        navigationCoordinator.showBottomSheet(ManageAccountsScreen())
                    }

                    NotificationCenterEvent.ProfileSideMenuAction.ManageSubscriptions -> {
                        navigationCoordinator.pushScreen(ManageSubscriptionsScreen())
                    }

                    NotificationCenterEvent.ProfileSideMenuAction.Bookmarks -> {
                        navigationCoordinator.pushScreen(SavedItemsScreen())
                    }

                    NotificationCenterEvent.ProfileSideMenuAction.Drafts -> {
                        navigationCoordinator.pushScreen(DraftsScreen())
                    }

                    NotificationCenterEvent.ProfileSideMenuAction.Votes -> {
                        val screen = FilteredContentsScreen(type = FilteredContentsType.Votes.toInt())
                        navigationCoordinator.pushScreen(screen)
                    }

                    NotificationCenterEvent.ProfileSideMenuAction.ModeratorZone -> {
                        navigationCoordinator.showBottomSheet(ModeratorZoneBottomSheet())
                    }

                    NotificationCenterEvent.ProfileSideMenuAction.Logout -> {
                        logoutConfirmDialogOpen = true
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                val maxTopInset = Dimensions.maxTopBarInset.toLocalPixel()
                var topInset by remember { mutableStateOf(maxTopInset) }
                snapshotFlow { topAppBarState.collapsedFraction }.onEach {
                    topInset = maxTopInset * (1 - it)
                }.launchIn(scope)

                TopAppBar(
                    windowInsets = if (settings.edgeToEdge) {
                        WindowInsets(0, topInset.roundToInt(), 0, 0)
                    } else {
                        TopAppBarDefaults.windowInsets
                    },
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = {
                                    scope.launch {
                                        drawerCoordinator.toggleDrawer()
                                    }
                                },
                            ),
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.navigationProfile,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        if (uiState.logged == true) {
                            Icon(
                                modifier = Modifier
                                    .padding(end = Spacing.s)
                                    .onClick(
                                        onClick = {
                                            notificationCenter.send(NotificationCenterEvent.ProfileSideMenuAction.ManageAccounts)
                                        },
                                    ),
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                            Icon(
                                modifier = Modifier
                                    .padding(horizontal = Spacing.xs)
                                    .onClick(
                                        onClick = {
                                            logoutConfirmDialogOpen = true
                                        },
                                    ),
                                imageVector = Icons.AutoMirrored.Default.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .nestedScroll(fabNestedScrollConnection)
                    .then(
                        if (settings.hideNavigationBarWhileScrolling) {
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // wait until logging status is determined
                val logged = uiState.logged
                if (logged != null) {
                    val screens = remember {
                        listOf(
                            ProfileNotLoggedScreen,
                            ProfileLoggedScreen,
                        )
                    }
                    val root = if (logged) screens[1] else screens[0]
                    TabNavigator(root) {
                        CurrentScreen()
                        val navigator = LocalTabNavigator.current
                        LaunchedEffect(model) {
                            model.uiState.map { s -> s.logged }.distinctUntilChanged()
                                .onEach { logged ->
                                    val index = when (logged) {
                                        true -> 1
                                        else -> 0
                                    }
                                    navigator.current = screens[index]
                                }.launchIn(this)
                        }
                    }
                }
            }
        }

        if (logoutConfirmDialogOpen) {
            AlertDialog(
                onDismissRequest = {
                    logoutConfirmDialogOpen = false
                },
                title = {
                    Text(text = LocalXmlStrings.current.actionLogout)
                },
                text = {
                    Text(text = LocalXmlStrings.current.messageAreYouSure)
                },
                dismissButton = {
                    Button(
                        onClick = {
                            logoutConfirmDialogOpen = false
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonCancel)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            logoutConfirmDialogOpen = false
                            model.reduce(ProfileMainMviModel.Intent.Logout)
                        },
                    ) {
                        Text(text = LocalXmlStrings.current.buttonConfirm)
                    }
                },
            )
        }
    }
}
