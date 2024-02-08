package com.github.diegoberaldin.raccoonforlemmy.unit.createpost

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.isValidUrl
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableName
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
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
    private val crossPostId: Int?,
    private val identityRepository: IdentityRepository,
    private val postRepository: PostRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val itemCache: LemmyItemCache,
) : CreatePostMviModel,
    DefaultMviModel<CreatePostMviModel.Intent, CreatePostMviModel.UiState, CreatePostMviModel.Effect>(
        initialState = CreatePostMviModel.UiState(),
    ) {

    override fun onStarted() {
        super.onStarted()
        scope?.launch {
            val editedPost = editedPostId?.let {
                itemCache.getPost(it)
            }
            val crossPost = crossPostId?.let {
                itemCache.getPost(it)
            }
            updateState { it.copy(editedPost = editedPost, crossPost = crossPost) }

            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        fullHeightImages = settings.fullHeightImages,
                        showScores = settings.showScores,
                    )
                }
            }.launchIn(this)

            if (uiState.value.currentUser.isEmpty()) {
                val auth = identityRepository.authToken.value.orEmpty()
                val currentUser = siteRepository.getCurrentUser(auth)
                val languages = siteRepository.getLanguages(auth)
                if (currentUser != null) {
                    updateState {
                        it.copy(
                            currentUser = currentUser.name,
                            currentInstance = currentUser.host,
                            availableLanguages = languages,
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
                val preferNicknames = uiState.value.preferNicknames
                updateState {
                    it.copy(
                        communityId = community.id,
                        communityInfo = community.readableName(preferNicknames),
                    )
                }
            }

            is CreatePostMviModel.Intent.SetTitle -> {
                updateState {
                    it.copy(title = intent.value)
                }
            }


            is CreatePostMviModel.Intent.ChangeNsfw -> {
                updateState {
                    it.copy(nsfw = intent.value)
                }
            }


            is CreatePostMviModel.Intent.SetUrl -> {
                updateState {
                    it.copy(url = intent.value)
                }
            }

            is CreatePostMviModel.Intent.ImageSelected -> {
                loadImageAndObtainUrl(intent.value)
            }

            is CreatePostMviModel.Intent.InsertImageInBody -> {
                loadImageAndAppendUrlInBody(intent.value)
            }


            is CreatePostMviModel.Intent.ChangeSection -> updateState {
                it.copy(section = intent.value)
            }

            is CreatePostMviModel.Intent.ChangeLanguage -> updateState {
                it.copy(currentLanguageId = intent.value)
            }

            is CreatePostMviModel.Intent.Send -> submit(intent.body)
        }
    }

    private fun loadImageAndObtainUrl(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }
        scope?.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            updateState {
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
        scope?.launch(Dispatchers.IO) {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            if (url != null) {
                emitEffect(CreatePostMviModel.Effect.AddImageToBody(url))
            }
            updateState {
                it.copy(loading = false)
            }
        }
    }

    private fun submit(body: String) {
        if (uiState.value.loading) {
            return
        }

        updateState {
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
        val languageId = uiState.value.currentLanguageId
        var valid = true
        if (title.isEmpty()) {
            updateState {
                it.copy(
                    titleError = message_missing_field.desc(),
                )
            }
            valid = false
        }
        if (body.isEmpty()) {
            updateState {
                it.copy(
                    bodyError = message_missing_field.desc(),
                )
            }
            valid = false
        }
        if (!url.isNullOrEmpty() && !url.isValidUrl()) {
            updateState {
                it.copy(
                    urlError = message_invalid_field.desc(),
                )
            }
            valid = false
        }
        if (communityId == null) {
            updateState {
                it.copy(
                    communityError = message_missing_field.desc(),
                )
            }
            valid = false
        }

        if (!valid) {
            return
        }

        updateState { it.copy(loading = true) }
        scope?.launch(Dispatchers.IO) {
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
                            languageId = languageId,
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
                            languageId = languageId,
                            auth = auth,
                        )
                    }
                }
                emitEffect(CreatePostMviModel.Effect.Success)
            } catch (e: Throwable) {
                val message = e.message
                emitEffect(CreatePostMviModel.Effect.Failure(message))
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }
}