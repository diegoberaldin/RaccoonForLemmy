package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val communityId: Int,
    private val mvi: DefaultMviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect> = DefaultMviModel(
        CreatePostMviModel.UiState()
    ),
    private val identityRepository: IdentityRepository,
    private val postsRepository: PostsRepository,
) : ScreenModel,
    MviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect> by mvi {

    override fun reduce(intent: CreatePostMviModel.Intent) {
        when (intent) {
            is CreatePostMviModel.Intent.SetTitle -> {
                mvi.updateState {
                    it.copy(title = intent.value)
                }
            }

            is CreatePostMviModel.Intent.SetText -> {
                mvi.updateState {
                    it.copy(body = intent.value)
                }
            }

            CreatePostMviModel.Intent.Send -> submit()
        }
    }

    private fun submit() {
        mvi.scope.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                val title = uiState.value.title
                val body = uiState.value.body
                postsRepository.create(
                    communityId = communityId,
                    title = title,
                    body = body,
                    auth = auth,
                )
                mvi.emitEffect(CreatePostMviModel.Effect.Success)
            } catch (e: Throwable) {
                val message = e.message
                mvi.emitEffect(CreatePostMviModel.Effect.Failure(message))
            }
        }
    }
}