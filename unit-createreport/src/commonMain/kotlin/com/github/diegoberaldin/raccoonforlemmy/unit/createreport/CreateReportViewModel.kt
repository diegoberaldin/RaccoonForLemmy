package com.github.diegoberaldin.raccoonforlemmy.unit.createreport

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class CreateReportViewModel(
    private val postId: Int?,
    private val commentId: Int?,
    private val mvi: DefaultMviModel<CreateReportMviModel.Intent, CreateReportMviModel.UiState, CreateReportMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
) : CreateReportMviModel,
    MviModel<CreateReportMviModel.Intent, CreateReportMviModel.UiState, CreateReportMviModel.Effect> by mvi {

    override fun reduce(intent: CreateReportMviModel.Intent) {
        when (intent) {
            is CreateReportMviModel.Intent.SetText -> {
                mvi.updateState {
                    it.copy(text = intent.value)
                }
            }

            CreateReportMviModel.Intent.Send -> submit()
        }
    }

    private fun submit() {
        if (mvi.uiState.value.loading) {
            return
        }
        val text = uiState.value.text

        mvi.updateState { it.copy(loading = true) }
        mvi.scope?.launch(Dispatchers.IO) {
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
                mvi.emitEffect(CreateReportMviModel.Effect.Success)
            } catch (e: Throwable) {
                val message = e.message
                mvi.emitEffect(CreateReportMviModel.Effect.Failure(message))
            } finally {
                mvi.updateState { it.copy(loading = false) }
            }
        }
    }
}