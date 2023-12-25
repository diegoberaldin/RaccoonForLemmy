package com.github.diegoberaldin.raccoonforlemmy.unit.myaccount

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.ProfileLoggedSection
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

interface ProfileLoggedMviModel :
    MviModel<ProfileLoggedMviModel.Intent, ProfileLoggedMviModel.UiState, ProfileLoggedMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class ChangeSection(val section: ProfileLoggedSection) : Intent
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class DeletePost(val id: Int) : Intent
        data class DeleteComment(val id: Int) : Intent
        data class Share(val url: String) : Intent
        data class UpVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Int, val feedback: Boolean = false) : Intent
        data class UpVoteComment(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val id: Int, val feedback: Boolean = false) : Intent

        data class SaveComment(val id: Int, val feedback: Boolean = false) : Intent
    }

    data class UiState(
        val user: UserModel? = null,
        val instance: String = "",
        val section: ProfileLoggedSection = ProfileLoggedSection.Posts,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val initial: Boolean = false,
        val canFetchMore: Boolean = true,
        val posts: List<PostModel> = emptyList(),
        val comments: List<CommentModel> = emptyList(),
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = true,
    )

    sealed interface Effect
}
