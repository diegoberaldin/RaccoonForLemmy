package com.github.diegoberaldin.raccoonforlemmy.feature.profile.content.saved

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel

interface ProfileSavedMviModel :
    MviModel<ProfileSavedMviModel.Intent, ProfileSavedMviModel.UiState, ProfileSavedMviModel.Effect> {

    sealed interface Intent {
        object Refresh : Intent
        object LoadNextPage : Intent
        data class ChangeSection(val section: ProfileSavedSection) : Intent
        data class UpVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val index: Int, val feedback: Boolean = false) : Intent
        data class UpVoteComment(val index: Int, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val index: Int, val feedback: Boolean = false) : Intent
        data class SaveComment(val index: Int, val feedback: Boolean = false) : Intent
    }

    data class UiState(
        val section: ProfileSavedSection = ProfileSavedSection.Posts,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val blurNsfw: Boolean = true,
        val posts: List<PostModel> = emptyList(),
        val comments: List<CommentModel> = emptyList(),
    )

    sealed interface Effect
}
