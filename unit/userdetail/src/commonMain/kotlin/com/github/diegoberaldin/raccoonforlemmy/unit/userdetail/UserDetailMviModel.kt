package com.github.diegoberaldin.raccoonforlemmy.unit.userdetail

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.commonui.lemmyui.UserDetailSection
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Stable
interface UserDetailMviModel :
    MviModel<UserDetailMviModel.Intent, UserDetailMviModel.UiState, UserDetailMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data class ChangeSort(val value: SortType) : Intent
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class ChangeSection(val section: UserDetailSection) : Intent
        data class UpVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Int, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Int, val feedback: Boolean = false) : Intent
        data class UpVoteComment(val id: Int, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val id: Int, val feedback: Boolean = false) : Intent
        data class SaveComment(val id: Int, val feedback: Boolean = false) : Intent
        data object HapticIndication : Intent
        data class Share(val url: String) : Intent
        data object Block : Intent
        data object BlockInstance : Intent
    }

    data class UiState(
        val isLogged: Boolean = false,
        val instance: String = "",
        val currentUserId: Int? = null,
        val section: UserDetailSection = UserDetailSection.Posts,
        val sortType: SortType = SortType.Active,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val initial: Boolean = true,
        val asyncInProgress: Boolean = false,
        val canFetchMore: Boolean = true,
        val posts: List<PostModel> = emptyList(),
        val comments: List<CommentModel> = emptyList(),
        val user: UserModel = UserModel(),
        val blurNsfw: Boolean = true,
        val swipeActionsEnabled: Boolean = true,
        val doubleTapActionEnabled: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val showScores: Boolean = true,
        val availableSortTypes: List<SortType> = emptyList(),
        val actionsOnSwipeToStartPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndPosts: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToStartComments: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndComments: List<ActionOnSwipe> = emptyList(),
    )

    sealed interface Effect {
        data object BlockSuccess : Effect
        data class BlockError(val message: String?) : Effect
        data object BackToTop : Effect
    }
}
