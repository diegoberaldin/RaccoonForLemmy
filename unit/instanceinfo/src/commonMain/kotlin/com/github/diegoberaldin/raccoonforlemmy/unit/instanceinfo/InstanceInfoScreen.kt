package com.github.diegoberaldin.raccoonforlemmy.unit.instanceinfo

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CustomizedContent
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.CommunityItemPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.getAdditionalLabel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toIcon
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toInt
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.parameter.parametersOf

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
        val model = getScreenModel<InstanceInfoMviModel>(
            tag = instanceName,
            parameters = { parametersOf(url) }
        )
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val listState = rememberLazyListState()
        val detailOpener = remember { getDetailOpener() }

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
                                onClick = rememberCallback {
                                    navigationCoordinator.popScreen()
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
                                    onClick = rememberCallback {
                                        val sheet = SortBottomSheet(
                                            sheetKey = key,
                                            comments = false,
                                            values = uiState.availableSortTypes.map { it.toInt() },
                                            expandTop = true,
                                        )
                                        navigationCoordinator.showBottomSheet(sheet)
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
                        CustomizedContent {
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
                    items(uiState.communities) { community ->
                        CommunityItem(
                            modifier = Modifier.onClick(
                                onClick = rememberCallback {
                                    detailOpener.openCommunityDetail(community, instanceName)
                                },
                            ),
                            community = community,
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
