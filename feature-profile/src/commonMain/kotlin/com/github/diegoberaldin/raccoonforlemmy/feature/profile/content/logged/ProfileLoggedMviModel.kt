package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.logged

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

interface ProfileLoggedMviModel :
    MviModel<ProfileLoggedMviModel.Intent, ProfileLoggedMviModel.UiState, ProfileLoggedMviModel.Effect> {

    sealed interface Intent {
        data class ChangeSection(val section: ProfileLoggedSection) : Intent
        object Refresh : Intent
        object LoadNextPage : Intent
        data class DeletePost(val id: Int) : Intent
        data class DeleteComment(val id: Int) : Intent
        data class SharePost(val index: Int) : Intent
    }

    data class UiState(
        val user: UserModel? = null,
        val section: ProfileLoggedSection = ProfileLoggedSection.Posts,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val posts: List<PostModel> = emptyList(),
        val comments: List<CommentModel> = emptyList(),
    )

    sealed interface Effect
}
