package com.github.diegoberaldin.raccoonforlemmy.unit.mentions

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
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxMentionsViewModel(
    private val mvi: DefaultMviModel<InboxMentionsMviModel.Intent, InboxMentionsMviModel.UiState, InboxMentionsMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val hapticFeedback: HapticFeedback,
    private val coordinator: InboxCoordinator,
    private val notificationCenter: NotificationCenter,
) : InboxMentionsMviModel,
    MviModel<InboxMentionsMviModel.Intent, InboxMentionsMviModel.UiState, InboxMentionsMviModel.Effect> by mvi {

    private var currentPage: Int = 1

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

    override fun reduce(intent: InboxMentionsMviModel.Intent) {
        when (intent) {
            InboxMentionsMviModel.Intent.LoadNextPage -> mvi.scope?.launch(Dispatchers.IO) {
                loadNextPage()
            }

            InboxMentionsMviModel.Intent.Refresh -> mvi.scope?.launch(Dispatchers.IO) {
                refresh()
                mvi.emitEffect(InboxMentionsMviModel.Effect.BackToTop)
            }

            is InboxMentionsMviModel.Intent.MarkAsRead -> {
                markAsRead(
                    read = intent.read,
                    mention = uiState.value.mentions.first { it.id == intent.id },
                )
            }

            InboxMentionsMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is InboxMentionsMviModel.Intent.DownVoteComment -> {
                hapticFeedback.vibrate()
                toggleDownVoteComment(
                    mention = uiState.value.mentions.first { it.id == intent.id },
                )
            }

            is InboxMentionsMviModel.Intent.UpVoteComment -> {
                hapticFeedback.vibrate()
                toggleUpVoteComment(
                    mention = uiState.value.mentions.first { it.id == intent.id },
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
        loadNextPage()
        updateUnreadItems()
    }

    private fun changeUnreadOnly(value: Boolean) {
        mvi.updateState { it.copy(unreadOnly = value) }
        mvi.scope?.launch(Dispatchers.IO) {
            refresh(initial = true)
            mvi.emitEffect(InboxMentionsMviModel.Effect.BackToTop)
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
        val itemList = userRepository.getMentions(
            auth = auth,
            page = currentPage,
            unreadOnly = unreadOnly,
            sort = SortType.New,
        )
        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }
        mvi.updateState {
            val newItems = if (refreshing) {
                itemList.orEmpty()
            } else {
                it.mentions + itemList.orEmpty()
            }
            it.copy(
                mentions = newItems,
                loading = false,
                canFetchMore = itemList?.isEmpty() != true,
                refreshing = false,
                initial = false,
            )
        }
    }

    private fun markAsRead(read: Boolean, mention: PersonMentionModel) {
        val auth = identityRepository.authToken.value
        mvi.scope?.launch(Dispatchers.IO) {
            userRepository.setMentionRead(
                read = read,
                mentionId = mention.id,
                auth = auth,
            )
            val currentState = uiState.value
            if (read && currentState.unreadOnly) {
                mvi.updateState {
                    it.copy(
                        mentions = currentState.mentions.filter { m ->
                            m.id != mention.id
                        }
                    )
                }
            } else {
                mvi.updateState {
                    it.copy(
                        mentions = currentState.mentions.map { m ->
                            if (m.id == mention.id) {
                                m.copy(read = read)
                            } else {
                                m
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
        val newComment = commentRepository.asUpVoted(
            comment = mention.comment,
            voted = newValue,
        )
        mvi.updateState {
            it.copy(
                mentions = it.mentions.map { m ->
                    if (m.comment.id != mention.comment.id) {
                        m
                    } else {
                        m.copy(
                            myVote = newComment.myVote,
                            score = newComment.score,
                        )
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
                        mentions = it.mentions.map { m ->
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
        val newComment = commentRepository.asDownVoted(mention.comment, newValue)
        mvi.updateState {
            it.copy(
                mentions = it.mentions.map { m ->
                    if (m.comment.id != mention.comment.id) {
                        m
                    } else {
                        m.copy(
                            myVote = newComment.myVote,
                            score = newComment.score
                        )
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
                        mentions = it.mentions.map { m ->
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

    private fun updateUnreadItems() {
        mvi.scope?.launch(Dispatchers.IO) {
            val unreadCount = coordinator.updateUnreadCount()
            mvi.emitEffect(InboxMentionsMviModel.Effect.UpdateUnreadItems(unreadCount))
        }
    }

    private fun handleLogout() {
        mvi.updateState { it.copy(mentions = emptyList()) }
    }
}
