package com.github.diegoberaldin.raccoonforlemmy.core.commonui.instanceinfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getInstanceInfoViewModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class InstanceInfoScreen(
    private val url: String,
    private val onBack: () -> Unit,
) : Screen {
    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalMaterialApi::class,
    )
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getInstanceInfoViewModel(url) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val instanceName = url.replace("https://", "")
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(Spacing.xs),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        Image(
                            modifier = Modifier.onClick {
                                onBack()
                            },
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(MR.strings.instance_detail_title, instanceName),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                )
            },
        ) { paddingValues ->
            val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                model.reduce(InstanceInfoMviModel.Intent.Refresh)
            })
            Box(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(paddingValues)
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    modifier = Modifier.padding(Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    item {
                        Column(
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
                    items(uiState.communities) {
                        val themeRepository = remember { getThemeRepository() }
                        val fontScale by themeRepository.contentFontScale.collectAsState()
                        CompositionLocalProvider(
                            LocalDensity provides Density(
                                density = LocalDensity.current.density,
                                fontScale = fontScale,
                            ),
                        ) {
                            CommunityItem(
                                modifier = Modifier.onClick {
                                    navigator.push(
                                        CommunityDetailScreen(
                                            community = it,
                                            otherInstance = instanceName,
                                            onBack = {
                                                navigator.pop()
                                            },
                                        ),
                                    )
                                },
                                community = it,
                            )
                        }
                    }
                    item {
                        if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                            model.reduce(InstanceInfoMviModel.Intent.LoadNextPage)
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
                }

                PullRefreshIndicator(
                    refreshing = uiState.refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
