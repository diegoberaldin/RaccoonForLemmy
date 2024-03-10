package com.github.diegoberaldin.raccoonforlemmy.unit.messages

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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.theme.Spacing
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.detailopener.api.getDetailOpener
import com.github.diegoberaldin.raccoonforlemmy.core.l10n.LocalXmlStrings
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.TabNavigationSection
import com.github.diegoberaldin.raccoonforlemmy.core.navigation.di.getNavigationCoordinator
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallback
import com.github.diegoberaldin.raccoonforlemmy.core.utils.compose.rememberCallbackArgs
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.otherUser
import com.github.diegoberaldin.raccoonforlemmy.unit.chat.InboxChatScreen
import com.github.diegoberaldin.raccoonforlemmy.unit.messages.components.ChatCard
import com.github.diegoberaldin.raccoonforlemmy.unit.messages.components.ChatCardPlaceholder
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
        val model = getScreenModel<InboxMessagesMviModel>()
        val uiState by model.uiState.collectAsState()
        val navigationCoordinator = remember { getNavigationCoordinator() }
        val lazyListState = rememberLazyListState()
        val detailOpener = remember { getDetailOpener() }

        LaunchedEffect(navigationCoordinator) {
            navigationCoordinator.onDoubleTabSelection.onEach { section ->
                if (section == TabNavigationSection.Inbox) {
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

                    InboxMessagesMviModel.Effect.BackToTop -> {
                        lazyListState.scrollToItem(0)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.m),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                if (uiState.chats.isEmpty() && uiState.initial) {
                    items(3) {
                        ChatCardPlaceholder()
                    }
                }
                if (uiState.chats.isEmpty() && !uiState.initial) {
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
                    items = uiState.chats,
                    key = {
                        it.id.toString() + (it.updateDate ?: it.publishDate) + it.read + uiState.unreadOnly
                    },
                ) { chat ->
                    ChatCard(
                        user = chat.otherUser(uiState.currentUserId),
                        autoLoadImages = uiState.autoLoadImages,
                        lastMessage = chat.content.orEmpty(),
                        lastMessageDate = chat.publishDate,
                        onOpenUser = rememberCallbackArgs { user ->
                            detailOpener.openUserDetail(user, "")
                        },
                        onOpen = rememberCallback {
                            val userId = chat.otherUser(uiState.currentUserId)?.id
                            if (userId != null) {
                                navigationCoordinator.pushScreen(
                                    InboxChatScreen(userId),
                                )
                            }
                        },
                    )
                }
                item {
                    if (!uiState.initial && !uiState.loading && !uiState.refreshing && uiState.canFetchMore) {
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
