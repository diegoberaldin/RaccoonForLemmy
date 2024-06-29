package com.github.diegoberaldin.raccoonforlemmy.unit.createcomment

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterEvent
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.DraftType
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.AccountRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.CommunityPreferredLanguageRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.DraftRepository
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.ValidationError
import com.github.diegoberaldin.raccoonforlemmy.core.utils.datetime.epochMillis
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.readableHandle
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyItemCache
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.LemmyValueCache
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CreateCommentViewModel(
    private val postId: Long?,
    private val parentId: Long?,
    private val editedCommentId: Long?,
    private val draftId: Long?,
    private val identityRepository: IdentityRepository,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
    private val siteRepository: SiteRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationCenter: NotificationCenter,
    private val itemCache: LemmyItemCache,
    private val accountRepository: AccountRepository,
    private val draftRepository: DraftRepository,
    private val communityPreferredLanguageRepository: CommunityPreferredLanguageRepository,
    private val lemmyValueCache: LemmyValueCache,
) : DefaultMviModel<CreateCommentMviModel.Intent, CreateCommentMviModel.UiState, CreateCommentMviModel.Effect>(
        initialState = CreateCommentMviModel.UiState(),
    ),
    CreateCommentMviModel {
    init {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val originalPostFromCache = postId?.let { itemCache.getPost(it) }
            val originalCommentFromCache = parentId?.let { itemCache.getComment(it) }
            val editedComment = editedCommentId?.let { itemCache.getComment(it) }
            val originalPost =
                if (originalPostFromCache != null && originalPostFromCache.title.isEmpty()) {
                    postRepository.get(originalPostFromCache.id) ?: originalPostFromCache
                } else {
                    originalPostFromCache
                }
            val originalComment =
                if (originalCommentFromCache != null && originalCommentFromCache.text.isNullOrEmpty()) {
                    commentRepository.getBy(
                        id = originalCommentFromCache.id,
                        auth = auth,
                    ) ?: originalCommentFromCache
                } else {
                    originalCommentFromCache
                }
            val communityHandle = originalPost?.community?.readableHandle.orEmpty()
            val preferredLanguageId = communityPreferredLanguageRepository.get(communityHandle)
            updateState {
                it.copy(
                    originalPost = originalPost,
                    originalComment = originalComment,
                    editedComment = editedComment,
                )
            }

            themeRepository.postLayout
                .onEach { layout ->
                    updateState { it.copy(postLayout = layout) }
                }.launchIn(this)

            lemmyValueCache.isDownVoteEnabled
                .onEach { value ->
                    updateState {
                        it.copy(downVoteEnabled = value)
                    }
                }.launchIn(this)

            if (uiState.value.currentUser.isEmpty()) {
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
            settingsRepository.currentSettings
                .onEach { settings ->
                    updateState {
                        it.copy(
                            voteFormat = settings.voteFormat,
                            autoLoadImages = settings.autoLoadImages,
                            preferNicknames = settings.preferUserNicknames,
                            fullHeightImages = settings.fullHeightImages,
                            fullWidthImages = settings.fullWidthImages,
                            showScores = settings.showScores,
                            currentLanguageId = preferredLanguageId ?: settings.defaultLanguageId,
                        )
                    }
                }.launchIn(this)
        }
    }

    override fun reduce(intent: CreateCommentMviModel.Intent) {
        when (intent) {
            is CreateCommentMviModel.Intent.ChangeSection -> {
                screenModelScope.launch {
                    updateState { it.copy(section = intent.value) }
                }
            }

            is CreateCommentMviModel.Intent.ImageSelected -> {
                loadImageAndAppendUrlInBody(intent.value)
            }

            is CreateCommentMviModel.Intent.ChangeLanguage -> {
                screenModelScope.launch {
                    updateState { it.copy(currentLanguageId = intent.value) }
                }
            }

            is CreateCommentMviModel.Intent.ChangeTextValue -> {
                screenModelScope.launch {
                    updateState { it.copy(textValue = intent.value) }
                }
            }

            is CreateCommentMviModel.Intent.Send -> submit()
            is CreateCommentMviModel.Intent.SaveDraft -> saveDraft()
        }
    }

    private fun submit() {
        val currentState = uiState.value
        if (currentState.loading) {
            return
        }

        screenModelScope.launch {
            updateState {
                it.copy(
                    textError = null,
                )
            }
        }
        val text = currentState.textValue.text.trim()
        val languageId = currentState.currentLanguageId

        var valid = true
        if (text.isEmpty()) {
            screenModelScope.launch {
                updateState {
                    it.copy(
                        textError = ValidationError.MissingField,
                    )
                }
            }
            valid = false
        }
        if (!valid) {
            return
        }

        screenModelScope.launch {
            updateState { it.copy(loading = true) }
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                when {
                    editedCommentId != null -> {
                        commentRepository.edit(
                            commentId = editedCommentId,
                            text = text,
                            languageId = languageId,
                            auth = auth,
                        )
                    }

                    postId != null -> {
                        commentRepository.create(
                            postId = postId,
                            parentId = parentId,
                            text = text,
                            languageId = languageId,
                            auth = auth,
                        )
                    }
                }
                // the comment count has changed, emits update
                emitPostUpdateNotification()
                if (draftId != null) {
                    deleteDraft()
                }
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
            notificationCenter.send(NotificationCenterEvent.PostUpdated(newPost))
        }
    }

    private fun loadImageAndAppendUrlInBody(bytes: ByteArray) {
        if (bytes.isEmpty()) {
            return
        }
        screenModelScope.launch {
            updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value.orEmpty()
            val url = postRepository.uploadImage(auth, bytes)
            if (url != null) {
                val newValue =
                    uiState.value.textValue.let {
                        it.copy(text = it.text + "\n![]($url)")
                    }
                updateState {
                    it.copy(
                        textValue = newValue,
                        loading = false,
                    )
                }
            } else {
                updateState {
                    it.copy(
                        loading = false,
                    )
                }
            }
        }
    }

    private fun saveDraft() {
        val currentState = uiState.value
        if (currentState.loading) {
            return
        }
        val body = currentState.textValue.text
        val languageId = currentState.currentLanguageId

        screenModelScope.launch {
            val accountId = accountRepository.getActive()?.id ?: return@launch
            updateState { it.copy(loading = true) }
            val draft =
                DraftModel(
                    id = draftId,
                    type = DraftType.Comment,
                    body = body,
                    postId = postId,
                    parentId = parentId,
                    languageId = languageId,
                    date = epochMillis(),
                    reference =
                        if (currentState.originalComment != null) {
                            currentState.originalComment.text
                        } else {
                            currentState.originalPost?.title
                        },
                )
            if (draftId == null) {
                draftRepository.create(
                    model = draft,
                    accountId = accountId,
                )
            } else {
                draftRepository.update(draft)
            }
            updateState { it.copy(loading = false) }
            emitEffect(CreateCommentMviModel.Effect.DraftSaved)
        }
    }

    private suspend fun deleteDraft() {
        draftId?.also { id ->
            draftRepository.delete(id)
            notificationCenter.send(NotificationCenterEvent.DraftDeleted)
        }
    }
}
