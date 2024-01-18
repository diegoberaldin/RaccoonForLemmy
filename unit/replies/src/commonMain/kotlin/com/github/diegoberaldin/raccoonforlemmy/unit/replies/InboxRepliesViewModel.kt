package com.github.diegoberaldin.raccoonforlemmy.unit.replies

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxRepliesViewModel(
    private val mvi: DefaultMviModel<InboxRepliesMviModel.Intent, InboxRepliesMviModel.UiState, InboxRepliesMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val siteRepository: SiteRepository,
    private val commentRepository: CommentRepository,
    private val themeRepository: ThemeRepository,
    private val hapticFeedback: HapticFeedback,
    private val coordinator: InboxCoordinator,
    private val notificationCenter: NotificationCenter,
    private val settingsRepository: SettingsRepository,
) : InboxRepliesMviModel,
    MviModel<InboxRepliesMviModel.Intent, InboxRepliesMviModel.UiState, InboxRepliesMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    private var currentUserId: Int? = null

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            coordinator.events.onEach {
                when (it) {
                    InboxCoordinator.Event.Refresh -> refresh()
                }
            }.launchIn(this)
            coordinator.unreadOnly.onEach {
                if (it != uiState.value.unreadOnly) {
                    changeUnreadOnly(it)
                }
            }.launchIn(this)
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        swipeActionsEnabled = settings.enableSwipeActions,
                        autoLoadImages = settings.autoLoadImages,
                        voteFormat = settings.voteFormat,
                        actionsOnSwipeToStartInbox = settings.actionsOnSwipeToStartInbox,
                        actionsOnSwipeToEndInbox = settings.actionsOnSwipeToEndInbox,
                    )
                }
            }.launchIn(this)
            notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                handleLogout()
            }.launchIn(this)

            if (uiState.value.initial) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: InboxRepliesMviModel.Intent) {
        when (intent) {
            InboxRepliesMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }

            InboxRepliesMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
                refresh()
            }

            is InboxRepliesMviModel.Intent.MarkAsRead -> {
                markAsRead(
                    read = intent.read,
                    reply = uiState.value.replies.first { it.id == intent.id },
                )
            }


            InboxRepliesMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is InboxRepliesMviModel.Intent.DownVoteComment -> {
                toggleDownVoteComment(
                    mention = uiState.value.replies.first { it.id == intent.id },
                )
            }

            is InboxRepliesMviModel.Intent.UpVoteComment -> {
                toggleUpVoteComment(
                    mention = uiState.value.replies.first { it.id == intent.id },
                )
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        mvi.updateState {
            it.copy(
                initial = initial,
                canFetchMore = true,
                refreshing = true
            )
        }
        val auth = identityRepository.authToken.value
        val currentUser = siteRepository.getCurrentUser(auth.orEmpty())
        currentUserId = currentUser?.id
        loadNextPage()
        updateUnreadItems()
    }

    private fun changeUnreadOnly(value: Boolean) {
        mvi.updateState { it.copy(unreadOnly = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            refresh(initial = true)
            mvi.emitEffect(InboxRepliesMviModel.Effect.BackToTop)
        }
    }

    private suspend fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val unreadOnly = currentState.unreadOnly
        val itemList = userRepository.getReplies(
            auth = auth,
            page = currentPage,
            unreadOnly = unreadOnly,
            sort = SortType.New,
        )?.map {
            it.copy(isCommentReply = it.comment.depth > 0)
        }

        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }
        mvi.updateState {
            val newItems = if (refreshing) {
                itemList.orEmpty()
            } else {
                it.replies + itemList.orEmpty()
            }
            it.copy(
                replies = newItems,
                loading = false,
                canFetchMore = itemList?.isEmpty() != true,
                refreshing = false,
                initial = false,
            )
        }
    }

    private fun handleItemUpdate(item: PersonMentionModel) {
        mvi.updateState {
            it.copy(
                replies = it.replies.map { i ->
                    if (i.id == item.id) {
                        item
                    } else {
                        i
                    }
                }
            )
        }
    }

    private fun markAsRead(read: Boolean, reply: PersonMentionModel) {
        val auth = identityRepository.authToken.value
        mvi.scope?.launch(Dispatchers.IO) {
            userRepository.setReplyRead(
                read = read,
                replyId = reply.id,
                auth = auth,
            )
            val currentState = uiState.value
            if (read && currentState.unreadOnly) {
                mvi.updateState {
                    it.copy(
                        replies = currentState.replies.filter { r ->
                            r.id != reply.id
                        }
                    )
                }
            } else {
                val newItem = reply.copy(read = read)
                handleItemUpdate(newItem)
            }
            updateUnreadItems()
        }
    }

    private fun toggleUpVoteComment(mention: PersonMentionModel) {
        val newValue = mention.myVote <= 0
        val newMention = commentRepository.asUpVoted(
            mention = mention,
            voted = newValue,
        )
        handleItemUpdate(newMention)
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.upVote(
                    auth = auth,
                    comment = mention.comment,
                    voted = newValue,
                )
            } catch (e: Throwable) {
                handleItemUpdate(mention)
            }
        }
    }

    private fun toggleDownVoteComment(mention: PersonMentionModel) {
        val newValue = mention.myVote >= 0
        val newMention = commentRepository.asDownVoted(
            mention = mention,
            downVoted = newValue
        )
        handleItemUpdate(newMention)
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.downVote(
                    auth = auth,
                    comment = mention.comment,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                handleItemUpdate(mention)
            }
        }
    }

    private suspend fun updateUnreadItems() {
        val unreadCount = coordinator.updateUnreadCount()
        mvi.emitEffect(InboxRepliesMviModel.Effect.UpdateUnreadItems(unreadCount))
    }

    private fun handleLogout() {
        mvi.updateState { it.copy(replies = emptyList()) }
    }
}
