package com.github.diegoberaldin.raccoonforlemmy.unit.createcomment

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ValidationError
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreateCommentViewModel(
    private val postId: Int?,
    private val parentId: Int?,
    private val editedCommentId: Int?,
    private val identityRepository: IdentityRepository,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val itemCache: LemmyItemCache,
) : CreateCommentMviModel,
    DefaultMviModel<CreateCommentMviModel.Intent, CreateCommentMviModel.UiState, CreateCommentMviModel.Effect>(
        initialState = CreateCommentMviModel.UiState(),
    ) {

    override fun onStarted() {
        super.onStarted()
        scope?.launch {
            val originalPost = postId?.let { itemCache.getPost(it) }
            val originalComment = parentId?.let { itemCache.getComment(it) }
            val editedComment = editedCommentId?.let { itemCache.getComment(it) }
            updateState {
                it.copy(
                    originalPost = originalPost,
                    originalComment = originalComment,
                    editedComment = editedComment,
                )
            }

            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
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
        }
    }

    override fun reduce(intent: CreateCommentMviModel.Intent) {
        when (intent) {
            is CreateCommentMviModel.Intent.ChangeSection -> {
                updateState { it.copy(section = intent.value) }
            }

            is CreateCommentMviModel.Intent.ImageSelected -> {
                loadImageAndAppendUrlInBody(intent.value)
            }

            is CreateCommentMviModel.Intent.ChangeLanguage -> {
                updateState { it.copy(currentLanguageId = intent.value) }
            }

            is CreateCommentMviModel.Intent.Send -> submit(intent.text)
        }
    }

    private fun submit(text: String) {
        if (uiState.value.loading) {
            return
        }

        updateState {
            it.copy(
                textError = null,
            )
        }
        var valid = true
        val languageId = uiState.value.currentLanguageId
        if (text.isEmpty()) {
            updateState {
                it.copy(
                    textError = ValidationError.MissingField,
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
                if (postId != null) {
                    commentRepository.create(
                        postId = postId,
                        parentId = parentId,
                        text = text,
                        languageId = languageId,
                        auth = auth,
                    )
                } else if (editedCommentId != null) {
                    commentRepository.edit(
                        commentId = editedCommentId,
                        text = text,
                        languageId = languageId,
                        auth = auth,
                    )
                }
                // the comment count has changed, emits update
                emitPostUpdateNotification()
                emitEffect(CreateCommentMviModel.Effect.Success(new = editedCommentId == null))
            } catch (e: Throwable) {
                val message = e.message
                emitEffect(CreateCommentMviModel.Effect.Failure(message))
            } finally {
                updateState { it.copy(loading = false) }
            }
        }
    }

    private suspend fun emitPostUpdateNotification() {
        val postId = postId ?: return
        val auth = identityRepository.authToken.value
        val newPost = postRepository.get(postId, auth)
        if (newPost != null) {
            notificationCenter.send(
                event = NotificationCenterEvent.PostUpdated(newPost),
            )
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
                emitEffect(CreateCommentMviModel.Effect.AddImageToText(url))
            }
            updateState {
                it.copy(
                    loading = false,
                )
            }
        }
    }
}