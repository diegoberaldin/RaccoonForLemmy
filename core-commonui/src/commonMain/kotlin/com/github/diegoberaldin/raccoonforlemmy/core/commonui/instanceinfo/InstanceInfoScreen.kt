package com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.ScaledContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getInstanceInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.selectcommunity.CommunityItemPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.getAdditionalLabel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class InstanceInfoScreen(
    private val url: String,
) : Screen {

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterialApi::class,
    )
    @Composable
    override fun Content() {
        val instanceName = url.replace("https://", "")
        val model = rememberScreenModel(instanceName) { getInstanceInfoViewModel(url) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val notificationCenter = remember { getNotificationCenter() }
        val listState = rememberLazyListState()

        DisposableEffect(key) {
            notificationCenter.addObserver(
                {
                    (it as? SortType)?.also { sortType ->
                        model.reduce(InstanceInfoMviModel.Intent.ChangeSortType(sortType))
                    }
                },
                key, NotificationCenterContractKeys.ChangeSortType,
            )
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    InstanceInfoMviModel.Effect.BackToTop -> {
                        listState.scrollToItem(0)
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick(
                                rememberCallback {
                                    navigationCoordinator.getRootNavigator()?.pop()
                                },
                            ),
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(MR.strings.instance_detail_title, instanceName),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    actions = {
                        Row {
                            val additionalLabel = uiState.sortType.getAdditionalLabel()
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
                            Image(
                                modifier = Modifier.onClick(
                                    rememberCallback {
                                        val sheet = SortBottomSheet(
                                            values = listOf(
                                                SortType.Active,
                                                SortType.Hot,
                                                SortType.New,
                                                SortType.NewComments,
                                                SortType.MostComments,
                                                SortType.Old,
                                                SortType.Top.Generic,
                                            ),
                                            expandTop = true,
                                        )
                                        navigationCoordinator.getBottomNavigator()?.show(sheet)
                                    },
                                ),
                                imageVector = uiState.sortType.toIcon(),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            )
                        }
                    }
                )
            },
        ) { paddingValues ->
            val pullRefreshState = rememberPullRefreshState(
                refreshing = uiState.refreshing,
                onRefresh = rememberCallback(model) {
                    model.reduce(InstanceInfoMviModel.Intent.Refresh)
                },
            )
            Box(
                modifier = Modifier
                    .let {
                        if (settings.hideNavigationBarWhileScrolling) {
                            it.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            it
                        }
                    }
                    .padding(paddingValues)
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(top = Spacing.m),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    item {
                        ScaledContent {
                            Column(
                                modifier = Modifier.padding(horizontal = Spacing.s),
                                verticalArrangement = Arrangement.spacedBy(Spacing.s),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = uiState.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                if (uiState.description.isNotEmpty()) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = uiState.description,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                }
                                Spacer(modifier = Modifier.height(Spacing.xxxs))
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(MR.strings.instance_detail_communities),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                    if (uiState.communities.isEmpty()) {
                        items(5) {
                            CommunityItemPlaceholder()
                        }
                    }
                    items(uiState.communities) {
                        CommunityItem(
                            modifier = Modifier.onClick(
                                rememberCallback {
                                    navigationCoordinator.getRootNavigator()?.push(
                                        CommunityDetailScreen(
                                            community = it,
                                            otherInstance = instanceName,
                                        ),
                                    )
                                },
                            ),
                            community = it,
                            autoLoadImages = uiState.autoLoadImages,
                            showSubscribers = true,
                        )
                    }
                    item {
                        if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                            model.reduce(InstanceInfoMviModel.Intent.LoadNextPage)
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
