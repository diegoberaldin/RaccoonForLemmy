package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail

import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface CommunityDetailMviModel :
    MviModel<CommunityDetailMviModel.Intent, CommunityDetailMviModel.UiState, CommunityDetailMviModel.Effect> {

    sealed interface Intent {
        object Refresh : Intent
        object LoadNextPage : Intent
        data class ChangeSort(val value: SortType) : Intent
        data class UpVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val index: Int, val feedback: Boolean = false) : Intent
        object HapticIndication : Intent
        object Subscribe : Intent
        object Unsubscribe : Intent
        data class DeletePost(val id: Int) : Intent
        data class SharePost(val index: Int) : Intent
    }

    data class UiState(
        val community: CommunityModel = CommunityModel(),
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val sortType: SortType = SortType.Active,
        val posts: List<PostModel> = emptyList(),
        val blurNsfw: Boolean = true,
        val currentUserId: Int? = null,
        val swipeActionsEnabled: Boolean = true,
    )

    sealed interface Effect
}
