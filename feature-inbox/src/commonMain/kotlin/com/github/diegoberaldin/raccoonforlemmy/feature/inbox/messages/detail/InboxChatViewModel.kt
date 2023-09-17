package com.github.diegoberaldin.raccoonforlemmy.feature.inbox.messages.detail

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.InboxCoordinator
import com.github.diegoberaldin.raccoonforlemmy.feature.inbox.main.InboxMviModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxChatViewModel(
    private val otherUserId: Int,
    private val mvi: DefaultMviModel<InboxChatMviModel.Intent, InboxChatMviModel.UiState, InboxChatMviModel.SideEffect>,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val messageRepository: PrivateMessageRepository,
    private val userRepository: UserRepository,
    private val coordinator: InboxCoordinator,
    private val notificationCenter: NotificationCenter,
) : ScreenModel,
    MviModel<InboxChatMviModel.Intent, InboxChatMviModel.UiState, InboxChatMviModel.SideEffect> by mvi {


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
            launch(Dispatchers.IO) {
                val auth = identityRepository.authToken.value.orEmpty()

                launch {
                    val currentUserId = siteRepository.getCurrentUser(auth)?.id ?: 0
                    mvi.updateState { it.copy(currentUserId = currentUserId) }
                }
                launch {
                    val user = userRepository.get(
                        id = otherUserId,
                        auth = auth,
                    )
                    mvi.updateState {
                        it.copy(
                            otherUserName = user?.name.orEmpty(),
                            otherUserAvatar = user?.avatar,
                        )
                    }
                }
            }
        }
    }

    override fun reduce(intent: InboxChatMviModel.Intent) {
        when (intent) {
            InboxChatMviModel.Intent.LoadNextPage -> loadNextPage()
            is InboxChatMviModel.Intent.SetNewMessageContent -> setNewMessageContent(intent.value)
            InboxChatMviModel.Intent.SubmitNewMessage -> submitNewMessage()
        }
    }

    private fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
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
            val itemList = messageRepository.getAll(
                auth = auth,
                page = currentPage,
                unreadOnly = false,
            ).filter {
                it.creator?.id == otherUserId || it.recipient?.id == otherUserId
            }.onEach {
                if (!it.read) {
                    launch {
                        markAsRead(true, it.id)
                    }
                }
            }
            currentPage++
            val canFetchMore = itemList.size >= CommentRepository.DEFAULT_PAGE_SIZE

            mvi.updateState {
                val newItems = if (refreshing) {
                    itemList
                } else {
                    it.messages + itemList
                }
                it.copy(
                    messages = newItems,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                )
            }
        }
    }

    private fun markAsRead(read: Boolean, messageId: Int) {
        val auth = identityRepository.authToken.value
        mvi.scope?.launch(Dispatchers.IO) {
            messageRepository.markAsRead(
                read = read,
                messageId = messageId,
                auth = auth,
            )
            refresh()
        }
    }

    private fun setNewMessageContent(text: String) {
        mvi.updateState { it.copy(newMessageContent = text) }
    }

    private fun submitNewMessage() {
        val text = uiState.value.newMessageContent
        if (text.isNotEmpty()) {
            mvi.scope?.launch {
                val auth = identityRepository.authToken.value
                messageRepository.create(
                    message = text,
                    auth = auth,
                    recipiendId = otherUserId,
                )
                mvi.updateState { it.copy(newMessageContent = "") }
                refresh()
            }
        }
    }

    private fun handleLogout() {
        mvi.updateState { it.copy(messages = emptyList()) }
    }
}