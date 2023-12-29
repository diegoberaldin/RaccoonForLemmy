package com.github.diegoberaldin.raccoonforlemmy.unit.chat

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PrivateMessageModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PrivateMessageRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class InboxChatViewModel(
    private val otherUserId: Int,
    private val mvi: DefaultMviModel<InboxChatMviModel.Intent, InboxChatMviModel.UiState, InboxChatMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val messageRepository: PrivateMessageRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val postRepository: PostRepository,
    private val notificationCenter: NotificationCenter,
) : InboxChatMviModel,
    MviModel<InboxChatMviModel.Intent, InboxChatMviModel.UiState, InboxChatMviModel.Effect> by mvi {


    private var currentPage: Int = 1

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            launch(Dispatchers.IO) {
                val auth = identityRepository.authToken.value.orEmpty()

                settingsRepository.currentSettings.onEach { settings ->
                    mvi.updateState { it.copy(autoLoadImages = settings.autoLoadImages) }
                }.launchIn(this)
                notificationCenter.subscribe(NotificationCenterEvent.Logout::class).onEach {
                    handleLogout()
                }.launchIn(this)
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

                    if (uiState.value.messages.isEmpty()) {
                        refresh(initial = true)
                    }
                }
            }
        }
    }

    override fun reduce(intent: InboxChatMviModel.Intent) {
        when (intent) {
            InboxChatMviModel.Intent.LoadNextPage -> {
                mvi.scope?.launch(Dispatchers.IO) {
                    loadNextPage()
                }
            }

            is InboxChatMviModel.Intent.SubmitNewMessage -> {
                submitNewMessage(intent.value)
            }

            is InboxChatMviModel.Intent.ImageSelected -> {
                loadImageAndAppendUrlInBody(intent.value)
            }

            is InboxChatMviModel.Intent.EditMessage -> {
                uiState.value.messages.firstOrNull { it.id == intent.value }?.also { message ->
                    startEditingMessage(message)
                }
            }

            is InboxChatMviModel.Intent.DeleteMessage -> {
                uiState.value.messages.firstOrNull { it.id == intent.value }?.also { message ->
                    deleteMessage(message)
                }
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
        val itemList = messageRepository.getAll(
            creatorId = otherUserId,
            auth = auth,
            page = currentPage,
            unreadOnly = false,
        )?.onEach {
            if (!it.read) {
                markAsRead(true, it.id)
            }
        }
        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }

        val itemsToAdd = itemList.orEmpty().filter {
            it.creator?.id == otherUserId || it.recipient?.id == otherUserId
        }
        mvi.updateState {
            val newItems = if (refreshing) {
                itemsToAdd
            } else {
                it.messages + itemsToAdd
            }
            it.copy(
                messages = newItems,
                loading = false,
                canFetchMore = itemList?.isEmpty() != true,
                refreshing = false,
                initial = itemsToAdd.isEmpty(),
            )
        }
        if (currentState.initial && itemsToAdd.isEmpty()) {
            loadNextPage()
        }
    }

    private fun markAsRead(read: Boolean, messageId: Int) {
        val auth = identityRepository.authToken.value
        mvi.scope?.launch(Dispatchers.IO) {
            val newMessage = messageRepository.markAsRead(
                read = read,
                messageId = messageId,
                auth = auth,
            )
            if (newMessage != null) {
                handleMessageUpdate(newMessage)
            }
        }
    }

    private fun handleMessageUpdate(newMessage: PrivateMessageModel) {
        mvi.updateState {
            it.copy(
                messages = it.messages.map { m ->
                    if (m.id == newMessage.id) {
                        newMessage
                    } else {
                        m
                    }
                }
            )
        }
    }

    private fun loadImageAndAppendUrlInBody(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            if (url != null) {
                mvi.emitEffect(InboxChatMviModel.Effect.AddImageToText(url))
            }
            mvi.updateState {
                it.copy(
                    loading = false,
                )
            }
        }
    }

    private fun startEditingMessage(message: PrivateMessageModel) {
        mvi.updateState {
            it.copy(
                editedMessageId = message.id,
            )
        }
    }

    private fun submitNewMessage(text: String) {
        val editedMessageId = uiState.value.editedMessageId
        if (text.isNotEmpty()) {
            mvi.scope?.launch(Dispatchers.IO) {
                val auth = identityRepository.authToken.value
                if (editedMessageId == null) {
                    messageRepository.create(
                        message = text,
                        recipiendId = otherUserId,
                        auth = auth,
                    )
                } else {
                    messageRepository.edit(
                        messageId = editedMessageId,
                        message = text,
                        auth = auth,
                    )
                }

                mvi.updateState {
                    it.copy(editedMessageId = null)
                }
                refresh()
            }
        }
    }

    private fun handleLogout() {
        mvi.updateState { it.copy(messages = emptyList()) }
    }

    private fun deleteMessage(message: PrivateMessageModel) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value
            runCatching {
                messageRepository.delete(
                    messageId = message.id,
                    auth = auth,
                )
                mvi.updateState {
                    it.copy(messages = it.messages.filter { m -> m.id != message.id })
                }
            }
        }
    }
}