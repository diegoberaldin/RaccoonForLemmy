package com.github.diegoberaldin.raccoonforlemmy.unit.manageban

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItemPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.UserItem
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.unit.manageban.components.InstanceItem
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ManageBanScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ManageBanMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
        val snackbarHostState = remember { SnackbarHostState() }
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val lazyListState = rememberLazyListState()
        val successMessage = LocalXmlStrings.current.messageOperationSuccessful
        val errorMessage = LocalXmlStrings.current.messageGenericError

        LaunchedEffect(model) {
            model.effects.onEach { evt ->
                when (evt) {
                    is ManageBanMviModel.Effect.Failure -> {
                        snackbarHostState.showSnackbar(evt.message ?: errorMessage)
                    }

                    ManageBanMviModel.Effect.Success -> {
                        snackbarHostState.showSnackbar(successMessage)
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.settingsManageBan,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    navigationIcon = {
                        if (navigationCoordinator.canPop.value) {
                            Image(
                                modifier =
                                Modifier.onClick(
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
            val pullRefreshState =
                rememberPullRefreshState(
                    refreshing = uiState.refreshing,
                    onRefresh =
                    rememberCallback(model) {
                        model.reduce(ManageBanMviModel.Intent.Refresh)
                    },
                )
            Column(
                modifier =
                Modifier
                    .padding(
                        top = padding.calculateTopPadding(),
                    )
                    .then(
                        if (settings.hideNavigationBarWhileScrolling) {
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            Modifier
                        },
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                SectionSelector(
                    modifier = Modifier.padding(vertical = Spacing.xs),
                    titles =
                    listOf(
                        LocalXmlStrings.current.exploreResultTypeUsers,
                        LocalXmlStrings.current.exploreResultTypeCommunities,
                        LocalXmlStrings.current.settingsManageBanSectionInstances,
                    ),
                    currentSection =
                    when (uiState.section) {
                        ManageBanSection.Instances -> 2
                        ManageBanSection.Communities -> 1
                        else -> 0
                    },
                    onSectionSelected = {
                        val section =
                            when (it) {
                                2 -> ManageBanSection.Instances
                                1 -> ManageBanSection.Communities
                                else -> ManageBanSection.Users
                            }
                        model.reduce(ManageBanMviModel.Intent.ChangeSection(section))
                    },
                )

                Box(
                    modifier =
                    Modifier
                        .then(
                            if (settings.hideNavigationBarWhileScrolling) {
                                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                Modifier
                            },
                        )
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.s),
                    ) {
                        when (uiState.section) {
                            ManageBanSection.Users -> {
                                if (uiState.bannedUsers.isEmpty()) {
                                    if (uiState.initial) {
                                        items(5) {
                                            CommunityItemPlaceholder()
                                        }
                                    } else {
                                        item {
                                            Text(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = Spacing.xs),
                                                textAlign = TextAlign.Center,
                                                text = LocalXmlStrings.current.messageEmptyList,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        }
                                    }
                                } else {
                                    items(uiState.bannedUsers) { user ->
                                        UserItem(
                                            user = user,
                                            autoLoadImages = uiState.autoLoadImages,
                                            preferNicknames = uiState.preferNicknames,
                                            options =
                                            buildList {
                                                this +=
                                                    Option(
                                                        OptionId.Unban,
                                                        LocalXmlStrings.current.settingsManageBanActionUnban,
                                                    )
                                            },
                                            onOptionSelected =
                                            rememberCallbackArgs(user) { optionId ->
                                                when (optionId) {
                                                    OptionId.Unban -> {
                                                        model.reduce(
                                                            ManageBanMviModel.Intent.UnblockUser(
                                                                user.id
                                                            ),
                                                        )
                                                    }

                                                    else -> Unit
                                                }
                                            },
                                        )
                                    }
                                }
                            }

                            ManageBanSection.Communities -> {
                                if (uiState.bannedCommunities.isEmpty()) {
                                    if (uiState.initial) {
                                        items(5) {
                                            CommunityItemPlaceholder()
                                        }
                                    } else {
                                        item {
                                            Text(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = Spacing.xs),
                                                textAlign = TextAlign.Center,
                                                text = LocalXmlStrings.current.messageEmptyList,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        }
                                    }
                                } else {
                                    items(uiState.bannedCommunities) { community ->
                                        CommunityItem(
                                            community = community,
                                            autoLoadImages = uiState.autoLoadImages,
                                            preferNicknames = uiState.preferNicknames,
                                            options =
                                            buildList {
                                                this +=
                                                    Option(
                                                        OptionId.Unban,
                                                        LocalXmlStrings.current.settingsManageBanActionUnban,
                                                    )
                                            },
                                            onOptionSelected =
                                            rememberCallbackArgs(community) { optionId ->
                                                when (optionId) {
                                                    OptionId.Unban -> {
                                                        model.reduce(
                                                            ManageBanMviModel.Intent.UnblockCommunity(
                                                                community.id
                                                            ),
                                                        )
                                                    }

                                                    else -> Unit
                                                }
                                            },
                                        )
                                    }
                                }
                            }

                            ManageBanSection.Instances -> {
                                if (uiState.bannedInstances.isEmpty()) {
                                    if (uiState.initial) {
                                        items(5) {
                                            CommunityItemPlaceholder()
                                        }
                                    } else {
                                        item {
                                            Text(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = Spacing.xs),
                                                textAlign = TextAlign.Center,
                                                text = LocalXmlStrings.current.messageEmptyList,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            )
                                        }
                                    }
                                } else {
                                    items(uiState.bannedInstances) { instance ->
                                        InstanceItem(
                                            instance = instance,
                                            options =
                                            buildList {
                                                this +=
                                                    Option(
                                                        OptionId.Unban,
                                                        LocalXmlStrings.current.settingsManageBanActionUnban,
                                                    )
                                            },
                                            onOptionSelected =
                                            rememberCallbackArgs(instance) { optionId ->
                                                when (optionId) {
                                                    OptionId.Unban -> {
                                                        model.reduce(
                                                            ManageBanMviModel.Intent.UnblockInstance(
                                                                instance.id
                                                            ),
                                                        )
                                                    }

                                                    else -> Unit
                                                }
                                            },
                                        )
                                    }
                                }
                            }
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
