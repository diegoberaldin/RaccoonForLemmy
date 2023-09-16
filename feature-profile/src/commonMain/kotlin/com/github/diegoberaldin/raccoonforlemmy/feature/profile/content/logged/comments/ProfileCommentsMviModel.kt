package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged.comments

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel

interface ProfileCommentsMviModel :
    MviModel<ProfileCommentsMviModel.Intent, ProfileCommentsMviModel.UiState, ProfileCommentsMviModel.Effect> {

    sealed interface Intent {
        object Refresh : Intent
        object LoadNextPage : Intent
        data class DeleteComment(val id: Int) : Intent
    }

    data class UiState(
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val comments: List<CommentModel> = emptyList(),
    )

    sealed interface Effect
}
