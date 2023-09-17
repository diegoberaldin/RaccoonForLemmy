package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.replies

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
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
    private val hapticFeedback: HapticFeedback,
    private val coordinator: InboxCoordinator,
    private val notificationCenter: NotificationCenter,
) : ScreenModel,
    MviModel<InboxRepliesMviModel.Intent, InboxRepliesMviModel.UiState, InboxRepliesMviModel.Effect> by mvi {

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
        }
    }

    override fun reduce(intent: InboxRepliesMviModel.Intent) {
        when (intent) {
            InboxRepliesMviModel.Intent.LoadNextPage -> loadNextPage()
            InboxRepliesMviModel.Intent.Refresh -> refresh()

            is InboxRepliesMviModel.Intent.MarkAsRead -> {
                markAsRead(read = intent.read, replyId = intent.mentionId)
            }

            InboxRepliesMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
        }
    }

    private fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
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
            val currentUser = siteRepository.getCurrentUser(auth.orEmpty())
            val refreshing = currentState.refreshing
            val unreadOnly = currentState.unreadOnly
            val itemList = userRepository.getReplies(
                auth = auth,
                page = currentPage,
                unreadOnly = unreadOnly,
                sort = SortType.New,
            ).map {
                val isOwnPost = it.post.creator?.id == currentUser?.id
                it.copy(isOwnPost = isOwnPost)
            }

            currentPage++
            val canFetchMore = itemList.size >= CommentRepository.DEFAULT_PAGE_SIZE
            mvi.updateState {
                val newItems = if (refreshing) {
                    itemList
                } else {
                    it.replies + itemList
                }
                it.copy(
                    replies = newItems,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                )
            }
        }
    }

    private fun markAsRead(read: Boolean, replyId: Int) {
        val auth = identityRepository.authToken.value
        mvi.scope?.launch(Dispatchers.IO) {
            userRepository.setReplyRead(
                read = read,
                replyId = replyId,
                auth = auth,
            )
            refresh()
        }
    }

    private fun handleLogout() {
        mvi.updateState { it.copy(replies = emptyList()) }
    }
}
