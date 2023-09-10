package com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.posts

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.racconforlemmy.core.utils.onClick
import com.github.diegoberaldin.racconforlemmy.core.utils.toLocalPixel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.PostCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SectionSelector
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserCounters
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.UserHeader
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment.CreateCommentScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getUserPostsViewModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailSection
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailViewModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class UserDetailPostsScreen(
    private val user: UserModel,
    ) : Tab {

    var onSectionSelected: ((UserDetailSection) -> Unit)? = null
    var parentModel: UserDetailViewModel? = null

    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel(
            user.id.toString(),
        ) {
            getUserPostsViewModel(
                user = user,
            )
        }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val bottomSheetNavigator = LocalBottomSheetNavigator.current

        LaunchedEffect(parentModel) {
            parentModel?.uiState?.map { it.sortType }?.distinctUntilChanged()?.onEach { sortType ->
                model.reduce(UserPostsMviModel.Intent.ChangeSort(sortType))
            }?.launchIn(this)
        }

        val pullRefreshState = rememberPullRefreshState(uiState.refreshing, {
            model.reduce(UserPostsMviModel.Intent.Refresh)
        })
        Box(
            modifier = Modifier.pullRefresh(pullRefreshState),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        UserHeader(user = uiState.user)
                        UserCounters(
                            modifier = Modifier.graphicsLayer(translationY = -Spacing.m.toLocalPixel()),
                            user = uiState.user,
                        )
                        Spacer(modifier = Modifier.height(Spacing.s))
                        SectionSelector(
                            titles = listOf(
                                stringResource(MR.strings.profile_section_posts),
                                stringResource(MR.strings.profile_section_comments),
                            ),
                            currentSection = 0,
                            onSectionSelected = {
                                val section = when (it) {
                                    0 -> UserDetailSection.POSTS
                                    else -> UserDetailSection.COMMENTS
                                }
                                onSectionSelected?.invoke(section)
                            },
                        )
                    }
                }
                items(uiState.posts) { post ->
                    SwipeableCard(
                        modifier = Modifier.fillMaxWidth(),
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
                        onGestureBegin = {
                            model.reduce(UserPostsMviModel.Intent.HapticIndication)
                        },
                        onDismissToStart = {
                            model.reduce(
                                UserPostsMviModel.Intent.UpVotePost(
                                    post = post,
                                ),
                            )
                        },
                        onDismissToEnd = {
                            model.reduce(
                                UserPostsMviModel.Intent.DownVotePost(
                                    post = post,
                                ),
                            )
                        },
                        content = {
                            PostCard(
                                modifier = Modifier.onClick {
                                    navigator.push(
                                        PostDetailScreen(
                                            post = post,
                                        ).apply {
                                            onBack = {
                                                navigator.pop()
                                            }
                                        },
                                    )
                                },
                                post = post,
                                blurNsfw = uiState.blurNsfw,
                                onUpVote = {
                                    model.reduce(
                                        UserPostsMviModel.Intent.UpVotePost(
                                            post = post,
                                            feedback = true,
                                        ),
                                    )
                                },
                                onDownVote = {
                                    model.reduce(
                                        UserPostsMviModel.Intent.DownVotePost(
                                            post = post,
                                            feedback = true,
                                        ),
                                    )
                                },
                                onSave = {
                                    model.reduce(
                                        UserPostsMviModel.Intent.SavePost(
                                            post = post,
                                            feedback = true,
                                        ),
                                    )
                                },
                                onOpenCommunity = { community ->
                                    navigator.push(
                                        CommunityDetailScreen(
                                            community = community,
                                        ).apply {
                                            onBack = {
                                                navigator.pop()
                                            }
                                        },
                                    )
                                },
                                onReply = {
                                    bottomSheetNavigator.show(
                                        CreateCommentScreen(
                                            originalPost = post,
                                            onCommentCreated = {
                                                bottomSheetNavigator.hide()
                                                model.reduce(UserPostsMviModel.Intent.Refresh)
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
                        model.reduce(UserPostsMviModel.Intent.LoadNextPage)
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
                    Spacer(modifier = Modifier.height(Spacing.s))
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
