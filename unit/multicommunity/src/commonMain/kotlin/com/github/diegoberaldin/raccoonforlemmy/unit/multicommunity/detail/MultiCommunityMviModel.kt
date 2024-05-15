package com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity.detail

import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.MultiCommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

interface MultiCommunityMviModel :
    MviModel<MultiCommunityMviModel.Intent, MultiCommunityMviModel.UiState, MultiCommunityMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data object HapticIndication : Intent
        data class UpVotePost(val id: Long, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Long, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Long, val feedback: Boolean = false) : Intent
        data class MarkAsRead(val id: Long) : Intent
        data class Hide(val id: Long) : Intent
        data object ClearRead : Intent
        data class Share(val url: String) : Intent
        data class Copy(val value: String) : Intent
        data object WillOpenDetail : Intent
    }

    data class UiState(
        val currentUserId: Long? = null,
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val instance: String = "",
        val isLogged: Boolean = false,
        val sortType: SortType? = null,
        val community: MultiCommunityModel = MultiCommunityModel(),
        val posts: List<PostModel> = emptyList(),
        val blurNsfw: Boolean = true,
        val swipeActionsEnabled: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val fullWidthImages: Boolean = false,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val showScores: Boolean = true,
        val availableSortTypes: List<SortType> = emptyList(),
        val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = emptyList(),
        val fadeReadPosts: Boolean = false,
        val showUnreadComments: Boolean = false,
    )

    sealed interface Effect {
        data object BackToTop : Effect
        data class TriggerCopy(val text: String) : Effect
    }
}
