package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages

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
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.bindToLifecycle
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.chat.InboxChatScreen
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.UserDetailScreen
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.di.getInboxMessagesViewModel
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.ui.InboxTab
import com.github.diegoberaldin.raccoonforlemmy.resources.MR
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class InboxMessagesScreen : Tab {

    override val options: TabOptions
        @Composable get() {
            return TabOptions(2u, "")
        }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val model = rememberScreenModel { getInboxMessagesViewModel() }
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
                    is InboxMessagesMviModel.Effect.UpdateUnreadItems -> {
                        navigationCoordinator.setInboxUnread(effect.value)
                    }
                }
            }.launchIn(this)
        }

        val pullRefreshState = rememberPullRefreshState(
            refreshing = uiState.refreshing,
            onRefresh = rememberCallback(model) {
                model.reduce(InboxMessagesMviModel.Intent.Refresh)
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
                if (uiState.chats.isEmpty() && uiState.initial) {
                    items(1) {
                        ChatCardPlaceholder()
                    }
                }
                if (uiState.chats.isEmpty() && !uiState.initial) {
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
                items(uiState.chats) { chat ->
                    val otherUser = if (chat.creator?.id == uiState.currentUserId) {
                        chat.recipient
                    } else {
                        chat.creator
                    }
                    ChatCard(
                        user = otherUser,
                        autoLoadImages = uiState.autoLoadImages,
                        lastMessage = chat.content.orEmpty(),
                        lastMessageDate = chat.publishDate,
                        onOpenUser = rememberCallbackArgs { user ->
                            navigationCoordinator.getRootNavigator()?.push(
                                UserDetailScreen(user)
                            )
                        },
                        onOpen = rememberCallback {
                            val userId = otherUser?.id
                            if (userId != null) {
                                navigationCoordinator.getRootNavigator()?.push(
                                    InboxChatScreen(userId)
                                )
                            }
                        }
                    )
                }
                item {
                    if (!uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
                        model.reduce(InboxMessagesMviModel.Intent.LoadNextPage)
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
