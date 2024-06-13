package com.github.diegoberaldin.raccoonforlemmy.unit.mentions

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.di.getThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeAction
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.components.SwipeActionCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.InboxCard
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.InboxCardPlaceholder
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.InboxCardType
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.messages.LocalStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.unit.zoomableimage.ZoomableImageScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class InboxMentionsScreen : Tab {
    override val options: TabOptions
        @Composable get() {
            return TabOptions(1u, "")
        }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = getScreenModel<InboxMentionsMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val lazyListState = rememberLazyListState()
        val detailOpener = remember { getDetailOpener() }
        val themeRepository = remember { getThemeRepository() }
        val upVoteColor by themeRepository.upVoteColor.collectAsState()
        val downVoteColor by themeRepository.downVoteColor.collectAsState()
        val toggleReadColor by themeRepository.saveColor.collectAsState()
        val defaultUpvoteColor = MaterialTheme.colorScheme.primary
        val defaultToggleReadColor = MaterialTheme.colorScheme.secondary
        val defaultDownVoteColor = MaterialTheme.colorScheme.tertiary

        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.onDoubleTabSelection.onEach { section ->
                runCatching {
                    if (section == TabNavigationSection.Inbox) {
                        lazyListState.scrollToItem(0)
                    }
                }
            }.launchIn(this)
        }
        LaunchedEffect(model) {
            model.effects.onEach { effect ->
                when (effect) {
                    is InboxMentionsMviModel.Effect.UpdateUnreadItems -> {
                        navigationCoordinator.setInboxUnread(effect.value)
                    }

                    InboxMentionsMviModel.Effect.BackToTop -> {
                        runCatching {
                            lazyListState.scrollToItem(0)
                        }
                    }
                }
            }.launchIn(this)
        }

        val pullRefreshState =
            rememberPullRefreshState(
                refreshing = uiState.refreshing,
                onRefresh =
                    rememberCallback(model) {
                        model.reduce(InboxMentionsMviModel.Intent.Refresh)
                    },
            )
        Box(
            modifier = Modifier.pullRefresh(pullRefreshState),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
            ) {
                if (uiState.mentions.isEmpty() && uiState.initial) {
                    items(3) {
                        InboxCardPlaceholder(
                            postLayout = uiState.postLayout,
                        )
                        if (uiState.postLayout != PostLayout.Card) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                        } else {
                            Spacer(modifier = Modifier.height(Spacing.interItem))
                        }
                    }
                }
                if (uiState.mentions.isEmpty() && !uiState.initial) {
                    item {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                            textAlign = TextAlign.Center,
                            text = LocalStrings.current.messageEmptyList,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
                items(
                    items = uiState.mentions,
                    key = { it.id.toString() + it.read + uiState.unreadOnly },
                ) { mention ->
                    @Composable
                    fun List<ActionOnSwipe>.toSwipeActions(): List<SwipeAction> =
                        mapNotNull {
                            when (it) {
                                ActionOnSwipe.UpVote ->
                                    SwipeAction(
                                        swipeContent = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowCircleUp,
                                                contentDescription = null,
                                                tint = Color.White,
                                            )
                                        },
                                        backgroundColor = upVoteColor ?: defaultUpvoteColor,
                                        onTriggered =
                                            rememberCallback {
                                                model.reduce(
                                                    InboxMentionsMviModel.Intent.UpVoteComment(
                                                        mention.id,
                                                    ),
                                                )
                                            },
                                    )

                                ActionOnSwipe.DownVote ->
                                    if (!uiState.downVoteEnabled) {
                                        null
                                    } else {
                                        SwipeAction(
                                            swipeContent = {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowCircleDown,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                )
                                            },
                                            backgroundColor = downVoteColor ?: defaultDownVoteColor,
                                            onTriggered =
                                                rememberCallback {
                                                    model.reduce(
                                                        InboxMentionsMviModel.Intent.DownVoteComment(mention.id),
                                                    )
                                                },
                                        )
                                    }

                                ActionOnSwipe.ToggleRead ->
                                    SwipeAction(
                                        swipeContent = {
                                            val icon =
                                                when {
                                                    mention.read -> Icons.Default.MarkChatUnread
                                                    else -> Icons.Default.MarkChatRead
                                                }
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color.White,
                                            )
                                        },
                                        backgroundColor = toggleReadColor ?: defaultToggleReadColor,
                                        onTriggered =
                                            rememberCallback {
                                                model.reduce(
                                                    InboxMentionsMviModel.Intent.MarkAsRead(
                                                        read = !mention.read,
                                                        id = mention.id,
                                                    ),
                                                )
                                            },
                                    )

                                else -> null
                            }
                        }

                    SwipeActionCard(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.swipeActionsEnabled,
                        onGestureBegin =
                            rememberCallback(model) {
                                model.reduce(InboxMentionsMviModel.Intent.HapticIndication)
                            },
                        swipeToStartActions = uiState.actionsOnSwipeToStartInbox.toSwipeActions(),
                        swipeToEndActions = uiState.actionsOnSwipeToEndInbox.toSwipeActions(),
                        content = {
                            InboxCard(
                                mention = mention,
                                postLayout = uiState.postLayout,
                                type = InboxCardType.Mention,
                                autoLoadImages = uiState.autoLoadImages,
                                preferNicknames = uiState.preferNicknames,
                                showScores = uiState.showScores,
                                voteFormat = uiState.voteFormat,
                                downVoteEnabled = uiState.downVoteEnabled,
                                onOpenPost =
                                    rememberCallbackArgs { post ->
                                        if (!mention.read) {
                                            model.reduce(
                                                InboxMentionsMviModel.Intent.MarkAsRead(
                                                    read = true,
                                                    id = mention.id,
                                                ),
                                            )
                                        }
                                        detailOpener.openPostDetail(
                                            post = post,
                                            highlightCommentId = mention.comment.id,
                                            otherInstance = "",
                                        )
                                    },
                                onOpenCreator =
                                    rememberCallbackArgs { user ->
                                        detailOpener.openUserDetail(user)
                                    },
                                onOpenCommunity =
                                    rememberCallbackArgs { community ->
                                        detailOpener.openCommunityDetail(community = community)
                                    },
                                onImageClick =
                                    rememberCallbackArgs { url ->
                                        navigationCoordinator.pushScreen(
                                            ZoomableImageScreen(
                                                url = url,
                                                source = mention.post.community?.readableHandle.orEmpty(),
                                            ),
                                        )
                                    },
                                onUpVote =
                                    rememberCallback(model) {
                                        model.reduce(InboxMentionsMviModel.Intent.UpVoteComment(mention.id))
                                    },
                                onDownVote =
                                    rememberCallback(model) {
                                        model.reduce(
                                            InboxMentionsMviModel.Intent.DownVoteComment(mention.id),
                                        )
                                    },
                                onReply =
                                    rememberCallback {
                                        detailOpener.openReply(
                                            originalPost = mention.post,
                                            originalComment = mention.comment,
                                        )
                                    },
                            )
                        },
                    )
                    if (uiState.postLayout != PostLayout.Card) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.interItem))
                    } else {
                        Spacer(modifier = Modifier.height(Spacing.interItem))
                    }
                }
                item {
                    if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                        model.reduce(InboxMentionsMviModel.Intent.LoadNextPage)
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
