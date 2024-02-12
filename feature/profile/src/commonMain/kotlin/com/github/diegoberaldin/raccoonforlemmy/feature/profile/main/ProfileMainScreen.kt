package com.github.diegoberaldin.raccoonforlemmy.feature.profile.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getDrawerCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.feature.profile.notlogged.ProfileNotLoggedScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.manageaccounts.ManageAccountsScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.myaccount.ProfileLoggedScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal object ProfileMainScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ProfileMainMviModel>()
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val drawerCoordinator = remember { getDrawerCoordinator() }
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val scope = rememberCoroutineScope()

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
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
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
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
                            Image(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        navigationCoordinator.showBottomSheet(ManageAccountsScreen())
                                    },
                                ),
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )

                            Spacer(modifier = Modifier.width(Spacing.m))

                            Image(
                                modifier = Modifier.onClick(
                                    onClick = rememberCallback {
                                        model.reduce(ProfileMainMviModel.Intent.Logout)
                                    },
                                ),
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
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
    }
}
