package com.github.diegoberaldin.raccoonforlemmy.feature.home.postlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.ListingTypeBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.modals.SortBottomSheet
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.feature.home.di.getHomeScreenModel

class PostListScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getHomeScreenModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val navigator = LocalNavigator.current?.parent ?: throw Exception("Navigator not found")
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            modifier = Modifier.padding(Spacing.xxs),
            topBar = {
                PostsTopBar(
                    currentInstance = uiState.instance,
                    listingType = uiState.listingType,
                    sortType = uiState.sortType,
                    scrollBehavior = scrollBehavior,
                    onSelectListingType = {
                        bottomSheetNavigator.show(
                            ListingTypeBottomSheet(
                                isLogged = uiState.isLogged,
                                onSelected = {
                                    model.reduce(PostListMviModel.Intent.ChangeListing(it))
                                },
                                onHide = {
                                    bottomSheetNavigator.hide()
                                },
                            ),
                        )
                    },
                    onSelectSortType = {
                        bottomSheetNavigator.show(
                            SortBottomSheet(
                                expandTop = true,
                                onSelected = {
                                    model.reduce(PostListMviModel.Intent.ChangeSort(it))
                                },
                                onHide = {
                                    bottomSheetNavigator.hide()
                                },
                            ),
                        )
                    },
                )
            },
        ) { padding ->
            val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
                model.reduce(PostListMviModel.Intent.Refresh)
            })
            Box(
                modifier = Modifier
                    .padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .pullRefresh(pullRefreshState),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    items(uiState.posts) { post ->
                        SwipeableCard(
                            modifier = Modifier.fillMaxWidth(),
                            onGestureBegin = {
                                model.reduce(PostListMviModel.Intent.HapticIndication)
                            },
                            onDismissToStart = {
                                model.reduce(PostListMviModel.Intent.UpVotePost(post))
                            },
                            onDismissToEnd = {
                                model.reduce(PostListMviModel.Intent.DownVotePost(post))
                            },
                            backgroundColor = {
                                when (it) {
                                    DismissValue.DismissedToStart -> MaterialTheme.colorScheme.secondary
                                    DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.tertiary
                                    else -> Color.Transparent
                                }
                            },
                            swipeContent = { direction ->
                                val icon = when (direction) {
                                    DismissDirection.StartToEnd -> Icons.Default.ArrowCircleDown
                                    DismissDirection.EndToStart -> Icons.Default.ArrowCircleUp
                                }
                                val (iconModifier, iconTint) = when {
                                    direction == DismissDirection.StartToEnd && post.myVote < 0 -> {
                                        Modifier.background(
                                            color = Color.Transparent,
                                            shape = CircleShape,
                                        ) to MaterialTheme.colorScheme.onTertiary
                                    }

                                    direction == DismissDirection.StartToEnd -> {
                                        Modifier.background(
                                            color = MaterialTheme.colorScheme.onTertiary,
                                            shape = CircleShape,
                                        ) to MaterialTheme.colorScheme.tertiary
                                    }

                                    direction == DismissDirection.EndToStart && post.myVote > 0 -> {
                                        Modifier.background(
                                            color = Color.Transparent,
                                            shape = CircleShape,
                                        ) to MaterialTheme.colorScheme.onSecondary
                                    }

                                    else -> {
                                        Modifier.background(
                                            color = MaterialTheme.colorScheme.onSecondary,
                                            shape = CircleShape,
                                        ) to MaterialTheme.colorScheme.secondary
                                    }
                                }
                                Icon(
                                    modifier = iconModifier,
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                )
                            },
                            content = {
                                PostCard(
                                    modifier = Modifier.onClick {
                                        navigator.push(
                                            PostDetailScreen(
                                                post = post,
                                                onBack = {
                                                    navigator.pop()
                                                },
                                            ),
                                        )
                                    },
                                    post = post,
                                    blurNsfw = uiState.blurNsfw,
                                    onOpenCommunity = { community ->
                                        navigator.push(
                                            CommunityDetailScreen(
                                                community = community,
                                                onBack = {
                                                    navigator.pop()
                                                },
                                            ),
                                        )
                                    },
                                    onOpenCreator = { user ->
                                        navigator.push(
                                            UserDetailScreen(
                                                user = user,
                                                onBack = {
                                                    navigator.pop()
                                                },
                                            ),
                                        )
                                    },
                                    onUpVote = {
                                        model.reduce(
                                            PostListMviModel.Intent.UpVotePost(
                                                post = post,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onDownVote = {
                                        model.reduce(
                                            PostListMviModel.Intent.DownVotePost(
                                                post = post,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onSave = {
                                        model.reduce(
                                            PostListMviModel.Intent.SavePost(
                                                post = post,
                                                feedback = true,
                                            ),
                                        )
                                    },
                                    onReply = {
                                        bottomSheetNavigator.show(
                                            CreateCommentScreen(
                                                originalPost = post,
                                                onCommentCreated = {
                                                    bottomSheetNavigator.hide()
                                                    model.reduce(PostListMviModel.Intent.Refresh)
                                                }
                                            )
                                        )
                                    }
                                )
                            },
                        )
                    }
                    item {
                        if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                            model.reduce(PostListMviModel.Intent.LoadNextPage)
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
