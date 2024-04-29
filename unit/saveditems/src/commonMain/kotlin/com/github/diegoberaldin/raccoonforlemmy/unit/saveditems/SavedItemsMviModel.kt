package com.github.diegoberaldin.raccoonforlemmy.unit.saveditems

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.ScreenModel
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.PostLayout
import com.github.diegoberaldin.raccoonforlemmy.core.appearance.data.VoteFormat
import com.github.diegoberaldin.raccoonforlemmy.core.architecture.MviModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.CommentModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.PostModel
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.SortType
import com.github.diegoberaldin.raccoonforlemmy.domain.lemmy.data.UserModel

@Stable
interface SavedItemsMviModel :
    MviModel<SavedItemsMviModel.Intent, SavedItemsMviModel.UiState, SavedItemsMviModel.Effect>,
    ScreenModel {

    sealed interface Intent {
        data object Refresh : Intent
        data object LoadNextPage : Intent
        data class ChangeSection(val section: SavedItemsSection) : Intent
        data class UpVotePost(val id: Long, val feedback: Boolean = false) : Intent
        data class DownVotePost(val id: Long, val feedback: Boolean = false) : Intent
        data class SavePost(val id: Long, val feedback: Boolean = false) : Intent
        data class UpVoteComment(val id: Long, val feedback: Boolean = false) : Intent
        data class DownVoteComment(val id: Long, val feedback: Boolean = false) : Intent
        data class SaveComment(val id: Long, val feedback: Boolean = false) : Intent
        data class Share(val url: String) : Intent
        data object WillOpenSave : Intent
    }

    data class UiState(
        val section: SavedItemsSection = SavedItemsSection.Posts,
        val user: UserModel? = null,
        val instance: String = "",
        val refreshing: Boolean = false,
        val loading: Boolean = false,
        val canFetchMore: Boolean = true,
        val sortType: SortType = SortType.New,
        val blurNsfw: Boolean = true,
        val posts: List<PostModel> = emptyList(),
        val comments: List<CommentModel> = emptyList(),
        val postLayout: PostLayout = PostLayout.Card,
        val fullHeightImages: Boolean = true,
        val voteFormat: VoteFormat = VoteFormat.Aggregated,
        val autoLoadImages: Boolean = true,
        val preferNicknames: Boolean = true,
        val showScores: Boolean = true,
        val availableSortTypes: List<SortType> = emptyList(),
    )

    sealed interface Effect
}
