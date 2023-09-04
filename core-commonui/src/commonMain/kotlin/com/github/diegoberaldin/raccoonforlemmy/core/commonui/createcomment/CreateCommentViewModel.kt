package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class CreateCommentViewModel(
    private val postId: Int,
    private val parentId: Int?,
    private val mvi: DefaultMviModel<CreateCommentMviModel.Intent, CreateCommentMviModel.UiState, CreateCommentMviModel.Effect> = DefaultMviModel(
        CreateCommentMviModel.UiState()
    ),
    private val identityRepository: IdentityRepository,
    private val commentRepository: CommentRepository,
) : ScreenModel,
    MviModel<CreateCommentMviModel.Intent, CreateCommentMviModel.UiState, CreateCommentMviModel.Effect> by mvi {

    override fun reduce(intent: CreateCommentMviModel.Intent) {
        when (intent) {
            is CreateCommentMviModel.Intent.SetText -> {
                mvi.updateState {
                    it.copy(text = intent.value)
                }
            }

            CreateCommentMviModel.Intent.Send -> submit()
        }
    }

    private fun submit() {
        mvi.scope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                val text = uiState.value.text
                commentRepository.create(
                    postId = postId,
                    parentId = parentId,
                    text = text,
                    auth = auth,
                )
                mvi.emitEffect(CreateCommentMviModel.Effect.Success)
            } catch (e: Throwable) {
                val message = e.message
                mvi.emitEffect(CreateCommentMviModel.Effect.Failure(message))
            }
        }
    }
}