package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail.CommunityDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.InboxCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.InboxCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.InboxCardType
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.Option
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.OptionId
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeableCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail.PostDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di.getInboxRepliesViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.ui.InboxTab
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class InboxRepliesScreen : Tab {
    override val options: TabOptions
        @Composable get() {
            return TabOptions(0u, "")
        }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getInboxRepliesViewModel() }
        model.bindToLifecycle(key)
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val lazyListState = rememberLazyListState()

        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.onDoubleTabSelection.onEach {
                if (it == InboxTab) {
                    lazyListState.scrollToItem(0)
                }
            }.launchIn(this)
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    is InboxRepliesMviModel.Effect.UpdateUnreadItems -> {
                        navigationCoordinator.setInboxUnread(effect.value)
                    }

                    InboxRepliesMviModel.Effect.BackToTop -> {
                        lazyListState.scrollToItem(0)
                    }
                }
            }.launchIn(this)
        }

        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.refreshing,
            onRefresh = rememberCallback(model) {
                model.reduce(InboxRepliesMviModel.Intent.Refresh)
            },
        )
        Box(
            modifier = Modifier.pullRefresh(pullRefreshState),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                if (uiState.replies.isEmpty() && uiState.initial) {
                    items(3) {
                        InboxCardPlaceholder(
                            postLayout = uiState.postLayout,
                        )
                        if (uiState.postLayout != PostLayout.Card) {
                            Divider(modifier = Modifier.padding(vertical = Spacing.s))
                        } else {
                            Spacer(modifier = Modifier.height(Spacing.s))
                        }
                    }
                }
                if (uiState.replies.isEmpty() && !uiState.initial) {
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
                    items = uiState.replies,
                    key = { it.id.toString() + it.read + uiState.unreadOnly },
                ) { reply ->
                    val endColor = MaterialTheme.colorScheme.secondary
                    SwipeableCard(
                        modifier = Modifier.fillMaxWidth(),
                        directions = setOf(DismissDirection.EndToStart),
                        enabled = uiState.swipeActionsEnabled,
                        backgroundColor = rememberCallbackArgs {
                            when (it) {
                                DismissValue.DismissedToStart -> endColor
                                else -> Color.Transparent
                            }
                        },
                        onGestureBegin = rememberCallback(model) {
                            model.reduce(InboxRepliesMviModel.Intent.HapticIndication)
                        },
                        onDismissToStart = rememberCallback(model) {
                            model.reduce(
                                InboxRepliesMviModel.Intent.MarkAsRead(
                                    read = !reply.read,
                                    id = reply.id,
                                ),
                            )
                        },
                        swipeContent = { _ ->
                            val icon = when {
                                reply.read -> Icons.Default.MarkChatUnread
                                else -> Icons.Default.MarkChatRead
                            }
                            Icon(
                                modifier = Modifier.padding(Spacing.xs),
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        },
                        content = {
                            InboxCard(
                                mention = reply,
                                postLayout = uiState.postLayout,
                                type = InboxCardType.Reply,
                                autoLoadImages = uiState.autoLoadImages,
                                voteFormat = uiState.voteFormat,
                                onOpenPost = rememberCallbackArgs { post ->
                                    navigationCoordinator.pushScreen(
                                        PostDetailScreen(
                                            post = post,
                                            highlightCommentId = reply.comment.id,
                                        ),
                                    )
                                },
                                onOpenCreator = rememberCallbackArgs { user ->
                                    navigationCoordinator.pushScreen(
                                        UserDetailScreen(user),
                                    )
                                },
                                onOpenCommunity = rememberCallbackArgs { community ->
                                    navigationCoordinator.pushScreen(
                                        CommunityDetailScreen(community),
                                    )
                                },
                                onUpVote = rememberCallbackArgs(model) {
                                    model.reduce(InboxRepliesMviModel.Intent.UpVoteComment(reply.id))
                                },
                                onDownVote = rememberCallbackArgs(model) {
                                    model.reduce(InboxRepliesMviModel.Intent.DownVoteComment(reply.id))
                                },
                                options = buildList {
                                    add(
                                        Option(
                                            OptionId.ToggleRead,
                                            if (reply.read) {
                                                stringResource(MR.strings.inbox_action_mark_unread)
                                            } else {
                                                stringResource(MR.strings.inbox_action_mark_read)
                                            },
                                        )
                                    )
                                },
                                onOptionSelected = rememberCallbackArgs(model) { optionId ->
                                    when (optionId) {
                                        OptionId.ToggleRead -> model.reduce(
                                            InboxRepliesMviModel.Intent.MarkAsRead(
                                                read = !reply.read,
                                                id = reply.id,
                                            ),
                                        )

                                        else -> Unit
                                    }
                                }
                            )
                        },
                    )
                    if (uiState.postLayout != PostLayout.Card) {
                        Divider(modifier = Modifier.padding(vertical = Spacing.s))
                    } else {
                        Spacer(modifier = Modifier.height(Spacing.s))
                    }
                }
                item {
                    if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                        model.reduce(InboxRepliesMviModel.Intent.LoadNextPage)
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
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
