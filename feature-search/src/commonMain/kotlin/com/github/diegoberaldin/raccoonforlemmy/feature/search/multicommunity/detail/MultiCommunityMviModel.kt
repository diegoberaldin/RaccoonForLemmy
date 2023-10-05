package com.github.diegoberaldin.raccoonforlemmy.feature.search.multicommunity.detail

import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface MultiCommunityMviModel :
    MviModel<MultiCommunityMviModel.Intent, MultiCommunityMviModel.UiState, MultiCommunityMviModel.Effect> {
    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class ChangeSort(val value: SortType) : Intent
        data object HapticIndication : Intent
        data class UpVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val index: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val index: Int, val feedback: Boolean = false) : Intent
        data class SharePost(val index: Int) : Intent
    }

    data class UiState(
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val instance: String = "",
        val isLogged: Boolean = false,
        val sortType: SortType? = null,
        val posts: List<PostModel> = emptyList(),
        val blurNsfw: Boolean = true,
        val swipeActionsEnabled: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
    )

    sealed interface Effect
}