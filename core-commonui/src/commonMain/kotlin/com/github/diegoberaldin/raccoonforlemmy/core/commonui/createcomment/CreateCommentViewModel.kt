package com.github.diegoberaldin.raccoonforlemmy.core.commonui.createcomment

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import com.github.diegoberaldin.raccoonforlemmy.resources.MR.strings.message_missing_field
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreateCommentViewModel(
    private val postId: Int?,
    private val parentId: Int?,
    private val editedCommentId: Int?,
    private val mvi: DefaultMviModel<CreateCommentMviModel.Intent, CreateCommentMviModel.UiState, CreateCommentMviModel.Effect>,
    private val identityRepository: IdentityRepository,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
) : CreateCommentMviModel,
    MviModel<CreateCommentMviModel.Intent, CreateCommentMviModel.UiState, CreateCommentMviModel.Effect> by mvi {

    override fun onStarted() {
        mvi.onStarted()
        mvi.scope?.launch {
            themeRepository.postLayout.onEach { layout ->
                mvi.updateState { it.copy(postLayout = layout) }
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
            settingsRepository.currentSettings.onEach { settings ->
                mvi.updateState {
                    it.copy(
                        voteFormat = settings.voteFormat,
                        autoLoadImages = settings.autoLoadImages,
                        fullHeightImages = settings.fullHeightImages,
                    )
                }
            }.launchIn(this)
        }
    }

    override fun reduce(intent: CreateCommentMviModel.Intent) {
        when (intent) {
            is CreateCommentMviModel.Intent.ChangeSection -> mvi.updateState {
                it.copy(section = intent.value)
            }

            is CreateCommentMviModel.Intent.ImageSelected -> loadImageAndAppendUrlInBody(intent.value)
            is CreateCommentMviModel.Intent.Send -> submit(intent.text)
        }
    }

    private fun submit(text: String) {
        if (mvi.uiState.value.loading) {
            return
        }

        mvi.updateState {
            it.copy(
                textError = null,
            )
        }
        var valid = true
        if (text.isEmpty()) {
            mvi.updateState {
                it.copy(
                    textError = message_missing_field.desc(),
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
                if (postId != null) {
                    commentRepository.create(
                        postId = postId,
                        parentId = parentId,
                        text = text,
                        auth = auth,
                    )
                } else if (editedCommentId != null) {
                    commentRepository.edit(
                        commentId = editedCommentId,
                        text = text,
                        auth = auth,
                    )
                }
                // the comment count has changed, emits update
                emitPostUpdateNotification()
                mvi.emitEffect(CreateCommentMviModel.Effect.Success(new = editedCommentId == null))
            } catch (e: Throwable) {
                val message = e.message
                mvi.emitEffect(CreateCommentMviModel.Effect.Failure(message))
            } finally {
                mvi.updateState { it.copy(loading = false) }
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
        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            if (url != null) {
                mvi.emitEffect(CreateCommentMviModel.Effect.AddImageToText(url))
            }
            mvi.updateState {
                it.copy(
                    loading = false,
                )
            }
        }
    }
}