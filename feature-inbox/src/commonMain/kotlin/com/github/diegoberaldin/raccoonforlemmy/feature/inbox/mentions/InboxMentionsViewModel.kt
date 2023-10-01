package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.mentions

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PersonMentionModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
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
) : ScreenModel,
    MviModel<InboxMentionsMviModel.Intent, InboxMentionsMviModel.UiState, InboxMentionsMviModel.Effect> by mvi {

    private var currentPage: Int = 1

    init {
        notificationCenter.addObserver({
            handleLogout()
        }, this::class.simpleName.orEmpty(), NotificationCenterContractKeys.Logout)
    }

    fun finalize() {
        notificationCenter.removeObserver(this::class.simpleName.orEmpty())
    }

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
                    )
                }
            }.launchIn(this)
        }
    }

    override fun reduce(intent: InboxMentionsMviModel.Intent) {
        when (intent) {
            InboxMentionsMviModel.Intent.LoadNextPage -> loadNextPage()
            InboxMentionsMviModel.Intent.Refresh -> refresh()
            is InboxMentionsMviModel.Intent.MarkAsRead -> {
                markAsRead(read = intent.read, mentionId = intent.mentionId)
            }

            InboxMentionsMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is InboxMentionsMviModel.Intent.DownVoteComment -> toggleDownVoteComment(
                mention = mvi.uiState.value.mentions[intent.index],
                feedback = true,
            )

            is InboxMentionsMviModel.Intent.UpVoteComment -> toggleUpVoteComment(
                mention = mvi.uiState.value.mentions[intent.index],
                feedback = true,
            )
        }
    }

    private fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
        updateUnreadItems()
    }

    private fun changeUnreadOnly(value: Boolean) {
        mvi.updateState { it.copy(unreadOnly = value) }
        refresh()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
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
            currentPage++
            val canFetchMore = itemList.size >= CommentRepository.DEFAULT_PAGE_SIZE
            mvi.updateState {
                val newItems = if (refreshing) {
                    itemList
                } else {
                    it.mentions + itemList
                }
                it.copy(
                    mentions = newItems,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                )
            }
        }
    }

    private fun markAsRead(read: Boolean, mentionId: Int) {
        val auth = identityRepository.authToken.value
        mvi.scope?.launch(Dispatchers.IO) {
            userRepository.setMentionRead(
                read = read,
                mentionId = mentionId,
                auth = auth,
            )
            val currentState = uiState.value
            if (read && currentState.unreadOnly) {
                mvi.updateState {
                    it.copy(
                        mentions = currentState.mentions.filter { m ->
                            m.id != mentionId
                        }
                    )
                }
            }
        }
    }

    private fun toggleUpVoteComment(
        mention: PersonMentionModel,
        feedback: Boolean,
    ) {
        val newValue = mention.myVote <= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
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
                        m.copy(myVote = newComment.myVote)
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
                                m.copy(myVote = mention.myVote)
                            }
                        },
                    )
                }
            }
        }
    }

    private fun toggleDownVoteComment(
        mention: PersonMentionModel,
        feedback: Boolean,
    ) {
        val newValue = mention.myVote >= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newComment = commentRepository.asDownVoted(mention.comment, newValue)
        mvi.updateState {
            it.copy(
                mentions = it.mentions.map { m ->
                    if (m.comment.id != mention.comment.id) {
                        m
                    } else {
                        m.copy(myVote = newComment.myVote)
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
                                m.copy(myVote = mention.myVote)
                            }
                        },
                    )
                }
            }
        }
    }

    private fun updateUnreadItems() {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            val unreadCount = if (!auth.isNullOrEmpty()) {
                val mentionCount =
                    userRepository.getMentions(auth, page = 1, limit = 50).count()
                val replyCount =
                    userRepository.getReplies(auth, page = 1, limit = 50).count()
                mentionCount + replyCount
            } else {
                0
            }
            mvi.emitEffect(InboxMentionsMviModel.Effect.UpdateUnreadItems(unreadCount))
        }
    }

    private fun handleLogout() {
        mvi.updateState { it.copy(mentions = emptyList()) }
    }
}
