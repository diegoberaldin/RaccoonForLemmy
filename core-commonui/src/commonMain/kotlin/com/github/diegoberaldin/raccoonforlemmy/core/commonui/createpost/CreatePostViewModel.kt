package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createpost

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.StringUtils.isValidUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR.strings.message_invalid_field
import com.github.diegoberaldin.raccoonforlemmy.resources.MR.strings.message_missing_field
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val editedPostId: Int?,
    private val mvi: DefaultMviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
) : CreatePostMviModel,
    MviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        fullHeightImages = settings.fullHeightImages,
                    )
                }
            }.launchIn(this)
            if (uiState.value.currentUser.isEmpty()) {
                val auth = identityRepository.authToken.value.orEmpty()
                val currentUser = siteRepository.getCurrentUser(auth)
                if (currentUser != null) {
                    mvi.updateState {
                        it.copy(
                            currentUser = currentUser.name,
                            currentInstance = currentUser.host,
                        )
                    }
                }
            }
        }
    }

    override fun reduce(intent: CreatePostMviModel.Intent) {
        when (intent) {
            is CreatePostMviModel.Intent.SetCommunity -> {
                val community = intent.value
                mvi.updateState {
                    it.copy(
                        communityId = community.id,
                        communityInfo = buildString {
                            append(community.name)
                            append("@")
                            append(community.host)
                        },
                    )
                }
            }

            is CreatePostMviModel.Intent.SetTitle -> {
                mvi.updateState {
                    it.copy(title = intent.value)
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

            is CreatePostMviModel.Intent.InsertImageInBody -> {
                loadImageAndAppendUrlInBody(intent.value)
            }


            is CreatePostMviModel.Intent.ChangeSection -> mvi.updateState {
                it.copy(section = intent.value)
            }

            is CreatePostMviModel.Intent.Send -> submit(intent.body)
        }
    }

    private fun loadImageAndObtainUrl(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            mvi.updateState {
                it.copy(
                    url = url.orEmpty(),
                    loading = false,
                )
            }
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
                mvi.emitEffect(CreatePostMviModel.Effect.AddImageToBody(url))
            }
            mvi.updateState {
                it.copy(
                    loading = false,
                )
            }
        }
    }

    private fun submit(body: String) {
        if (mvi.uiState.value.loading) {
            return
        }

        mvi.updateState {
            it.copy(
                titleError = null,
                urlError = null,
                bodyError = null,
            )
        }

        val communityId = uiState.value.communityId
        val title = uiState.value.title
        val url = uiState.value.url.takeIf { it.isNotEmpty() }?.trim()
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
        if (!url.isNullOrEmpty() && !url.isValidUrl()) {
            mvi.updateState {
                it.copy(
                    urlError = message_invalid_field.desc(),
                )
            }
            valid = false
        }
        if (communityId == null) {
            mvi.updateState {
                it.copy(
                    communityError = message_missing_field.desc(),
                )
            }
            valid = false
        }

        if (!valid) {
            return
        }

        mvi.updateState { it.copy(loading = true) }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                when {
                    editedPostId != null -> {
                        postRepository.edit(
                            postId = editedPostId,
                            title = title,
                            body = body,
                            url = url,
                            nsfw = nsfw,
                            auth = auth,
                        )
                    }

                    communityId != null -> {
                        postRepository.create(
                            communityId = communityId,
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