package com.github.diegoberaldin.raccoonforlemmy.unit.postlist

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.ListingType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

@Stable
interface PostListMviModel :
    MviModel<PostListMviModel.Intent, PostListMviModel.UiState, PostListMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class ChangeListing(val value: ListingType) : Intent
        data class UpVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Int, val feedback: Boolean = false) : Intent
        data class HandlePostUpdate(val post: PostModel) : Intent
        data object HapticIndication : Intent
        data class DeletePost(val id: Int) : Intent
        data class Share(val url: String) : Intent
        data class MarkAsRead(val id: Int) : Intent
        data class Hide(val id: Int) : Intent
        data object ClearRead : Intent
        data class StartZombieMode(val index: Int) : Intent
        data object PauseZombieMode : Intent
        data class Copy(val value: String) : Intent
    }

    data class UiState(
        val initial: Boolean = true,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val instance: String = "",
        val isLogged: Boolean = false,
        val listingType: ListingType? = null,
        val sortType: SortType? = null,
        val posts: List<PostModel> = emptyList(),
        val blurNsfw: Boolean = true,
        val currentUserId: Int? = null,
        val swipeActionsEnabled: Boolean = true,
        val doubleTapActionEnabled: Boolean = false,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val showScores: Boolean = true,
        val zombieModeActive: Boolean = false,
        val availableSortTypes: List<SortType> = emptyList(),
        val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = emptyList(),
    )

    sealed interface Effect {
        data object BackToTop : Effect
        data class ZombieModeTick(val index: Int) : Effect
        data class TriggerCopy(val text: String) : Effect
    }
}
