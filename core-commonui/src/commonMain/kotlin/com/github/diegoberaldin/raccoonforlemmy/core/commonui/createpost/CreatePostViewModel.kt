package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.StringUtils.isValidUrl
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR.strings.message_invalid_field
import com.github.diegoberaldin.raccoonforlemmy.resources.MR.strings.message_missing_field
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val communityId: Int?,
    private val editedPostId: Int?,
    private val mvi: DefaultMviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect>,
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


            is CreatePostMviModel.Intent.ChangeNsfw -> {
                mvi.updateState {
                    it.copy(nsfw = intent.value)
                }
            }


            is CreatePostMviModel.Intent.SetUrl -> {
                mvi.updateState {
                    it.copy(url = intent.value)
                }
            }

            is CreatePostMviModel.Intent.ImageSelected -> {
                loadImageAndObtainUrl(intent.value)
            }

            CreatePostMviModel.Intent.Send -> submit()
        }
    }

    private fun loadImageAndObtainUrl(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postsRepository.uploadImage(auth, bytes)
            mvi.updateState {
                it.copy(
                    url = url.orEmpty(),
                    loading = false,
                )
            }
        }
    }

    private fun submit() {
        mvi.updateState {
            it.copy(
                titleError = null,
                urlError = null,
                bodyError = null,
            )
        }
        val title = uiState.value.title
        val body = uiState.value.body
        val url = uiState.value.url
        val nsfw = uiState.value.nsfw
        var valid = true
        if (title.isEmpty()) {
            mvi.updateState {
                it.copy(
                    titleError = message_missing_field.desc(),
                )
            }
            valid = false
        }
        if (body.isEmpty()) {
            mvi.updateState {
                it.copy(
                    bodyError = message_missing_field.desc(),
                )
            }
            valid = false
        }
        if (url.isNotEmpty() && !url.isValidUrl()) {
            mvi.updateState {
                it.copy(
                    urlError = message_invalid_field.desc(),
                )
            }
            valid = false
        }
        if (!valid) {
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                when {
                    communityId != null -> {
                        postsRepository.create(
                            communityId = communityId,
                            title = title,
                            body = body,
                            url = url,
                            nsfw = nsfw,
                            auth = auth,
                        )
                    }

                    editedPostId != null -> {
                        postsRepository.edit(
                            postId = editedPostId,
                            title = title,
                            body = body,
                            url = url,
                            nsfw = nsfw,
                            auth = auth,
                        )
                    }
                }
                mvi.emitEffect(CreatePostMviModel.Effect.Success)
            } catch (e: Throwable) {
                val message = e.message
                mvi.emitEffect(CreatePostMviModel.Effect.Failure(message))
            } finally {
                mvi.updateState { it.copy(loading = false) }
            }
        }
    }
}