package com.github.diegoberaldin.raccoonforlemmy.feature.search.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommentCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.CommunityItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserItem
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.image.ZoomableImageScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.di.getNotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SearchResultType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.feature.search.di.getExploreViewModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource

class ExploreScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getExploreViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = remember { getNavigationCoordinator().getRootNavigator() }
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val notificationCenter = remember { getNotificationCenter() }
        DisposableEffect(key) {
            onDispose {
                notificationCenter.removeObserver(key)
            }
        }

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                CommunityTopBar(
                    scrollBehavior = scrollBehavior,
                    listingType = uiState.listingType,
                    sortType = uiState.sortType,
                    onSelectListingType = {
                        val sheet = ListingTypeBottomSheet(
                            isLogged = uiState.isLogged,
                        )
                        notificationCenter.addObserver({ result ->
                            (result as? ListingType)?.also {
                                model.reduce(ExploreMviModel.Intent.SetListingType(it))
                            }
                        }, key, NotificationCenterContractKeys.ChangeFeedType)
                        bottomSheetNavigator.show(sheet)
                    },
                    onSelectSortType = {
                        val sheet = SortBottomSheet(
                            expandTop = true,
                        )
                        notificationCenter.addObserver({
                            (it as? SortType)?.also { sortType ->
                                model.reduce(
                                    ExploreMviModel.Intent.SetSortType(sortType)
                                )
                            }
                        }, key, NotificationCenterContractKeys.ChangeSortType)
                        bottomSheetNavigator.show(sheet)
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                TextField(
                    modifier = Modifier.padding(
                        horizontal = Spacing.m,
                        vertical = Spacing.s,
                    ).fillMaxWidth(),
                    label = {
                        Text(text = stringResource(MR.strings.explore_search_placeholder))
                    },
                    singleLine = true,
                    value = uiState.searchText,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                    ),
                    onValueChange = { value ->
                        model.reduce(ExploreMviModel.Intent.SetSearch(value))
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.onClick {
                                if (uiState.searchText.isNotEmpty()) {
                                    model.reduce(ExploreMviModel.Intent.SetSearch(""))
                                }
                            },
                            imageVector = if (uiState.searchText.isEmpty()) Icons.Default.Search else Icons.Default.Clear,
                            contentDescription = null,
                        )
                    },
                )
                val currentSection = when (uiState.resultType) {
                    SearchResultType.Posts -> 1
                    SearchResultType.Comments -> 2
                    SearchResultType.Communities -> 3
                    SearchResultType.Users -> 4
                    else -> 0
                }
                ScrollableTabRow(
                    selectedTabIndex = currentSection,
                    edgePadding = 0.dp,
                    tabs = {
                        listOf(
                            stringResource(MR.strings.explore_result_type_all),
                            stringResource(MR.strings.explore_result_type_posts),
                            stringResource(MR.strings.explore_result_type_comments),
                            stringResource(MR.strings.explore_result_type_communities),
                            stringResource(MR.strings.explore_result_type_users),
                        ).forEachIndexed { i, title ->
                            Tab(
                                selected = i == currentSection,
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                onClick = {
                                    val section = when (i) {
                                        1 -> SearchResultType.Posts
                                        2 -> SearchResultType.Comments
                                        3 -> SearchResultType.Communities
                                        4 -> SearchResultType.Users
                                        else -> SearchResultType.All
                                    }
                                    model.reduce(ExploreMviModel.Intent.SetResultType(section))
                                },
                            )
                        }
                    }
                )

                val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                    model.reduce(ExploreMviModel.Intent.Refresh)
                })
                Box(
                    modifier = Modifier.padding(Spacing.xxs).pullRefresh(pullRefreshState),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        itemsIndexed(uiState.results) { idx, result ->
                            val themeRepository = remember { getThemeRepository() }
                            val fontScale by themeRepository.contentFontScale.collectAsState()
                            CompositionLocalProvider(
                                LocalDensity provides Density(
                                    density = LocalDensity.current.density,
                                    fontScale = fontScale,
                                ),
                            ) {
                                when (result) {
                                    is CommunityModel -> {
                                        CommunityItem(
                                            modifier = Modifier.fillMaxWidth().onClick {
                                                navigator?.push(
                                                    CommunityDetailScreen(result),
                                                )
                                            },
                                            community = result,
                                        )
                                    }

                                    is PostModel -> {
                                        PostCard(
                                            modifier = Modifier.onClick {
                                                navigator?.push(
                                                    PostDetailScreen(result),
                                                )
                                            },
                                            post = result,
                                            blurNsfw = uiState.blurNsfw,
                                            onOpenCommunity = { community ->
                                                navigator?.push(
                                                    CommunityDetailScreen(community),
                                                )
                                            },
                                            onOpenCreator = { user ->
                                                navigator?.push(
                                                    UserDetailScreen(user),
                                                )
                                            },
                                            onUpVote = {
                                                model.reduce(
                                                    ExploreMviModel.Intent.UpVotePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onDownVote = {
                                                model.reduce(
                                                    ExploreMviModel.Intent.DownVotePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onSave = {
                                                model.reduce(
                                                    ExploreMviModel.Intent.SavePost(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onReply = {
                                                val screen = CreateCommentScreen(
                                                    originalPost = result,
                                                )
                                                notificationCenter.addObserver(
                                                    {
                                                        model.reduce(ExploreMviModel.Intent.Refresh)
                                                    },
                                                    key,
                                                    NotificationCenterContractKeys.CommentCreated
                                                )
                                                bottomSheetNavigator.show(screen)
                                            },
                                            onImageClick = { url ->
                                                navigator?.push(
                                                    ZoomableImageScreen(url),
                                                )
                                            },
                                        )
                                    }

                                    is CommentModel -> {
                                        CommentCard(
                                            comment = result,
                                            onUpVote = {
                                                model.reduce(
                                                    ExploreMviModel.Intent.UpVoteComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onDownVote = {
                                                model.reduce(
                                                    ExploreMviModel.Intent.DownVoteComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onSave = {
                                                model.reduce(
                                                    ExploreMviModel.Intent.SaveComment(
                                                        index = idx,
                                                        feedback = true,
                                                    ),
                                                )
                                            },
                                            onReply = {
                                                val screen = CreateCommentScreen(
                                                    originalPost = PostModel(id = result.postId),
                                                    originalComment = result,
                                                )
                                                notificationCenter.addObserver(
                                                    {
                                                        model.reduce(ExploreMviModel.Intent.Refresh)
                                                    },
                                                    key,
                                                    NotificationCenterContractKeys.CommentCreated
                                                )
                                                bottomSheetNavigator.show(screen)
                                            },
                                        )
                                    }

                                    is UserModel -> {
                                        UserItem(
                                            modifier = Modifier.fillMaxWidth().onClick {
                                                navigator?.push(
                                                    UserDetailScreen(result),
                                                )
                                            },
                                            user = result,
                                        )
                                    }

                                    else -> {
                                        Text(
                                            modifier = Modifier.padding(Spacing.s),
                                            text = "Unknown result type: ${result::class.simpleName}"
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                                model.reduce(ExploreMviModel.Intent.LoadNextPage)
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

                        item {
                            Spacer(modifier = Modifier.height(Spacing.xxxl))
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
}
