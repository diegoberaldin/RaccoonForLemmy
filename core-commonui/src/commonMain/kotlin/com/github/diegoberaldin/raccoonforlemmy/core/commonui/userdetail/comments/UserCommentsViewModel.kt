package com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail.comments

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class UserCommentsViewModel(
    private val mvi: DefaultMviModel<UserCommentsMviModel.Intent, UserCommentsMviModel.UiState, UserCommentsMviModel.Effect>,
    private val user: UserModel,
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
    private val hapticFeedback: HapticFeedback,
    private val notificationCenter: NotificationCenter,
) : ScreenModel,
    MviModel<UserCommentsMviModel.Intent, UserCommentsMviModel.UiState, UserCommentsMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    override fun onStarted() {
        mvi.onStarted()

        mvi.scope?.launch(Dispatchers.IO) {
            val user = userRepository.get(user.id)
            if (user != null) {
                mvi.updateState { it.copy(user = user) }
            }
        }
    }

    override fun reduce(intent: UserCommentsMviModel.Intent) {
        when (intent) {
            UserCommentsMviModel.Intent.LoadNextPage -> loadNextPage()
            UserCommentsMviModel.Intent.Refresh -> refresh()
            is UserCommentsMviModel.Intent.DownVoteComment -> toggleDownVoteComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            UserCommentsMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is UserCommentsMviModel.Intent.SaveComment -> toggleSaveComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            is UserCommentsMviModel.Intent.UpVoteComment -> toggleUpVoteComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            is UserCommentsMviModel.Intent.ChangeSort -> applySortType(intent.value)
        }
    }

    private fun refresh() {
        currentPage = 1
        mvi.updateState { it.copy(canFetchMore = true, refreshing = true) }
        loadNextPage()
    }

    private fun loadNextPage() {
        val currentState = mvi.uiState.value
        if (!currentState.canFetchMore || currentState.loading) {
            mvi.updateState { it.copy(refreshing = false) }
            return
        }

        mvi.scope?.launch(Dispatchers.IO) {
            mvi.updateState { it.copy(loading = true) }
            val auth = identityRepository.authToken.value
            val refreshing = currentState.refreshing
            val commentList = userRepository.getComments(
                auth = auth,
                id = user.id,
                page = currentPage,
                sort = currentState.sortType,
            )
            currentPage++
            val canFetchMore = commentList.size >= PostsRepository.DEFAULT_PAGE_SIZE
            mvi.updateState {
                val newcomments = if (refreshing) {
                    commentList
                } else {
                    it.comments + commentList
                }
                it.copy(
                    comments = newcomments,
                    loading = false,
                    canFetchMore = canFetchMore,
                    refreshing = false,
                )
            }
        }
    }

    private fun applySortType(value: SortType) {
        mvi.updateState { it.copy(sortType = value) }
        refresh()
    }

    private fun toggleUpVoteComment(
        comment: CommentModel,
        feedback: Boolean,
    ) {
        val newValue = comment.myVote <= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newComment = commentRepository.asUpVoted(
            comment = comment,
            voted = newValue,
        )
        mvi.updateState {
            it.copy(
                comments = it.comments.map { c ->
                    if (c.id == comment.id) {
                        newComment
                    } else {
                        c
                    }
                },
            )
        }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.upVote(
                    auth = auth,
                    comment = comment,
                    voted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
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
        }
    }

    private fun toggleDownVoteComment(
        comment: CommentModel,
        feedback: Boolean,
    ) {
        val newValue = comment.myVote >= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newComment = commentRepository.asDownVoted(comment, newValue)
        mvi.updateState {
            it.copy(
                comments = it.comments.map { c ->
                    if (c.id == comment.id) {
                        newComment
                    } else {
                        c
                    }
                },
            )
        }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.downVote(
                    auth = auth,
                    comment = comment,
                    downVoted = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
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
        }
    }

    private fun toggleSaveComment(
        comment: CommentModel,
        feedback: Boolean,
    ) {
        val newValue = !comment.saved
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newComment = commentRepository.asSaved(
            comment = comment,
            saved = newValue,
        )
        mvi.updateState {
            it.copy(
                comments = it.comments.map { c ->
                    if (c.id == comment.id) {
                        newComment
                    } else {
                        c
                    }
                },
            )
        }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                commentRepository.save(
                    auth = auth,
                    comment = comment,
                    saved = newValue,
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState {
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
        }
    }
}
