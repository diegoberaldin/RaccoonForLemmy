package com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.racconforlemmy.core.utils.HapticFeedback
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenter
import com.github.diegoberaldin.raccoonforlemmy.core.notifications.NotificationCenterContractKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.KeyStoreKeys
import com.github.diegoberaldin.raccoonforlemmy.core.preferences.TemporaryKeyStore
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.toSortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.SiteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class PostDetailViewModel(
    private val mvi: DefaultMviModel<PostDetailMviModel.Intent, PostDetailMviModel.UiState, PostDetailMviModel.Effect>,
    private val post: PostModel,
    private val identityRepository: IdentityRepository,
    private val siteRepository: SiteRepository,
    private val postsRepository: PostsRepository,
    private val commentRepository: CommentRepository,
    private val keyStore: TemporaryKeyStore,
    private val notificationCenter: NotificationCenter,
    private val hapticFeedback: HapticFeedback,
) : MviModel<PostDetailMviModel.Intent, PostDetailMviModel.UiState, PostDetailMviModel.Effect> by mvi,
    ScreenModel {

    private var currentPage: Int = 1

    override fun onStarted() {
        mvi.onStarted()
        val sortType = keyStore[KeyStoreKeys.DefaultCommentSortType, 3].toSortType()
        mvi.updateState {
            it.copy(
                sortType = sortType,
                post = post,
            )
        }
        mvi.scope?.launch {
            if (uiState.value.currentUserId == null) {
                val auth = identityRepository.authToken.value.orEmpty()
                val user = siteRepository.getCurrentUser(auth)
                mvi.updateState { it.copy(currentUserId = user?.id ?: 0) }
            }
            if (mvi.uiState.value.comments.isEmpty()) {
                refresh()
            }
        }
    }

    override fun reduce(intent: PostDetailMviModel.Intent) {
        when (intent) {
            PostDetailMviModel.Intent.LoadNextPage -> loadNextPage()
            PostDetailMviModel.Intent.Refresh -> refresh()
            PostDetailMviModel.Intent.HapticIndication -> hapticFeedback.vibrate()
            is PostDetailMviModel.Intent.ChangeSort -> applySortType(intent.value)

            is PostDetailMviModel.Intent.DownVoteComment -> toggleDownVoteComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.DownVotePost -> toggleDownVotePost(
                post = uiState.value.post,
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.SaveComment -> toggleSaveComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.SavePost -> toggleSavePost(
                post = intent.post,
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.UpVoteComment -> toggleUpVoteComment(
                comment = uiState.value.comments[intent.index],
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.UpVotePost -> toggleUpVotePost(
                post = uiState.value.post,
                feedback = intent.feedback,
            )

            is PostDetailMviModel.Intent.FetchMoreComments -> {
                loadMoreComments(intent.parentId)
            }

            is PostDetailMviModel.Intent.DeleteComment -> deleteComment(intent.id)
            PostDetailMviModel.Intent.DeletePost -> deletePost()
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
            val sort = currentState.sortType
            val commentList = commentRepository.getAll(
                auth = auth,
                postId = post.id,
                page = currentPage,
                sort = sort,
                maxDepth = CommentRepository.MAX_COMMENT_DEPTH,
            )
            currentPage++
            val canFetchMore = commentList.size >= CommentRepository.DEFAULT_PAGE_SIZE
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

    private fun loadMoreComments(parentId: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val currentState = mvi.uiState.value
            val auth = identityRepository.authToken.value
            val sort = currentState.sortType
            val fetchResult = commentRepository.getChildren(
                auth = auth,
                parentId = parentId,
                sort = sort,
                maxDepth = CommentRepository.MAX_COMMENT_DEPTH,
            )
            val newList = uiState.value.comments.let { list ->
                val index = list.indexOfFirst { c -> c.id == parentId }
                list.toMutableList().apply {
                    addAll(index, fetchResult)
                }.toList()
            }
            mvi.updateState { it.copy(comments = newList) }
        }
    }

    private fun toggleUpVotePost(
        post: PostModel,
        feedback: Boolean,
    ) {
        val newValue = post.myVote <= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newPost = postsRepository.asUpVoted(
            post = post,
            voted = newValue,
        )
        mvi.updateState { it.copy(post = newPost) }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.upVote(
                    auth = auth,
                    post = post,
                    voted = newValue,
                )
                notificationCenter.getObserver(NotificationCenterContractKeys.PostUpdated)?.also {
                    it.invoke(newPost)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState { it.copy(post = post) }
            }
        }
    }

    private fun toggleDownVotePost(
        post: PostModel,
        feedback: Boolean,
    ) {
        val newValue = post.myVote >= 0
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newPost = postsRepository.asDownVoted(
            post = post,
            downVoted = newValue,
        )
        mvi.updateState { it.copy(post = newPost) }

        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.downVote(
                    auth = auth,
                    post = post,
                    downVoted = newValue,
                )
                notificationCenter.getObserver(NotificationCenterContractKeys.PostUpdated)?.also {
                    it.invoke(newPost)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState { it.copy(post = post) }
            }
        }
    }

    private fun toggleSavePost(
        post: PostModel,
        feedback: Boolean,
    ) {
        val newValue = !post.saved
        if (feedback) {
            hapticFeedback.vibrate()
        }
        val newPost = postsRepository.asSaved(
            post = post,
            saved = newValue,
        )
        mvi.updateState { it.copy(post = newPost) }
        mvi.scope?.launch(Dispatchers.IO) {
            try {
                val auth = identityRepository.authToken.value.orEmpty()
                postsRepository.save(
                    auth = auth,
                    post = post,
                    saved = newValue,
                )
                notificationCenter.getObserver(NotificationCenterContractKeys.PostUpdated)?.also {
                    it.invoke(newPost)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                mvi.updateState { it.copy(post = post) }
            }
        }
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

    private fun deleteComment(id: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            commentRepository.delete(id, auth)
            refresh()
        }
    }

    private fun deletePost() {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            postsRepository.delete(id = post.id, auth = auth)
            notificationCenter.getObserver(NotificationCenterContractKeys.PostDeleted)?.also {
                it.invoke(post)
            }
            mvi.emitEffect(PostDetailMviModel.Effect.Close)
        }
    }
}
