package com.github.diegoberaldin.raccoonforlemmy.unit.moddedcontents.comments

import cafe.adriel.voyager.core.model.screenModelScope
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.repository.ThemeRepository
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.repository.SettingsRepository
import com.github.diegoberaldin.raccoonforlemmy.core.utils.vibrate.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ModdedCommentsViewModel(
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val identityRepository: IdentityRepository,
    private val commentRepository: CommentRepository,
    private val hapticFeedback: HapticFeedback,
) : ModdedCommentsMviModel,
    DefaultMviModel<ModdedCommentsMviModel.Intent, ModdedCommentsMviModel.State, ModdedCommentsMviModel.Effect>(
        initialState = ModdedCommentsMviModel.State(),
    ) {

    private var currentPage = 1

    init {
        screenModelScope.launch {
            themeRepository.postLayout.onEach { layout ->
                updateState { it.copy(postLayout = layout) }
            }.launchIn(this)
            settingsRepository.currentSettings.onEach { settings ->
                updateState {
                    it.copy(
                        autoLoadImages = settings.autoLoadImages,
                        preferNicknames = settings.preferUserNicknames,
                        swipeActionsEnabled = settings.enableSwipeActions,
                        voteFormat = settings.voteFormat,
                        actionsOnSwipeToStartComments = settings.actionsOnSwipeToStartComments,
                        actionsOnSwipeToEndComments = settings.actionsOnSwipeToEndComments,
                    )
                }
            }.launchIn(this)

            if (uiState.value.comments.isEmpty()) {
                refresh(initial = true)
            }
        }
    }

    override fun reduce(intent: ModdedCommentsMviModel.Intent) {
        when (intent) {
            ModdedCommentsMviModel.Intent.LoadNextPage -> screenModelScope.launch {
                loadNextPage()
            }

            ModdedCommentsMviModel.Intent.Refresh -> refresh()
            is ModdedCommentsMviModel.Intent.DownVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleDownVoteComment(comment = comment)
                    }
            }

            ModdedCommentsMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is ModdedCommentsMviModel.Intent.ModDistinguishComment -> uiState.value.comments.firstOrNull { it.id == intent.commentId }
                ?.also { comment ->
                    distinguish(comment)
                }

            is ModdedCommentsMviModel.Intent.SaveComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleSaveComment(comment = comment)
                    }
            }

            is ModdedCommentsMviModel.Intent.UpVoteComment -> {
                if (intent.feedback) {
                    hapticFeedback.vibrate()
                }
                uiState.value.comments.firstOrNull { it.id == intent.commentId }
                    ?.also { comment ->
                        toggleUpVoteComment(comment = comment)
                    }
            }
        }
    }

    private fun refresh(initial: Boolean = false) {
        currentPage = 1
        updateState {
            it.copy(
                canFetchMore = true,
                refreshing = true,
                initial = initial,
            )
        }
        screenModelScope.launch {
            loadNextPage()
        }
    }

    private suspend fun loadNextPage() {
        val currentState = uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            updateState { it.copy(refreshing = false) }
            return
        }

        updateState { it.copy(loading = true) }
        val auth = identityRepository.authToken.value.orEmpty()
        val refreshing = currentState.refreshing

        val itemList = commentRepository.getAll(
            auth = auth,
            page = currentPage,
            type = ListingType.ModeratorView,
        )?.let { list ->
            if (refreshing) {
                list
            } else {
                list.filter { c1 ->
                    // prevents accidental duplication
                    currentState.comments.none { c2 -> c1.id == c2.id }
                }
            }
        }

        val itemsToAdd = itemList.orEmpty().filter { comment ->
            !comment.deleted
        }
        updateState {
            val comments = if (refreshing) {
                itemsToAdd
            } else {
                it.comments + itemsToAdd
            }
            it.copy(
                comments = comments,
                loading = if (it.initial) itemsToAdd.isEmpty() else false,
                canFetchMore = itemList?.isEmpty() != true,
                refreshing = false,
                initial = if (it.initial) itemsToAdd.isEmpty() else false,
            )
        }
        if (!itemList.isNullOrEmpty()) {
            currentPage++
        }
    }

    private fun handleCommentUpdate(comment: CommentModel) {
        updateState {
            it.copy(
                comments = it.comments.map { c ->
                    if (c.id == comment.id) {
                        comment
                    } else {
                        c
                    }
                },
            )
        }
    }

    private fun toggleUpVoteComment(comment: CommentModel) {
        val newValue = comment.myVote <= 0
        val newComment = commentRepository.asUpVoted(
            comment = comment,
            voted = newValue,
        )
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.upVote(
                    auth = auth,
                    comment = comment,
                    voted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
            }
        }
    }

    private fun toggleDownVoteComment(comment: CommentModel) {
        val newValue = comment.myVote >= 0
        val newComment = commentRepository.asDownVoted(comment, newValue)
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.downVote(
                    auth = auth,
                    comment = comment,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
            }
        }
    }

    private fun toggleSaveComment(comment: CommentModel) {
        val newValue = !comment.saved
        val newComment = commentRepository.asSaved(
            comment = comment,
            saved = newValue,
        )
        handleCommentUpdate(newComment)
        screenModelScope.launch {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.save(
                    auth = auth,
                    comment = comment,
                    saved = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                handleCommentUpdate(comment)
            }
        }
    }

    private fun distinguish(comment: CommentModel) {
        screenModelScope.launch {
            val auth = identityRepository.authToken.value.orEmpty()
            val newComment = commentRepository.distinguish(
                commentId = comment.id,
                auth = auth,
                distinguished = !comment.distinguished,
            )
            if (newComment != null) {
                handleCommentUpdate(newComment)
            }
        }
    }
}