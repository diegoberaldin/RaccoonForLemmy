package com.github.diegoberaldin.raccoonforlemmy.unit.modlog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.di.getSettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ModlogItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.AdminPurgeCommentItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.AdminPurgeCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.AdminPurgePersonItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.AdminPurgePostItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.HideCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModAddCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModAddItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModBanFromCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModBanItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModFeaturePostItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModLockPostItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModRemoveCommentItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModRemovePostItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModTransferCommunityItem
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.ModlogItemPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.unit.modlog.components.RemoveCommunityItem
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf

class ModlogScreen(
    private val communityId: Long? = null,
) : Screen {

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<ModlogMviModel> { parametersOf(communityId) }
        val uiState by model.uiState.collectAsState()
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
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
                            topAppBarState.heightOffset = 0f
                            topAppBarState.contentOffset = 0f
                        }
                    }
                }
            }.launchIn(this)
        }

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.xxs),
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
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                        )
                    },
                    title = {
                        Text(
                            modifier = Modifier.padding(horizontal = Spacing.s),
                            text = LocalXmlStrings.current.modlogTitle,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .then(
                        if (settings.hideNavigationBarWhileScrolling) {
                            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                Box(
                    modifier = Modifier
                        .then(
                            if (settings.hideNavigationBarWhileScrolling) {
                                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            } else {
                                Modifier
                            }
                        )
                        .pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = Spacing.xs),
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(Spacing.interItem)
                    ) {
                        if (uiState.items.isEmpty() && uiState.loading && uiState.initial) {
                            items(5) {
                                ModlogItemPlaceholder(uiState.postLayout)
                                if (uiState.postLayout != PostLayout.Card) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                                } else {
                                    Spacer(modifier = Modifier.height(Spacing.interItem))
                                }
                            }
                        }
                        if (uiState.items.isEmpty() && !uiState.initial) {
                            item {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    text = LocalXmlStrings.current.messageEmptyList,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                        items(
                            items = uiState.items,
                            key = { "${it.type}${it.id}" },
                        ) { item ->
                            when (item) {
                                is ModlogItem.ModAdd -> ModAddItem(
                                    item = item,
                                    autoLoadImages = uiState.autoLoadImages,
                                    postLayout = uiState.postLayout,
                                    onOpenUser = rememberCallbackArgs { user ->
                                        detailOpener.openUserDetail(user)
                                    },
                                )

                                is ModlogItem.ModBan -> ModBanItem(
                                    item = item,
                                    autoLoadImages = uiState.autoLoadImages,
                                    postLayout = uiState.postLayout,
                                    onOpenUser = rememberCallbackArgs { user ->
                                        detailOpener.openUserDetail(user)
                                    },
                                )

                                is ModlogItem.ModAddCommunity -> {
                                    ModAddCommunityItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.ModBanFromCommunity -> {
                                    ModBanFromCommunityItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.ModFeaturePost -> {
                                    ModFeaturePostItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.ModLockPost -> {
                                    ModLockPostItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.ModRemoveComment -> {
                                    ModRemoveCommentItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.ModRemovePost -> {
                                    ModRemovePostItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.ModTransferCommunity -> {
                                    ModTransferCommunityItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.AdminPurgeComment -> {
                                    AdminPurgeCommentItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.AdminPurgeCommunity -> {
                                    AdminPurgeCommunityItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.AdminPurgePerson -> {
                                    AdminPurgePersonItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.AdminPurgePost -> {
                                    AdminPurgePostItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.HideCommunity -> {
                                    HideCommunityItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }

                                is ModlogItem.RemoveCommunity -> {
                                    RemoveCommunityItem(
                                        item = item,
                                        autoLoadImages = uiState.autoLoadImages,
                                        postLayout = uiState.postLayout,
                                        onOpenUser = rememberCallbackArgs { user ->
                                            detailOpener.openUserDetail(user)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
