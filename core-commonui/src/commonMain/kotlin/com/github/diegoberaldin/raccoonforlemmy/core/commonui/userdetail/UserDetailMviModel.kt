package com.github.diegoberaldin.raccoonforlemmy.core.commonui.userdetail

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

interface UserDetailMviModel :
    MviModel<UserDetailMviModel.Intent, UserDetailMviModel.UiState, UserDetailMviModel.Effect> {

    sealed interface Intent {
        data class ChangeSort(val value: SortType) : Intent
        object Refresh : Intent
        object LoadNextPage : Intent
        data class ChangeSection(val section: UserDetailSection) : Intent
        data class UpVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val index: Int, val feedback: Boolean = false) : Intent
        data class UpVoteComment(val index: Int, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val index: Int, val feedback: Boolean = false) : Intent

        data class SaveComment(val index: Int, val feedback: Boolean = false) : Intent
        object HapticIndication : Intent
        data class SharePost(val index: Int) : Intent
    }

    data class UiState(
        val section: UserDetailSection = UserDetailSection.Posts,
        val sortType: SortType = SortType.Active,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val posts: List<PostModel> = emptyList(),
        val comments: List<CommentModel> = emptyList(),
        val user: UserModel = UserModel(),
        val blurNsfw: Boolean = true,
        val swipeActionsEnabled: Boolean = true,
    )

    sealed interface Effect
}
