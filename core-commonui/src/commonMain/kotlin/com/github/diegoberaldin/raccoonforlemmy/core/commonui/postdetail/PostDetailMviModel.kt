package com.github.diegoberaldin.raccoonforlemmy.core.commonui.postdetail

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface PostDetailMviModel :
    MviModel<PostDetailMviModel.Intent, PostDetailMviModel.UiState, PostDetailMviModel.Effect> {

    sealed interface Intent {
        object Refresh : Intent
        object LoadNextPage : Intent
        data class FetchMoreComments(val parentId: Int) : Intent
        data class ChangeSort(val value: SortType) : Intent
        data class UpVotePost(val post: PostModel, val feedback: Boolean = false) : Intent
        data class DownVotePost(val post: PostModel, val feedback: Boolean = false) : Intent
        data class SavePost(val post: PostModel, val feedback: Boolean = false) : Intent
        data class UpVoteComment(val comment: CommentModel, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val comment: CommentModel, val feedback: Boolean = false) :
            Intent

        data class SaveComment(val comment: CommentModel, val feedback: Boolean = false) : Intent
        object HapticIndication : Intent
    }

    data class UiState(
        val post: PostModel = PostModel(),
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val sortType: SortType = SortType.New,
        val comments: List<CommentModel> = emptyList(),
    )

    sealed interface Effect
}
