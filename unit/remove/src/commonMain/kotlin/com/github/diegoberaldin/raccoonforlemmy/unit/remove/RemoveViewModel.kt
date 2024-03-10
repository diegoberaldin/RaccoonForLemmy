package com.github.diegoberaldin.raccoonforlemmy.unit.remove

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class RemoveViewModel(
    private val postId: Int?,
    private val commentId: Int?,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val notificationCenter: NotificationCenter,
) : RemoveMviModel,
    DefaultMviModel<RemoveMviModel.Intent, RemoveMviModel.UiState, RemoveMviModel.Effect>(
        initialState = RemoveMviModel.UiState(),
    ) {

    override fun reduce(intent: RemoveMviModel.Intent) {
        when (intent) {
            is RemoveMviModel.Intent.SetText -> {
                updateState {
                    it.copy(text = intent.value)
                }
            }

            RemoveMviModel.Intent.Submit -> submit()
        }
    }

    private fun submit() {
        if (uiState.value.loading) {
            return
        }
        val text = uiState.value.text

        updateState { it.copy(loading = true) }
        screenModelScope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                if (postId != null) {
                    postRepository.remove(
                        postId = postId,
                        reason = text,
                        auth = auth,
                        removed = true,
                    )?.also { post ->
                        notificationCenter.send(NotificationCenterEvent.PostRemoved(post))
                    }
                } else if (commentId != null) {
                    commentRepository.remove(
                        commentId = commentId,
                        reason = text,
                        auth = auth,
                        removed = true,
                    )?.also { comment ->
                        notificationCenter.send(NotificationCenterEvent.CommentRemoved(comment))
                    }
                }
                emitEffect(RemoveMviModel.Effect.Success)
            } catch (e: Throwable) {
                val message = e.message
                emitEffect(RemoveMviModel.Effect.Failure(message))
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }
}