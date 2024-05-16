package com.github.diegoberaldin.raccoonforlemmy.unit.mentions

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxMentionsViewModel(
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val hapticFeedback: HapticFeedback,
    private val coordinator: InboxCoordinator,
    private val notificationCenter: NotificationCenter,
) : InboxMentionsMviModel,
    DefaultMviModel<InboxMentionsMviModel.Intent, InboxMentionsMviModel.UiState, InboxMentionsMviModel.Effect>(
        initialState = InboxMentionsMviModel.UiState(),
    ) {
    private var currentPage: Int = 1

    init {
        screenModelScope.launch {
            coordinator.events.onEach {
                when (it) {
                    InboxCoordinator.Event.Refresh -> {
                        refresh()
                        emitEffect(InboxMentionsMviModel.Effect.BackToTop)
                    }
                }
            }.launchIn(this)
            coordinator.unreadOnly.onEach {
                if (it != uiState.value.unreadOnly) {
                    changeUnreadOnly(it)
                }
            }.launchIn(this)
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        swipeActionsEnabled = settings.enableSwipeActions,
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        voteFormat = settings.voteFormat,
                        actionsOnSwipeToStartInbox = settings.actionsOnSwipeToStartInbox,
                        actionsOnSwipeToEndInbox = settings.actionsOnSwipeToEndInbox,
                        showScores = settings.showScores,
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
            InboxMentionsMviModel.Intent.LoadNextPage ->
                screenModelScope.launch {
                    loadNextPage()
                }

            InboxMentionsMviModel.Intent.Refresh ->
                screenModelScope.launch {
                    refresh()
                    emitEffect(InboxMentionsMviModel.Effect.BackToTop)
                }

            is InboxMentionsMviModel.Intent.MarkAsRead -> {
                markAsRead(
                    read = intent.read,
                    mention = uiState.value.mentions.first { it.id == intent.id },
                )
            }

            InboxMentionsMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is InboxMentionsMviModel.Intent.DownVoteComment -> {
                toggleDownVoteComment(
                    mention = uiState.value.mentions.first { it.id == intent.id },
                )
            }

            is InboxMentionsMviModel.Intent.UpVoteComment -> {
                toggleUpVoteComment(
                    mention = uiState.value.mentions.first { it.id == intent.id },
                )
            }
        }
    }

    private suspend fun refresh(initial: Boolean = false) {
        currentPage = 1
        updateState {
            it.copy(
                initial = initial,
                canFetchMore = true,
                refreshing = !initial,
                loading = false,
            )
        }
        loadNextPage()
        updateUnreadItems()
    }

    private fun changeUnreadOnly(value: Boolean) {
        updateState { it.copy(unreadOnly = value) }
        screenModelScope.launch {
            refresh(initial = true)
            emitEffect(InboxMentionsMviModel.Effect.BackToTop)
        }
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value
        val refreshing = currentState.refreshing
        val unreadOnly = currentState.unreadOnly
        val itemList =
            userRepository.getMentions(
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
        updateState {
            val newItems =
                if (refreshing) {
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

    private fun handleItemUpdate(item: PersonMentionModel) {
        updateState {
            it.copy(
                mentions =
                    it.mentions.map { i ->
                        if (i.id == item.id) {
                            item
                        } else {
                            i
                        }
                    },
            )
        }
    }

    private fun markAsRead(
        read: Boolean,
        mention: PersonMentionModel,
    ) {
        val auth = identityRepository.authToken.value
        screenModelScope.launch {
            userRepository.setMentionRead(
                read = read,
                mentionId = mention.id,
                auth = auth,
            )
            val currentState = uiState.value
            if (read && currentState.unreadOnly) {
                updateState {
                    it.copy(
                        mentions =
                            currentState.mentions.filter { m ->
                                m.id != mention.id
                            },
                    )
                }
            } else {
                val newMention = mention.copy(read = read)
                handleItemUpdate(newMention)
            }
            updateUnreadItems()
        }
    }

    private fun toggleUpVoteComment(mention: PersonMentionModel) {
        val newValue = mention.myVote <= 0
        val newComment =
            commentRepository.asUpVoted(
                comment = mention.comment,
                voted = newValue,
            )
        val newMention =
            mention.copy(
                myVote = newComment.myVote,
                score = newComment.score,
            )
        handleItemUpdate(newMention)
        screenModelScope.launch {
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
        val newComment = commentRepository.asDownVoted(mention.comment, newValue)
        val newMention =
            mention.copy(
                myVote = newComment.myVote,
                score = newComment.score,
            )
        handleItemUpdate(newMention)
        screenModelScope.launch {
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

    private fun updateUnreadItems() {
        screenModelScope.launch {
            val unreadCount = coordinator.updateUnreadCount()
            emitEffect(InboxMentionsMviModel.Effect.UpdateUnreadItems(unreadCount))
        }
    }

    private fun handleLogout() {
        updateState { it.copy(mentions = emptyList()) }
        screenModelScope.launch {
            refresh(initial = true)
        }
    }
}
