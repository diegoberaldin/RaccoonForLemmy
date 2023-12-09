package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
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
            coordinator.effects.onEach {
                when (it) {
                    InboxMviModel.Effect.Refresh -> refresh()
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
                mvi.emitEffect(InboxRepliesMviModel.Effect.BackToTop)
            }

            is InboxRepliesMviModel.Intent.MarkAsRead -> {
                markAsRead(
                    read = intent.read,
                    reply = uiState.value.replies.first { it.id == intent.id },
                )
            }


            InboxRepliesMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is InboxRepliesMviModel.Intent.DownVoteComment -> {
                hapticFeedback.vibrate()
                toggleDownVoteComment(
                    mention = uiState.value.replies.first { it.id == intent.id },
                )
            }

            is InboxRepliesMviModel.Intent.UpVoteComment -> {
                hapticFeedback.vibrate()
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
            val isOwnPost = it.post.creator?.id == currentUserId
            it.copy(isOwnPost = isOwnPost)
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
                mvi.updateState {
                    it.copy(
                        replies = currentState.replies.map { r ->
                            if (r.id == reply.id) {
                                r.copy(read = read)
                            } else {
                                r
                            }
                        }
                    )
                }
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
        mvi.updateState {
            it.copy(
                replies = it.replies.map { m ->
                    if (m.comment.id != mention.comment.id) {
                        m
                    } else {
                        newMention
                    }
                },
            )
        }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.upVote(
                    auth = auth,
                    comment = mention.comment,
                    voted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
                    it.copy(
                        replies = it.replies.map { m ->
                            if (m.comment.id != mention.comment.id) {
                                m
                            } else {
                                mention
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleDownVoteComment(mention: PersonMentionModel) {
        val newValue = mention.myVote >= 0
        val newMention = commentRepository.asDownVoted(
            mention = mention,
            downVoted = newValue
        )
        mvi.updateState {
            it.copy(
                replies = it.replies.map { m ->
                    if (m.comment.id != mention.comment.id) {
                        m
                    } else {
                        newMention
                    }
                },
            )
        }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.downVote(
                    auth = auth,
                    comment = mention.comment,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
                    it.copy(
                        replies = it.replies.map { m ->
                            if (m.comment.id != mention.comment.id) {
                                m
                            } else {
                                mention
                            }
                        },
                    )
                }
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
