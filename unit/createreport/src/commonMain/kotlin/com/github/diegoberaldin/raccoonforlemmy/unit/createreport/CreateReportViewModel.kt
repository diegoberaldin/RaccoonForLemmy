package com.github.diegoberaldin.raccoonforlemmy.unit.createreport

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class CreateReportViewModel(
    private val postId: Int?,
    private val commentId: Int?,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
) : CreateReportMviModel,
    DefaultMviModel<CreateReportMviModel.Intent, CreateReportMviModel.UiState, CreateReportMviModel.Effect>(
        initialState = CreateReportMviModel.UiState(),
    ) {

    override fun reduce(intent: CreateReportMviModel.Intent) {
        when (intent) {
            is CreateReportMviModel.Intent.SetText -> {
                updateState {
                    it.copy(text = intent.value)
                }
            }

            CreateReportMviModel.Intent.Send -> submit()
        }
    }

    private fun submit() {
        if (uiState.value.loading) {
            return
        }
        val text = uiState.value.text

        updateState { it.copy(loading = true) }
        scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                if (postId != null) {
                    postRepository.report(
                        postId = postId,
                        reason = text,
                        auth = auth,
                    )
                } else if (commentId != null) {
                    commentRepository.report(
                        commentId = commentId,
                        reason = text,
                        auth = auth,
                    )
                }
                emitEffect(CreateReportMviModel.Effect.Success)
            } catch (e: Throwable) {
                val message = e.message
                emitEffect(CreateReportMviModel.Effect.Failure(message))
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }
}