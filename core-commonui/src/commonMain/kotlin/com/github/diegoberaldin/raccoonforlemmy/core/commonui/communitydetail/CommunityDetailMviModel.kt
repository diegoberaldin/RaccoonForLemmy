package com.github.diegoberaldin.raccoonforlemmy.core.commonui.communitydetail

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommunityModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType

@Stable
interface CommunityDetailMviModel :
    MviModel<CommunityDetailMviModel.Intent, CommunityDetailMviModel.UiState, CommunityDetailMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class ChangeSort(val value: SortType) : Intent
        data class UpVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Int, val feedback: Boolean = false) : Intent
        data object HapticIndication : Intent
        data object Subscribe : Intent
        data object Unsubscribe : Intent
        data class DeletePost(val id: Int) : Intent
        data class SharePost(val id: Int) : Intent
        data class MarkAsRead(val id: Int) : Intent
        data class Hide(val id: Int) : Intent
        data object Block : Intent
        data object BlockInstance : Intent
        data object ClearRead : Intent
        data class StartZombieMode(val index: Int) : Intent
        data object PauseZombieMode : Intent
        data class ModFeaturePost(val id: Int) : Intent
        data class ModLockPost(val id: Int) : Intent
    }

    data class UiState(
        val community: CommunityModel = CommunityModel(),
        val isLogged: Boolean = false,
        val refreshing: Boolean = false,
        val asyncInProgress: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val sortType: SortType = SortType.Active,
        val posts: List<PostModel> = emptyList(),
        val blurNsfw: Boolean = true,
        val currentUserId: Int? = null,
        val swipeActionsEnabled: Boolean = true,
        val doubleTapActionEnabled: Boolean = false,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val separateUpAndDownVotes: Boolean = false,
        val autoLoadImages: Boolean = true,
        val zombieModeActive: Boolean = false,
        val isModerator: Boolean = false,
    )

    sealed interface Effect {
        data object BlockSuccess : Effect
        data class BlockError(val message: String?) : Effect
        data object BackToTop : Effect
        data class ZombieModeTick(val index: Int) : Effect
    }
}
