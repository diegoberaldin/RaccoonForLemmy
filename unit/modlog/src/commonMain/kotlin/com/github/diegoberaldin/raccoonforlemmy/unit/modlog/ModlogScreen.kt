package com.github.diegoberaldin.raccoonforlemmy.unit.modlog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModlogItemPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.di.getModlogViewModel
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ModlogScreen(
    private val communityId: Int,
) : Screen {

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getModlogViewModel(communityId) }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val scope = rememberCoroutineScope()
        val settingsRepository = remember { getSettingsRepository() }
        val settings by settingsRepository.currentSettings.collectAsState()
        val lazyListState = rememberLazyListState()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.refreshing,
            onRefresh = rememberCallback(model) {
                model.reduce(ModlogMviModel.Intent.Refresh)
            },
        )
        val detailOpener = remember { getDetailOpener() }

        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    ModlogMviModel.Effect.BackToTop -> {
                        scope.launch {
                            lazyListState.scrollToItem(0)
                        }
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
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
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = stringResource(MR.strings.modlog_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues).let {
                    if (settings.hideNavigationBarWhileScrolling) {
                        it.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        it
                    }
                },
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Box(
                    modifier = Modifier
                        .let {
                            if (settings.hideNavigationBarWhileScrolling) {
                                it.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                it
                            }
                        }
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        state = lazyListState,
                    ) {
                        if (uiState.items.isEmpty() && uiState.loading && uiState.initial) {
                            items(5) {
                                ModlogItemPlaceholder(uiState.postLayout)
                                if (uiState.postLayout != PostLayout.Card) {
                                    Divider(modifier = Modifier.padding(vertical = Spacing.s))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.s))
                                }
                            }
                        }
                        if (uiState.items.isEmpty() && !uiState.initial) {
                            item {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    text = stringResource(MR.strings.message_empty_list),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                        items(
                            items = uiState.items,
                            key = { "${it.type}_${it.id}" },
                        ) { item ->
                            when (item) {
                                is ModlogItem.ModAdd -> {
                                    ModAddItem(item = item)
                                }

                                is ModlogItem.ModBanFromCommunity -> {
                                    ModBanFromCommunityItem(item = item)
                                }

                                is ModlogItem.ModFeaturePost -> {
                                    ModFeaturePostItem(item = item)
                                }

                                is ModlogItem.ModLockPost -> {
                                    ModLockPostItem(item = item)
                                }

                                is ModlogItem.ModRemoveComment -> {
                                    ModRemoveCommentItem(item = item)
                                }

                                is ModlogItem.ModRemovePost -> {
                                    ModRemovePostItem(item = item)
                                }

                                is ModlogItem.ModTransferCommunity -> {
                                    ModTransferCommunityItem(item)
                                }

                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModAddItem(
    item: ModlogItem.ModAdd,
) {
    Text(
        text = "Mod was added",
    )
}

@Composable
fun ModBanFromCommunityItem(
    item: ModlogItem.ModBanFromCommunity,
) {
    Text(
        text = "User was banned",
    )
}

@Composable
fun ModFeaturePostItem(
    item: ModlogItem.ModFeaturePost,
) {
    Text(
        text = "Post was featured",
    )
}

@Composable
fun ModLockPostItem(
    item: ModlogItem.ModLockPost,
) {
    Text(
        text = "Post was locked",
    )
}

@Composable
fun ModRemovePostItem(
    item: ModlogItem.ModRemovePost,
) {
    Text(
        text = "Post was removed",
    )
}

@Composable
fun ModRemoveCommentItem(
    item: ModlogItem.ModRemoveComment,
) {
    Text(
        text = "Comment was removed",
    )
}

@Composable
fun ModTransferCommunityItem(
    item: ModlogItem.ModTransferCommunity,
) {
    Text(
        text = "Community was transferred",
    )
}