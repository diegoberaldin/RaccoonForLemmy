package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.comments

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.DefaultMviModel
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.identity.repository.IdentityRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.CommentRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.PostsRepository
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class ProfileCommentsViewModel(
    private val mvi: DefaultMviModel<ProfileCommentsMviModel.Intent, ProfileCommentsMviModel.UiState, ProfileCommentsMviModel.Effect>,
    private val user: UserModel,
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository,
    private val commentRepository: CommentRepository,
) : ScreenModel,
    MviModel<ProfileCommentsMviModel.Intent, ProfileCommentsMviModel.UiState, ProfileCommentsMviModel.Effect> by mvi {

    private var currentPage: Int = 1
    override fun onStarted() {
        mvi.onStarted()

        if (mvi.uiState.value.comments.isEmpty()) {
            refresh()
        }
    }

    override fun reduce(intent: ProfileCommentsMviModel.Intent) {
        when (intent) {
            ProfileCommentsMviModel.Intent.LoadNextPage -> loadNextPage()
            ProfileCommentsMviModel.Intent.Refresh -> refresh()
            is ProfileCommentsMviModel.Intent.DeleteComment -> deleteComment(intent.id)
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
                sort = SortType.New,
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

    private fun deleteComment(id: Int) {
        mvi.scope?.launch(Dispatchers.IO) {
            val auth = identityRepository.authToken.value.orEmpty()
            commentRepository.delete(id, auth)
            refresh()
        }
    }
}
