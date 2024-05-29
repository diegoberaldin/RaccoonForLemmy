package com.github.diegoberaldin.raccoonforlemmy.unit.postdetail

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.core.persistence.data.ActionOnSwipe
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Stable
interface PostDetailMviModel :
    MviModel<PostDetailMviModel.Intent, PostDetailMviModel.UiState, PostDetailMviModel.Effect>,
    ScreenModel {
    sealed interface Intent {
        data object Refresh : Intent

        data object RefreshPost : Intent

        data object LoadNextPage : Intent

        data class FetchMoreComments(val parentId: Long) : Intent

        data class UpVotePost(val feedback: Boolean = false) : Intent

        data class DownVotePost(val feedback: Boolean = false) : Intent

        data class SavePost(val post: PostModel, val feedback: Boolean = false) : Intent

        data class UpVoteComment(val commentId: Long, val feedback: Boolean = false) : Intent

        data class DownVoteComment(val commentId: Long, val feedback: Boolean = false) : Intent

        data class SaveComment(val commentId: Long, val feedback: Boolean = false) : Intent

        data class ToggleExpandComment(val commentId: Long) : Intent

        data class DeleteComment(val commentId: Long) : Intent

        data object DeletePost : Intent

        data object HapticIndication : Intent

        data object ModFeaturePost : Intent

        data object AdminFeaturePost : Intent

        data object ModLockPost : Intent

        data class ModDistinguishComment(val commentId: Long) : Intent

        data class ModToggleModUser(val id: Long) : Intent

        data class Share(val url: String) : Intent

        data class Copy(val value: String) : Intent

        data class SetSearch(val value: String) : Intent

        data class ChangeSearching(val value: Boolean) : Intent

        data object NavigatePrevious : Intent

        data object NavigateNext : Intent

        data class NavigatePreviousComment(val currentIndex: Int) : Intent

        data class NavigateNextComment(val currentIndex: Int) : Intent
    }

    data class UiState(
        val post: PostModel = PostModel(),
        val instance: String = "",
        val isModerator: Boolean = false,
        val isAdmin: Boolean = false,
        val isLogged: Boolean = false,
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val initial: Boolean = true,
        val canFetchMore: Boolean = true,
        val sortType: SortType = SortType.New,
        val comments: List<CommentModel> = emptyList(),
        val commentBarThickness: Int = 1,
        val commentIndentAmount: Int = 2,
        val currentUserId: Long? = null,
        val swipeActionsEnabled: Boolean = true,
        val doubleTapActionEnabled: Boolean = true,
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val fullWidthImages: Boolean = false,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val showScores: Boolean = true,
        val moderators: List<UserModel> = emptyList(),
        val availableSortTypes: List<SortType> = emptyList(),
        val actionsOnSwipeToStartComments: List<ActionOnSwipe> = emptyList(),
        val actionsOnSwipeToEndComments: List<ActionOnSwipe> = emptyList(),
        val searching: Boolean = false,
        val searchText: String = "",
        val enableButtonsToScrollBetweenComments: Boolean = false,
        val isNavigationSupported: Boolean = false,
        val downVoteEnabled: Boolean = true,
    )

    sealed interface Effect {
        data object Close : Effect

        data class ScrollToComment(val index: Int) : Effect

        data object BackToTop : Effect

        data class TriggerCopy(val text: String) : Effect
    }
}
